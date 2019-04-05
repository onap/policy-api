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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Base64;
import org.junit.Before;
import org.junit.Test;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class performs unit test of {@link PolicyTypeProvider}
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class TestPolicyTypeProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestPolicyTypeProvider.class);

    private PolicyTypeProvider policyTypeProvider;

    /**
     * Initialize parameters.
     */
    @Before
    public void setupParameters() throws PfModelException {

        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
        parameters.setDatabaseUrl("jdbc:h2:mem:testdb");
        parameters.setDatabaseUser("policy");
        parameters.setDatabasePassword(Base64.getEncoder().encodeToString("P01icY".getBytes()));
        parameters.setPersistenceUnit("ToscaConceptTest");
        ApiParameterGroup paramGroup = new ApiParameterGroup("ApiGroup", null, parameters);
        ParameterService.register(paramGroup, true);
        policyTypeProvider = new PolicyTypeProvider();
    }

    @Test
    public void testFetchPolicyTypes() {

    }

    @Test
    public void testFetchLatestPolicyTypes() {

    }

    @Test
    public void testCreatePolicyType() {

    }

    @Test
    public void testDeletePolicyType() {

    }
}
