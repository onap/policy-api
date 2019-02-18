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

package org.onap.policy.api.main.rest.model;

/**
 * Class to represent API statistics report.
 */
public class StatisticsReport {
    
    private int code;
    private long totalApiCallCount;
    private long apiCallSuccessCount;
    private long apiCallFailureCount;
    private long totalPolicyGetCount;
    private long totalPolicyPostCount;
    private long totalTemplateGetCount;
    private long totalTemplatePostCount;
    private long policyGetSuccessCount;
    private long policyGetFailureCount;
    private long policyPostSuccessCount;
    private long policyPostFailureCount;
    private long templateGetSuccessCount;
    private long templateGetFailureCount;
    private long templatePostSuccessCount;
    private long templatePostFailureCount;
    
    /**
     * Returns the code of this {@link StatisticsReport} instance.
     *
     * @return the code
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Set code in this {@link StatisticsReport} instance.
     *
     * @param code the code to set
     */
    public void setCode(final int code) {
        this.code = code;
    }
    
    /**
     * Returns the totalApiCallCount of this {@link StatisticsReport} instance.
     * 
     * @return the totalApiCallCount
     */
    public long getTotalApiCallCount() {
        return totalApiCallCount;
    }
    
    /**
     * Set totalApiCallCount in this {@link StatisticsReport} instance.
     * 
     * @param totalApiCallCount the totalApiCallCount to set
     */
    public void setTotalApiCallCount(final long totalApiCallCount) {
        this.totalApiCallCount = totalApiCallCount;
    }
   
    /**
     * Returns the apiCallSuccessCount of this {@link StatisticsReport} instance.
     * 
     * @return the apiCallSuccessCount
     */
    public long getApiCallSuccessCount() {
        return apiCallSuccessCount;
    }
    
    /**
     * Set apiCallSuccessCount in this {@link StatisticsReport} instance.
     * 
     * @param apiCallSuccessCount the apiCallSuccessCount to set
     */
    public void setApiCallSuccessCount(final long apiCallSuccessCount) {
        this.apiCallSuccessCount = apiCallSuccessCount;
    }
    
    /**
     * Returns the apiCallFailureCount of this {@link StatisticsReport} instance.
     * 
     * @return the apiCallFailureCount
     */
    public long getApiCallFailureCount() {
        return apiCallFailureCount;
    } 
    
    /**
     * Set apiCallFailureCount in this {@link StatisticsReport} instance.
     * 
     * @param apiCallFailureCount the apiCallFailureCount to set
     */
    public void setApiCallFailureCount(final long apiCallFailureCount) {
        this.apiCallFailureCount = apiCallFailureCount;
    }
    
    /**
     * Returns the totalPolicyGetCount of this {@link StatisticsReport} instance.
     * 
     * @return the totalPolicyGetCount
     */
    public long getTotalPolicyGetCount() {
        return totalPolicyGetCount;
    }
    
    /**
     * Set totalPolicyGetCount in this {@link StatisticsReport} instance.
     * 
     * @param totalPolicyGetCount the totalPolicyGetCount to set
     */
    public void setTotalPolicyGetCount(final long totalPolicyGetCount) {
        this.totalPolicyGetCount = totalPolicyGetCount;
    }
    
    /**
     * Returns the totalPolicyPostCount of this {@link StatisticsReport} instance.
     * 
     * @return the totalPolicyPostCount
     */
    public long getTotalPolicyPostCount() {
        return totalPolicyPostCount;
    } 
    
    /**
     * Set totalPolicyPostCount in this {@link StatisticsReport} instance.
     * 
     * @param totalPolicyPostCount the totalPolicyPostCount to set
     */
    public void setTotalPolicyPostCount(final long totalPolicyPostCount) {
        this.totalPolicyPostCount = totalPolicyPostCount;
    }
    
    /**
     * Returns the totalTemplateGetCount of this {@link StatisticsReport} instance.
     * 
     * @return the totalTemplateGetCount
     */
    public long getTotalTemplateGetCount() {
        return totalTemplateGetCount;
    }
    
    /**
     * Set totalTemplateGetCount in this {@link StatisticsReport} instance.
     * 
     * @param totalTemplateGetCount the totalTemplateGetCount to set
     */
    public void setTotalTemplateGetCount(final long totalTemplateGetCount) {
        this.totalTemplateGetCount = totalTemplateGetCount;
    }
    
    /**
     * Returns the totalTemplatePostCount of this {@link StatisticsReport} instance.
     * 
     * @return the totalTemplatePostCount
     */
    public long getTotalTemplatePostCount() {
        return totalTemplatePostCount;
    }
    
    /**
     * Set totalTemplatePostCount in this {@link StatisticsReport} instance.
     * 
     * @param totalTemplatePostCount the totalTemplatePostCount to set
     */
    public void setTotalTemplatePostCount(final long totalTemplatePostCount) {
        this.totalTemplatePostCount = totalTemplatePostCount;
    }
    
    /**
     * Returns the policyGetSuccessCount of this {@link StatisticsReport} instance.
     * 
     * @return the policyGetSuccessCount
     */
    public long getPolicyGetSuccessCount() {
        return policyGetSuccessCount;
    }
    
    /**
     * Set policyGetSuccessCount in this {@link StatisticsReport} instance.
     * 
     * @param policyGetSuccessCount the policyGetSuccessCount to set
     */
    public void setPolicyGetSuccessCount(final long policyGetSuccessCount) {
        this.policyGetSuccessCount = policyGetSuccessCount;
    }
   
    /**
     * Returns the policyGetFailureCount of this {@link StatisticsReport} instance.
     * 
     * @return the policyGetFailureCount 
     */
    public long getPolicyGetFailureCount() {
        return policyGetFailureCount;
    }
    
    /**
     * Set policyGetFailureCount in this {@link StatisticsReport} instance.
     * 
     * @param policyGetFailureCount the policyGetFailureCount to set
     */
    public void setPolicyGetFailureCount(final long policyGetFailureCount) {
        this.policyGetFailureCount = policyGetFailureCount;
    }
    
    /**
     * Returns the policyPostSuccessCount of this {@link StatisticsReport} instance.
     * 
     * @return the policyPostSuccessCount
     */
    public long getPolicyPostSuccessCount() {
        return policyPostSuccessCount;
    }
    
    /**
     * Set policyPostSuccessCount in this {@link StatisticsReport} instance.
     * 
     * @param policyPostSuccessCount the policyPostSuccessCount to set
     */
    public void setPolicyPostSuccessCount(final long policyPostSuccessCount) {
        this.policyPostSuccessCount = policyPostSuccessCount;
    }
    
    /**
     * Returns the policyPostFailureCount of this {@link StatisticsReport} instance.
     * 
     * @return the policyPostFailureCount
     */
    public long getPolicyPostFailureCount() {
        return policyPostFailureCount;
    }
    
    /**
     * Set policyPostFailureCount in this {@link StatisticsReport} instance.
     * 
     * @param policyPostFailureCount the policyPostFailureCount to set
     */
    public void setPolicyPostFailureCount(final long policyPostFailureCount) {
        this.policyPostFailureCount = policyPostFailureCount;
    }
   
    /**
     * Returns the templateGetSuccessCount of this {@link StatisticsReport} instance.
     * 
     * @return the templateGetSuccessCount
     */
    public long getTemplateGetSuccessCount() {
        return templateGetSuccessCount;
    }
    
    /**
     * Set templateGetSuccessCount in this {@link StatisticsReport} instance.
     *  
     * @param templateGetSuccessCount the templateGetSuccessCount to set
     */
    public void setTemplateGetSuccessCount(final long templateGetSuccessCount) {
        this.templateGetSuccessCount = templateGetSuccessCount;
    }
    
    /**
     * Returns the templateGetFailureCount of this {@link StatisticsReport} instance.
     * 
     * @return the templateGetFailureCount
     */
    public long getTemplateGetFailureCount() {
        return templateGetFailureCount;
    }
    
    /**
     * Set templateGetFailureCount in this {@link StatisticsReport} instance.
     * 
     * @param templateGetFailureCount the templateGetFailureCount to set
     */
    public void setTemplateGetFailureCount(final long templateGetFailureCount) {
        this.templateGetFailureCount = templateGetFailureCount;
    }
    
    /**
     * Returns the templatePostSuccessCount of this {@link StatisticsReport} instance.
     * 
     * @return the templatePostSuccessCount
     */
    public long getTemplatePostSuccessCount() {
        return templatePostSuccessCount;
    }
    
    /**
     * Set templatePostSuccessCount in this {@link StatisticsReport} instance.
     * 
     * @param templatePostSuccessCount the templatePostSuccessCount to set
     */
    public void setTemplatePostSuccessCount(final long templatePostSuccessCount) {
        this.templatePostSuccessCount = templatePostSuccessCount;
    }
    
    /**
     * Returns the templatePostFailureCount of this {@link StatisticsReport} instance.
     * 
     * @return the templatePostFailureCount
     */
    public long getTemplatePostFailureCount() {
        return templatePostFailureCount;
    }
    
    /**
     * Set templatePostFailureCount in this {@link StatisticsReport} instance.
     * 
     * @param templatePostFailureCount the templatePostFailureCount to set
     */
    public void setTemplatePostFailureCount(final long templatePostFailureCount) {
        this.templatePostFailureCount = templatePostFailureCount;
    }
   
    /**
     * {@inheritDoc}}.
     */
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("StatisticsReport [code=");
        builder.append(getCode());
        builder.append(", totalApiCallCount=");
        builder.append(getTotalApiCallCount());
        builder.append(", apiCallSuccessCount=");
        builder.append(getApiCallSuccessCount());
        builder.append(", apiCallFailureCount=");
        builder.append(getApiCallFailureCount());
        builder.append(", totalPolicyGetCount=");
        builder.append(getTotalPolicyGetCount());
        builder.append(", totalPolicyPostCount=");
        builder.append(getTotalPolicyPostCount());
        builder.append(", totalTemplateGetCount=");
        builder.append(getTotalTemplateGetCount());
        builder.append(", totalTemplatePostCount=");
        builder.append(getTotalTemplatePostCount());
        builder.append(", policyGetSuccessCount=");
        builder.append(getPolicyGetSuccessCount());
        builder.append(", policyGetFailureCount=");
        builder.append(getPolicyGetFailureCount());
        builder.append(", policyPostSuccessCount=");
        builder.append(getPolicyPostSuccessCount());
        builder.append(", policyPostFailureCount=");
        builder.append(getPolicyPostFailureCount());
        builder.append(", templateGetSuccessCount=");
        builder.append(getTemplateGetSuccessCount());
        builder.append(", templateGetFailureCount=");
        builder.append(getTemplateGetFailureCount());
        builder.append(", templatePostSuccessCount=");
        builder.append(getTemplatePostSuccessCount());
        builder.append(", templatePostFailureCount=");
        builder.append(getTemplatePostFailureCount());
        builder.append("]");
        return builder.toString();
    }
}
