/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.After;
import org.junit.Test;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.parameters.CommonTestData;
import org.onap.policy.api.main.parameters.RestServerParameters;
import org.onap.policy.api.main.startstop.Main;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to perform unit test of {@link ApiRestServer}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class TestApiRestServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestApiRestServer.class);
    private static final String NOT_ALIVE = "not alive";
    private static final String ALIVE = "alive";
    private static final String SELF = "self";
    private static final String NAME = "Policy API";
    private static final String HEALTHCHECK_ENDPOINT = "healthcheck";
    private static final String STATISTICS_ENDPOINT = "statistics";
    private static String KEYSTORE = System.getProperty("user.dir") + "/src/test/resources/ssl/policy-keystore";
    private Main main;
    private ApiRestServer restServer;

    /**
     * Method for cleanup after each test.
     */
    @After
    public void teardown() {
        try {
            if (NetworkUtil.isTcpPortOpen("localhost", 6969, 1, 1000L)) {
                if (main != null) {
                    stopApiService(main);
                } else if (restServer != null) {
                    restServer.stop();
                }
            }
        } catch (InterruptedException | IOException | PolicyApiException exp) {
            LOGGER.error("teardown failed", exp);
        }
    }

    @Test
    public void testHealthCheckSuccess() {
        try {
            main = startApiService(true);
            final Invocation.Builder invocationBuilder = sendHttpRequest(HEALTHCHECK_ENDPOINT);
            final HealthCheckReport report = invocationBuilder.get(HealthCheckReport.class);
            validateHealthCheckReport(NAME, SELF, true, 200, ALIVE, report);
        } catch (final Exception exp) {
            LOGGER.error("testHealthCheckSuccess failed", exp);
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHealthCheckFailure() throws InterruptedException, IOException {
        final RestServerParameters restServerParams = new CommonTestData().getRestServerParameters(false);
        restServerParams.setName(CommonTestData.API_GROUP_NAME);
        restServer = new ApiRestServer(restServerParams);
        try {
            restServer.start();
            final Invocation.Builder invocationBuilder = sendHttpRequest(HEALTHCHECK_ENDPOINT);
            final HealthCheckReport report = invocationBuilder.get(HealthCheckReport.class);
            validateHealthCheckReport(NAME, SELF, false, 500, NOT_ALIVE, report);
            assertTrue(restServer.isAlive());
            assertTrue(restServer.toString().startsWith("ApiRestServer [servers="));
        } catch (final Exception exp) {
            LOGGER.error("testHealthCheckFailure failed", exp);
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHttpsHealthCheckSuccess() {
        try {
            main = startApiService(false);
            final Invocation.Builder invocationBuilder = sendHttpsRequest(HEALTHCHECK_ENDPOINT);
            final HealthCheckReport report = invocationBuilder.get(HealthCheckReport.class);
            validateHealthCheckReport(NAME, SELF, true, 200, ALIVE, report);
        } catch (final Exception exp) {
            LOGGER.error("testHttpsHealthCheckSuccess failed", exp);
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testApiStatistics_200() {
        try {
            main = startApiService(true);
            Invocation.Builder invocationBuilder = sendHttpRequest(STATISTICS_ENDPOINT);
            StatisticsReport report = invocationBuilder.get(StatisticsReport.class);
            validateStatisticsReport(report, 0, 200);
            updateApiStatistics();
            invocationBuilder = sendHttpRequest(STATISTICS_ENDPOINT);
            report = invocationBuilder.get(StatisticsReport.class);
            validateStatisticsReport(report, 1, 200);
            ApiStatisticsManager.resetAllStatistics();
        } catch (final Exception exp) {
            LOGGER.error("testApiStatistics_200 failed", exp);
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testApiStatistics_500() {
        final RestServerParameters restServerParams = new CommonTestData().getRestServerParameters(false);
        restServerParams.setName(CommonTestData.API_GROUP_NAME);
        restServer = new ApiRestServer(restServerParams);
        try {
            restServer.start();
            final Invocation.Builder invocationBuilder = sendHttpRequest(STATISTICS_ENDPOINT);
            final StatisticsReport report = invocationBuilder.get(StatisticsReport.class);
            validateStatisticsReport(report, 0, 500);
            ApiStatisticsManager.resetAllStatistics();
        } catch (final Exception exp) {
            LOGGER.error("testApiStatistics_500 failed", exp);
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHttpsApiStatistics() {
        try {
            main = startApiService(false);
            final Invocation.Builder invocationBuilder = sendHttpsRequest(STATISTICS_ENDPOINT);
            final StatisticsReport report = invocationBuilder.get(StatisticsReport.class);
            validateStatisticsReport(report, 0, 200);
        } catch (final Exception exp) {
            LOGGER.error("testHttpsApiStatistics failed", exp);
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testApiStatisticsConstructorIsPrivate() {
        try {
            final Constructor<ApiStatisticsManager> constructor = ApiStatisticsManager.class.getDeclaredConstructor();
            assertTrue(Modifier.isPrivate(constructor.getModifiers()));
            constructor.setAccessible(true);
            constructor.newInstance();
        } catch (final Exception exp) {
            assertTrue(exp.getCause().toString().contains("Instantiation of the class is not allowed"));
        }
    }

    private Main startApiService(final boolean http) {
        final String[] apiConfigParameters = new String[2];
        if (http) {
            apiConfigParameters[0] = "-c";
            apiConfigParameters[1] = "parameters/ApiConfigParameters.json";
        } else {
            final Properties systemProps = System.getProperties();
            systemProps.put("javax.net.ssl.keyStore", KEYSTORE);
            systemProps.put("javax.net.ssl.keyStorePassword", "Pol1cy_0nap");
            System.setProperties(systemProps);
            apiConfigParameters[0] = "-c";
            apiConfigParameters[1] = "parameters/ApiConfigParameters_Https.json";
        }
        return new Main(apiConfigParameters);
    }

    private void stopApiService(final Main main) throws PolicyApiException {
        main.shutdown();
    }

    private Invocation.Builder sendHttpRequest(final String endpoint) throws Exception {
        final ClientConfig clientConfig = new ClientConfig();

        final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("healthcheck", "zb!XztG34");
        clientConfig.register(feature);

        final Client client = ClientBuilder.newClient(clientConfig);
        final WebTarget webTarget = client.target("http://localhost:6969/policy/api/v1/" + endpoint);

        final Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        if (!NetworkUtil.isTcpPortOpen("localhost", 6969, 6, 10000L)) {
            throw new IllegalStateException("cannot connect to port 6969");
        }
        return invocationBuilder;
    }

    private Invocation.Builder sendHttpsRequest(final String endpoint) throws Exception {

        final TrustManager[] noopTrustManager = new TrustManager[] { new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {}

            @Override
            public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {}
        } };

        final SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, noopTrustManager, new SecureRandom());
        final ClientBuilder clientBuilder = ClientBuilder.newBuilder().sslContext(sc).hostnameVerifier((host,
                session) -> true);
        final Client client = clientBuilder.build();
        final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("healthcheck", "zb!XztG34");
        client.register(feature);

        final WebTarget webTarget = client.target("https://localhost:6969/policy/api/v1/" + endpoint);

        final Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        if (!NetworkUtil.isTcpPortOpen("localhost", 6969, 6, 10000L)) {
            throw new IllegalStateException("cannot connect to port 6969");
        }
        return invocationBuilder;
    }

    private void updateApiStatistics() {
        ApiStatisticsManager.updateTotalApiCallCount();
        ApiStatisticsManager.updateApiCallSuccessCount();
        ApiStatisticsManager.updateApiCallFailureCount();
        ApiStatisticsManager.updateTotalPolicyGetCount();
        ApiStatisticsManager.updateTotalPolicyPostCount();
        ApiStatisticsManager.updateTotalPolicyTypeGetCount();
        ApiStatisticsManager.updateTotalPolicyTypePostCount();
        ApiStatisticsManager.updatePolicyGetSuccessCount();
        ApiStatisticsManager.updatePolicyGetFailureCount();
        ApiStatisticsManager.updatePolicyPostSuccessCount();
        ApiStatisticsManager.updatePolicyPostFailureCount();
        ApiStatisticsManager.updatePolicyTypeGetSuccessCount();
        ApiStatisticsManager.updatePolicyTypeGetFailureCount();
        ApiStatisticsManager.updatePolicyTypePostSuccessCount();
        ApiStatisticsManager.updatePolicyTypePostFailureCount();
    }

    private void validateStatisticsReport(final StatisticsReport report, final int count, final int code) {
        assertEquals(code, report.getCode());
        assertEquals(count, report.getTotalApiCallCount());
        assertEquals(count, report.getApiCallSuccessCount());
        assertEquals(count, report.getApiCallFailureCount());
        assertEquals(count, report.getTotalPolicyGetCount());
        assertEquals(count, report.getTotalPolicyPostCount());
        assertEquals(count, report.getTotalPolicyTypeGetCount());
        assertEquals(count, report.getTotalPolicyTypePostCount());
        assertEquals(count, report.getPolicyGetSuccessCount());
        assertEquals(count, report.getPolicyGetFailureCount());
        assertEquals(count, report.getPolicyPostSuccessCount());
        assertEquals(count, report.getPolicyPostFailureCount());
        assertEquals(count, report.getPolicyTypeGetSuccessCount());
        assertEquals(count, report.getPolicyTypeGetFailureCount());
        assertEquals(count, report.getPolicyTypePostSuccessCount());
        assertEquals(count, report.getPolicyTypePostFailureCount());
    }

    private void validateHealthCheckReport(final String name, final String url, final boolean healthy, final int code,
            final String message, final HealthCheckReport report) {
        assertEquals(name, report.getName());
        assertEquals(url, report.getUrl());
        assertEquals(healthy, report.isHealthy());
        assertEquals(code, report.getCode());
        assertEquals(message, report.getMessage());
    }
}