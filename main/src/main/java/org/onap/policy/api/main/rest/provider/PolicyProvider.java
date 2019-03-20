/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.HashMap;
import java.util.Map;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.provider.impl.DummyPolicyModelsProviderImpl;
import org.onap.policy.models.tosca.authorative.concepts.PlainToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.mapping.PlainToscaServiceTemplateMapper;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;

/**
 * Class to provide all kinds of policy operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class PolicyProvider {

    private static final String POLICY_VERSION = "policy-version";

    private PolicyModelsProvider modelsProvider;

    /**
     * Default constructor.
     */
    public PolicyProvider() throws PfModelException {

        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
        // Use dummy provider tentatively to test dummy things
        // Will change to use real database version
        parameters.setImplementation(DummyPolicyModelsProviderImpl.class.getCanonicalName());
        parameters.setDatabaseUrl("jdbc:dummy");
        parameters.setPersistenceUnit("dummy");

        modelsProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);
        modelsProvider.init();
    }

    /**
     * Retrieves a list of policies matching specified ID and version of both policy type and policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the PlainToscaServiceTemplate object
     * @throws PfModelException the PfModel parsing exception
     */
    public PlainToscaServiceTemplate fetchPolicies(String policyTypeId, String policyTypeVersion,
                                         String policyId, String policyVersion) throws PfModelException {

        ToscaServiceTemplate serviceTemplate = modelsProvider.getPolicies(
                new PfConceptKey("dummyName", "dummyVersion"));
        return new PlainToscaServiceTemplateMapper().fromToscaServiceTemplate(serviceTemplate);
    }

    /**
     * Creates a new policy for a policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param body the entity body of policy
     *
     * @return the PlainToscaServiceTemplate object
     * @throws PfModelException the PfModel parsing exception
     */
    public PlainToscaServiceTemplate createPolicy(String policyTypeId, String policyTypeVersion,
                                             PlainToscaServiceTemplate body) throws PfModelException {

        PlainToscaServiceTemplateMapper mapper = new PlainToscaServiceTemplateMapper();
        ToscaServiceTemplate mappedBody = mapper.toToscaServiceTemplate(body);

        // Manually add policy-version: 1 into metadata
        // TODO: need more elegant way to do this later
        for (ToscaPolicy policy : mappedBody.getTopologyTemplate().getPolicies().getConceptMap().values()) {
            if (policy.getMetadata() == null) {
                Map<String, String> newMetadata = new HashMap<>();
                newMetadata.put(POLICY_VERSION, "1");
                policy.setMetadata(newMetadata);
            } else {
                policy.getMetadata().put(POLICY_VERSION, "1");
            }
        }

        ToscaServiceTemplate serviceTemplate = modelsProvider.createPolicies(mappedBody);
        return mapper.fromToscaServiceTemplate(serviceTemplate);
    }

    /**
     * Deletes the policies matching specified ID and version of both policy type and policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the PlainToscaServiceTemplate object
     * @throws PfModelException the PfModel parsing exception
     */
    public PlainToscaServiceTemplate deletePolicies(String policyTypeId, String policyTypeVersion,
                                 String policyId, String policyVersion) throws PfModelException {
        // placeholder
        return new PlainToscaServiceTemplate();
    }
}