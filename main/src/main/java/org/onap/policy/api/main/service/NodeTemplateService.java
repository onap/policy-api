/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Nordix Foundation. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.api.main.service;

import jakarta.ws.rs.core.Response;
import java.util.Optional;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.onap.policy.api.main.repository.NodeTemplateRepository;
import org.onap.policy.api.main.repository.NodeTypeRepository;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaNodeTemplate;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaNodeTemplates;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaNodeType;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.JpaToscaServiceTemplate;
import org.onap.policy.models.tosca.utils.ToscaUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class NodeTemplateService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeTemplateService.class);

    private final NodeTemplateRepository nodeTemplateRepository;
    private final NodeTypeRepository nodeTypeRepository;

    /**
     * Delete the specified node template.
     *
     * @param nodeTemplateKey the node template key containing name and version
     */
    public void deleteNodeTemplate(final PfConceptKey nodeTemplateKey) {
        nodeTemplateRepository.deleteById(nodeTemplateKey);
    }


    /**
     * Update the specified tosca node template.
     * @param incomingServiceTemplate incoming service template
     */
    public void updateToscaNodeTemplates(@NonNull final JpaToscaServiceTemplate incomingServiceTemplate)
        throws PfModelRuntimeException, PfModelException {
        for (JpaToscaNodeTemplate nodeTemplate : incomingServiceTemplate.getTopologyTemplate().getNodeTemplates()
            .getAll(null)) {
            //verify if the node template is referenced in the metadata of created policies
            assertNodeTemplateNotUsedInPolicy(nodeTemplate.getName(), nodeTemplate.getVersion(),
                incomingServiceTemplate);
            verifyNodeTypeInDbTemplate(nodeTemplate);
            Optional<JpaToscaNodeTemplate> dbNodeTemplate = nodeTemplateRepository.findById(nodeTemplate.getKey());
            if (dbNodeTemplate.isPresent()) {
                nodeTemplateRepository.save(nodeTemplate);
            } else {
                throw new PfModelRuntimeException(Response.Status.NOT_ACCEPTABLE, "node template "
                    + nodeTemplate.getName() + " version " + nodeTemplate.getVersion() + " does not exist in database");
            }
        }

        // Return the service template with updated node templates
        var updatedNodeTemplates = new JpaToscaNodeTemplates();
        updatedNodeTemplates.setKey(incomingServiceTemplate.getTopologyTemplate().getNodeTemplates().getKey());

        for (PfConceptKey key : incomingServiceTemplate.getTopologyTemplate().getNodeTemplates()
            .getConceptMap().keySet()) {
            Optional<JpaToscaNodeTemplate> jpaNodeTemplate = nodeTemplateRepository.findById(key);
            jpaNodeTemplate.ifPresent(
                jpaToscaNodeTemplate -> updatedNodeTemplates.getConceptMap().put(key, jpaToscaNodeTemplate));
        }
        incomingServiceTemplate.getTopologyTemplate().setNodeTemplates(updatedNodeTemplates);

    }


    /**
     * Verify the node type for a toscaNodeTemplate .
     *
     * @param toscaNodeTemplate the toscaNodeTemplate to check the toscaNodeTemplate type for
     */
    public void verifyNodeTypeInDbTemplate(final JpaToscaNodeTemplate toscaNodeTemplate) throws
        PfModelException {
        PfConceptKey nodeTypeKey = toscaNodeTemplate.getType();

        Optional<JpaToscaNodeType> nodeType = nodeTypeRepository.findById(nodeTypeKey);

        if (nodeType.isEmpty()) {
            String errorMessage =
                "NODE_TYPE " + nodeTypeKey + " for toscaNodeTemplate " + toscaNodeTemplate.getId()
                    + " does not exist";
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errorMessage);
        }
    }

    /**
     * Assert that the node template is not referenced in any Tosca policy.
     *
     * @param name the name of node template
     * @param version the version of node template
     * @throws PfModelException if node template referenced in a policy
     */
    public void assertNodeTemplateNotUsedInPolicy(String name, String version, JpaToscaServiceTemplate dbTemplate)
        throws PfModelException {
        try {
            //Retrieve all the policies from db, return if policies doesn't exist
            ToscaUtils.assertPoliciesExist(dbTemplate);
        } catch (PfModelRuntimeException e) {
            LOGGER.debug("Could not verify the node template reference in created policies ", e);
            return;
        }
        for (JpaToscaPolicy policy : dbTemplate.getTopologyTemplate().getPolicies().getConceptMap().values()) {
            if (policy.getMetadata().getOrDefault("metadataSetName", "").equals(name)
                && policy.getMetadata().getOrDefault("metadataSetVersion", "").equals(version)) {
                throw new PfModelException(Response.Status.NOT_ACCEPTABLE,
                    "Node template is in use, it is referenced in Tosca Policy " + policy.getName() + " version "
                        + policy.getVersion());
            }
        }

    }
}
