/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Modifications Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2020-2021 Bell Canada. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.api.main.exception.PolicyApiRuntimeException;
import org.onap.policy.api.main.parameters.CommonTestData;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.resources.MessageConstants;

/**
 * Class to perform unit test of Main.
 *
 */
public class TestMain {
    private static final CommonTestData COMMON_TEST_DATA = new CommonTestData();

    @Test
    public void testMain() throws Exception {
        COMMON_TEST_DATA.makeParameters("src/test/resources/parameters/ApiConfigParameters_Https.json",
                "src/test/resources/parameters/ApiConfigParametersXXX.json", NetworkUtil.allocPort());
        final String[] apiConfigParameters = {"-c", "src/test/resources/parameters/ApiConfigParametersXXX.json"};
        final Main main = new Main(apiConfigParameters);
        assertTrue(main.getParameterGroup().isValid());
        assertEquals(CommonTestData.API_GROUP_NAME, main.getParameterGroup().getName());
        main.shutdown();
    }

    @Test
    public void testMain_NoArguments() {
        final String[] apiConfigParameters = {};
        assertThatThrownBy(() -> new Main(apiConfigParameters)).isInstanceOf(PolicyApiRuntimeException.class)
            .hasMessage(String.format(MessageConstants.START_FAILURE_MSG, MessageConstants.POLICY_API));
    }

    @Test
    public void testMain_InvalidArguments() {
        final String[] apiConfigParameters = {"parameters/ApiConfigParameters.json"};
        assertThatThrownBy(() -> new Main(apiConfigParameters)).isInstanceOf(PolicyApiRuntimeException.class)
            .hasMessage(String.format(MessageConstants.START_FAILURE_MSG, MessageConstants.POLICY_API));
    }

    @Test
    public void testMain_Help() {
        final String[] apiConfigParameters = {"-h"};
        assertThatCode(() -> Main.main(apiConfigParameters)).doesNotThrowAnyException();
    }

    @Test
    public void testMain_InvalidParameters() {
        final String[] apiConfigParameters = {"-c", "parameters/ApiConfigParameters_InvalidName.json"};
        assertThatThrownBy(() -> new Main(apiConfigParameters)).isInstanceOf(PolicyApiRuntimeException.class)
            .hasMessage(String.format(MessageConstants.START_FAILURE_MSG, MessageConstants.POLICY_API));
    }
}
