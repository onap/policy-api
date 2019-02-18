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

import org.onap.policy.api.main.rest.model.PolicyType;
import org.onap.policy.api.main.rest.model.PolicyTypeList;

/**
 *
 * Class for all kinds of policy type operations
 *
 */
public class PolicyTypeProvider {

    private static final String POST_OK = "Successfully created";
    private static final String DELETE_OK = "Successfully deleted";

    /**
     * Retrieves all policy types stored in Policy Framework
     *
     * @return the PolicyTypeList object containing a list of current policy types
     */
    public PolicyTypeList fetchAllPolicyTypes() {
        // placeholder
        return new PolicyTypeList();
    }

    /**
     * Retrieves a list of all versions of particular policy type ID
     *
     * @param policyTypeId the policy type ID
     *
     * @return the PolicyTypeList object containing a list of all available versions for the policy type ID
     */
    public PolicyTypeList fetchAllVersionsOfPolicyType(String policyTypeId) {
        // placeholder
        return new PolicyTypeList();
    }

    /**
     * Retrieves one version of policy Type ID
     *
     * @param policyTypeId the policy type ID
     * @param versionId the version ID
     *
     * @return the PolicyType object containing a version of policy type matching policy type ID and version ID
     */
    public PolicyType fetchOneVersionOfPolicyType(String policyTypeId, String versionId) {
        // placeholder
        return new PolicyType();
    }

    /**
     * Creates a new policy type
     *
     * @param body the policy type body
     *
     * @return a string message indicating the operation results
     */
    public String createPolicyType(PolicyType body) {
        // placeholder
        return POST_OK;
    }

    /**
     * Delete all versions of a policy type
     *
     * @param policyTypeId the ID of policy type
     *
     * @return a string message indicating the operation results
     */
    public String deleteAllVersionsOfPolicyType(String policyTypeId) {
        // placeholder
        return DELETE_OK;
    }

    /**
     * Delete one version of a policy type
     *
     * @param policyTypeId the ID of policy type
     * @param versionId the ID of version
     *
     * @return a string message indicating the operation results
     */
    public String deleteOneVersionOfPolicyType(String policyTypeId, String versionId) {
        // placeholder
        return DELETE_OK;
    }
}
