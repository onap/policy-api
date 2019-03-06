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

import org.onap.policy.models.tosca.ToscaPolicy;
import org.onap.policy.models.tosca.ToscaPolicyList;

/**
 * Class to provide all kinds of policy operations.
 */
public class PolicyProvider {

    private static final String POST_OK = "Successfully created";
    private static final String DELETE_OK = "Successfully deleted";

    /**
     * Retrieves a list of policies matching specified ID and version of both policy type and policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the ToscaPolicyList object containing a list of policies matching specified fields
     */
    public ToscaPolicyList fetchPolicies(String policyTypeId, String policyTypeVersion,
                                         String policyId, String policyVersion) {
        // placeholder
        return new ToscaPolicyList();
    }

    /**
     * Creates a new policy for a policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param body the entity body of policy
     *
     * @return a string message indicating the operation results
     */
    public String createPolicy(String policyTypeId, String policyTypeVersion, ToscaPolicy body) {
        // placeholder
        return POST_OK;
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
     */
    public String deletePolicies(String policyTypeId, String policyTypeVersion,
                                 String policyId, String policyVersion) {
        // placeholder
        return DELETE_OK;
    }
}
