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
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.tosca.simple.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;

/**
 * Class to provide all kinds of policy operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class PolicyProvider {

    private static final String POLICY_VERSION = "policy-version";
    private static final String DELETE_OK = "Successfully deleted";

    /**
     * Retrieves a list of policies matching specified ID and version of both policy type and policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the ToscaServiceTemplate object
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate fetchPolicies(String policyTypeId, String policyTypeVersion,
                                         String policyId, String policyVersion) throws PfModelException {
        // placeholder
        return new PolicyModelsProviderFactory().createPolicyModelsProvider()
                .getPolicies(new PfConceptKey("dummyName", "dummyVersion"));
    }

    /**
     * Creates a new policy for a policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param body the entity body of policy
     *
     * @return the ToscaServiceTemplate object
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate createPolicy(String policyTypeId, String policyTypeVersion,
                                             ToscaServiceTemplate body) throws PfModelException {
        // Manually add policy-version: 1 into metadata
        // TODO: need more elegant way to do this later
        for (ToscaPolicy policy : body.getTopologyTemplate().getPolicies().getConceptMap().values()) {
            if (policy.getMetadata() == null) {
                Map<String, String> newMetadata = new HashMap<>();
                newMetadata.put(POLICY_VERSION, "1");
                policy.setMetadata(newMetadata);
            } else {
                policy.getMetadata().put(POLICY_VERSION, "1");
            }
        }
        return new PolicyModelsProviderFactory().createPolicyModelsProvider().createPolicies(body);
    }

    /**
     * Deletes the policies matching specified ID and version of both policy type and policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return a string message indicating the operation results
     * @throws PfModelException the PfModel parsing exception
     */
    public String deletePolicies(String policyTypeId, String policyTypeVersion,
                                 String policyId, String policyVersion) throws PfModelException {
        // placeholder
        return DELETE_OK;
    }
}