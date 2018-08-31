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

package org.onap.policy.api.main.startstop;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.api.main.PolicyApiException;
import org.onap.policy.api.main.parameters.CommonTestData;

/**
 * Class to perform unit test of Main.
 *
 */
public class TestMain {

    @Test
    public void testMain() throws PolicyApiException {
        final String[] apiConfigParameters =
        { "-c", "parameters/ApiConfigParameters.json" };
        final Main main = new Main(apiConfigParameters);
        assertTrue(main.getParameters().isValid());
        assertEquals(CommonTestData.API_GROUP_NAME, main.getParameters().getName());
        main.shutdown();
    }

    @Test
    public void testMain_NoArguments() {
        final String[] apiConfigParameters =
        {};
        final Main main = new Main(apiConfigParameters);
        assertTrue(main.getParameters() == null);
    }

    @Test
    public void testMain_InvalidArguments() {
        final String[] apiConfigParameters =
        { "parameters/ApiConfigParameters.json" };
        final Main main = new Main(apiConfigParameters);
        assertTrue(main.getParameters() == null);
    }

    @Test
    public void testMain_Help() {
        final String[] apiConfigParameters =
        { "-h" };
        Main.main(apiConfigParameters);
    }

    @Test
    public void testMain_InvalidParameters() {
        final String[] apiConfigParameters =
        { "-c", "parameters/ApiConfigParameters_InvalidName.json" };
        final Main main = new Main(apiConfigParameters);
        assertTrue(main.getParameters() == null);
    }
}
