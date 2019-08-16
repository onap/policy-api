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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.After;
import org.junit.Test;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.api.main.parameters.CommonTestData;
import org.onap.policy.api.main.rest.provider.PolicyProvider;
import org.onap.policy.api.main.rest.provider.PolicyTypeProvider;
import org.onap.policy.api.main.startstop.Main;
import org.onap.policy.common.endpoints.http.server.RestServer;
import org.onap.policy.common.endpoints.parameters.RestServerParameters;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.errors.concepts.ErrorResponse;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;
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

    private static final String POLICYTYPES = "policytypes";
    private static final String POLICYTYPES_TCA = "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app";
    private static final String POLICYTYPES_COLLECTOR =
            "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server";
    private static final String POLICYTYPES_TCA_VERSION =
            "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0";
    private static final String POLICYTYPES_TCA_LATEST =
            "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app/versions/latest";
    private static final String POLICYTYPES_COLLECTOR_VERSION =
            "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server/versions/1.0.0";
    private static final String POLICYTYPES_COLLECTOR_LATEST =
            "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server/versions/latest";

    private static final String POLICYTYPES_TCA_POLICIES =
            "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE =
            "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies/onap.restart.tca";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE_VERSION = "policytypes/"
        + "onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies/onap.restart.tca/versions/1.0.0";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE_LATEST = "policytypes/"
        + "onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies/onap.restart.tca/versions/latest";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE_DEPLOYED = "policytypes/"
            + "onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies/onap.restart.tca/versions/deployed";

    private static final String GUARD_POLICIES =
            "policytypes/onap.policies.controlloop.Guard/versions/1.0.0/policies";
    private static final String GUARD_POLICIES_VDNS_FL_LATEST =
            "policytypes/onap.policies.controlloop.Guard/versions/1.0.0/policies/guard.frequency.scaleout"
            + "/versions/latest";
    private static final String GUARD_POLICIES_VDNS_FL_DEPLOYED =
            "policytypes/onap.policies.controlloop.Guard/versions/1.0.0/policies/guard.frequency.scaleout"
            + "/versions/deployed";
    private static final String GUARD_POLICIES_VDNS_MINMAX_LATEST =
            "policytypes/onap.policies.controlloop.Guard/versions/1.0.0/policies/guard.minmax.scaleout"
            + "/versions/latest";
    private static final String GUARD_POLICIES_VDNS_FL_VERSION = "policytypes/"
        + "onap.policies.controlloop.Guard/versions/1.0.0/policies/guard.frequency.scaleout/versions/1";
    private static final String GUARD_POLICIES_VDNS_MINMAX_VERSION = "policytypes/"
        + "onap.policies.controlloop.Guard/versions/1.0.0/policies/guard.minmax.scaleout/versions/1";

    private static final String OPS_POLICIES =
            "policytypes/onap.policies.controlloop.Operational/versions/1.0.0/policies";
    private static final String OPS_POLICIES_VCPE_LATEST =
            "policytypes/onap.policies.controlloop.Operational/versions/1.0.0/policies/operational.restart"
            + "/versions/latest";
    private static final String OPS_POLICIES_VCPE_DEPLOYED =
            "policytypes/onap.policies.controlloop.Operational/versions/1.0.0/policies/operational.restart"
            + "/versions/deployed";
    private static final String OPS_POLICIES_VDNS_LATEST =
            "policytypes/onap.policies.controlloop.Operational/versions/1.0.0/policies/operational.scaleout"
            + "/versions/latest";
    private static final String OPS_POLICIES_VFIREWALL_LATEST =
            "policytypes/onap.policies.controlloop.Operational/versions/1.0.0/policies/operational.modifyconfig"
            + "/versions/latest";
    private static final String OPS_POLICIES_VCPE_VERSION = "policytypes/"
        + "onap.policies.controlloop.Operational/versions/1.0.0/policies/operational.restart/versions/1";
    private static final String OPS_POLICIES_VDNS_VERSION = "policytypes/"
        + "onap.policies.controlloop.Operational/versions/1.0.0/policies/operational.scaleout/versions/1";
    private static final String OPS_POLICIES_VFIREWALL_VERSION = "policytypes/"
        + "onap.policies.controlloop.Operational/versions/1.0.0/policies/operational.modifyconfig/versions/1";

    private static final String KEYSTORE = System.getProperty("user.dir") + "/src/test/resources/ssl/policy-keystore";
    private static final CommonTestData COMMON_TEST_DATA = new CommonTestData();
    private Main main;
    private RestServer restServer;
    private StandardCoder standardCoder = new StandardCoder();
    private int port;

    // @formatter:off
    private String[] toscaPolicyResourceNames = {
        "policies/vCPE.policy.monitoring.input.tosca.json",
        "policies/vDNS.policy.monitoring.input.tosca.json",
        "policies/vFirewall.policy.monitoring.input.tosca.json",
    };

    private String[] toscaPolicyTypeResourceNames = {
        "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.json",
        "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.json"
    };

    private String[] legacyGuardPolicyResourceNames = {
        "policies/vDNS.policy.guard.frequency.input.json",
        "policies/vDNS.policy.guard.minmax.input.json",
    };

    private String[] legacyOperationalPolicyResourceNames = {
        "policies/vCPE.policy.operational.input.json",
        "policies/vDNS.policy.operational.input.json",
        "policies/vFirewall.policy.operational.input.json"
    };
    private static PolicyModelsProviderParameters providerParams;
    private static ApiParameterGroup apiParamGroup;
    private static PolicyProvider policyProvider;
    private static PolicyTypeProvider policyTypeProvider;

    // @formatter:on

    /**
     * Initializes parameters.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void setupParameters() throws PfModelException {

        standardCoder = new StandardCoder();
        providerParams = new PolicyModelsProviderParameters();
        providerParams.setDatabaseDriver("org.h2.Driver");
        providerParams.setDatabaseUrl("jdbc:h2:mem:testdb");
        providerParams.setDatabaseUser("policy");
        providerParams.setDatabasePassword(Base64.getEncoder().encodeToString("P01icY".getBytes()));
        providerParams.setPersistenceUnit("ToscaConceptTest");
        apiParamGroup = new ApiParameterGroup("ApiGroup", null, providerParams);
        ParameterService.register(apiParamGroup, true);
        policyTypeProvider = new PolicyTypeProvider();
        policyProvider = new PolicyProvider();
    }

    /**
     * Method for cleanup after each test.
     */
    @After
    public void teardown() throws Exception {
        if (policyTypeProvider != null) {
            policyTypeProvider.close();
        }
        if (policyProvider != null) {
            policyProvider.close();
        }
        if (main != null) {
            stopApiService(main);
        } else if (restServer != null) {
            restServer.stop();
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

        port = NetworkUtil.allocPort();
        final RestServerParameters restServerParams = new CommonTestData().getRestServerParameters(false, port);
        restServerParams.setName(CommonTestData.API_GROUP_NAME);
        restServer = new RestServer(restServerParams, null, ApiRestController.class);
        try {
            restServer.start();
            final Invocation.Builder invocationBuilder = sendHttpRequest(HEALTHCHECK_ENDPOINT);
            final HealthCheckReport report = invocationBuilder.get(HealthCheckReport.class);
            validateHealthCheckReport(NAME, SELF, false, 500, NOT_ALIVE, report);
            assertTrue(restServer.isAlive());
            assertTrue(restServer.toString().startsWith("RestServer [servers="));
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
            validateStatisticsReport(report, 200);
            updateApiStatistics();
            invocationBuilder = sendHttpRequest(STATISTICS_ENDPOINT);
            report = invocationBuilder.get(StatisticsReport.class);
            validateStatisticsReport(report, 200);
            ApiStatisticsManager.resetAllStatistics();
        } catch (final Exception exp) {
            LOGGER.error("testApiStatistics_200 failed", exp);
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testApiStatistics_500() throws Exception {

        port = NetworkUtil.allocPort();
        final RestServerParameters restServerParams = new CommonTestData().getRestServerParameters(false, port);
        restServerParams.setName(CommonTestData.API_GROUP_NAME);
        restServer = new RestServer(restServerParams, null, ApiRestController.class);
        try {
            restServer.start();
            final Invocation.Builder invocationBuilder = sendHttpRequest(STATISTICS_ENDPOINT);
            final StatisticsReport report = invocationBuilder.get(StatisticsReport.class);
            validateStatisticsReport(report, 500);
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
            validateStatisticsReport(report, 200);
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

    @Test
    public void testCreatePolicyTypes() {

        assertThatCode(() -> {
            main = startApiService(true);
            for (String resrcName : toscaPolicyTypeResourceNames) {
                Response rawResponse = createResource(POLICYTYPES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
                ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
                assertNotNull(response);
                assertFalse(response.getPolicyTypes().get(0).isEmpty());
            }
        }).doesNotThrowAnyException();
    }

    @Test
    public void testReadPolicyTypes() {

        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = readResource(POLICYTYPES, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertTrue(response.getPolicyTypes().isEmpty());

            rawResponse = readResource(POLICYTYPES_TCA, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:null does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_TCA_VERSION, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_TCA_LATEST, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:null does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_COLLECTOR, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID "
                + "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server:null does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_COLLECTOR_VERSION, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID "
                + "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_COLLECTOR_LATEST, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID "
                + "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server:null does not exist",
                    error.getErrorMessage());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testReadPolicyTypesPersistent() throws Exception {

        setupParameters();
        main = startApiService(true);
        for (String resrcName : toscaPolicyTypeResourceNames) {
            Response rawResponse = createResource(POLICYTYPES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().get(0).isEmpty());
        }

        for (String resrcName : toscaPolicyResourceNames) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        Response rawResponse = readResource(POLICYTYPES_TCA, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(POLICYTYPES_TCA_LATEST, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(POLICYTYPES_COLLECTOR, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(POLICYTYPES_COLLECTOR_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(POLICYTYPES_COLLECTOR_LATEST, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

    }

    @Test
    public void testDeletePolicyType() {

        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = deleteResource(POLICYTYPES_TCA_VERSION, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = deleteResource(POLICYTYPES_COLLECTOR_VERSION, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID "
                + "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server:1.0.0 does not exist",
                    error.getErrorMessage());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeletePolicyTypePersistent() throws Exception {

        setupParameters(); //setup DB

        main = startApiService(true);// create policy types
        for (String resrcName : toscaPolicyTypeResourceNames) {
            Response rawResponse = createResource(POLICYTYPES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().get(0).isEmpty());
        }
        Response rawResponse = deleteResource(POLICYTYPES_TCA_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

    }

    @Test
    public void testCreatePolicies() {

        assertThatCode(() -> {
            main = startApiService(true);
            for (String resrcName : toscaPolicyResourceNames) {
                Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
                assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
                ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
                assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                        error.getErrorMessage());
            }
        }).doesNotThrowAnyException();
    }

    @Test
    public void testReadPolicies() {

        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy with ID null:null and "
                + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy with ID onap.restart.tca:null and "
                + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy with ID onap.restart.tca:1.0.0 and "
                + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_LATEST, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy with ID onap.restart.tca:null and "
                + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testReadPoliciesPersistent() throws Exception {

        setupParameters();
        main = startApiService(true);
        for (String resrcName : toscaPolicyTypeResourceNames) {
            Response rawResponse = createResource(POLICYTYPES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().get(0).isEmpty());
        }

        for (String resrcName : toscaPolicyResourceNames) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        Response rawResponse;
        rawResponse = readResource(POLICYTYPES_TCA, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_LATEST, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

    }

    @Test
    public void testDeletePolicies() {

        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy with ID onap.restart.tca:1.0.0 and "
                + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeletePolicyVersion() throws Exception {

        setupParameters(); //setup DB

        main = startApiService(true);// create policy types
        for (String resrcName : toscaPolicyTypeResourceNames) {
            Response rawResponse = createResource(POLICYTYPES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().get(0).isEmpty());
        }
        for (String resrcName : toscaPolicyResourceNames) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
        Response rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    public void testGetAllVersionOfPolicy() throws Exception {

        setupParameters(); //setup DB

        main = startApiService(true);// create policy types
        for (String resrcName : toscaPolicyTypeResourceNames) {
            Response rawResponse = createResource(POLICYTYPES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().get(0).isEmpty());
        }
        for (String resrcName : toscaPolicyResourceNames) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
        Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

    }

    @Test
    public void testCreateGuardPolicies() {

        assertThatCode(() -> {
            main = startApiService(true);
            for (String resrcName : legacyGuardPolicyResourceNames) {
                Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName, true);
                assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            }
        }).doesNotThrowAnyException();
    }

    @Test
    public void testCreateGuardPolicies1() throws Exception {

        setupParameters(); //setup DB

        main = startApiService(true);
        for (String resrcName : legacyGuardPolicyResourceNames) {
            Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

    }

    @Test
    public void testCreateOperationalPolicies() {

        assertThatCode(() -> {
            main = startApiService(true);
            for (String resrcName : legacyOperationalPolicyResourceNames) {
                Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName, true);
                assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            }
        }).doesNotThrowAnyException();
    }

    @Test
    public void testCreateOperationalPolicies1() throws Exception {

        setupParameters(); //setup DB

        main = startApiService(true);
        for (String resrcName : legacyOperationalPolicyResourceNames) {
            Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

    }

    @Test
    public void testReadGuardPolicies() {

        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = readResource(GUARD_POLICIES_VDNS_FL_LATEST, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: guard.frequency.scaleout:null",
                    error.getErrorMessage());

            rawResponse = readResource(GUARD_POLICIES_VDNS_FL_VERSION, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: guard.frequency.scaleout:1",
                    error.getErrorMessage());

            rawResponse = readResource(GUARD_POLICIES_VDNS_MINMAX_LATEST, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: guard.minmax.scaleout:null",
                    error.getErrorMessage());

            rawResponse = readResource(GUARD_POLICIES_VDNS_MINMAX_VERSION, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: guard.minmax.scaleout:1",
                    error.getErrorMessage());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testReadGuardPolicies1() throws Exception {

        setupParameters(); //setup DB

        main = startApiService(true);
        for (String resrcName : legacyGuardPolicyResourceNames) {
            Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        Response rawResponse = readResource(GUARD_POLICIES_VDNS_FL_LATEST, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(GUARD_POLICIES_VDNS_FL_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(GUARD_POLICIES_VDNS_MINMAX_LATEST, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(GUARD_POLICIES_VDNS_MINMAX_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

    }

    @Test
    public void testReadOperationalPolicies() {

        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = readResource(OPS_POLICIES_VCPE_LATEST, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.restart:null",
                    error.getErrorMessage());

            rawResponse = readResource(OPS_POLICIES_VCPE_VERSION, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.restart:1",
                    error.getErrorMessage());

            rawResponse = readResource(OPS_POLICIES_VDNS_LATEST, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.scaleout:null",
                    error.getErrorMessage());

            rawResponse = readResource(OPS_POLICIES_VDNS_VERSION, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.scaleout:1",
                    error.getErrorMessage());

            rawResponse = readResource(OPS_POLICIES_VFIREWALL_LATEST, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.modifyconfig:null",
                    error.getErrorMessage());

            rawResponse = readResource(OPS_POLICIES_VFIREWALL_VERSION, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.modifyconfig:1",
                    error.getErrorMessage());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testReadOperationalPolicies1() throws Exception {

        setupParameters(); //setup DB

        main = startApiService(true);
        for (String resrcName : legacyOperationalPolicyResourceNames) {
            Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        Response rawResponse = readResource(OPS_POLICIES_VCPE_LATEST, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(OPS_POLICIES_VCPE_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(OPS_POLICIES_VDNS_LATEST, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(OPS_POLICIES_VDNS_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(OPS_POLICIES_VFIREWALL_LATEST, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


        rawResponse = readResource(OPS_POLICIES_VFIREWALL_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


    }

    @Test
    public void testDeleteGuardPolicy() {

        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = deleteResource(GUARD_POLICIES_VDNS_FL_VERSION, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: guard.frequency.scaleout:1",
                    error.getErrorMessage());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeleteGuardPolicy1() throws Exception {

        setupParameters(); //setup DB
        main = startApiService(true);

        for (String resrcName : legacyGuardPolicyResourceNames) {
            Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        Response rawResponse = deleteResource(GUARD_POLICIES_VDNS_FL_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

    }

    @Test
    public void testGetDeployedVersionsOfGuardPolicy() {
        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = readResource(GUARD_POLICIES_VDNS_FL_DEPLOYED, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeleteOperationalPolicy() {

        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = deleteResource(OPS_POLICIES_VCPE_VERSION, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.restart:1",
                    error.getErrorMessage());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeleteOperationalPolicy1() throws Exception {

        setupParameters(); //setup DB

        main = startApiService(true);
        for (String resrcName : legacyOperationalPolicyResourceNames) {
            Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
        Response rawResponse = deleteResource(OPS_POLICIES_VCPE_VERSION, true);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

    }

    @Test
    public void testHttpsCreatePolicyTypes() {

        assertThatCode(() -> {
            main = startApiService(false);
            for (String resrcName : toscaPolicyTypeResourceNames) {
                Response rawResponse = createResource(POLICYTYPES, resrcName, false);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
                ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
                assertNotNull(response);
                assertFalse(response.getPolicyTypes().get(0).isEmpty());
            }
        }).doesNotThrowAnyException();
    }

    @Test
    public void testHttpsCreatePolicies() {

        assertThatCode(() -> {
            main = startApiService(false);
            for (String resrcName : toscaPolicyResourceNames) {
                Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, false);
                assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
                ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
                assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                        error.getErrorMessage());
            }
        }).doesNotThrowAnyException();
    }

    @Test
    public void testHttpsCreateGuardPolicies() {

        assertThatCode(() -> {
            main = startApiService(false);
            for (String resrcName : legacyGuardPolicyResourceNames) {
                Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName, false);
                assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            }
        }).doesNotThrowAnyException();
    }

    @Test
    public void testHttpsCreateOperationalPolicies() {

        assertThatCode(() -> {
            main = startApiService(false);
            for (String resrcName : legacyOperationalPolicyResourceNames) {
                Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName, false);
                assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            }
        }).doesNotThrowAnyException();
    }

    @Test
    public void testGetDeployedVersionsOfPolicy() {
        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_DEPLOYED, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testGetLatestVersionOfOperationalPolicy() {
        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = readResource(OPS_POLICIES_VDNS_LATEST, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testGetSpecificVersionOfOperationalPolicy() {
        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = readResource(OPS_POLICIES_VDNS_VERSION, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testGetDeployedVersionsOfOperationalPolicy() {
        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = readResource(OPS_POLICIES_VCPE_DEPLOYED, true);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        }).doesNotThrowAnyException();
    }

    @Test
    public void testDeleteSpecificVersionOfOperationalPolicy() {
        assertThatCode(() -> {
            main = startApiService(true);
            Response rawResponse = deleteResource(OPS_POLICIES_VDNS_VERSION, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
        }).doesNotThrowAnyException();
    }

    private Response createResource(String endpoint, String resourceName, boolean http) throws Exception {

        ToscaServiceTemplate rawServiceTemplate = standardCoder.decode(
                ResourceUtils.getResourceAsString(resourceName), ToscaServiceTemplate.class);
        final Invocation.Builder invocationBuilder;

        if (http) {
            invocationBuilder = sendHttpRequest(endpoint);
        } else {
            invocationBuilder = sendHttpsRequest(endpoint);
        }

        Entity<ToscaServiceTemplate> entity = Entity.entity(rawServiceTemplate, MediaType.APPLICATION_JSON);
        return invocationBuilder.post(entity);
    }

    private Response createGuardPolicy(String endpoint, String resourceName, boolean http) throws Exception {

        LegacyGuardPolicyInput rawGuardPolicy = standardCoder.decode(
                ResourceUtils.getResourceAsString(resourceName), LegacyGuardPolicyInput.class);
        final Invocation.Builder invocationBuilder;

        if (http) {
            invocationBuilder = sendHttpRequest(endpoint);
        } else {
            invocationBuilder = sendHttpsRequest(endpoint);
        }

        Entity<LegacyGuardPolicyInput> entity = Entity.entity(rawGuardPolicy, MediaType.APPLICATION_JSON);
        return invocationBuilder.post(entity);
    }

    private Response createOperationalPolicy(String endpoint, String resourceName, boolean http) throws Exception {

        LegacyOperationalPolicy rawOpsPolicy = standardCoder.decode(
                ResourceUtils.getResourceAsString(resourceName), LegacyOperationalPolicy.class);
        final Invocation.Builder invocationBuilder;

        if (http) {
            invocationBuilder = sendHttpRequest(endpoint);
        } else {
            invocationBuilder = sendHttpsRequest(endpoint);
        }

        Entity<LegacyOperationalPolicy> entity = Entity.entity(rawOpsPolicy, MediaType.APPLICATION_JSON);
        return invocationBuilder.post(entity);
    }

    private Response readResource(String endpoint, boolean http) throws Exception {

        final Invocation.Builder invocationBuilder;

        if (http) {
            invocationBuilder = sendHttpRequest(endpoint);
        } else {
            invocationBuilder = sendHttpsRequest(endpoint);
        }

        return invocationBuilder.get();

    }

    private Response deleteResource(String endpoint, boolean http) throws Exception {

        final Invocation.Builder invocationBuilder;

        if (http) {
            invocationBuilder = sendHttpRequest(endpoint);
        } else {
            invocationBuilder = sendHttpsRequest(endpoint);
        }

        return invocationBuilder.delete();
    }

    private Main startApiService(final boolean http) throws Exception {
        port = NetworkUtil.allocPort();

        final String[] apiConfigParameters = new String[2];
        if (http) {
            COMMON_TEST_DATA.makeParameters("src/test/resources/parameters/ApiConfigParameters.json",
                            "src/test/resources/parameters/ApiConfigParametersXXX.json", port);
            apiConfigParameters[0] = "-c";
            apiConfigParameters[1] = "src/test/resources/parameters/ApiConfigParametersXXX.json";
        } else {
            final Properties systemProps = System.getProperties();
            systemProps.put("javax.net.ssl.keyStore", KEYSTORE);
            systemProps.put("javax.net.ssl.keyStorePassword", "Pol1cy_0nap");
            System.setProperties(systemProps);
            COMMON_TEST_DATA.makeParameters("src/test/resources/parameters/ApiConfigParameters_Https.json",
                            "src/test/resources/parameters/ApiConfigParameters_HttpsXXX.json", port);
            apiConfigParameters[0] = "-c";
            apiConfigParameters[1] = "src/test/resources/parameters/ApiConfigParameters_HttpsXXX.json";
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

        client.property(ClientProperties.METAINF_SERVICES_LOOKUP_DISABLE, "true");
        client.register(GsonMessageBodyHandler.class);

        final WebTarget webTarget = client.target("http://localhost:" + port + "/policy/api/v1/" + endpoint);

        final Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        if (!NetworkUtil.isTcpPortOpen("localhost", port, 60, 1000L)) {
            throw new IllegalStateException("cannot connect to port " + port);
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

        client.property(ClientProperties.METAINF_SERVICES_LOOKUP_DISABLE, "true");
        client.register(GsonMessageBodyHandler.class);

        final WebTarget webTarget = client.target("https://localhost:" + port + "/policy/api/v1/" + endpoint);

        final Invocation.Builder invocationBuilder = webTarget.request(MediaType.APPLICATION_JSON);

        if (!NetworkUtil.isTcpPortOpen("localhost", port, 60, 1000L)) {
            throw new IllegalStateException("cannot connect to port " + port);
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

    private void validateStatisticsReport(final StatisticsReport report, final int code) {

        assertEquals(code, report.getCode());
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
