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

import lombok.Data;

import org.onap.policy.api.main.validator.impl.DefaultPolicyValidatorImpl;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;

/**
 * Class to hold all the plugin validator parameters.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Data
public class PolicyValidatorParameters implements ParameterGroup {
    private static final String DEFAULT_IMPLEMENTATION = DefaultPolicyValidatorImpl.class.getName();
    private static final String DEFAULT_NEXUS_NAME = "nexus";
    private static final String DEFAULT_NEXUS_PORT = "8081";
    private static final String DEFAULT_VALIDATOR_NAME = "defaultValidator";

    private String name = DEFAULT_VALIDATOR_NAME;
    private String implementation = DEFAULT_IMPLEMENTATION;
    private String nexusName = DEFAULT_NEXUS_NAME;
    private String nexusPort = DEFAULT_NEXUS_PORT;

    @Override
    public GroupValidationResult validate() {

        final GroupValidationResult validationResult = new GroupValidationResult(this);

        if (!ParameterValidationUtils.validateStringParameter(implementation)) {
            validationResult.setResult("implementation", ValidationStatus.INVALID,
                    "a PolicyValidator implementation must be specified");
        }

        if (!ParameterValidationUtils.validateStringParameter(nexusName)) {
            validationResult.setResult("nexusName", ValidationStatus.INVALID,
                    "a PolicyValidator nexusName must be specified");
        }

        if (!ParameterValidationUtils.validateStringParameter(nexusPort)) {
            validationResult.setResult("nexusPort", ValidationStatus.INVALID,
                    "a PolicyValidator nexusPort must be specified");
        }

        if (!ParameterValidationUtils.validateStringParameter(name)) {
            validationResult.setResult("name", ValidationStatus.INVALID,
                    "a PolicyValidator name must be specified");
        }

        return validationResult;
    }
}
