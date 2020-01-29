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

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.Test;

/**
 * Test the {@link PolicyValidatorFactory} class.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 *
 */
public class PolicyValidatorFactoryTest {

    @Test
    public void testFactory() throws Exception {
        PolicyValidatorFactory factory = new PolicyValidatorFactory();
        PolicyValidatorParameters params =
                PolicyValidatorParameters.builder().nexusName("localhost").nexusPort("8081").build();

        // @formatter:off
        assertThatThrownBy(() -> {
            factory.createPolicyValidator(null);
        }).hasMessage("parameters is marked @NonNull but is null");

        assertThatThrownBy(() -> {
            params.setImplementation(null);
            factory.createPolicyValidator(params);
        }).hasMessage("could not find the implementation of \"PolicyValidator\" interface: \"null\"");

        assertThatThrownBy(() -> {
            params.setImplementation("com.dummy.dummy");
            factory.createPolicyValidator(params);
        }).hasMessage("could not find the implementation of \"PolicyValidator\" interface: \"com.dummy.dummy\"");

        assertThatThrownBy(() -> {
            params.setImplementation("java.lang.String");
            factory.createPolicyValidator(params);
        }).hasMessage("the class \"java.lang.String\" is not an implementation of the \"PolicyValidator\" interface");

        assertThatThrownBy(() -> {
            params.setImplementation("org.onap.policy.api.main.validator.impl.DummyBadValidatorImpl");
            factory.createPolicyValidator(params);
        }).hasMessage("could not create an instance of PolicyValidator "
                + "\"org.onap.policy.api.main.validator.impl.DummyBadValidatorImpl\"");
        // @formatter:on

        params.setImplementation("org.onap.policy.api.main.validator.impl.DefaultPolicyValidatorImpl");
        factory.createPolicyValidator(params);
    }
}
