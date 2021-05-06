/*-
/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.api.main.rest.provider;

import org.onap.policy.api.main.rest.ApiStatisticsManager;
import org.onap.policy.api.main.rest.StatisticsReport;
import org.onap.policy.api.main.startstop.ApiActivator;

/**
 * Class to fetch API statistics.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class StatisticsProvider {

    /**
     * Return the current API statistics.
     *
     * @return Report containing API statistics
     */
    public StatisticsReport fetchCurrentStatistics() {
        final var report = new StatisticsReport();
        report.setCode(ApiActivator.isAlive() ? 200 : 500);
        report.setTotalApiCallCount(ApiStatisticsManager.getTotalApiCallCount());
        report.setApiCallSuccessCount(ApiStatisticsManager.getApiCallSuccessCount());
        report.setApiCallFailureCount(ApiStatisticsManager.getApiCallFailureCount());
        report.setTotalPolicyGetCount(ApiStatisticsManager.getTotalPolicyGetCount());
        report.setTotalPolicyPostCount(ApiStatisticsManager.getTotalPolicyPostCount());
        report.setTotalPolicyTypeGetCount(ApiStatisticsManager.getTotalPolicyTypeGetCount());
        report.setTotalPolicyTypePostCount(ApiStatisticsManager.getTotalPolicyTypePostCount());
        report.setPolicyGetSuccessCount(ApiStatisticsManager.getPolicyGetSuccessCount());
        report.setPolicyGetFailureCount(ApiStatisticsManager.getPolicyGetFailureCount());
        report.setPolicyPostSuccessCount(ApiStatisticsManager.getPolicyPostSuccessCount());
        report.setPolicyPostFailureCount(ApiStatisticsManager.getPolicyPostFailureCount());
        report.setPolicyTypeGetSuccessCount(ApiStatisticsManager.getPolicyTypeGetSuccessCount());
        report.setPolicyTypeGetFailureCount(ApiStatisticsManager.getPolicyTypeGetFailureCount());
        report.setPolicyTypePostSuccessCount(ApiStatisticsManager.getPolicyTypePostSuccessCount());
        report.setPolicyTypePostFailureCount(ApiStatisticsManager.getPolicyTypePostFailureCount());
        return report;
    }
}

