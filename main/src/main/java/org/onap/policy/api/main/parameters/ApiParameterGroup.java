/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

import java.util.List;
import lombok.Getter;
import org.onap.policy.common.endpoints.parameters.RestServerParameters;
import org.onap.policy.common.parameters.ParameterGroupImpl;
import org.onap.policy.common.parameters.annotations.NotBlank;
import org.onap.policy.common.parameters.annotations.NotNull;
import org.onap.policy.common.parameters.annotations.Valid;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;

/**
 * Class to hold all parameters needed for Api component.
 *
 */
@Getter
public class ApiParameterGroup extends ParameterGroupImpl {

    @NotNull @Valid
    private final RestServerParameters restServerParameters;
    @NotNull @Valid
    private final PolicyModelsProviderParameters databaseProviderParameters;
    private final List<@NotNull @NotBlank String> preloadPolicyTypes;
    private final List<@NotNull @NotBlank String> preloadPolicies;

    /**
     * Create the api parameter group.
     *
     * @param name the parameter group name
     * @param restServerParameters the parameters for instantiating API rest server
     * @param databaseProviderParameters the parameters for instantiating database provider
     * @param preloadPolicyTypes the list of preloaded policy types
     * @param preloadPolicies the list of preloaded policies
     */
    public ApiParameterGroup(final String name, final RestServerParameters restServerParameters,
            final PolicyModelsProviderParameters databaseProviderParameters, final List<String> preloadPolicyTypes,
            final List<String> preloadPolicies) {
        super(name);
        this.restServerParameters = restServerParameters;
        this.databaseProviderParameters = databaseProviderParameters;
        this.preloadPolicyTypes = preloadPolicyTypes;
        this.preloadPolicies = preloadPolicies;
    }
}
