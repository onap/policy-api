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

package org.onap.policy.api.main.validator;

import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;

/**
 * This interface describes the operations that can be used to validate the payloads provided
 * in the native PDP policy API calls.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public interface PayloadValidator {

    /**
     * Validates policy property values against their definitions in the corresponding policy type.
     * @param resultIn the initial validation results
     * @param policy the TOSCA policy to validate
     * @param policyType the policy type including property definitions to validate against
     * @return the validation results
     * @throws PfModelException on errors validating policy against its policy type
     */
    public PfValidationResult validatePolicyAgainstPolicyType(PfValidationResult resultIn, final ToscaPolicy policy,
            final ToscaPolicyType policyType) throws PfModelException;

    /**
     * Validates the existence of the DRL artifact in nexus repo that is required by the native Drools policy.
     * @param resultIn the initial validation results
     * @param policy the TOSCA compliant native Drools policy
     * @return the validation results
     * @throws PfModelException on errors validating DRL artifact existence
     */
    public PfValidationResult validateDroolsPolicyDependency(PfValidationResult resultIn, final ToscaPolicy droolsPolicy)
            throws PfModelException;

    /**
     * Validates the native XACML rules URL encoded in the TOSCA compliant policy.
     * @param resultIn the initial validation results
     * @param policy the TOSCA compliant native XACML policy
     * @return the validation results
     * @throws PfModelException on errors validating native XACML rules
     */
    public PfValidationResult validateXacmlPolicy(PfValidationResult resultIn, final ToscaPolicy xacmlPolicy)
            throws PfModelException;

    /**
     * Validates the native APEX policy.
     * @param resultIn the initial validation results
     * @param apexPolicy the TOSCA compliant native APEX policy
     * @return the validation results
     * @throws PfModelException on errors validating native APEX policy
     */
    public PfValidationResult validateApexPolicy(PfValidationResult resultIn, final ToscaPolicy apexPolicy)
            throws PfModelException;
}

