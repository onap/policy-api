/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.api.main.parameters.CommonTestData;
import org.onap.policy.api.main.rest.provider.PolicyProvider;
import org.onap.policy.api.main.rest.provider.PolicyTypeProvider;
import org.onap.policy.api.main.startstop.Main;
import org.onap.policy.common.endpoints.http.server.YamlMessageBodyHandler;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.common.utils.resources.TextFileUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.errors.concepts.ErrorResponse;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaEntityKey;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicyInput;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

/**
 * Class to perform unit test of {@link ApiRestController}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class TestApiRestServer {
    private static final String ALIVE = "alive";
    private static final String SELF = NetworkUtil.getHostname();
    private static final String NAME = "Policy API";
    private static final String APP_JSON = "application/json";
    private static final String APP_YAML = "application/yaml";

    private static final String HEALTHCHECK_ENDPOINT = "healthcheck";
    private static final String STATISTICS_ENDPOINT = "statistics";

    private static final String OP_POLICY_NAME_VCPE = "operational.restart";
    private static final String OP_POLICY_NAME_VDNS = "operational.scaleout";
    private static final String OP_POLICY_NAME_VFW = "operational.modifyconfig";

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

    private static final String POLICYTYPES_DROOLS = "policytypes/onap.policies.controlloop.operational.common.Drools";
    private static final String POLICYTYPES_DROOLS_VERSION = POLICYTYPES_DROOLS + "/versions/1.0.0";
    private static final String POLICYTYPES_DROOLS_VERSION_LATEST = POLICYTYPES_DROOLS + "/versions/latest";

    private static final String POLICYTYPES_TCA_POLICIES =
            "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE =
            "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies/onap.restart.tca";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE_VERSION1 = "policytypes/"
            + "onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies/onap.restart.tca/versions/1.0.0";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE_VERSION2 = "policytypes/"
            + "onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies/onap.restart.tca/versions/2.0.0";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE_LATEST = "policytypes/"
            + "onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies/onap.restart.tca/versions/latest";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE_DEPLOYED = "policytypes/"
            + "onap.policies.monitoring.cdap.tca.hi.lo.app/versions/1.0.0/policies/onap.restart.tca/versions/deployed";

    private static final String POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION =
            POLICYTYPES_DROOLS_VERSION + "/policies/" + OP_POLICY_NAME_VCPE + "/versions/1.0.0";

    private static final String GUARD_POLICYTYPE = "onap.policies.controlloop.Guard";
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

    private static final String OPS_POLICYTYPE = "onap.policies.controlloop.Operational";
    private static final String OPS_POLICIES =
            "policytypes/onap.policies.controlloop.Operational/versions/1.0.0/policies";
    private static final String OPS_POLICIES_VCPE_LATEST =
            "policytypes/onap.policies.controlloop.Operational/versions/1.0.0/policies/" + OP_POLICY_NAME_VCPE
                    + "/versions/latest";
    private static final String OPS_POLICIES_VCPE_DEPLOYED =
            "policytypes/onap.policies.controlloop.Operational/versions/1.0.0/policies/" + OP_POLICY_NAME_VCPE
                    + "/versions/deployed";
    private static final String OPS_POLICIES_VDNS_LATEST =
            "policytypes/onap.policies.controlloop.Operational/versions/1.0.0/policies/" + OP_POLICY_NAME_VDNS
                    + "/versions/latest";
    private static final String OPS_POLICIES_VFIREWALL_LATEST =
            "policytypes/onap.policies.controlloop.Operational/versions/1.0.0/policies/" + OP_POLICY_NAME_VFW
                    + "/versions/latest";
    private static final String OPS_POLICIES_VCPE_VERSION = "policytypes/"
            + "onap.policies.controlloop.Operational/versions/1.0.0/policies/" + OP_POLICY_NAME_VCPE + "/versions/1";
    private static final String OPS_POLICIES_VDNS_VERSION = "policytypes/"
            + "onap.policies.controlloop.Operational/versions/1.0.0/policies/" + OP_POLICY_NAME_VDNS + "/versions/1";
    private static final String OPS_POLICIES_VFIREWALL_VERSION = "policytypes/"
            + "onap.policies.controlloop.Operational/versions/1.0.0/policies/" + OP_POLICY_NAME_VFW + "/versions/1";
    private static final String POLICIES = "policies";

    private static final String KEYSTORE = System.getProperty("user.dir") + "/src/test/resources/ssl/policy-keystore";

    // @formatter:off
    private static final String[] TOSCA_POLICY_RESOURCE_NAMES = {
        "policies/vCPE.policy.monitoring.input.tosca.json",
        "policies/vCPE.policy.monitoring.input.tosca.v2.yaml",
        "policies/vDNS.policy.monitoring.input.tosca.json",
        "policies/vDNS.policy.monitoring.input.tosca.v2.yaml"
    };

    private static final String[] TOSCA_POLICIES_RESOURCE_NAMES = {
        "policies/vCPE.policies.optimization.input.tosca.json",
        "policies/vCPE.policies.optimization.input.tosca.v2.yaml"
    };

    private static final String TOSCA_POLICYTYPE_OP_RESOURCE =
        "policytypes/onap.policies.controlloop.operational.Common.yaml";

    private static final String LEGACY_POLICYTYPE_OP_RESOURCE =
        "policytypes/onap.policies.controlloop.Operational.yaml";

    private static final String[] TOSCA_POLICYTYPE_RESOURCE_NAMES = {
        "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.yaml",
        "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.yaml",
        "policytypes/onap.policies.Optimization.yaml",
        LEGACY_POLICYTYPE_OP_RESOURCE,
        TOSCA_POLICYTYPE_OP_RESOURCE,
        "policytypes/onap.policies.controlloop.guard.Blacklist.yaml",
        "policytypes/onap.policies.controlloop.guard.FrequencyLimiter.yaml",
        "policytypes/onap.policies.controlloop.guard.MinMax.yaml",
        "policytypes/onap.policies.controlloop.guard.coordination.FirstBlocksSecond.yaml",
        "policytypes/onap.policies.optimization.resource.AffinityPolicy.yaml",
        "policytypes/onap.policies.optimization.resource.DistancePolicy.yaml",
        "policytypes/onap.policies.optimization.resource.HpaPolicy.yaml",
        "policytypes/onap.policies.optimization.resource.OptimizationPolicy.yaml",
        "policytypes/onap.policies.optimization.resource.PciPolicy.yaml",
        "policytypes/onap.policies.optimization.service.QueryPolicy.yaml",
        "policytypes/onap.policies.optimization.service.SubscriberPolicy.yaml",
        "policytypes/onap.policies.optimization.resource.Vim_fit.yaml",
        "policytypes/onap.policies.optimization.resource.VnfPolicy.yaml"
    };

    private static final String TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_JSON =
        "policies/vCPE.policy.operational.input.tosca.json";

    private static final String TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_YAML =
        "policies/vCPE.policy.operational.input.tosca.yaml";

    private static final String[] LEGACY_GUARD_POLICY_RESOURCE_NAMES = {
        "policies/vDNS.policy.guard.frequency.input.json",
        "policies/vDNS.policy.guard.minmax.input.json"
    };

    private static final String[] LEGACY_GUARD_POLICY_NAMES = {
        "guard.frequency.scaleout",
        "guard.minmax.scaleout"
    };

    private static final String[] LEGACY_OPERATIONAL_POLICY_RESOURCE_NAMES = {
        "policies/vCPE.policy.operational.input.json",
        "policies/vDNS.policy.operational.input.json",
        "policies/vFirewall.policy.operational.input.json"
    };

    private static final String[] LEGACY_OPERATIONAL_POLICY_NAMES = {
        OP_POLICY_NAME_VCPE,
        OP_POLICY_NAME_VDNS,
        OP_POLICY_NAME_VFW
    };

    private static PolicyModelsProviderParameters providerParams;
    private static ApiParameterGroup apiParamGroup;
    private static PolicyProvider policyProvider;
    private static PolicyTypeProvider policyTypeProvider;

    // @formatter:on

    private static final StandardCoder standardCoder = new StandardCoder();

    private static int apiPort;
    private static Main apiMain;

    private static StandardYamlCoder standardYamlCoder = new StandardYamlCoder();

    /**
     * Initializes parameters.
     *
     * @throws PfModelException the PfModel parsing exception
     */
    @BeforeClass
    public static void setupParameters() throws PfModelException {
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
     * Set up test environemnt.
     *
     * @throws Exception on test setup exceptions
     */
    @BeforeClass
    public static void beforeStartApiService() throws Exception {
        apiPort = NetworkUtil.allocPort();

        final String[] apiConfigParameters = new String[2];
        final Properties systemProps = System.getProperties();
        systemProps.put("javax.net.ssl.keyStore", KEYSTORE);
        systemProps.put("javax.net.ssl.keyStorePassword", "Pol1cy_0nap");
        System.setProperties(systemProps);
        new CommonTestData().makeParameters("src/test/resources/parameters/ApiConfigParameters_Https.json",
                "src/test/resources/parameters/ApiConfigParameters_HttpsXXX.json", apiPort);
        apiConfigParameters[0] = "-c";
        apiConfigParameters[1] = "src/test/resources/parameters/ApiConfigParameters_HttpsXXX.json";
        apiMain = new Main(apiConfigParameters);
    }

    /**
     * Method for cleanup after each test.
     */
    @AfterClass
    public static void teardown() throws Exception {
        policyTypeProvider.close();
        policyProvider.close();

        if (apiMain != null) {
            apiMain.shutdown();
        }
    }

    /**
     * Clear the database before each test.
     *
     * @throws Exception on clearing exceptions
     */
    @Before
    public void beforeClearDatabase() throws Exception {

        Response rawResponse = readResource(POLICYTYPES, APP_JSON);
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);

        for (ToscaEntityKey policyTypeKey : response.getPolicyTypesAsMap().keySet()) {
            if (GUARD_POLICYTYPE.equals(policyTypeKey.getName())
                    || OPS_POLICYTYPE.equals(policyTypeKey.getName())) {
                deleteLegacyPolicies(LEGACY_GUARD_POLICY_NAMES, GUARD_POLICYTYPE);
                deleteLegacyPolicies(LEGACY_OPERATIONAL_POLICY_NAMES, OPS_POLICYTYPE);
            } else {
                deleteToscaPolicies(policyTypeKey);
            }

            String deletePolicyTypePath =
                    "policytypes/" + policyTypeKey.getName() + "/versions/" + policyTypeKey.getVersion();
            deleteResource(deletePolicyTypePath, APP_JSON);
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
    public void testCreatePolicyTypes() throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().isEmpty());
        }

        // Send a policy type with a null value to trigger an error
        Response rawResponse = readResource(POLICYTYPES, APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        String firstPolicyType = response.getPolicyTypes().keySet().iterator().next();
        response.getPolicyTypes().put(firstPolicyType, null);
        Response rawResponse2 = createResource(POLICYTYPES, standardCoder.encode(response));
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse2.getStatus());
        ErrorResponse errorResponse = rawResponse2.readEntity(ErrorResponse.class);
        assertEquals("no policy types specified in the service template", errorResponse.getErrorMessage());
    }

    @Test
    public void testCreatePolicies() throws Exception {
        createPolicyTypes();

        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        new File("src/test/resources/policies/BadTestPolicy.yaml").deleteOnExit();

        // Send a policy with no policy type trigger an error
        String toscaPolicy = ResourceUtils
                .getResourceAsString(TOSCA_POLICY_RESOURCE_NAMES[TOSCA_POLICIES_RESOURCE_NAMES.length - 1]);

        toscaPolicy = toscaPolicy.replaceAll("onap.policies.monitoring.cdap.tca.hi.lo.app", "");
        TextFileUtils.putStringAsTextFile(toscaPolicy, "src/test/resources/policies/BadTestPolicy.yaml");

        Response rawResponse2 =
                createResource(POLICYTYPES_TCA_POLICIES, "src/test/resources/policies/BadTestPolicy.yaml");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse2.getStatus());
        ErrorResponse errorResponse = rawResponse2.readEntity(ErrorResponse.class);
        assertEquals("policy type id does not match", errorResponse.getErrorMessage());
    }

    @Test
    public void testSimpleCreatePolicies() throws Exception {
        testCreatePolicyTypes();

        for (String resrcName : TOSCA_POLICIES_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        new File("src/test/resources/policies/BadTestPolicy.yaml").deleteOnExit();

        // Send a policy with no policy type trigger an error
        String toscaPolicy = ResourceUtils
                .getResourceAsString(TOSCA_POLICY_RESOURCE_NAMES[TOSCA_POLICIES_RESOURCE_NAMES.length - 1]);

        toscaPolicy = toscaPolicy.replaceAll("onap.policies.monitoring.cdap.tca.hi.lo.app", "");
        TextFileUtils.putStringAsTextFile(toscaPolicy, "src/test/resources/policies/BadTestPolicy.yaml");

        Response rawResponse2 = createResource(POLICIES, "src/test/resources/policies/BadTestPolicy.yaml");
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse2.getStatus());
        ErrorResponse errorResponse = rawResponse2.readEntity(ErrorResponse.class);
        assertEquals("policy type NULL:0.0.0 for policy onap.restart.tca:2.0.0 does not exist",
                errorResponse.getErrorMessage());
    }

    @Test
    public void testCreateGuardPolicies() throws Exception {
        createPolicyTypes();

        for (String resrcName : LEGACY_GUARD_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
    }

    @Test
    public void testCreateOperationalPolicies() throws Exception {
        createPolicyTypes();

        for (String resrcName : LEGACY_OPERATIONAL_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
    }

    @Test
    public void testToscaCompliantOpDroolsPolicies() throws Exception {
        Response rawResponse =
                createResource(POLICYTYPES, TOSCA_POLICYTYPE_OP_RESOURCE);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_VERSION, APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICIES, TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICIES, TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_YAML);
        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION, APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = deleteResource(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION, APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICIES, TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_YAML);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION, APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION, APP_YAML);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        ToscaServiceTemplate toscaVcpeSt = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, toscaVcpeSt.getToscaTopologyTemplate().getPolicies().size());
        assertEquals(OP_POLICY_NAME_VCPE,
            toscaVcpeSt.getToscaTopologyTemplate().getPolicies().get(0).get(OP_POLICY_NAME_VCPE).getName());

        Map<String, Object> props =
                toscaVcpeSt.getToscaTopologyTemplate().getPolicies().get(0).get(OP_POLICY_NAME_VCPE).getProperties();
        assertNotNull(props);

        List<Object> operations = (List<Object>) props.get("operations");
        assertEquals(1, operations.size());
        assertEquals(props.get("trigger"), ((Map<String, Object>) operations.get(0)).get("id"));

        Map<String, Object> operation =
                (Map<String, Object>) ((Map<String, Object>) operations.get(0)).get("operation");
        assertEquals("APPC", operation.get("actor"));
        assertEquals("Restart", operation.get("recipe"));
    }

    @Test
    public void testHealthCheckSuccessJson() throws Exception {
        testHealthCheckSuccess(APP_JSON);
    }

    @Test
    public void testHealthCheckSuccessYaml() throws Exception {
        testHealthCheckSuccess(APP_YAML);
    }

    private void testHealthCheckSuccess(String mediaType) throws Exception {
        final Invocation.Builder invocationBuilder = sendHttpsRequest(HEALTHCHECK_ENDPOINT, mediaType);
        final HealthCheckReport report = invocationBuilder.get(HealthCheckReport.class);
        validateHealthCheckReport(NAME, SELF, true, 200, ALIVE, report);
    }

    @Test
    public void testApiStatistics_200_Json() throws Exception {
        testApiStatistics_200(APP_JSON);
    }

    @Test
    public void testApiStatistics_200_Yaml() throws Exception {
        testApiStatistics_200(APP_YAML);
    }

    private void testApiStatistics_200(String mediaType) throws Exception {
        Invocation.Builder invocationBuilder = sendHttpsRequest(STATISTICS_ENDPOINT, mediaType);
        StatisticsReport report = invocationBuilder.get(StatisticsReport.class);
        validateStatisticsReport(report, 200);
        updateApiStatistics();
        invocationBuilder = sendHttpsRequest(STATISTICS_ENDPOINT, mediaType);
        report = invocationBuilder.get(StatisticsReport.class);
        validateStatisticsReport(report, 200);
        ApiStatisticsManager.resetAllStatistics();
    }

    @Test
    public void testReadPolicyTypesJson() throws Exception {
        testReadPolicyTypes(APP_JSON);
    }

    @Test
    public void testReadPolicyTypesYaml() throws Exception {
        testReadPolicyTypes(APP_YAML);
    }

    private void testReadPolicyTypes(String mediaType) throws Exception {
        createPolicyTypes();

        Response rawResponse = readResource(POLICYTYPES, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertFalse(response.getPolicyTypes().isEmpty());

        rawResponse = readResource(POLICYTYPES_TCA, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_LATEST, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_COLLECTOR, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_COLLECTOR_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_COLLECTOR_LATEST, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_VERSION_LATEST, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    public void testDeletePolicyTypeJson() throws Exception {
        testDeletePolicyType(APP_JSON);
    }

    @Test
    public void testDeletePolicyTypeYaml() throws Exception {
        testDeletePolicyType(APP_YAML);
    }

    private void testDeletePolicyType(String mediaType) throws Exception {
        createPolicyTypes();

        Response rawResponse = deleteResource(POLICYTYPES_TCA_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_VERSION, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());

        rawResponse = deleteResource(POLICYTYPES_COLLECTOR_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_COLLECTOR_VERSION, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_COLLECTOR, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_COLLECTOR_LATEST, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("policy type with ID "
                + "onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server:null does not exist",
                errorResponse.getErrorMessage());
    }

    @Test
    public void testReadPoliciesJson() throws Exception {
        testReadPolicies(APP_JSON);
    }

    @Test
    public void testReadPoliciesYaml() throws Exception {
        testReadPolicies(APP_YAML);
    }

    private void testReadPolicies(String mediaType) throws Exception {
        testCreatePolicies();

        Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION2, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_LATEST, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    public void testDeletePoliciesJson() throws Exception {
        testDeletePolicies(APP_JSON);
    }

    @Test
    public void testDeletePoliciesYaml() throws Exception {
        testDeletePolicies(APP_YAML);
    }

    private void testDeletePolicies(String mediaType) throws Exception {
        createPolicyTypes();

        Response rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
        assertEquals(
                "policy with ID onap.restart.tca:1.0.0 and "
                        + "type onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                error.getErrorMessage());
    }

    @Test
    public void testDeletePolicyVersionJson() throws Exception {
        testDeletePolicyVersion(APP_JSON);
    }

    @Test
    public void testDeletePolicyVersionYaml() throws Exception {
        testDeletePolicyVersion(APP_YAML);
    }

    private void testDeletePolicyVersion(String mediaType) throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().isEmpty());
        }
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
        Response rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals(
                "policy with ID onap.restart.tca:1.0.0 and type "
                        + "onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                errorResponse.getErrorMessage());

        rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION2, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION2, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals(
                "policy with ID onap.restart.tca:2.0.0 and type "
                        + "onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                errorResponse.getErrorMessage());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals(
                "policy with ID onap.restart.tca:null and type "
                        + "onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                errorResponse.getErrorMessage());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_LATEST, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals(
                "policy with ID onap.restart.tca:null and type "
                        + "onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 does not exist",
                errorResponse.getErrorMessage());
    }

    @Test
    public void testGetAllVersionOfPolicyJson() throws Exception {
        testGetAllVersionOfPolicy(APP_JSON);
    }

    @Test
    public void testGetAllVersionOfPolicyYaml() throws Exception {
        testGetAllVersionOfPolicy(APP_YAML);
    }

    private void testGetAllVersionOfPolicy(String mediaType) throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().isEmpty());
        }
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
        Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    public void testReadGuardPoliciesJson() throws Exception {
        testReadGuardPolicies(APP_JSON);
    }

    @Test
    public void testReadGuardPoliciesYaml() throws Exception {
        testReadGuardPolicies(APP_YAML);
    }

    private void testReadGuardPolicies(String mediaType) throws Exception {
        createGuardPolicies();

        Response rawResponse = readResource(GUARD_POLICIES_VDNS_FL_LATEST, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(GUARD_POLICIES_VDNS_FL_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(GUARD_POLICIES_VDNS_MINMAX_LATEST, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(GUARD_POLICIES_VDNS_MINMAX_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    public void testReadOperationalPoliciesJson() throws Exception {
        testReadOperationalPolicies(APP_JSON);
    }

    @Test
    public void testReadOperationalPoliciesYaml() throws Exception {
        testReadOperationalPolicies(APP_YAML);
    }

    private void testReadOperationalPolicies(String mediaType) throws Exception {
        createOperationalPolicies();

        Response rawResponse = readResource(OPS_POLICIES_VCPE_LATEST, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(OPS_POLICIES_VCPE_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(OPS_POLICIES_VDNS_LATEST, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(OPS_POLICIES_VDNS_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(OPS_POLICIES_VFIREWALL_LATEST, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(OPS_POLICIES_VFIREWALL_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    public void testDeleteGuardPolicyJson() throws Exception {
        testDeleteGuardPolicy(APP_JSON);
    }

    @Test
    public void testDeleteGuardPolicyYaml() throws Exception {
        testDeleteGuardPolicy(APP_YAML);
    }

    private void testDeleteGuardPolicy(String mediaType) throws Exception {
        createGuardPolicies();

        Response rawResponse = deleteResource(GUARD_POLICIES_VDNS_FL_VERSION, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    public void testGetDeployedVersionsOfGuardPolicyJson() throws Exception {
        testGetDeployedVersionsOfGuardPolicy(APP_JSON);
    }

    @Test
    public void testGetDeployedVersionsOfGuardPolicyYaml() throws Exception {
        testGetDeployedVersionsOfGuardPolicy(APP_YAML);
    }

    private void testGetDeployedVersionsOfGuardPolicy(String mediaType) throws Exception {
        Response rawResponse = readResource(GUARD_POLICIES_VDNS_FL_DEPLOYED, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals(
                "could not find policy with ID guard.frequency.scaleout and type "
                        + "onap.policies.controlloop.guard.FrequencyLimiter:1.0.0 deployed in any pdp group",
                errorResponse.getErrorMessage());
    }

    @Test
    public void testDeleteOperationalPolicyJson() throws Exception {
        testDeleteOperationalPolicy(APP_JSON);
    }

    @Test
    public void testDeleteOperationalPolicyYaml() throws Exception {
        testDeleteOperationalPolicy(APP_YAML);
    }

    private void testDeleteOperationalPolicy(String mediaType) throws Exception {
        createPolicyTypes();

        Response rawResponse = deleteResource(OPS_POLICIES_VCPE_VERSION, mediaType);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
        ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("no policy found for policy: " + OP_POLICY_NAME_VCPE + ":1", error.getErrorMessage());
    }

    @Test
    public void testGetDeployedVersionsOfPolicyJson() throws Exception {
        testGetDeployedVersionsOfPolicy(APP_JSON);
    }

    @Test
    public void testGetDeployedVersionsOfPolicyYaml() throws Exception {
        testGetDeployedVersionsOfPolicy(APP_YAML);
    }

    private void testGetDeployedVersionsOfPolicy(String mediaType) throws Exception {
        Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_DEPLOYED, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals(
                "could not find policy with ID onap.restart.tca and type "
                        + "onap.policies.monitoring.cdap.tca.hi.lo.app:1.0.0 deployed in any pdp group",
                errorResponse.getErrorMessage());
    }

    @Test
    public void testGetLatestVersionOfOperationalPolicyJson() throws Exception {
        testGetLatestVersionOfOperationalPolicy(APP_JSON);
    }

    @Test
    public void testGetLatestVersionOfOperationalPolicyYaml() throws Exception {
        testGetLatestVersionOfOperationalPolicy(APP_YAML);
    }

    private void testGetLatestVersionOfOperationalPolicy(String mediaType) throws Exception {
        Response rawResponse = readResource(OPS_POLICIES_VDNS_LATEST, mediaType);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
        ErrorResponse errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("no policy found for policy: " + OP_POLICY_NAME_VDNS + ":null", errorResponse.getErrorMessage());
    }

    @Test
    public void testGetSpecificVersionOfOperationalPolicyJson() throws Exception {
        testGetSpecificVersionOfOperationalPolicy(APP_JSON);
    }

    @Test
    public void testGetSpecificVersionOfOperationalPolicyYaml() throws Exception {
        testGetSpecificVersionOfOperationalPolicy(APP_YAML);
    }

    private void testGetSpecificVersionOfOperationalPolicy(String mediaType) throws Exception {
        Response rawResponse = readResource(OPS_POLICIES_VDNS_VERSION, mediaType);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
        ErrorResponse errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("no policy found for policy: " + OP_POLICY_NAME_VDNS + ":1", errorResponse.getErrorMessage());
    }

    @Test
    public void testGetDeployedVersionsOfOperationalPolicyJson() throws Exception {
        testGetDeployedVersionsOfOperationalPolicy(APP_JSON);
    }

    @Test
    public void testGetDeployedVersionsOfOperationalPolicyYaml() throws Exception {
        testGetDeployedVersionsOfOperationalPolicy(APP_YAML);
    }

    private void testGetDeployedVersionsOfOperationalPolicy(String mediaType) throws Exception {
        Response rawResponse = readResource(OPS_POLICIES_VCPE_DEPLOYED, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals(
                "could not find policy with ID " + OP_POLICY_NAME_VCPE + " and type "
                        + "onap.policies.controlloop.Operational:1.0.0 deployed in any pdp group",
                errorResponse.getErrorMessage());
    }

    @Test
    public void testDeleteSpecificVersionOfOperationalPolicy() throws Exception {
        createOperationalPolicies();

        Response rawResponse = deleteResource(OPS_POLICIES_VDNS_VERSION, APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = deleteResource(OPS_POLICIES_VDNS_VERSION, APP_YAML);
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), rawResponse.getStatus());
        ErrorResponse errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("no policy found for policy: " + OP_POLICY_NAME_VDNS + ":1", errorResponse.getErrorMessage());
    }

    private Response createResource(String endpoint, String resourceName) throws Exception {

        String mediaType = APP_JSON; // default media type
        ToscaServiceTemplate rawServiceTemplate = new ToscaServiceTemplate();
        if (resourceName.endsWith(".json")) {
            rawServiceTemplate =
                    standardCoder.decode(ResourceUtils.getResourceAsString(resourceName), ToscaServiceTemplate.class);
        } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
            mediaType = APP_YAML;
            rawServiceTemplate = standardYamlCoder.decode(ResourceUtils.getResourceAsString(resourceName),
                    ToscaServiceTemplate.class);
        }

        final Invocation.Builder invocationBuilder;

        invocationBuilder = sendHttpsRequest(endpoint, mediaType);

        Entity<ToscaServiceTemplate> entity = Entity.entity(rawServiceTemplate, mediaType);
        return invocationBuilder.post(entity);
    }

    private Response createGuardPolicy(String endpoint, String resourceName) throws Exception {

        String mediaType = APP_JSON; // default media type
        LegacyGuardPolicyInput rawGuardPolicy = new LegacyGuardPolicyInput();
        if (resourceName.endsWith(".json")) {
            rawGuardPolicy =
                    standardCoder.decode(ResourceUtils.getResourceAsString(resourceName), LegacyGuardPolicyInput.class);
        } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
            mediaType = APP_YAML;
            rawGuardPolicy = standardYamlCoder.decode(ResourceUtils.getResourceAsString(resourceName),
                    LegacyGuardPolicyInput.class);
        }

        final Invocation.Builder invocationBuilder;

        invocationBuilder = sendHttpsRequest(endpoint, mediaType);

        Entity<LegacyGuardPolicyInput> entity = Entity.entity(rawGuardPolicy, mediaType);
        return invocationBuilder.post(entity);
    }

    private Response createOperationalPolicy(String endpoint, String resourceName) throws Exception {

        String mediaType = APP_JSON; // default media type
        LegacyOperationalPolicy rawOpsPolicy = new LegacyOperationalPolicy();
        if (resourceName.endsWith(".json")) {
            rawOpsPolicy = standardCoder.decode(ResourceUtils.getResourceAsString(resourceName),
                    LegacyOperationalPolicy.class);
        } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
            mediaType = APP_YAML;
            rawOpsPolicy = standardYamlCoder.decode(ResourceUtils.getResourceAsString(resourceName),
                    LegacyOperationalPolicy.class);
        }

        final Invocation.Builder invocationBuilder;

        invocationBuilder = sendHttpsRequest(endpoint, mediaType);

        Entity<LegacyOperationalPolicy> entity = Entity.entity(rawOpsPolicy, mediaType);
        return invocationBuilder.post(entity);
    }

    private Response readResource(String endpoint, String mediaType) throws Exception {

        final Invocation.Builder invocationBuilder;

        invocationBuilder = sendHttpsRequest(endpoint, mediaType);

        return invocationBuilder.get();

    }

    private Response deleteResource(String endpoint, String mediaType) throws Exception {

        final Invocation.Builder invocationBuilder;

        invocationBuilder = sendHttpsRequest(endpoint, mediaType);

        return invocationBuilder.delete();
    }

    private void createPolicyTypes() throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
    }

    private void createGuardPolicies() throws Exception {
        createPolicyTypes();

        for (String resrcName : LEGACY_GUARD_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createGuardPolicy(GUARD_POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
    }

    private void createOperationalPolicies() throws Exception {
        createPolicyTypes();

        for (String resrcName : LEGACY_OPERATIONAL_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createOperationalPolicy(OPS_POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
    }

    private Invocation.Builder sendHttpsRequest(final String endpoint, String mediaType) throws Exception {

        final TrustManager[] noopTrustManager = NetworkUtil.getAlwaysTrustingManager();

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

        final WebTarget webTarget = client.target("https://localhost:" + apiPort + "/policy/api/v1/" + endpoint);

        final Invocation.Builder invocationBuilder = webTarget.request(mediaType);

        if (!NetworkUtil.isTcpPortOpen("localhost", apiPort, 60, 1000L)) {
            throw new IllegalStateException("cannot connect to port " + apiPort);
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

    private void deleteToscaPolicies(ToscaEntityKey policyTypeKey) throws Exception {

        String getPoliciesPath =
                "policytypes/" + policyTypeKey.getName() + "/versions/" + policyTypeKey.getVersion() + "/policies";

        Response rawPolicyResponse = readResource(getPoliciesPath, APP_JSON);
        if (Response.Status.OK.getStatusCode() == rawPolicyResponse.getStatus()) {
            ToscaServiceTemplate policyResponse = rawPolicyResponse.readEntity(ToscaServiceTemplate.class);

            for (ToscaEntityKey policyKey : policyResponse.getToscaTopologyTemplate().getPoliciesAsMap().keySet()) {
                String deletePolicyPath =
                        "policytypes/" + policyTypeKey.getName() + "/versions/" + policyTypeKey.getVersion()
                                + "/policies/" + policyKey.getName() + "/versions/" + policyKey.getVersion();
                deleteResource(deletePolicyPath, APP_JSON);
            }
        }
    }

    private void deleteLegacyPolicies(String[] legacyPolicyNames, String legacyPolicyType) throws Exception {

        for (String policyName : legacyPolicyNames) {
            String policyPath =
                    "policytypes/" + legacyPolicyType + "/versions/1.0.0/policies/" + policyName + "/versions/1";
            if (Response.Status.OK.getStatusCode() == readResource(policyPath, APP_JSON).getStatus()) {
                deleteResource(policyPath, APP_JSON);
            }
        }
    }
}