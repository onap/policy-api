/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API 
 * ================================================================================ 
 * Copyright (C) 2019 IBM.
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

import org.junit.Before;
import org.junit.Test;

public class TestRestServerParameters {

    private RestServerParameters restServerParameters;

    @Before
    public void setUp() {
        restServerParameters = new RestServerParameters("host", 22, "userName", "password");
    }

    @Test
    public void testGetMethods() {
        assertEquals("userName", restServerParameters.getUserName());
        assertEquals("host", restServerParameters.getHost());
        assertEquals(22, restServerParameters.getPort());
        assertEquals("password", restServerParameters.getPassword());
    }
}
