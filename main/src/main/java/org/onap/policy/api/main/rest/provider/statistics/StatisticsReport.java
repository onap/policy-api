/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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

package org.onap.policy.api.main.rest.provider.statistics;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Class to represent API statistics report.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@ToString
@Getter
@Setter
public class StatisticsReport {
    private int code;
    protected long totalApiCallCount;
    protected long apiCallSuccessCount;
    protected long apiCallFailureCount;
    protected long totalPolicyGetCount;
    protected long totalPolicyPostCount;
    protected long totalPolicyTypeGetCount;
    protected long totalPolicyTypePostCount;
    protected long policyGetSuccessCount;
    protected long policyGetFailureCount;
    protected long policyPostSuccessCount;
    protected long policyPostFailureCount;
    protected long policyTypeGetSuccessCount;
    protected long policyTypeGetFailureCount;
    protected long policyTypePostSuccessCount;
    protected long policyTypePostFailureCount;
}