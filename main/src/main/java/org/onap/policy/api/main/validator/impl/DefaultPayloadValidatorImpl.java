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

package org.onap.policy.api.main.validator.impl;

import org.onap.policy.api.main.validator.PayloadValidator;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfValidationResult;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;

/**
 * Class to implement a default payload validator that can be used in policy API.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class DefaultPayloadValidatorImpl implements PayloadValidator {

    private static final String NEXUS_HOSTNAME = "nexus";
    private static final String NEXUS_PORT = "8081";
    private static final String NEXUS_ENDPOINT = "/nexus/service/local/artifact/maven/resolve";

    @Override
    public PfValidationResult validatePolicyAgainstPolicyType(PfValidationResult resultIn, final ToscaPolicy policy,
            final ToscaPolicyType policyType) throws PfModelException {

        // TODO
        return null;
    }

    @Override
    public PfValidationResult validateDroolsPolicyDependency(
            PfValidationResult resultIn, final ToscaPolicy droolsPolicy) throws PfModelException {

        // TODO: use Nexus 2 API to check the existence of the specified DRL artifact in the nexus repo
        return null;
    }

    @Override
    public PfValidationResult validateXacmlPolicy(PfValidationResult resultIn, final ToscaPolicy xacmlPolicy)
            throws PfModelException {

        // TODO
        return null;
    }

    @Override
    public PfValidationResult validateApexPolicy(PfValidationResult resultIn, final ToscaPolicy apexPolicy)
            throws PfModelException {

        // TODO
        return null;
    }
}
