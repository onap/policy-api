/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 IBM.
 * Modifications Copyright (C) 2020 Bell Canada.
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

import org.junit.Before;
import org.junit.Test;

public class TestApiStatisticsManager {

    @Before
    public void setUp() {
        ApiStatisticsManager.resetAllStatistics();
    }

    @Test
    public void testUpdateMethods() {
        assertEquals(1, ApiStatisticsManager.updateTotalApiCallCount());
        assertEquals(1, ApiStatisticsManager.updateApiCallSuccessCount());
        assertEquals(1, ApiStatisticsManager.updateApiCallFailureCount());
        assertEquals(1, ApiStatisticsManager.updateTotalPolicyGetCount());
        assertEquals(1, ApiStatisticsManager.updateTotalPolicyPostCount());
        assertEquals(1, ApiStatisticsManager.updateTotalPolicyDeleteCount());
        assertEquals(1, ApiStatisticsManager.updateTotalPolicyTypeGetCount());
        assertEquals(1, ApiStatisticsManager.updateTotalPolicyTypePostCount());
        assertEquals(1, ApiStatisticsManager.updateTotalPolicyTypeDeleteCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyGetSuccessCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyGetFailureCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyPostSuccessCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyPostFailureCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyDeleteSuccessCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyDeleteFailureCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyTypeGetSuccessCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyTypeGetFailureCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyTypePostSuccessCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyTypePostFailureCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyTypeDeleteSuccessCount());
        assertEquals(1, ApiStatisticsManager.updatePolicyTypeDeleteFailureCount());
    }
}
