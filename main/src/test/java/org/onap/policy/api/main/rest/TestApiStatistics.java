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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.IOException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Test;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.parameters.CommonTestData;
import org.onap.policy.api.main.parameters.RestServerParameters;
import org.onap.policy.api.main.rest.model.StatisticsReport;
import org.onap.policy.api.main.rest.provider.ApiStatisticsManager;
import org.onap.policy.api.main.startstop.Main;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to perform unit test of {@link ApiRestController}.
 */
public class TestApiStatistics {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestApiStatistics.class);

    @Test
    public void testApiStatistics_200() throws PolicyApiException, InterruptedException {
        try {
            final Main main = startApiService();
            StatisticsReport report = getApiStatistics();
            validateReport(report, 0, 200);
            updateApiStatistics();
            report = getApiStatistics();
            validateReport(report, 1, 200);
            stopApiService(main);
            ApiStatisticsManager.resetAllStatistics();
        } catch (final Exception exp) {
            LOGGER.error("testApiStatistics_200 failed", exp);
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testApiStatistics_500() throws InterruptedException {
        final RestServerParameters restServerParams = new CommonTestData().getRestServerParameters(false);
        restServerParams.setName(CommonTestData.API_GROUP_NAME);

        final ApiRestServer restServer = new ApiRestServer(restServerParams);
        try {
            restServer.start();
            final StatisticsReport report = getApiStatistics();
            validateReport(report, 0, 500);
            restServer.shutdown();
            ApiStatisticsManager.resetAllStatistics();
        } catch (final Exception exp) {
            LOGGER.error("testApiStatistics_500 failed", exp);
            fail("Test should not throw an exception");
        }
    }


    private Main startApiService() {
        final String[] distributionConfigParameters = { "-c", "parameters/ApiConfigParameters.json" };
        return new Main(distributionConfigParameters);
    }

    private void stopApiService(final Main main) throws PolicyApiException {
        main.shutdown();
    }

    private StatisticsReport getApiStatistics() throws InterruptedException, IOException {
        final ClientConfig clientConfig = new ClientConfig();

        final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("healthcheck", "zb!XztG34");
        clientConfig.register(feature);

        final Client client = ClientBuilder.newClient(clientConfig);
        final WebTarget webTarget = client.target("http://localhost:6969/statistics");

        final Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        
        if (!NetworkUtil.isTcpPortOpen("localhost", 6969, 6, 10000L)) {
            throw new IllegalStateException("cannot connect to port 6969");
        }
        return invocationBuilder.get(StatisticsReport.class);
    }

    private void updateApiStatistics() {
        ApiStatisticsManager.updateTotalApiCallCount();
        ApiStatisticsManager.updateApiCallSuccessCount();
        ApiStatisticsManager.updateApiCallFailureCount();
        ApiStatisticsManager.updateTotalPolicyGetCount();
        ApiStatisticsManager.updateTotalPolicyPostCount();
        ApiStatisticsManager.updateTotalTemplateGetCount();
        ApiStatisticsManager.updateTotalTemplatePostCount();
        ApiStatisticsManager.updatePolicyGetSuccessCount();
        ApiStatisticsManager.updatePolicyGetFailureCount();
        ApiStatisticsManager.updatePolicyPostSuccessCount();
        ApiStatisticsManager.updatePolicyPostFailureCount();
        ApiStatisticsManager.updateTemplateGetSuccessCount();
        ApiStatisticsManager.updateTemplateGetFailureCount();
        ApiStatisticsManager.updateTemplatePostSuccessCount();
        ApiStatisticsManager.updateTemplatePostFailureCount();
    }

    private void validateReport(final StatisticsReport report, final int count, final int code) {
        assertEquals(code, report.getCode());
        assertEquals(count, report.getTotalApiCallCount());
        assertEquals(count, report.getApiCallSuccessCount());
        assertEquals(count, report.getApiCallFailureCount());
        assertEquals(count, report.getTotalPolicyGetCount());
        assertEquals(count, report.getTotalPolicyPostCount());
        assertEquals(count, report.getTotalTemplateGetCount());
        assertEquals(count, report.getTotalTemplatePostCount());
        assertEquals(count, report.getPolicyGetSuccessCount());
        assertEquals(count, report.getPolicyGetFailureCount());
        assertEquals(count, report.getPolicyPostSuccessCount());
        assertEquals(count, report.getPolicyPostFailureCount());
        assertEquals(count, report.getTemplateGetSuccessCount());
        assertEquals(count, report.getTemplateGetFailureCount());
        assertEquals(count, report.getTemplatePostSuccessCount());
        assertEquals(count, report.getTemplatePostFailureCount());
    }
}
