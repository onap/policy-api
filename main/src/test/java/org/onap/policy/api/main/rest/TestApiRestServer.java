/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2019-2020 Nordix Foundation.
 *  Modifications Copyright (C) 2020-2022 Bell Canada. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.onap.policy.api.main.PolicyApiApplication;
import org.onap.policy.common.endpoints.http.server.YamlMessageBodyHandler;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.gson.GsonMessageBodyHandler;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.common.utils.resources.TextFileUtils;
import org.onap.policy.common.utils.security.SelfSignedKeyStore;
import org.onap.policy.models.errors.concepts.ErrorResponse;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * Class to perform unit test of {@link ApiRestController}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PolicyApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
public class TestApiRestServer {

    private static final String ALIVE = "alive";
    private static final String SELF = NetworkUtil.getHostname();
    private static final String NAME = "Policy API";
    private static final String APP_JSON = "application/json";
    private static final String APP_YAML = "application/yaml";

    private static final String HEALTHCHECK_ENDPOINT = "healthcheck";
    private static final String STATISTICS_ENDPOINT = "statistics";

    private static final String OP_POLICY_NAME_VCPE = "operational.restart";

    private static final String POLICYTYPES = "policytypes";
    private static final String POLICYTYPES_TCA = "policytypes/onap.policies.monitoring.tcagen2";
    private static final String POLICYTYPES_COLLECTOR =
            "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server";
    private static final String POLICYTYPES_TCA_VERSION = "policytypes/onap.policies.monitoring.tcagen2/versions/1.0.0";
    private static final String POLICYTYPES_TCA_LATEST = "policytypes/onap.policies.monitoring.tcagen2/versions/latest";
    private static final String POLICYTYPES_COLLECTOR_VERSION =
            "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server/versions/1.0.0";
    private static final String POLICYTYPES_COLLECTOR_LATEST =
            "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server/versions/latest";

    private static final String POLICYTYPES_DROOLS = "policytypes/onap.policies.controlloop.operational.common.Drools";
    private static final String POLICYTYPES_DROOLS_VERSION = POLICYTYPES_DROOLS + "/versions/1.0.0";
    private static final String POLICYTYPES_DROOLS_VERSION_LATEST = POLICYTYPES_DROOLS + "/versions/latest";

    private static final String POLICYTYPES_NAMING_VERSION = POLICYTYPES + "/onap.policies.Naming/versions/1.0.0";

    private static final String POLICYTYPES_TCA_POLICIES =
            "policytypes/onap.policies.monitoring.tcagen2/versions/1.0.0/policies";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE =
            "policytypes/onap.policies.monitoring.tcagen2/versions/1.0.0/policies/onap.restart.tca";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE_VERSION1 =
            "policytypes/" + "onap.policies.monitoring.tcagen2/versions/1.0.0/policies/onap.restart.tca/versions/1.0.0";
    private static final String POLICYTYPES_TCA_POLICIES_VCPE_LATEST = "policytypes/"
            + "onap.policies.monitoring.tcagen2/versions/1.0.0/policies/onap.restart.tca/versions/latest";

    private static final String POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION =
            POLICYTYPES_DROOLS_VERSION + "/policies/" + OP_POLICY_NAME_VCPE + "/versions/1.0.0";

    private static final String POLICIES = "policies";

    // @formatter:off
    private static final String[] TOSCA_POLICY_RESOURCE_NAMES = {"policies/vCPE.policy.monitoring.input.tosca.json",
        "policies/vCPE.policy.monitoring.input.tosca.yaml", "policies/vDNS.policy.monitoring.input.tosca.json",
        "policies/vDNS.policy.monitoring.input.tosca.v2.yaml"};

    private static final String[] TOSCA_POLICIES_RESOURCE_NAMES = {
        "policies/vCPE.policies.optimization.input.tosca.json", "policies/vCPE.policies.optimization.input.tosca.yaml"};

    private static final String TOSCA_POLICYTYPE_OP_RESOURCE =
        "policytypes/onap.policies.controlloop.operational.Common.yaml";

    private static final String[] TOSCA_POLICYTYPE_RESOURCE_NAMES = {
        "policytypes/onap.policies.monitoring.tcagen2.yaml",
        "policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.yaml",
        "policytypes/onap.policies.controlloop.operational.common.Drools.yaml",
        "policytypes/onap.policies.controlloop.guard.Common.yaml",
        "policytypes/onap.policies.controlloop.guard.common.Blacklist.yaml",
        "policytypes/onap.policies.controlloop.guard.common.FrequencyLimiter.yaml",
        "policytypes/onap.policies.controlloop.guard.common.MinMax.yaml",
        "policytypes/onap.policies.controlloop.guard.coordination.FirstBlocksSecond.yaml",
        "policytypes/onap.policies.optimization.resource.AffinityPolicy.yaml",
        "policytypes/onap.policies.optimization.resource.DistancePolicy.yaml",
        "policytypes/onap.policies.optimization.resource.HpaPolicy.yaml",
        "policytypes/onap.policies.optimization.resource.OptimizationPolicy.yaml",
        "policytypes/onap.policies.optimization.resource.PciPolicy.yaml",
        "policytypes/onap.policies.optimization.service.QueryPolicy.yaml",
        "policytypes/onap.policies.optimization.service.SubscriberPolicy.yaml",
        "policytypes/onap.policies.optimization.resource.Vim_fit.yaml",
        "policytypes/onap.policies.optimization.resource.VnfPolicy.yaml"};

    private static final String TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_JSON =
        "policies/vCPE.policy.operational.input.tosca.json";

    private static final String TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_YAML =
        "policies/vCPE.policy.operational.input.tosca.yaml";

    private static final String POLICIES_VCPE_VERSION1 = "policies/onap.restart.tca/versions/1.0.0";
    // @formatter:on

    private static final StandardCoder standardCoder = new StandardCoder();
    private static StandardYamlCoder standardYamlCoder = new StandardYamlCoder();
    private static SelfSignedKeyStore keystore;

    @LocalServerPort
    private int apiPort;

    @Autowired
    private ApiStatisticsManager mgr;

    /**
     * Initializes parameters and set up test environment.
     *
     * @throws IOException on I/O exceptions
     * @throws InterruptedException if interrupted
     */
    @BeforeClass
    public static void setupParameters() throws IOException, InterruptedException {
        keystore = new SelfSignedKeyStore();
    }

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("server.ssl.enabled", () -> "true");
        registry.add("server.ssl.key-store", () -> keystore.getKeystoreName());
        registry.add("server.ssl.key-store-password", () -> SelfSignedKeyStore.KEYSTORE_PASSWORD);
        registry.add("server.ssl.key-store-type", () -> "PKCS12");
        registry.add("server.ssl.key-alias", () -> "policy@policy.onap.org");
        registry.add("server.ssl.key-password", () -> SelfSignedKeyStore.PRIVATE_KEY_PASSWORD);
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
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse2.getStatus());
        ErrorResponse errorResponse = rawResponse2.readEntity(ErrorResponse.class);
        assertEquals("no policy types specified on service template", errorResponse.getErrorMessage());
    }

    @Test
    public void testCreatePolicies() throws Exception {
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        new File("src/test/resources/policies/BadTestPolicy.yaml").deleteOnExit();

        // Send a policy with no policy type trigger an error
        String toscaPolicy = ResourceUtils
                .getResourceAsString(TOSCA_POLICY_RESOURCE_NAMES[TOSCA_POLICIES_RESOURCE_NAMES.length - 1]);

        toscaPolicy = toscaPolicy.replaceAll("onap.policies.monitoring.tcagen2", "IDontExist");
        TextFileUtils.putStringAsTextFile(toscaPolicy, "src/test/resources/policies/BadTestPolicy.yaml");

        Response rawResponse2 =
                createResource(POLICYTYPES_TCA_POLICIES, "src/test/resources/policies/BadTestPolicy.yaml");
        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), rawResponse2.getStatus());
        ErrorResponse errorResponse = rawResponse2.readEntity(ErrorResponse.class);
        assertThat(errorResponse.getErrorMessage())
                .contains("item \"entity\" value \"onap.restart.tca:1.0.0\" INVALID, does not equal existing entity");
    }

    @Test
    public void testSimpleCreatePolicies() throws Exception {
        for (String resrcName : TOSCA_POLICIES_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        new File("src/test/resources/policies/BadTestPolicy.yaml").deleteOnExit();

        // Send a policy with no policy type trigger an error
        String toscaPolicy = ResourceUtils
                .getResourceAsString(TOSCA_POLICY_RESOURCE_NAMES[TOSCA_POLICIES_RESOURCE_NAMES.length - 1]);

        toscaPolicy = toscaPolicy.replaceAll("onap.policies.monitoring.tcagen2", "IDontExist");
        toscaPolicy = toscaPolicy.replaceAll("onap.restart.tca", "onap.restart.tca.IDontExist");
        TextFileUtils.putStringAsTextFile(toscaPolicy, "src/test/resources/policies/BadTestPolicy.yaml");

        Response rawResponse2 = createResource(POLICIES, "src/test/resources/policies/BadTestPolicy.yaml");
        ErrorResponse errorResponse = rawResponse2.readEntity(ErrorResponse.class);
        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), rawResponse2.getStatus());
        assertThat(errorResponse.getErrorMessage())
                .contains("item \"policy type\" value \"IDontExist:1.0.0\" INVALID, not found");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testToscaCompliantOpDroolsPolicies() throws Exception {
        Response rawResponse = createResource(POLICYTYPES, TOSCA_POLICYTYPE_OP_RESOURCE);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_VERSION, APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICIES, TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICIES, TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_YAML);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

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
        assertEquals("Restart", operation.get("operation"));

        rawResponse = deleteResource(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION, APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
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
        // ApiStatisticsManager.resetAllStatistics();
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
        Response rawResponse = readResource("policytypes/onap.policies.optimization.resource.HpaPolicy", mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertNotNull(namingServiceTemplate);
        assertEquals(3, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(5, namingServiceTemplate.getDataTypesAsMap().size());

        rawResponse = readResource(POLICYTYPES, mediaType);
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

        rawResponse = readResource(POLICYTYPES_NAMING_VERSION, mediaType);
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
        Response rawResponse = deleteResource("policytypes/onap.policies.IDoNotExist/versions/1.0.0", mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICYTYPES, "policytypes/onap.policies.Test.yaml");
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource("policytypes/onap.policies.Test/versions/1.0.0", mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = deleteResource("policytypes/onap.policies.Test/versions/1.0.0", mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource("policytypes/onap.policies.Test/versions/1.0.0", mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
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
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_LATEST, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

    }

    @Test
    public void testNamingPolicyGet() throws Exception {

        Response rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/1.0.0", APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/1.0.0?mode=referenced", APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        ToscaServiceTemplate namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(3, namingServiceTemplate.getDataTypesAsMap().size());

        rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/latest?mode=referenced", APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(3, namingServiceTemplate.getDataTypesAsMap().size());

        rawResponse =
                readResource("policytypes/onap.policies.Naming/versions/1.0.0/policies?mode=referenced", APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(3, namingServiceTemplate.getDataTypesAsMap().size());

        rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/1.0.0", APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);

        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertNull(namingServiceTemplate.getPolicyTypes());
        assertNull(namingServiceTemplate.getDataTypes());

        rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/latest", APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertNull(namingServiceTemplate.getPolicyTypes());
        assertNull(namingServiceTemplate.getDataTypes());

        rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/policies", APP_JSON);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertNull(namingServiceTemplate.getPolicyTypes());
        assertNull(namingServiceTemplate.getDataTypes());
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
        Response rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("policy onap.restart.tca:1.0.0 not found", error.getErrorMessage());
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
        assertEquals("policies for onap.restart.tca:1.0.0 do not exist", errorResponse.getErrorMessage());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("policies for onap.restart.tca:null do not exist", errorResponse.getErrorMessage());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_LATEST, mediaType);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("policies for onap.restart.tca:null do not exist", errorResponse.getErrorMessage());
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
    public void testGetPoliciesJson() throws Exception {
        getPolicies(APP_JSON);
    }

    @Test
    public void testGetPoliciesYaml() throws Exception {
        getPolicies(APP_YAML);
    }

    private void getPolicies(String mediaType) throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertThat(response).isNotNull();
            assertThat(response.getPolicyTypes()).isNotEmpty();
        }
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        }
        Response rawResponse = readResource(POLICIES, mediaType);
        assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertThat(response.getToscaTopologyTemplate().getPolicies()).isNotEmpty();
    }

    @Test
    public void testGetSpecificPolicyJson() throws Exception {
        getSpecificPolicy(APP_JSON);
    }

    @Test
    public void testGetSpecificPolicyYaml() throws Exception {
        getSpecificPolicy(APP_YAML);
    }

    private void getSpecificPolicy(String mediaType) throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertThat(response).isNotNull();
            assertThat(response.getPolicyTypes()).isNotEmpty();
        }
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        }
        Response rawResponse = readResource(POLICIES_VCPE_VERSION1, mediaType);
        assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertThat(response.getToscaTopologyTemplate().getPolicies()).hasSize(1);
    }

    @Test
    public void testDeleteSpecificPolicy() throws Exception {
        Response rawResponse;
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            rawResponse = createResource(POLICYTYPES, resrcName);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertThat(response).isNotNull();
            assertThat(response.getPolicyTypes()).isNotEmpty();
        }
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        }

        rawResponse = readResource(POLICIES_VCPE_VERSION1, APP_JSON);
        assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // delete a particular policy
        rawResponse = deleteResource(POLICIES_VCPE_VERSION1, APP_JSON);
        assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        rawResponse = readResource(POLICIES_VCPE_VERSION1, APP_JSON);
        assertThat(rawResponse.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

        rawResponse = deleteResource(POLICIES_VCPE_VERSION1, APP_JSON);
        assertThat(rawResponse.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

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

    private Invocation.Builder sendHttpsRequest(final String endpoint, String mediaType) throws Exception {

        final TrustManager[] noopTrustManager = NetworkUtil.getAlwaysTrustingManager();

        final SSLContext sc = SSLContext.getInstance("TLSv1.2");
        sc.init(null, noopTrustManager, new SecureRandom());
        final ClientBuilder clientBuilder =
                ClientBuilder.newBuilder().sslContext(sc).hostnameVerifier((host, session) -> true);
        final Client client = clientBuilder.build();
        final HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic("policyadmin", "zb!XztG34");
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
        mgr.updateTotalApiCallCount();
        mgr.updateApiCallSuccessCount();
        mgr.updateApiCallFailureCount();
        mgr.updateTotalPolicyGetCount();
        mgr.updateTotalPolicyPostCount();
        mgr.updateTotalPolicyTypeGetCount();
        mgr.updateTotalPolicyTypePostCount();
        mgr.updatePolicyGetSuccessCount();
        mgr.updatePolicyGetFailureCount();
        mgr.updatePolicyPostSuccessCount();
        mgr.updatePolicyPostFailureCount();
        mgr.updatePolicyTypeGetSuccessCount();
        mgr.updatePolicyTypeGetFailureCount();
        mgr.updatePolicyTypePostSuccessCount();
        mgr.updatePolicyTypePostFailureCount();
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