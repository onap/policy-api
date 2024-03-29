/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2022 Bell Canada. All rights reserved.
 * Modifications Copyright (C) 2023 Nordix Foundation.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.api.main.startstop;

import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;
import org.onap.policy.api.main.PolicyApiApplication;
import org.onap.policy.api.main.config.PolicyPreloadConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = PolicyApiApplication.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class ApiDatabaseInitializerTest {

    @Autowired
    private PolicyPreloadConfig params;

    @Autowired
    private ApiDatabaseInitializer adi;

    @Test
    void testInitializeApiDatabase() {
        assertThatCode(() -> adi.initializeApiDatabase(params.getPolicyTypes(),
                params.getPolicies())).doesNotThrowAnyException();

        // invoke it again - should still be OK
        assertThatCode(() -> adi.initializeApiDatabase(params.getPolicyTypes(),
                params.getPolicies())).doesNotThrowAnyException();
    }
}