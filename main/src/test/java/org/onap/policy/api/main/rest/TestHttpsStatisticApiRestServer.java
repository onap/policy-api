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

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
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

import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.Test;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.rest.model.StatisticsReport;
import org.onap.policy.api.main.startstop.Main;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to perform unit test of API statistics via https set on API REST server.
 */
public class TestHttpsStatisticApiRestServer {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestHttpsStatisticApiRestServer.class);
    private static String KEYSTORE = System.getProperty("user.dir") + "/src/test/resources/ssl/policy-keystore";

    @Test
    public void testHttpsApiStatistic()
        throws PolicyApiException, InterruptedException, KeyManagementException, NoSuchAlgorithmException {
        final String reportString = "StatisticsReport [code=200, totalApiCallCount=0, apiCallSuccessCount=0, "
            + "apiCallFailureCount=0, " + "totalPolicyGetCount=0, totalPolicyPostCount=0, " 
            + "totalTemplateGetCount=0, totalTemplatePostCount=0, " 
            + "policyGetSuccessCount=0, policyGetFailureCount=0, " 
            + "policyPostSuccessCount=0, policyPostFailureCount=0, " 
            + "templateGetSuccessCount=0, templateGetFailureCount=0, " 
            + "templatePostSuccessCount=0, templatePostFailureCount=0]";
        try {
            final Main main = startApiService();
            final StatisticsReport report = performStatisticCheck();
            validateReport(200, 0, reportString, report);
            stopApiService(main);
        } catch (final Exception exp) {
            LOGGER.error("testHttpsApiStatistic failed", exp);
            fail("Test should not throw an exception");
        }
    }

    private Main startApiService() {
        final Properties systemProps = System.getProperties();
        systemProps.put("javax.net.ssl.keyStore", KEYSTORE);
        systemProps.put("javax.net.ssl.keyStorePassword", "Pol1cy_0nap");
        System.setProperties(systemProps);

        final String[] apiConfigParameters = { "-c", "parameters/ApiConfigParameters_Https.json" };
        return new Main(apiConfigParameters);
    }

    private void stopApiService(final Main main) throws PolicyApiException {
        main.shutdown();
    }

    private StatisticsReport performStatisticCheck() throws Exception {

        final TrustManager[] noopTrustManager = new TrustManager[] { new X509TrustManager() {

            @Override
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }

            @Override
            public void checkClientTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
            }

            @Override
            public void checkServerTrusted(final java.security.cert.X509Certificate[] certs, final String authType) {
            }
        } };

        final SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, noopTrustManager, new SecureRandom());
        final ClientBuilder clientBuilder = 
                ClientBuilder.newBuilder().sslContext(sc).hostnameVerifier((host, session) -> true);
        final Client client = clientBuilder.build();
        final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("healthcheck", "zb!XztG34");
        client.register(feature);

        final WebTarget webTarget = client.target("https://localhost:6969/statistics");

        final Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);
        
        if (!NetworkUtil.isTcpPortOpen("localhost", 6969, 6, 10000L)) {
            throw new IllegalStateException("cannot connect to port 6969");
        }
        return invocationBuilder.get(StatisticsReport.class);
    }

    private void validateReport(final int code, final int count, 
            final String reportString, final StatisticsReport report) {
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
        assertEquals(reportString, report.toString());
    }
}
