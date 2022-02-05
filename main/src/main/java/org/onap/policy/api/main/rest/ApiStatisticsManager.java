/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020-2022 Bell Canada. All rights reserved.
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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Class to hold statistical data for API access.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Component
public class ApiStatisticsManager {

    @Autowired
    private StatisticsReport report;

    private long totalPolicyDeleteCount;
    private long totalPolicyTypeDeleteCount;
    private long policyDeleteSuccessCount;
    private long policyDeleteFailureCount;
    private long policyTypeDeleteSuccessCount;
    private long policyTypeDeleteFailureCount;

    /**
     * Method to update the total api call count.
     *
     * @return the updated value of totalApiCallCount
     */
    public long updateTotalApiCallCount() {
        return ++report.totalApiCallCount;
    }

    /**
     * Method to update the successful api call count.
     *
     * @return the updated value of apiCallSuccessCount
     */
    public long updateApiCallSuccessCount() {
        return ++report.apiCallSuccessCount;
    }

    /**
     * Method to update the failed api call count.
     *
     * @return the updated value of apiCallFailureCount
     */
    public long updateApiCallFailureCount() {
        return ++report.apiCallFailureCount;
    }

    /**
     * Method to update the total policy GET count.
     *
     * @return the updated value of totalPolicyGetCount
     */
    public long updateTotalPolicyGetCount() {
        return ++report.totalPolicyGetCount;
    }

    /**
     * Method to update the total policy POST count.
     *
     * @return the updated value of totalPolicyPostCount
     */
    public long updateTotalPolicyPostCount() {
        return ++report.totalPolicyPostCount;
    }

    /**
     * Method to update the total policy DELETE count.
     *
     * @return the updated value of  totalPolicyDeleteCount
     */
    public long updateTotalPolicyDeleteCount() {
        return ++totalPolicyDeleteCount;
    }

    /**
     * Method to update the total policyType GET count.
     *
     * @return the updated value of totalPolicyTypeGetCount
     */
    public long updateTotalPolicyTypeGetCount() {
        return ++report.totalPolicyTypeGetCount;
    }

    /**
     * Method to update the total policyType POST count.
     *
     * @return the updated value of totalPolicyTypePostCount
     */
    public long updateTotalPolicyTypePostCount() {
        return ++report.totalPolicyTypePostCount;
    }

    /**
     * Method to update the total policyType DELETE count.
     *
     * @return the updated value of totalPolicyTypeDeleteCount
     */
    public long updateTotalPolicyTypeDeleteCount() {
        return ++totalPolicyTypeDeleteCount;
    }

    /**
     * Method to update successful policy GET count.
     *
     * @return the updated value of policyGetSuccessCount
     */
    public long updatePolicyGetSuccessCount() {
        return ++report.policyGetSuccessCount;
    }

    /**
     * Method to update failed policy GET count.
     *
     * @return the updated value of policyGetFailureCount
     */
    public long updatePolicyGetFailureCount() {
        return ++report.policyGetFailureCount;
    }

    /**
     * Method to update successful policy POST count.
     *
     * @return the updated value of policyPostSuccessCount
     */
    public long updatePolicyPostSuccessCount() {
        return ++report.policyPostSuccessCount;
    }

    /**
     * Method to update failed policy POST count.
     *
     * @return the updated value of policyPostFailureCount
     */
    public long updatePolicyPostFailureCount() {
        return ++report.policyPostFailureCount;
    }

    /**
     * Method to update successful policy DELETE count.
     *
     * @return the updated value of policyDeleteSuccessCount
     */
    public long updatePolicyDeleteSuccessCount() {
        return ++policyDeleteSuccessCount;
    }

    /**
     * Method to update failed policy DELETE count.
     *
     * @return the updated value of policyDeleteFailureCount
     */
    public long updatePolicyDeleteFailureCount() {
        return ++policyDeleteFailureCount;
    }

    /**
     * Method to update successful policyType GET count.
     *
     * @return the updated value of policyTypeGetSuccessCount
     */
    public long updatePolicyTypeGetSuccessCount() {
        return ++report.policyTypeGetSuccessCount;
    }

    /**
     * Method to update failed policyType GET count.
     *
     * @return the updated value of policyTypeGetFailureCount
     */
    public long updatePolicyTypeGetFailureCount() {
        return ++report.policyTypeGetFailureCount;
    }

    /**
     * Method to update successful policyType POST count.
     *
     * @return the updated value of policyTypePostSuccessCount
     */
    public long updatePolicyTypePostSuccessCount() {
        return ++report.policyTypePostSuccessCount;
    }

    /**
     * Method to update failed policyType POST count.
     *
     * @return the updated value of policyTypePostFailureCount
     */
    public long updatePolicyTypePostFailureCount() {
        return ++report.policyTypePostFailureCount;
    }

    /**
     * Method to update successful policyType DELETE count.
     *
     * @return the updated value of policyTypeDeleteSuccessCount
     */
    public long updatePolicyTypeDeleteSuccessCount() {
        return ++policyTypeDeleteSuccessCount;
    }

    /**
     * Method to update failed policyType DELETE count.
     *
     * @return the updated value of policyTypePostFailureCount
     */
    public long updatePolicyTypeDeleteFailureCount() {
        return ++policyTypeDeleteFailureCount;
    }
}