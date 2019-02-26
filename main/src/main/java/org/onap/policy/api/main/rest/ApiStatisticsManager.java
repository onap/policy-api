/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import lombok.Getter;

/**
 * Class to hold statistical data for API access.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ApiStatisticsManager {

    @Getter
    private static long totalApiCallCount;

    @Getter
    private static long apiCallSuccessCount;

    @Getter
    private static long apiCallFailureCount;

    @Getter
    private static long totalPolicyGetCount;

    @Getter
    private static long totalPolicyPostCount;

    @Getter
    private static long totalPolicyTypeGetCount;

    @Getter
    private static long totalPolicyTypePostCount;

    @Getter
    private static long policyGetSuccessCount;

    @Getter
    private static long policyGetFailureCount;

    @Getter
    private static long policyPostSuccessCount;

    @Getter
    private static long policyPostFailureCount;

    @Getter
    private static long policyTypeGetSuccessCount;

    @Getter
    private static long policyTypeGetFailureCount;

    @Getter
    private static long policyTypePostSuccessCount;

    @Getter
    private static long policyTypePostFailureCount;

    private ApiStatisticsManager() {
        throw new IllegalStateException("Instantiation of the class is not allowed");
    }

    /**
     * Method to update the total api call count.
     *
     * @return the updated value of totalApiCallCount
     */
    public static long updateTotalApiCallCount() {
        return ++totalApiCallCount;
    }

    /**
     * Method to update the successful api call count.
     *
     * @return the updated value of apiCallSuccessCount
     */
    public static long updateApiCallSuccessCount() {
        return ++apiCallSuccessCount;
    }

    /**
     * Method to update the failed api call count.
     *
     * @return the updated value of apiCallFailureCount
     */
    public static long updateApiCallFailureCount() {
        return ++apiCallFailureCount;
    }

    /**
     * Method to update the total policy GET count.
     *
     * @return the updated value of totalPolicyGetCount
     */
    public static long updateTotalPolicyGetCount() {
        return ++totalPolicyGetCount;
    }

    /**
     * Method to update the total policy POST count.
     *
     * @return the updated value of totalPolicyPostCount
     */
    public static long updateTotalPolicyPostCount() {
        return ++totalPolicyPostCount;
    }

    /**
     * Method to update the total policyType GET count.
     *
     * @return the updated value of totalPolicyTypeGetCount
     */
    public static long updateTotalPolicyTypeGetCount() {
        return ++totalPolicyTypeGetCount;
    }

    /**
     * Method to update the total policyType POST count.
     *
     * @return the updated value of totalPolicyTypePostCount
     */
    public static long updateTotalPolicyTypePostCount() {
        return ++totalPolicyTypePostCount;
    }

    /**
     * Method to update successful policy GET count.
     *
     * @return the updated value of policyGetSuccessCount
     */
    public static long updatePolicyGetSuccessCount() {
        return ++policyGetSuccessCount;
    }

    /**
     * Method to update failed policy GET count.
     *
     * @return the updated value of policyGetFailureCount
     */
    public static long updatePolicyGetFailureCount() {
        return ++policyGetFailureCount;
    }

    /**
     * Method to update successful policy POST count.
     *
     * @return the updated value of policyPostSuccessCount
     */
    public static long updatePolicyPostSuccessCount() {
        return ++policyPostSuccessCount;
    }

    /**
     * Method to update failed policy POST count.
     *
     * @return the updated value of policyPostFailureCount
     */
    public static long updatePolicyPostFailureCount() {
        return ++policyPostFailureCount;
    }

    /**
     * Method to update successful policyType GET count.
     *
     * @return the updated value of policyTypeGetSuccessCount
     */
    public static long updatePolicyTypeGetSuccessCount() {
        return ++policyTypeGetSuccessCount;
    }

    /**
     * Method to update failed policyType GET count.
     *
     * @return the updated value of policyTypeGetFailureCount
     */
    public static long updatePolicyTypeGetFailureCount() {
        return ++policyTypeGetFailureCount;
    }

    /**
     * Method to update successful policyType POST count.
     *
     * @return the updated value of policyTypePostSuccessCount
     */
    public static long updatePolicyTypePostSuccessCount() {
        return ++policyTypePostSuccessCount;
    }

    /**
     * Method to update failed policyType POST count.
     *
     * @return the updated value of policyTypePostFailureCount
     */
    public static long updatePolicyTypePostFailureCount() {
        return ++policyTypePostFailureCount;
    }

    /**
     * Reset all the statistics counts to 0.
     */
    public static void resetAllStatistics() {
        totalApiCallCount = 0L;
        apiCallSuccessCount = 0L;
        apiCallFailureCount = 0L;
        totalPolicyGetCount = 0L;
        totalPolicyPostCount = 0L;
        totalPolicyTypeGetCount = 0L;
        totalPolicyTypePostCount = 0L;
        policyGetSuccessCount = 0L;
        policyGetFailureCount = 0L;
        policyPostSuccessCount = 0L;
        policyPostFailureCount = 0L;
        policyTypeGetSuccessCount = 0L;
        policyTypeGetFailureCount = 0L;
        policyTypePostSuccessCount = 0L;
        policyTypePostFailureCount = 0L;
    }
}
