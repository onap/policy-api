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

import org.onap.policy.api.main.validator.impl.DefaultPayloadValidatorImpl;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import lombok.Data;

/**
 * Class to hold all the plugin validator parameters.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Data
public class PayloadValidatorParameters implements ParameterGroup {
    private static final String DEFAULT_IMPLEMENTATION = DefaultPayloadValidatorImpl.class.getName();

    private String name;
    private String implementation = DEFAULT_IMPLEMENTATION;

    @Override
    public GroupValidationResult validate() {
        final GroupValidationResult validationResult = new GroupValidationResult(this);

        if (!ParameterValidationUtils.validateStringParameter(implementation)) {
            validationResult.setResult("implementation", ValidationStatus.INVALID,
                    "a PayloadValidator implementation must be specified");
        }

        return validationResult;
    }
}
