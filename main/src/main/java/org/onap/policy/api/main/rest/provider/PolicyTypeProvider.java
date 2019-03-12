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

import org.onap.policy.models.tosca.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.concepts.ToscaPolicyTypes;

/**
 * Class to provide all kinds of policy type operations.
 */
public class PolicyTypeProvider {

    private static final String POST_OK = "Successfully created";
    private static final String DELETE_OK = "Successfully deleted";

    /**
     * Retrieves a list of policy types matching specified policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @return the ToscaPolicyTypeList object containing a list of policy types matching specified fields
     */
    public ToscaPolicyTypes fetchPolicyTypes(String policyTypeId, String policyTypeVersion) {
        // placeholder
        return new ToscaPolicyTypes();
    }

    /**
     * Creates a new policy type.
     *
     * @param body the entity body of policy type
     *
     * @return a string message indicating the operation results
     */
    public String createPolicyType(ToscaPolicyType body) {
        // placeholder
        return POST_OK;
    }

    /**
     * Delete the policy types matching specified policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     *
     * @return a string message indicating the operation results
     */
    public String deletePolicyTypes(String policyTypeId, String policyTypeVersion) {
        // placeholder
        return DELETE_OK;
    }
}
