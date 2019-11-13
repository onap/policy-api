/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
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

package org.onap.policy.api.main.parameters;

import java.util.ArrayList;
import java.util.List;
import org.onap.policy.common.endpoints.parameters.RestServerParameters;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.common.parameters.ParameterGroup;
import org.onap.policy.common.parameters.ValidationStatus;
import org.onap.policy.common.utils.validation.ParameterValidationUtils;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;

/**
 * Class to hold all parameters needed for Api component.
 *
 */
public class ApiParameterGroup implements ParameterGroup {

    private String name;
    private RestServerParameters restServerParameters;
    private PolicyModelsProviderParameters databaseProviderParameters;
    private List<String> preloadPolicyTypes;

    /**
     * Create the api parameter group.
     *
     * @param name the parameter group name
     * @param restServerParameters the parameters for instantiating API rest server
     * @param databaseProviderParameters the parameters for instantiating database provider
     * @param preloadPolicyTypes the list of preloaded policy types
     */
    public ApiParameterGroup(final String name, final RestServerParameters restServerParameters,
            final PolicyModelsProviderParameters databaseProviderParameters, final List<String> preloadPolicyTypes) {
        this.name = name;
        this.restServerParameters = restServerParameters;
        this.databaseProviderParameters = databaseProviderParameters;
        this.preloadPolicyTypes = preloadPolicyTypes;
    }

    /**
     * Return the name of this parameter group instance.
     *
     * @return name the parameter group name
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * Set the name of this parameter group instance.
     *
     * @param name the parameter group name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Return the restServerParameters of this parameter group instance.
     *
     * @return the restServerParameters
     */
    public RestServerParameters getRestServerParameters() {
        return restServerParameters;
    }

    /**
     * Return the databaseProviderParameters of this parameter group instance.
     *
     * @return the databaseProviderParameters
     */
    public PolicyModelsProviderParameters getDatabaseProviderParameters() {
        return databaseProviderParameters;
    }

    /**
     * Return the preloadPolicyTypes of this parameter group instance.
     *
     * @return the preloadPolicyTypes
     */
    public List<String> getPreloadPolicyTypes() {
        return preloadPolicyTypes;
    }

    /**
     * Validate the parameter group.
     *
     * @return the result of the validation
     */
    @Override
    public GroupValidationResult validate() {
        final GroupValidationResult validationResult = new GroupValidationResult(this);
        if (!ParameterValidationUtils.validateStringParameter(name)) {
            validationResult.setResult("name", ValidationStatus.INVALID, "must be a non-blank string");
        }
        if (restServerParameters == null) {
            validationResult.setResult("restServerParameters", ValidationStatus.INVALID,
                    "must have restServerParameters to configure api rest server");
        } else {
            validationResult.setResult("restServerParameters", restServerParameters.validate());
        }
        if (databaseProviderParameters == null) {
            validationResult.setResult("databaseProviderParameters", ValidationStatus.INVALID,
                    "must have databaseProviderParameters to configure api rest server");
        } else {
            validationResult.setResult("databaseProviderParameters", databaseProviderParameters.validate());
        }
        return validationResult;
    }
}
