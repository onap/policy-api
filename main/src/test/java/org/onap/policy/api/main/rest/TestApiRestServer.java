/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2019 Nordix Foundation.
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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collections;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
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
import org.onap.policy.common.endpoints.http.server.YamlMessageBodyHandler;
import org.onap.policy.common.endpoints.parameters.RestServerParameters;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
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
    private static final String APP_JSON = "application/json";
    private static final String APP_YAML = "application/yaml";

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

    private static final String GUARD_POLICIES = "policytypes/onap.policies.controlloop.Guard/versions/1.0.0/policies";
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
    private static final String GUARD_POLICIES_VDNS_MINMAX_VERSION =
            "policytypes/" + "onap.policies.controlloop.Guard/versions/1.0.0/policies/guard.minmax.scaleout/versions/1";

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
    private static final String POLICIES = "policies";

    private static final String KEYSTORE = System.getProperty("user.dir") + "/src/test/resources/ssl/policy-keystore";
    private static final CommonTestData COMMON_TEST_DATA = new CommonTestData();
    private Main main;
    private RestServer restServer;
    private StandardCoder standardCoder = new StandardCoder();
    private StandardYamlCoder standardYamlCoder = new StandardYamlCoder();
    private int port;

    // @formatter:off
    private String[] toscaPolicyResourceNames = {
        "policies/vCPE.policy.monitoring.input.tosca.json",
        "policies/vCPE.policy.monitoring.input.tosca.yaml",
        "policies/vDNS.policy.monitoring.input.tosca.json",
        "policies/vDNS.policy.monitoring.input.tosca.yaml",
        "policies/vFirewall.policy.monitoring.input.tosca.json",
        "policies/vFirewall.policy.monitoring.input.tosca.yaml"
    };

    private String[] toscaPoliciesResourceNames = {
        "policies/vCPE.policies.optimization.input.tosca.json",
        "policies/vCPE.policies.optimization.input.tosca.yaml"
    };

    private String[] toscaPolicyTypeResourceNames = {
        "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml",
        "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.yaml",
        "policytypes/onap.policies.Optimization.yaml",
        "policytypes/onap.policies.controlloop.Operational.yaml",
        "policytypes/onap.policies.controlloop.guard.Blacklist.yaml",
        "policytypes/onap.policies.controlloop.guard.FrequencyLimiter.yaml",
        "policytypes/onap.policies.controlloop.guard.MinMax.yaml",
        "policytypes/onap.policies.controlloop.guard.coordination.FirstBlocksSecond.yaml",
        "policytypes/onap.policies.optimization.AffinityPolicy.yaml",
        "policytypes/onap.policies.optimization.DistancePolicy.yaml",
        "policytypes/onap.policies.optimization.HpaPolicy.yaml",
        "policytypes/onap.policies.optimization.OptimizationPolicy.yaml",
        "policytypes/onap.policies.optimization.PciPolicy.yaml",
        "policytypes/onap.policies.optimization.QueryPolicy.yaml",
        "policytypes/onap.policies.optimization.SubscriberPolicy.yaml",
        "policytypes/onap.policies.optimization.Vim_fit.yaml",
        "policytypes/onap.policies.optimization.VnfPolicy.yaml"
    };

    private String[] legacyGuardPolicyResourceNames = {
        "policies/vDNS.policy.guard.frequency.input.json",
        "policies/vDNS.policy.guard.minmax.input.json"
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
        apiParamGroup = new ApiParameterGroup("ApiGroup", null, providerParams, Collections.emptyList());
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
        try {
            main = startApiService(true);
            for (String resrcName : toscaPolicyTypeResourceNames) {
                Response rawResponse = createResource(POLICYTYPES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
                ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
                assertNotNull(response);
                assertFalse(response.getPolicyTypes().isEmpty());
            }
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testCreatePolicies() {
        try {
            main = startApiService(true);
            for (String resrcName : toscaPolicyResourceNames) {
                Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
                assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
                ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
                assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                        error.getErrorMessage());
            }
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testCreatePoliciesPersistent() throws Exception {
        setupParameters(); // setup DB
        main = startApiService(true);
        for (String resrcName : toscaPolicyResourceNames) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getToscaTopologyTemplate().getPolicies().isEmpty());
        }
    }

    @Test
    public void testSimpleCreatePolicies() throws Exception {
        main = startApiService(true);
        for (String resrcName : toscaPoliciesResourceNames) {
            Response rawResponse = createResource(POLICIES, resrcName, true);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type onap.policies.optimization.AffinityPolicy:0.0.0 for "
                + "policy OSDF_CASABLANCA.Affinity_vCPE_1:1.0.0 does not exist", error.getErrorMessage());
        }
    }

    @Test
    public void testSimpleCreatePoliciesPersistent() throws Exception {
        setupParameters(); // setup DB
        main = startApiService(true);
        for (String resrcName : toscaPoliciesResourceNames) {
            Response rawResponse = createResource(POLICIES, resrcName, true);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getToscaTopologyTemplate().getPolicies().isEmpty());
        }
    }

    @Test
    public void testCreateGuardPolicies() {
        try {
            main = startApiService(true);
            for (String resrcName : legacyGuardPolicyResourceNames) {
                Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName, true);
                assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            }
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testCreateGuardPoliciesPersistent() {
        try {
            setupParameters(); // setup DB
            main = startApiService(true);
            for (String resrcName : legacyGuardPolicyResourceNames) {
                Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            }
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testCreateOperationalPolicies() {
        try {
            main = startApiService(true);
            for (String resrcName : legacyOperationalPolicyResourceNames) {
                Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName, true);
                assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            }
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testCreateOperationalPoliciesPersistent() {
        try {
            setupParameters(); // setup DB
            main = startApiService(true);
            for (String resrcName : legacyOperationalPolicyResourceNames) {
                Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            }
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHttpsCreatePolicyTypes() {
        try {
            main = startApiService(false);
            for (String resrcName : toscaPolicyTypeResourceNames) {
                Response rawResponse = createResource(POLICYTYPES, resrcName, false);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
                ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
                assertNotNull(response);
                assertFalse(response.getPolicyTypes().isEmpty());
            }
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHttpsCreatePolicies() {
        try {
            main = startApiService(false);
            for (String resrcName : toscaPolicyResourceNames) {
                Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, false);
                assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
                ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
                assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                        error.getErrorMessage());
            }
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHttpsCreatePoliciesPersistent() throws Exception {
        setupParameters(); // setup DB
        main = startApiService(false);
        for (String resrcName : toscaPolicyResourceNames) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, false);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getToscaTopologyTemplate().getPolicies().isEmpty());
        }
    }

    @Test
    public void testHttpsSimpleCreatePolicies() throws Exception {
        main = startApiService(false);
        for (String resrcName : toscaPoliciesResourceNames) {
            Response rawResponse = createResource(POLICIES, resrcName, false);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type onap.policies.optimization.AffinityPolicy:0.0.0 for "
                + "policy OSDF_CASABLANCA.Affinity_vCPE_1:1.0.0 does not exist", error.getErrorMessage());
        }
    }

    @Test
    public void testHttpsSimpleCreatePoliciesPersistent() throws Exception {
        setupParameters(); // setup DB
        main = startApiService(false);
        for (String resrcName : toscaPoliciesResourceNames) {
            Response rawResponse = createResource(POLICIES, resrcName, false);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getToscaTopologyTemplate().getPolicies().isEmpty());
        }
    }

    @Test
    public void testHttpsCreateGuardPolicies() {
        try {
            main = startApiService(false);
            for (String resrcName : legacyGuardPolicyResourceNames) {
                Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName, false);
                assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            }
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHttpsCreateOperationalPolicies() {
        try {
            main = startApiService(false);
            for (String resrcName : legacyOperationalPolicyResourceNames) {
                Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName, false);
                assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            }
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHealthCheckSuccessJson() {
        testHealthCheckSuccess(APP_JSON);
    }

    @Test
    public void testHealthCheckSuccessYaml() {
        testHealthCheckSuccess(APP_YAML);
    }



    private void testHealthCheckSuccess(String mediaType) {

        try {
            main = startApiService(true);
            final Invocation.Builder invocationBuilder = sendHttpRequest(HEALTHCHECK_ENDPOINT, mediaType);
            final HealthCheckReport report = invocationBuilder.get(HealthCheckReport.class);
            validateHealthCheckReport(NAME, SELF, true, 200, ALIVE, report);
        } catch (final Exception exp) {
            LOGGER.error("testHealthCheckSuccess failed", exp);
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHealthCheckFailureJson() {
        testHealthCheckFailure(APP_JSON);
    }

    @Test
    public void testHealthCheckFailureYaml() {
        testHealthCheckFailure(APP_YAML);
    }

    private void testHealthCheckFailure(String mediaType) {
        try {
            port = NetworkUtil.allocPort();
            final RestServerParameters restServerParams = new CommonTestData().getRestServerParameters(false, port);
            restServerParams.setName(CommonTestData.API_GROUP_NAME);
            restServer = new RestServer(restServerParams, null, ApiRestController.class);

            restServer.start();
            final Invocation.Builder invocationBuilder = sendHttpRequest(HEALTHCHECK_ENDPOINT, mediaType);
            final HealthCheckReport report = invocationBuilder.get(HealthCheckReport.class);
            validateHealthCheckReport(NAME, SELF, false, 500, NOT_ALIVE, report);
            assertTrue(restServer.isAlive());
            assertTrue(restServer.toString().startsWith("RestServer [servers="));
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHttpsHealthCheckSuccessJson() {
        testHttpsHealthCheckSuccess(APP_JSON);
    }

    @Test
    public void testHttpsHealthCheckSuccessYaml() {
        testHttpsHealthCheckSuccess(APP_YAML);
    }

    private void testHttpsHealthCheckSuccess(String mediaType) {
        try {
            main = startApiService(false);
            final Invocation.Builder invocationBuilder = sendHttpsRequest(HEALTHCHECK_ENDPOINT, mediaType);
            final HealthCheckReport report = invocationBuilder.get(HealthCheckReport.class);
            validateHealthCheckReport(NAME, SELF, true, 200, ALIVE, report);
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testApiStatistics_200_Json() {
        testApiStatistics_200(APP_JSON);
    }

    @Test
    public void testApiStatistics_200_Yaml() {
        testApiStatistics_200(APP_YAML);
    }

    private void testApiStatistics_200(String mediaType) {
        try {
            main = startApiService(true);
            Invocation.Builder invocationBuilder = sendHttpRequest(STATISTICS_ENDPOINT, mediaType);
            StatisticsReport report = invocationBuilder.get(StatisticsReport.class);
            validateStatisticsReport(report, 200);
            updateApiStatistics();
            invocationBuilder = sendHttpRequest(STATISTICS_ENDPOINT, mediaType);
            report = invocationBuilder.get(StatisticsReport.class);
            validateStatisticsReport(report, 200);
            ApiStatisticsManager.resetAllStatistics();
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testHttpsApiStatisticsJson() {
        testHttpsApiStatistics(APP_JSON);
    }

    @Test
    public void testHttpsApiStatisticsYaml() {
        testHttpsApiStatistics(APP_YAML);
    }

    private void testHttpsApiStatistics(String mediaType) {
        try {
            main = startApiService(false);
            final Invocation.Builder invocationBuilder = sendHttpsRequest(STATISTICS_ENDPOINT, mediaType);
            final StatisticsReport report = invocationBuilder.get(StatisticsReport.class);
            validateStatisticsReport(report, 200);
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testApiStatistics_500_Json() {
        testApiStatistics_500(APP_JSON);
    }

    @Test
    public void testApiStatistics_500_Yaml() {
        testApiStatistics_500(APP_YAML);
    }


    private void testApiStatistics_500(String mediaType) {
        try {
            port = NetworkUtil.allocPort();
            final RestServerParameters restServerParams = new CommonTestData().getRestServerParameters(false, port);
            restServerParams.setName(CommonTestData.API_GROUP_NAME);
            restServer = new RestServer(restServerParams, null, ApiRestController.class);

            restServer.start();
            final Invocation.Builder invocationBuilder = sendHttpRequest(STATISTICS_ENDPOINT, mediaType);
            final StatisticsReport report = invocationBuilder.get(StatisticsReport.class);
            validateStatisticsReport(report, 500);
            ApiStatisticsManager.resetAllStatistics();
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }


    @Test
    public void testReadPolicyTypesJson() {
        testReadPolicyTypes(APP_JSON);
    }

    @Test
    public void testReadPolicyTypesYaml() {
        testReadPolicyTypes(APP_YAML);
    }

    private void testReadPolicyTypes(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = readResource(POLICYTYPES, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertTrue(response.getPolicyTypes().isEmpty());

            rawResponse = readResource(POLICYTYPES_TCA, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:null does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_TCA_VERSION, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_TCA_LATEST, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:null does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_COLLECTOR, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID "
                    + "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server:null does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_COLLECTOR_VERSION, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID "
                    + "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_COLLECTOR_LATEST, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID "
                    + "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server:null does not exist",
                    error.getErrorMessage());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testReadPreloadPolicyTypesPersistentJson() throws Exception {
        setupParameters();
        main = startApiService(true);
        Response rawResponse = readResource(POLICYTYPES, true, APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(21, response.getPolicyTypes().size());
    }

    @Test
    public void testReadPreloadPolicyTypesPersistentYaml() throws Exception {
        setupParameters();
        main = startApiService(true);
        Response rawResponse = readResource(POLICYTYPES, true, APP_YAML);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(21, response.getPolicyTypes().size());
    }

    @Test
    public void testReadPolicyTypesPersistentJson() {
        testReadPolicyTypesPersistent(APP_JSON);
    }

    @Test
    public void testReadPolicyTypesPersistentYaml() {
        testReadPolicyTypesPersistent(APP_YAML);
    }

    private void testReadPolicyTypesPersistent(String mediaType) {
        try {
            setupParameters();
            main = startApiService(true);
            for (String resrcName : toscaPolicyTypeResourceNames) {
                Response rawResponse = createResource(POLICYTYPES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
                ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
                assertNotNull(response);
                assertFalse(response.getPolicyTypes().isEmpty());
            }

            for (String resrcName : toscaPolicyResourceNames) {
                Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            }

            Response rawResponse = readResource(POLICYTYPES_TCA, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

            rawResponse = readResource(POLICYTYPES_TCA_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(POLICYTYPES_TCA_LATEST, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(POLICYTYPES_COLLECTOR, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(POLICYTYPES_COLLECTOR_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(POLICYTYPES_COLLECTOR_LATEST, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testDeletePolicyTypeJson() {
        testDeletePolicyType(APP_JSON);
    }

    @Test
    public void testDeletePolicyTypeYaml() {
        testDeletePolicyType(APP_YAML);
    }

    private void testDeletePolicyType(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = deleteResource(POLICYTYPES_TCA_VERSION, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = deleteResource(POLICYTYPES_COLLECTOR_VERSION, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("policy type with ID "
                    + "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server:1.0.0 does not exist",
                    error.getErrorMessage());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testDeletePolicyTypePersistentJson() {
        testDeletePolicyTypePersistent(APP_JSON);
    }

    @Test
    public void testDeletePolicyTypePersistentYaml() {
        testDeletePolicyTypePersistent(APP_YAML);
    }

    private void testDeletePolicyTypePersistent(String mediaType) {
        try {
            setupParameters(); // setup DB
            main = startApiService(true);// create policy types
            for (String resrcName : toscaPolicyTypeResourceNames) {
                Response rawResponse = createResource(POLICYTYPES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
                ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
                assertNotNull(response);
                assertFalse(response.getPolicyTypes().isEmpty());
            }
            Response rawResponse = deleteResource(POLICYTYPES_TCA_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }


    @Test
    public void testReadPoliciesJson() {
        testReadPolicies(APP_JSON);
    }

    @Test
    public void testReadPoliciesYaml() {
        testReadPolicies(APP_YAML);
    }

    private void testReadPolicies(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals(
                    "policy with ID null:null and "
                            + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals(
                    "policy with ID onap.restart.tca:null and "
                            + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals(
                    "policy with ID onap.restart.tca:1.0.0 and "
                            + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());

            rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_LATEST, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals(
                    "policy with ID onap.restart.tca:null and "
                            + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testReadPoliciesPersistentJson() {
        testReadPoliciesPersistent(APP_JSON);
    }

    @Test
    public void testReadPoliciesPersistentYaml() {
        testReadPoliciesPersistent(APP_YAML);
    }

    private void testReadPoliciesPersistent(String mediaType) {
        try {
            setupParameters();
            main = startApiService(true);
            for (String resrcName : toscaPolicyTypeResourceNames) {
                Response rawResponse = createResource(POLICYTYPES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
                ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
                assertNotNull(response);
                assertFalse(response.getPolicyTypes().isEmpty());
            }

            for (String resrcName : toscaPolicyResourceNames) {
                Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            }

            Response rawResponse;
            rawResponse = readResource(POLICYTYPES_TCA, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

            rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

            rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

            rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_LATEST, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testDeletePoliciesJson() {
        testDeletePolicies(APP_JSON);
    }

    @Test
    public void testDeletePoliciesYaml() {
        testDeletePolicies(APP_YAML);
    }

    private void testDeletePolicies(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals(
                    "policy with ID onap.restart.tca:1.0.0 and "
                            + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                    error.getErrorMessage());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testDeletePolicyVersionJson() {
        testDeletePolicyVersion(APP_JSON);
    }

    @Test
    public void testDeletePolicyVersionYaml() {
        testDeletePolicyVersion(APP_YAML);
    }

    private void testDeletePolicyVersion(String mediaType) {
        try {
            setupParameters(); // setup DB
            main = startApiService(true);// create policy types
            for (String resrcName : toscaPolicyTypeResourceNames) {
                Response rawResponse = createResource(POLICYTYPES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
                ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
                assertNotNull(response);
                assertFalse(response.getPolicyTypes().isEmpty());
            }
            for (String resrcName : toscaPolicyResourceNames) {
                Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            }
            Response rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testGetAllVersionOfPolicyJson() {
        testGetAllVersionOfPolicy(APP_JSON);
    }

    @Test
    public void testGetAllVersionOfPolicyYaml() {
        testGetAllVersionOfPolicy(APP_YAML);
    }

    private void testGetAllVersionOfPolicy(String mediaType) {
        try {
            setupParameters(); // setup DB
            main = startApiService(true);// create policy types
            for (String resrcName : toscaPolicyTypeResourceNames) {
                Response rawResponse = createResource(POLICYTYPES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
                ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
                assertNotNull(response);
                assertFalse(response.getPolicyTypes().isEmpty());
            }
            for (String resrcName : toscaPolicyResourceNames) {
                Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            }
            Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }


    @Test
    public void testReadGuardPoliciesJson() {
        testReadGuardPolicies(APP_JSON);
    }

    @Test
    public void testReadGuardPoliciesYaml() {
        testReadGuardPolicies(APP_YAML);
    }

    private void testReadGuardPolicies(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = readResource(GUARD_POLICIES_VDNS_FL_LATEST, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: guard.frequency.scaleout:null", error.getErrorMessage());

            rawResponse = readResource(GUARD_POLICIES_VDNS_FL_VERSION, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: guard.frequency.scaleout:1", error.getErrorMessage());

            rawResponse = readResource(GUARD_POLICIES_VDNS_MINMAX_LATEST, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: guard.minmax.scaleout:null", error.getErrorMessage());

            rawResponse = readResource(GUARD_POLICIES_VDNS_MINMAX_VERSION, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: guard.minmax.scaleout:1", error.getErrorMessage());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testReadGuardPoliciesPersistentJson() {
        testReadGuardPoliciesPersistent(APP_JSON);
    }

    @Test
    public void testReadGuardPoliciesPersistentYaml() {
        testReadGuardPoliciesPersistent(APP_YAML);
    }

    private void testReadGuardPoliciesPersistent(String mediaType) {
        try {
            setupParameters(); // setup DB
            main = startApiService(true);
            for (String resrcName : legacyGuardPolicyResourceNames) {
                Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            }

            Response rawResponse = readResource(GUARD_POLICIES_VDNS_FL_LATEST, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(GUARD_POLICIES_VDNS_FL_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

            rawResponse = readResource(GUARD_POLICIES_VDNS_MINMAX_LATEST, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(GUARD_POLICIES_VDNS_MINMAX_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }


    @Test
    public void testReadOperationalPoliciesJson() {
        testReadOperationalPolicies(APP_JSON);
    }

    @Test
    public void testReadOperationalPoliciesYaml() {
        testReadOperationalPolicies(APP_YAML);
    }

    private void testReadOperationalPolicies(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = readResource(OPS_POLICIES_VCPE_LATEST, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.restart:null", error.getErrorMessage());

            rawResponse = readResource(OPS_POLICIES_VCPE_VERSION, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.restart:1", error.getErrorMessage());

            rawResponse = readResource(OPS_POLICIES_VDNS_LATEST, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.scaleout:null", error.getErrorMessage());

            rawResponse = readResource(OPS_POLICIES_VDNS_VERSION, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.scaleout:1", error.getErrorMessage());

            rawResponse = readResource(OPS_POLICIES_VFIREWALL_LATEST, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.modifyconfig:null", error.getErrorMessage());

            rawResponse = readResource(OPS_POLICIES_VFIREWALL_VERSION, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.modifyconfig:1", error.getErrorMessage());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testReadOperationalPoliciesPersistentJson() {
        testReadOperationalPoliciesPersistent(APP_JSON);
    }

    @Test
    public void testReadOperationalPoliciesPersistentYaml() {
        testReadOperationalPoliciesPersistent(APP_YAML);
    }

    private void testReadOperationalPoliciesPersistent(String mediaType) {
        try {
            setupParameters(); // setup DB
            main = startApiService(true);
            for (String resrcName : legacyOperationalPolicyResourceNames) {
                Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            }

            Response rawResponse = readResource(OPS_POLICIES_VCPE_LATEST, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(OPS_POLICIES_VCPE_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(OPS_POLICIES_VDNS_LATEST, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(OPS_POLICIES_VDNS_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(OPS_POLICIES_VFIREWALL_LATEST, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());


            rawResponse = readResource(OPS_POLICIES_VFIREWALL_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testDeleteGuardPolicyJson() {
        testDeleteGuardPolicy(APP_JSON);
    }

    @Test
    public void testDeleteGuardPolicyYaml() {
        testDeleteGuardPolicy(APP_YAML);
    }

    private void testDeleteGuardPolicy(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = deleteResource(GUARD_POLICIES_VDNS_FL_VERSION, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: guard.frequency.scaleout:1", error.getErrorMessage());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testDeleteGuardPolicyPersistentJson() {
        testDeleteGuardPolicyPersistent(APP_JSON);
    }

    @Test
    public void testDeleteGuardPolicyPersistentYaml() {
        testDeleteGuardPolicyPersistent(APP_YAML);
    }

    private void testDeleteGuardPolicyPersistent(String mediaType) {
        try {
            setupParameters(); // setup DB
            main = startApiService(true);

            for (String resrcName : legacyGuardPolicyResourceNames) {
                Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            }

            Response rawResponse = deleteResource(GUARD_POLICIES_VDNS_FL_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testGetDeployedVersionsOfGuardPolicyJson() {
        testGetDeployedVersionsOfGuardPolicy(APP_JSON);
    }

    @Test
    public void testGetDeployedVersionsOfGuardPolicyYaml() {
        testGetDeployedVersionsOfGuardPolicy(APP_YAML);
    }

    private void testGetDeployedVersionsOfGuardPolicy(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = readResource(GUARD_POLICIES_VDNS_FL_DEPLOYED, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testDeleteOperationalPolicyJson() {
        testDeleteOperationalPolicy(APP_JSON);
    }

    @Test
    public void testDeleteOperationalPolicyYaml() {
        testDeleteOperationalPolicy(APP_YAML);
    }

    private void testDeleteOperationalPolicy(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = deleteResource(OPS_POLICIES_VCPE_VERSION, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
            ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
            assertEquals("no policy found for policy: operational.restart:1", error.getErrorMessage());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testDeleteOperationalPolicyPersistentJson() {
        testDeleteOperationalPolicyPersistent(APP_JSON);
    }

    @Test
    public void testDeleteOperationalPolicyPersistentYaml() {
        testDeleteOperationalPolicyPersistent(APP_YAML);
    }

    private void testDeleteOperationalPolicyPersistent(String mediaType) {
        try {
            setupParameters(); // setup DB
            main = startApiService(true);
            for (String resrcName : legacyOperationalPolicyResourceNames) {
                Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName, true);
                assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            }
            Response rawResponse = deleteResource(OPS_POLICIES_VCPE_VERSION, true, mediaType);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }


    @Test
    public void testGetDeployedVersionsOfPolicyJson() {
        testGetDeployedVersionsOfPolicy(APP_JSON);
    }

    @Test
    public void testGetDeployedVersionsOfPolicyYaml() {
        testGetDeployedVersionsOfPolicy(APP_YAML);
    }

    private void testGetDeployedVersionsOfPolicy(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_DEPLOYED, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testGetLatestVersionOfOperationalPolicyJson() {
        testGetLatestVersionOfOperationalPolicy(APP_JSON);
    }

    @Test
    public void testGetLatestVersionOfOperationalPolicyYaml() {
        testGetLatestVersionOfOperationalPolicy(APP_YAML);
    }

    private void testGetLatestVersionOfOperationalPolicy(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = readResource(OPS_POLICIES_VDNS_LATEST, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testGetSpecificVersionOfOperationalPolicyJson() {
        testGetSpecificVersionOfOperationalPolicy(APP_JSON);
    }

    @Test
    public void testGetSpecificVersionOfOperationalPolicyYaml() {
        testGetSpecificVersionOfOperationalPolicy(APP_YAML);
    }

    private void testGetSpecificVersionOfOperationalPolicy(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = readResource(OPS_POLICIES_VDNS_VERSION, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testGetDeployedVersionsOfOperationalPolicyJson() {
        testGetDeployedVersionsOfOperationalPolicy(APP_JSON);
    }

    @Test
    public void testGetDeployedVersionsOfOperationalPolicyYaml() {
        testGetDeployedVersionsOfOperationalPolicy(APP_YAML);
    }

    private void testGetDeployedVersionsOfOperationalPolicy(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = readResource(OPS_POLICIES_VCPE_DEPLOYED, true, mediaType);
            assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    @Test
    public void testDeleteSpecificVersionOfOperationalPolicyJson() {
        testDeleteSpecificVersionOfOperationalPolicy(APP_JSON);
    }

    @Test
    public void testDeleteSpecificVersionOfOperationalPolicyYaml() {
        testDeleteSpecificVersionOfOperationalPolicy(APP_YAML);
    }

    private void testDeleteSpecificVersionOfOperationalPolicy(String mediaType) {
        try {
            main = startApiService(true);
            Response rawResponse = deleteResource(OPS_POLICIES_VDNS_VERSION, true, mediaType);
            assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
        } catch (Exception exp) {
            fail("Test should not throw an exception");
        }
    }

    private Response createResource(String endpoint, String resourceName, boolean http)
            throws Exception {

        String mediaType = APP_JSON; // default media type
        ToscaServiceTemplate rawServiceTemplate = new ToscaServiceTemplate();
        if (resourceName.endsWith(".json")) {
            rawServiceTemplate = standardCoder.decode(
                    ResourceUtils.getResourceAsString(resourceName), ToscaServiceTemplate.class);
        } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
            mediaType = APP_YAML;
            rawServiceTemplate = standardYamlCoder.decode(
                    ResourceUtils.getResourceAsString(resourceName), ToscaServiceTemplate.class);
        }

        final Invocation.Builder invocationBuilder;

        if (http) {
            invocationBuilder = sendHttpRequest(endpoint, mediaType);
        } else {
            invocationBuilder = sendHttpsRequest(endpoint, mediaType);
        }

        Entity<ToscaServiceTemplate> entity = Entity.entity(rawServiceTemplate, mediaType);
        return invocationBuilder.post(entity);
    }

    private Response createGuardPolicy(String endpoint, String resourceName, boolean http)
            throws Exception {

        String mediaType = APP_JSON; // default media type
        LegacyGuardPolicyInput rawGuardPolicy = new LegacyGuardPolicyInput();
        if (resourceName.endsWith(".json")) {
            rawGuardPolicy = standardCoder.decode(
                    ResourceUtils.getResourceAsString(resourceName), LegacyGuardPolicyInput.class);
        } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
            mediaType = APP_YAML;
            rawGuardPolicy = standardYamlCoder.decode(
                    ResourceUtils.getResourceAsString(resourceName), LegacyGuardPolicyInput.class);
        }

        final Invocation.Builder invocationBuilder;

        if (http) {
            invocationBuilder = sendHttpRequest(endpoint, mediaType);
        } else {
            invocationBuilder = sendHttpsRequest(endpoint, mediaType);
        }

        Entity<LegacyGuardPolicyInput> entity = Entity.entity(rawGuardPolicy, mediaType);
        return invocationBuilder.post(entity);
    }

    private Response createOperationalPolicy(String endpoint, String resourceName, boolean http)
            throws Exception {

        String mediaType = APP_JSON; // default media type
        LegacyOperationalPolicy rawOpsPolicy = new LegacyOperationalPolicy();
        if (resourceName.endsWith(".json")) {
            rawOpsPolicy = standardCoder.decode(
                    ResourceUtils.getResourceAsString(resourceName), LegacyOperationalPolicy.class);
        } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
            mediaType = APP_YAML;
            rawOpsPolicy = standardYamlCoder.decode(
                    ResourceUtils.getResourceAsString(resourceName), LegacyOperationalPolicy.class);
        }

        final Invocation.Builder invocationBuilder;

        if (http) {
            invocationBuilder = sendHttpRequest(endpoint, mediaType);
        } else {
            invocationBuilder = sendHttpsRequest(endpoint, mediaType);
        }

        Entity<LegacyOperationalPolicy> entity = Entity.entity(rawOpsPolicy, mediaType);
        return invocationBuilder.post(entity);
    }

    private Response readResource(String endpoint, boolean http, String mediaType) throws Exception {

        final Invocation.Builder invocationBuilder;

        if (http) {
            invocationBuilder = sendHttpRequest(endpoint, mediaType);
        } else {
            invocationBuilder = sendHttpsRequest(endpoint, mediaType);
        }

        return invocationBuilder.get();

    }

    private Response deleteResource(String endpoint, boolean http, String mediaType) throws Exception {

        final Invocation.Builder invocationBuilder;

        if (http) {
            invocationBuilder = sendHttpRequest(endpoint, mediaType);
        } else {
            invocationBuilder = sendHttpsRequest(endpoint, mediaType);
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

    private Invocation.Builder sendHttpRequest(final String endpoint, String mediaType) throws Exception {

        final ClientConfig clientConfig = new ClientConfig();

        final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("healthcheck", "zb!XztG34");
        clientConfig.register(feature);

        final Client client = ClientBuilder.newClient(clientConfig);

        client.property(ClientProperties.METAINF_SERVICES_LOOKUP_DISABLE, "true");
        if (APP_JSON.equalsIgnoreCase(mediaType)) {
            client.register(GsonMessageBodyHandler.class);
        } else if (APP_YAML.equalsIgnoreCase(mediaType)) {
            client.register(YamlMessageBodyHandler.class);
        }

        final WebTarget webTarget = client.target("http://localhost:" + port + "/policy/api/v1/" + endpoint);

        final Invocation.Builder invocationBuilder = webTarget.request(mediaType);

        if (!NetworkUtil.isTcpPortOpen("localhost", port, 60, 1000L)) {
            throw new IllegalStateException("cannot connect to port " + port);
        }
        return invocationBuilder;
    }

    private Invocation.Builder sendHttpsRequest(final String endpoint, String mediaType) throws Exception {

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
        final ClientBuilder clientBuilder =
                ClientBuilder.newBuilder().sslContext(sc).hostnameVerifier((host, session) -> true);
        final Client client = clientBuilder.build();
        final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("healthcheck", "zb!XztG34");
        client.register(feature);

        client.property(ClientProperties.METAINF_SERVICES_LOOKUP_DISABLE, "true");
        if (APP_JSON.equalsIgnoreCase(mediaType)) {
            client.register(GsonMessageBodyHandler.class);
        } else if (APP_YAML.equalsIgnoreCase(mediaType)) {
            client.register(YamlMessageBodyHandler.class);
        }

        final WebTarget webTarget = client.target("https://localhost:" + port + "/policy/api/v1/" + endpoint);

        final Invocation.Builder invocationBuilder = webTarget.request(mediaType);

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