/*-
 * ============LICENSE_START=======================================================
 * ONAP
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

/**
 * Class to hold statistical data for API access.
 */
public class ApiStatisticsManager {
   
    private static long totalApiCallCount;
    private static long apiCallSuccessCount;
    private static long apiCallFailureCount;
    private static long totalPolicyGetCount;
    private static long totalPolicyPostCount;
    private static long totalTemplateGetCount;
    private static long totalTemplatePostCount;
    private static long policyGetSuccessCount;
    private static long policyGetFailureCount;
    private static long policyPostSuccessCount;
    private static long policyPostFailureCount;
    private static long templateGetSuccessCount;
    private static long templateGetFailureCount;
    private static long templatePostSuccessCount;
    private static long templatePostFailureCount;
   
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
     * Method to update the total template GET count.
     * 
     * @return the updated value of totalTemplateGetCount
     */
    public static long updateTotalTemplateGetCount() {
        return ++totalTemplateGetCount;
    }
    
    /**
     * Method to update the total template POST count.
     * 
     * @return the updated value of totalTemplatePostCount
     */
    public static long updateTotalTemplatePostCount() {
        return ++totalTemplatePostCount;
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
     * Method to update successful template GET count.
     * 
     * @return the updated value of templateGetSuccessCount
     */
    public static long updateTemplateGetSuccessCount() {
        return ++templateGetSuccessCount;
    }
    
    /**
     * Method to update failed template GET count.
     * 
     * @return the updated value of templateGetFailureCount
     */
    public static long updateTemplateGetFailureCount() {
        return ++templateGetFailureCount;
    }
    
    /**
     * Method to update successful template POST count.
     * 
     * @return the updated value of templatePostSuccessCount
     */
    public static long updateTemplatePostSuccessCount() {
        return ++templatePostSuccessCount;
    }
    
    /**
     * Method to update failed template POST count.
     * 
     * @return the updated value of templatePostFailureCount
     */
    public static long updateTemplatePostFailureCount() {
        return ++templatePostFailureCount;
    }
    
    /**
     * Returns the current value of totalApiCallCount.
     * 
     * @return the totalApiCallCount
     */
    public static long getTotalApiCallCount() {
        return totalApiCallCount;
    }
   
    /**
     * Returns the current value of apiCallSuccessCount.
     * 
     * @return the apiCallSuccessCount
     */
    public static long getApiCallSuccessCount() {
        return apiCallSuccessCount;
    }
    
    /**
     * Returns the current value of apiCallFailureCount.
     * 
     * @return the apiCallFailureCount
     */
    public static long getApiCallFailureCount() {
        return apiCallFailureCount;
    } 
    
    /**
     * Returns the current value of totalPolicyGetCount.
     * 
     * @return the totalPolicyGetCount
     */
    public static long getTotalPolicyGetCount() {
        return totalPolicyGetCount;
    }
    
    /**
     * Returns the current value of totalPolicyPostCount.
     * 
     * @return the totalPolicyPostCount
     */
    public static long getTotalPolicyPostCount() {
        return totalPolicyPostCount;
    } 
    
    /**
     * Returns the current value of totalTemplateGetCount.
     * 
     * @return the totalTemplateGetCount
     */
    public static long getTotalTemplateGetCount() {
        return totalTemplateGetCount;
    }
    
    /**
     * Returns the current value of totalTemplatePostCount.
     * 
     * @return the totalTemplatePostCount
     */
    public static long getTotalTemplatePostCount() {
        return totalTemplatePostCount;
    }
    
    /**
     * Returns the current value of policyGetSuccessCount.
     * 
     * @return the policyGetSuccessCount
     */
    public static long getPolicyGetSuccessCount() {
        return policyGetSuccessCount;
    }
    
    /**
     * Returns the current value of policyGetFailureCount.
     * 
     * @return the policyGetFailureCount 
     */
    public static long getPolicyGetFailureCount() {
        return policyGetFailureCount;
    }
    
    /**
     * Returns the current value of policyPostSuccessCount.
     * 
     * @return the policyPostSuccessCount
     */
    public static long getPolicyPostSuccessCount() {
        return policyPostSuccessCount;
    }
    
    /**
     * Returns the current value of policyPostFailureCount.
     * 
     * @return the policyPostFailureCount
     */
    public static long getPolicyPostFailureCount() {
        return policyPostFailureCount;
    }
    
    /**
     * Returns the current value of templateGetSuccessCount.
     * 
     * @return the templateGetSuccessCount
     */
    public static long getTemplateGetSuccessCount() {
        return templateGetSuccessCount;
    }
    
    /**
     * Returns the current value of templateGetFailureCount.
     * 
     * @return the templateGetFailureCount
     */
    public static long getTemplateGetFailureCount() {
        return templateGetFailureCount;
    }
    
    /**
     * Returns the current value of templatePostSuccessCount.
     * 
     * @return the templatePostSuccessCount
     */
    public static long getTemplatePostSuccessCount() {
        return templatePostSuccessCount;
    }
    
    /**
     * Returns the current value of templatePostFailureCount.
     * 
     * @return the templatePostFailureCount
     */
    public static long getTemplatePostFailureCount() {
        return templatePostFailureCount;
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
        totalTemplateGetCount = 0L;
        totalTemplatePostCount = 0L;
        policyGetSuccessCount = 0L;
        policyGetFailureCount = 0L;
        policyPostSuccessCount = 0L;
        policyPostFailureCount = 0L;
        templateGetSuccessCount = 0L;
        templateGetFailureCount = 0L;
        templatePostSuccessCount = 0L;
        templatePostFailureCount = 0L;
    }
}
