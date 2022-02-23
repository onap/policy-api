/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 IBM.
 * Modifications Copyright (C) 2020-2022 Bell Canada.
 * Modifications Copyright (C) 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.api.main.rest;

import static org.junit.Assert.assertEquals;

import lombok.RequiredArgsConstructor;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.policy.api.main.PolicyApiApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyApiApplication.class)
@ActiveProfiles("test")
public class TestApiStatisticsManager {

    @Autowired
    private ApiStatisticsManager mgr;

    @Test
    public void testUpdateMethods() {
        assertEquals(1, mgr.updateTotalApiCallCount());
        assertEquals(1, mgr.updateApiCallSuccessCount());
        assertEquals(1, mgr.updateApiCallFailureCount());
        assertEquals(1, mgr.updateTotalPolicyGetCount());
        assertEquals(1, mgr.updateTotalPolicyPostCount());
        assertEquals(1, mgr.updateTotalPolicyDeleteCount());
        assertEquals(1, mgr.updateTotalPolicyTypeGetCount());
        assertEquals(1, mgr.updateTotalPolicyTypePostCount());
        assertEquals(1, mgr.updateTotalPolicyTypeDeleteCount());
        assertEquals(1, mgr.updatePolicyGetSuccessCount());
        assertEquals(1, mgr.updatePolicyGetFailureCount());
        assertEquals(1, mgr.updatePolicyPostSuccessCount());
        assertEquals(1, mgr.updatePolicyPostFailureCount());
        assertEquals(1, mgr.updatePolicyDeleteSuccessCount());
        assertEquals(1, mgr.updatePolicyDeleteFailureCount());
        assertEquals(1, mgr.updatePolicyTypeGetSuccessCount());
        assertEquals(1, mgr.updatePolicyTypeGetFailureCount());
        assertEquals(1, mgr.updatePolicyTypePostSuccessCount());
        assertEquals(1, mgr.updatePolicyTypePostFailureCount());
        assertEquals(1, mgr.updatePolicyTypeDeleteSuccessCount());
        assertEquals(1, mgr.updatePolicyTypeDeleteFailureCount());
    }
}