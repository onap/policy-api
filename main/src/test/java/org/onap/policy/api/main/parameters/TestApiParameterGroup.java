/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
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

package org.onap.policy.api.main.parameters;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.common.endpoints.parameters.RestServerParameters;
import org.onap.policy.common.parameters.GroupValidationResult;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;

/**
 * Class to perform unit test of ApiParameterGroup.
 *
 */
public class TestApiParameterGroup {
    private static final int PORT = 6969;
    private CommonTestData commonTestData = new CommonTestData();

    @Test
    public void testApiParameterGroup() {
        final RestServerParameters restServerParameters = commonTestData.getRestServerParameters(false, PORT);
        final PolicyModelsProviderParameters databaseProviderParameters =
                commonTestData.getDatabaseProviderParameters(false);
        final ApiParameterGroup apiParameters = new ApiParameterGroup(
                CommonTestData.API_GROUP_NAME, restServerParameters, databaseProviderParameters);
        final GroupValidationResult validationResult = apiParameters.validate();
        assertTrue(validationResult.isValid());
        assertEquals(restServerParameters.getHost(), apiParameters.getRestServerParameters().getHost());
        assertEquals(restServerParameters.getPort(), apiParameters.getRestServerParameters().getPort());
        assertEquals(restServerParameters.getUserName(),
                apiParameters.getRestServerParameters().getUserName());
        assertEquals(restServerParameters.getPassword(),
                apiParameters.getRestServerParameters().getPassword());
        assertEquals(restServerParameters.isHttps(), apiParameters.getRestServerParameters().isHttps());
        assertEquals(restServerParameters.isAaf(), apiParameters.getRestServerParameters().isAaf());
        assertEquals(CommonTestData.API_GROUP_NAME, apiParameters.getName());
    }

    @Test
    public void testApiParameterGroup_NullName() {
        final RestServerParameters restServerParameters = commonTestData.getRestServerParameters(false, PORT);
        final PolicyModelsProviderParameters databaseProviderParameters =
                commonTestData.getDatabaseProviderParameters(false);
        final ApiParameterGroup apiParameters = new ApiParameterGroup(null,
                        restServerParameters, databaseProviderParameters);
        final GroupValidationResult validationResult = apiParameters.validate();
        assertFalse(validationResult.isValid());
        assertEquals(null, apiParameters.getName());
        assertTrue(validationResult.getResult()
                        .contains("field \"name\" type \"java.lang.String\" value \"null\" INVALID, "
                                        + "must be a non-blank string"));
    }

    @Test
    public void testApiParameterGroup_EmptyName() {
        final RestServerParameters restServerParameters = commonTestData.getRestServerParameters(false, PORT);
        final PolicyModelsProviderParameters databaseProviderParameters =
                commonTestData.getDatabaseProviderParameters(false);
        final ApiParameterGroup apiParameters = new ApiParameterGroup("",
                        restServerParameters, databaseProviderParameters);
        final GroupValidationResult validationResult = apiParameters.validate();
        assertFalse(validationResult.isValid());
        assertEquals("", apiParameters.getName());
        assertTrue(validationResult.getResult().contains("field \"name\" type \"java.lang.String\" value \"\" INVALID, "
                        + "must be a non-blank string"));
    }

    @Test
    public void testApiParameterGroup_EmptyRestServerParameters() {
        final RestServerParameters restServerParameters = commonTestData.getRestServerParameters(true, PORT);
        final PolicyModelsProviderParameters databaseProviderParameters =
                commonTestData.getDatabaseProviderParameters(false);
        final ApiParameterGroup apiParameters = new ApiParameterGroup(
                        CommonTestData.API_GROUP_NAME, restServerParameters, databaseProviderParameters);
        final GroupValidationResult validationResult = apiParameters.validate();
        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getResult()
                        .contains("\"org.onap.policy.common.endpoints.parameters.RestServerParameters\" INVALID, "
                                        + "parameter group has status INVALID"));
    }

    @Test
    public void testApiParameterGroup_NullRestServerParameters() {
        final RestServerParameters restServerParameters = null;
        final PolicyModelsProviderParameters databaseProviderParameters =
                commonTestData.getDatabaseProviderParameters(false);
        final ApiParameterGroup apiParameters = new ApiParameterGroup(
                        CommonTestData.API_GROUP_NAME, restServerParameters, databaseProviderParameters);
        final GroupValidationResult validationResult = apiParameters.validate();
        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getResult()
                        .contains("must have restServerParameters to configure api rest server"));
    }


    @Test
    public void testApiParameterGroup_EmptyDatabaseProviderParameters() {
        final RestServerParameters restServerParameters = commonTestData.getRestServerParameters(false, PORT);
        final PolicyModelsProviderParameters databaseProviderParameters =
                commonTestData.getDatabaseProviderParameters(true);
        final ApiParameterGroup apiParameters = new ApiParameterGroup(
                        CommonTestData.API_GROUP_NAME, restServerParameters, databaseProviderParameters);
        final GroupValidationResult validationResult = apiParameters.validate();
        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getResult()
                        .contains("\"org.onap.policy.models.provider.PolicyModelsProviderParameters\" INVALID, "
                                        + "parameter group has status INVALID"));
    }

    @Test
    public void testApiParameterGroup_NullDatabaseProviderParameters() {
        final RestServerParameters restServerParameters = commonTestData.getRestServerParameters(false, PORT);
        final PolicyModelsProviderParameters databaseProviderParameters = null;
        final ApiParameterGroup apiParameters = new ApiParameterGroup(
                        CommonTestData.API_GROUP_NAME, restServerParameters, databaseProviderParameters);
        final GroupValidationResult validationResult = apiParameters.validate();
        assertFalse(validationResult.isValid());
        assertTrue(validationResult.getResult()
                        .contains("must have databaseProviderParameters to configure api rest server"));
    }

    @Test
    public void testApiParameterGroup_SetName() {
        final ApiParameterGroup apiParameters = new ApiParameterGroup(
                        CommonTestData.API_GROUP_NAME, null, null);
        assertEquals(CommonTestData.API_GROUP_NAME, apiParameters.getName());
        apiParameters.setName("SampleName");
        assertEquals("SampleName", apiParameters.getName());
    }
}
