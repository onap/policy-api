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

import org.onap.policy.tosca.ToscaPolicy;
import org.onap.policy.tosca.ToscaPolicyList;

/**
 * Class to provide all kinds of policy operations.
 */
public class PolicyProvider {

    private static final String POST_OK = "Successfully created";
    private static final String DELETE_OK = "Successfully deleted";

    /**
     * Retrieves all versions of a policy created for a policy type version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersionId the ID of policy type version
     *
     * @return the ToscaPolicyList object containing a list of all versions of the specified policy
     */
    public ToscaPolicyList fetchAllVersionsOfPolicy(String policyTypeId, String policyTypeVersionId) {
        // placeholder
        return new ToscaPolicyList();
    }

    /**
     * Retrieves all version details of a policy for a policy type version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersionId the ID of policy type version
     * @param policyId the ID of policy
     *
     * @return the ToscaPolicyList object containing a list of all versions of the specified policy
     */
    public ToscaPolicyList fetchAllVersionDetailsOfPolicy(String policyTypeId, String policyTypeVersionId,
                                                          String policyId) {
        // placeholder
        return new ToscaPolicyList();
    }

    /**
     * Retrieves one version of a policy created for a policy type version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersionId the ID of policy type version
     * @param policyId the ID of policy
     * @param policyVersionId the ID of policy version
     *
     * @return the ToscaPolicy object containing a specified version of the specified policy
     */
    public ToscaPolicy fetchOneVersionOfPolicy(String policyTypeId, String policyTypeVersionId,
                                               String policyId, String policyVersionId) {
        // placeholder
        return new ToscaPolicy();
    }

    /**
     * Retrieves the latest version of a policy created for a policy type version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersionId the ID of policy type version
     * @param policyId the ID of policy
     *
     * @return the ToscaPolicy object containing the latest version of the specified policy
     */
    public ToscaPolicy fetchLatestVersionOfPolicy(String policyTypeId, String policyTypeVersionId, String policyId) {
        // placeholder
        return new ToscaPolicy();
    }

    /**
     * Retrieves the deployed version of a policy created for a policy type version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersionId the ID of policy type version
     * @param policyId the ID of policy
     *
     * @return the ToscaPolicy object containing the deployed version of the specified policy
     */
    public ToscaPolicy fetchDeployedVersionOfPolicy(String policyTypeId, String policyTypeVersionId, String policyId) {
        // placeholder
        return new ToscaPolicy();
    }

    /**
     * Creates a new policy for a policy type version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersionId the ID of policy type version
     * @param body the policy body
     *
     * @return a string message indicating the operation results
     */
    public String createPolicy(String policyTypeId, String policyTypeVersionId, ToscaPolicy body) {
        // placeholder
        return POST_OK;
    }

    /**
     * Deletes all versions of a policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersionId the ID of policy type version
     * @param policyId the ID of policy
     *
     * @return a string message indicating the operation results
     */
    public String deleteAllVersionsOfPolicy(String policyTypeId, String policyTypeVersionId, String policyId) {
        // placeholder
        return DELETE_OK;
    }

    /**
     * Deletes a particular version of policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersionId the ID of policy type version
     * @param policyId the ID of policy
     * @param policyVersionId the ID of policy version
     *
     * @return a string message indicating the operation results
     */
    public String deleteOneVersionOfPolicy(String policyTypeId, String policyTypeVersionId,
                                           String policyId, String policyVersionId) {
        // placeholder
        return DELETE_OK;
    }
}
