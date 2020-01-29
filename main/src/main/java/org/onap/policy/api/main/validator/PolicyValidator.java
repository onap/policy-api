/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2020 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.api.main.validator;

import java.util.Map;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

/**
 * This interface describes the operations that can be used to validate the payloads provided
 * in the native PDP policy API calls.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public interface PolicyValidator {

    /**
     * Validates policy property values against their definitions in the corresponding policy type.
     * @param policies the TOSCA policies to validate
     * @throws PfModelException on errors validating policy against its policy type
     */
    public void validatePoliciesAgainstPolicyTypes(final Map<ToscaEntityKey, ToscaPolicy> policies)
            throws PfModelException;

    /**
     * Validates TOSCA compliant native policies.
     * @param policies the TOSCA policies to validate
     * @throws PfModelException on errors validating the native policies
     */
    public void validateNativePolicies(final Map<ToscaEntityKey, ToscaPolicy> policies) throws PfModelException;

    /**
     * Validates the existence of the DRL artifacts in nexus repo that is required by the native Drools policies.
     * @param droolsPolicies the TOSCA compliant native Drools policies
     * @throws PfModelException on errors validating DRL artifacts
     */
    public void validateDroolsPolicies(final Map<ToscaEntityKey, ToscaPolicy> droolsPolicies) throws PfModelException;

    /**
     * Validates the native XACML rules URL encoded in the TOSCA compliant policies.
     * @param xacmlPolicies the TOSCA compliant native XACML policies
     * @throws PfModelException on errors validating native XACML rules
     */
    public void validateXacmlPolicies(final Map<ToscaEntityKey, ToscaPolicy> xacmlPolicies) throws PfModelException;

    /**
     * Validates the native APEX policies.
     * @param apexPolicies the TOSCA compliant native APEX policies
     * @throws PfModelException on errors validating native APEX policies
     */
    public void validateApexPolicies(final Map<ToscaEntityKey, ToscaPolicy> apexPolicies) throws PfModelException;
}

