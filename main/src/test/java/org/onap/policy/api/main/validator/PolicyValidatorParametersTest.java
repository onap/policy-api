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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.onap.policy.common.parameters.GroupValidationResult;

/**
 * Performs the test of {@link PolicyValidatorParameters} class.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 *
 */
public class PolicyValidatorParametersTest {

    @Test
    public void testParameters() {

        PolicyValidatorParameters params = new PolicyValidatorParameters();
        GroupValidationResult result = params.validate();
        assertTrue(result.isValid());

        params.setImplementation(null);
        result = params.validate();
        assertFalse(result.isValid());
        params.setImplementation("An Implementation");
        result = params.validate();
        assertTrue(result.isValid());

        params.setName(null);
        result = params.validate();
        assertFalse(result.isValid());
        params.setName("An Name");
        result = params.validate();
        assertTrue(result.isValid());

        params.setNexusName(null);
        result = params.validate();
        assertFalse(result.isValid());
        params.setNexusName("An Nexus Name");
        result = params.validate();
        assertTrue(result.isValid());

        params.setNexusPort(null);
        result = params.validate();
        assertFalse(result.isValid());
        params.setNexusPort("An Nexus Port");
        result = params.validate();
        assertTrue(result.isValid());
    }
}