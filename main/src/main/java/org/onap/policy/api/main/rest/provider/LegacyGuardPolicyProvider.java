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

import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicy;
import org.onap.policy.models.tosca.simple.concepts.ToscaServiceTemplate;

/**
 * Class to provide all kinds of legacy guard policy operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class LegacyGuardPolicyProvider {

    private static final String DELETE_OK = "Successfully deleted";

    /**
     * Retrieves a list of guard policies matching specified ID and version.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the ToscaServiceTemplate object
     */
    public ToscaServiceTemplate fetchGuardPolicies(String policyId, String policyVersion) {
        // placeholder
        return new ToscaServiceTemplate();
    }

    /**
     * Creates a new guard policy.
     *
     * @param body the entity body of policy
     *
     * @return the ToscaServiceTemplate object
     */
    public ToscaServiceTemplate createGuardPolicy(LegacyGuardPolicy body) {
        // placeholder
        return new ToscaServiceTemplate();
    }

    /**
     * Deletes the guard policies matching specified ID and version.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return a string message indicating the operation results
     */
    public String deleteGuardPolicies(String policyId, String policyVersion) {
        // placeholder
        return DELETE_OK;
    }
}