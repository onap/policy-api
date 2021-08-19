/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import org.onap.policy.api.main.rest.PolicyFetchMode;
import org.onap.policy.api.main.startstop.ApiActivator;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to fetch health check of api service.
 *
 */
public class HealthCheckProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(HealthCheckProvider.class);

    private static final String NOT_ALIVE = "not alive";
    private static final String ALIVE = "alive";
    private static final String URL = NetworkUtil.getHostname();
    private static final String NAME = "Policy API";
    private static final String DB_CONN_FAILURE = "unable to connect with database";

    /**
     * Performs the health check of api service.
     *
     * @return Report containing health check status
     */
    public HealthCheckReport performHealthCheck() {
        final var report = new HealthCheckReport();
        boolean heathStatus = ApiActivator.isAlive();
        if (heathStatus) {
            boolean dbConnectionStatus = verifyApiDatabase();
            report.setName(NAME);
            report.setUrl(URL);
            report.setHealthy(dbConnectionStatus);
            report.setCode(dbConnectionStatus ? 200 : 503);
            report.setMessage(dbConnectionStatus ? ALIVE : DB_CONN_FAILURE);
        } else {
            report.setName(NAME);
            report.setUrl(URL);
            report.setHealthy(heathStatus);
            report.setCode(500);
            report.setMessage(NOT_ALIVE);
        }
        return report;
    }

    /**
     * Verifies the connectivity between api component & policy database.
     *
     * @return boolean signaling the verification result
     */
    private boolean verifyApiDatabase() {
        try (var policyProvider = new PolicyProvider()) {
            policyProvider.fetchPolicies(null, null, null, null, PolicyFetchMode.BARE);
            return true;
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("Api to database connection check failed. Details - ", pfme);
            return false;
        }
    }
}
