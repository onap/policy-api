/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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

package org.onap.policy.api.main.startstop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.api.main.parameters.ApiParameterHandler;
import org.onap.policy.api.main.parameters.CommonTestData;
import org.onap.policy.common.utils.network.NetworkUtil;

/**
 * Class to perform unit test of ApiActivator.
 *
 */
public class TestApiActivator {
    private static final CommonTestData COMMON_TEST_DATA = new CommonTestData();

    @Test
    public void testApiActivator() throws Exception {
        COMMON_TEST_DATA.makeParameters("src/test/resources/parameters/ApiConfigParameters_Https.json",
                "src/test/resources/parameters/ApiConfigParametersXXX.json", NetworkUtil.allocPort());
        final String[] apiConfigParameters = {"-c", "src/test/resources/parameters/ApiConfigParametersXXX.json"};
        final ApiCommandLineArguments arguments = new ApiCommandLineArguments(apiConfigParameters);
        final ApiParameterGroup parGroup = new ApiParameterHandler().getParameters(arguments);
        final ApiActivator activator = new ApiActivator(parGroup);
        activator.initialize();
        assertTrue(activator.getParameterGroup().isValid());
        assertEquals(CommonTestData.API_GROUP_NAME, activator.getParameterGroup().getName());
        activator.terminate();
    }
}
