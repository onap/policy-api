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
import lombok.Setter;
import lombok.ToString;

/**
 * Class to represent API statistics report.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@ToString
public class StatisticsReport {

    @Getter
    @Setter
    private int code;

    @Getter
    @Setter
    private long totalApiCallCount;

    @Getter
    @Setter
    private long apiCallSuccessCount;

    @Getter
    @Setter
    private long apiCallFailureCount;

    @Getter
    @Setter
    private long totalPolicyGetCount;

    @Getter
    @Setter
    private long totalPolicyPostCount;

    @Getter
    @Setter
    private long totalPolicyTypeGetCount;

    @Getter
    @Setter
    private long totalPolicyTypePostCount;

    @Getter
    @Setter
    private long policyGetSuccessCount;

    @Getter
    @Setter
    private long policyGetFailureCount;

    @Getter
    @Setter
    private long policyPostSuccessCount;

    @Getter
    @Setter
    private long policyPostFailureCount;

    @Getter
    @Setter
    private long policyTypeGetSuccessCount;

    @Getter
    @Setter
    private long policyTypeGetFailureCount;

    @Getter
    @Setter
    private long policyTypePostSuccessCount;

    @Getter
    @Setter
    private long policyTypePostFailureCount;
}
