/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation. All rights reserved.
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

package org.onap.policy.api.main.rest.provider;

import java.util.List;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NodeTemplateProvider extends CommonModelProvider {

    /**
     * Default constructor.
     */
    @Autowired
    public NodeTemplateProvider(PolicyModelsProvider modelsProvider) throws PfModelException {
        super(modelsProvider);
    }


    /**
     * Retrieves a node template matching specified ID and version.
     *
     * @param name the name of the node template, null to return all entries
     * @param version the version of node template, null to return all entries
     *
     * @return the List of node templates
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public List<ToscaNodeTemplate> fetchToscaNodeTemplates(final String name, final String version)
        throws PfModelException {

        return modelsProvider.getToscaNodeTemplate(name, version);
    }


    /**
     * Creates one or more new node templates.
     *
     * @param serviceTemplate service template containing node template definitions
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate createNodeTemplates(ToscaServiceTemplate serviceTemplate)
        throws PfModelException {

        return modelsProvider.createToscaNodeTemplates(serviceTemplate);
    }


    /**
     * Update one or more node templates.
     *
     * @param serviceTemplate service template with updated node templates
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate updateToscaNodeTemplates(ToscaServiceTemplate serviceTemplate)
        throws PfModelException {

        return modelsProvider.updateToscaNodeTemplates(serviceTemplate);
    }


    /**
     * Deletes the node template matching specified ID and version.
     *
     * @param name the name of the node template
     * @param version the version of the node template
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate deleteToscaNodeTemplate(String name, String version)
        throws PfModelException {

        return modelsProvider.deleteToscaNodeTemplate(name, version);
    }


}
