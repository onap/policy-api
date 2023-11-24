/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2019-2020, 2022-2023 Nordix Foundation.
 *  Modifications Copyright (C) 2020-2023 Bell Canada. All rights reserved.
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.onap.policy.api.main.PolicyApiApplication;
import org.onap.policy.api.main.rest.utils.CommonTestRestController;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.network.NetworkUtil;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.common.utils.resources.TextFileUtils;
import org.onap.policy.common.utils.security.SelfSignedKeyStore;
import org.onap.policy.models.errors.concepts.ErrorResponse;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

/**
 * Class to perform unit test of {@link ApiRestController}.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@SpringBootTest(classes = PolicyApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({ "test", "default" })
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class TestApiRestServer extends CommonTestRestController {

    private static final String ALIVE = "alive";
    private static final String SELF = NetworkUtil.getHostname();
    private static final String NAME = "Policy API";
    private static final String APP_JSON = "application/json";
    private static final String APP_YAML = "application/yaml";

    private static final String HEALTHCHECK_ENDPOINT = "healthcheck";

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

    private static final String TOSCA_POLICY_VER_RESOURCE =
        "policytypes/onap.restart.tca.snapshot.yaml";
    // @formatter:off

    private static final String[] TOSCA_POLICY_RESOURCE_NAMES = {"policies/vCPE.policy.monitoring.input.tosca.json",
        "policies/vCPE.policy.monitoring.input.tosca.yaml", "policies/vDNS.policy.monitoring.input.tosca.json",
        "policies/vDNS.policy.monitoring.input.tosca.v2.yaml"};

    private static final String[] TOSCA_POLICIES_RESOURCE_NAMES = {
        "policies/vCPE.policies.optimization.input.tosca.json", "policies/vCPE.policies.optimization.input.tosca.yaml"};

    private static final String TOSCA_POLICYTYPE_OP_RESOURCE =
        "policytypes/onap.policies.controlloop.operational.Common.yaml";

    private static final String TOSCA_POLICYTYPE_VER_RESOURCE =
        "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.snapshot.yaml";

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
    private static SelfSignedKeyStore keystore;

    @LocalServerPort
    private int apiPort;

    /**
     * Initializes parameters and set up test environment.
     *
     * @throws IOException on I/O exceptions
     * @throws InterruptedException if interrupted
     */
    @BeforeAll
    static void setupParameters() throws IOException, InterruptedException {
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
    void testSwagger() throws Exception {
        super.testSwagger(apiPort);
    }

    @Test
    void testCreatePolicyTypes() throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName, apiPort);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().isEmpty());
        }

        // Send a policy type with a null value to trigger an error
        Response rawResponse = readResource(POLICYTYPES, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        String firstPolicyType = response.getPolicyTypes().keySet().iterator().next();
        response.getPolicyTypes().put(firstPolicyType, null);
        Response rawResponse2 = createResource(POLICYTYPES, standardCoder.encode(response), apiPort);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse2.getStatus());
        ErrorResponse errorResponse = rawResponse2.readEntity(ErrorResponse.class);
        assertEquals("no policy types specified on service template", errorResponse.getErrorMessage());
    }

    @Test
    void testCreatePolicies() throws Exception {
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, apiPort);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        new File("src/test/resources/policies/BadTestPolicy.yaml").deleteOnExit();

        // Send a policy with no policy type trigger an error
        String toscaPolicy = ResourceUtils
                .getResourceAsString(TOSCA_POLICY_RESOURCE_NAMES[TOSCA_POLICIES_RESOURCE_NAMES.length - 1]);

        toscaPolicy = toscaPolicy.replaceAll("onap.policies.monitoring.tcagen2", "IDontExist");
        TextFileUtils.putStringAsTextFile(toscaPolicy, "src/test/resources/policies/BadTestPolicy.yaml");

        Response rawResponse2 =
                createResource(POLICYTYPES_TCA_POLICIES,
                    "src/test/resources/policies/BadTestPolicy.yaml", apiPort);
        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), rawResponse2.getStatus());
        ErrorResponse errorResponse = rawResponse2.readEntity(ErrorResponse.class);
        assertThat(errorResponse.getErrorMessage())
                .contains("item \"entity\" value \"onap.restart.tca:1.0.0\" INVALID, does not equal existing entity");
    }

    @Test
    void testSimpleCreatePolicies() throws Exception {
        for (String resrcName : TOSCA_POLICIES_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICIES, resrcName, apiPort);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        new File("src/test/resources/policies/BadTestPolicy.yaml").deleteOnExit();

        // Send a policy with no policy type trigger an error
        String toscaPolicy = ResourceUtils
                .getResourceAsString(TOSCA_POLICY_RESOURCE_NAMES[TOSCA_POLICIES_RESOURCE_NAMES.length - 1]);

        toscaPolicy = toscaPolicy.replaceAll("onap.policies.monitoring.tcagen2", "IDontExist");
        toscaPolicy = toscaPolicy.replaceAll("onap.restart.tca", "onap.restart.tca.IDontExist");
        TextFileUtils.putStringAsTextFile(toscaPolicy, "src/test/resources/policies/BadTestPolicy.yaml");

        Response rawResponse2 =
            createResource(POLICIES, "src/test/resources/policies/BadTestPolicy.yaml", apiPort);
        ErrorResponse errorResponse = rawResponse2.readEntity(ErrorResponse.class);
        assertEquals(Response.Status.NOT_ACCEPTABLE.getStatusCode(), rawResponse2.getStatus());
        assertThat(errorResponse.getErrorMessage())
                .contains("item \"policy type\" value \"IDontExist:1.0.0\" INVALID, not found");
    }

    @Test
    void testPoliciesVersioning() throws Exception {
        var rawResponse = createResource(POLICYTYPES, TOSCA_POLICYTYPE_VER_RESOURCE, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICIES, TOSCA_POLICY_VER_RESOURCE, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testToscaCompliantOpDroolsPolicies() throws Exception {
        Response rawResponse = createResource(POLICYTYPES, TOSCA_POLICYTYPE_OP_RESOURCE, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_VERSION, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICIES, TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICIES, TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_YAML, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = deleteResource(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICIES, TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_YAML, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION, APP_YAML, apiPort);
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

        rawResponse = deleteResource(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION, APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    void testHealthCheckSuccessJson() throws Exception {
        testHealthCheckSuccess(APP_JSON);
    }

    @Test
    void testHealthCheckSuccessYaml() throws Exception {
        testHealthCheckSuccess(APP_YAML);
    }

    private void testHealthCheckSuccess(String mediaType) throws Exception {
        final Invocation.Builder invocationBuilder = sendHttpsRequest(
                HEALTHCHECK_ENDPOINT, mediaType, apiPort);
        final HealthCheckReport report = invocationBuilder.get(HealthCheckReport.class);
        validateHealthCheckReport(NAME, SELF, true, 200, ALIVE, report);
    }

    @Test
    void testReadPolicyTypesJson() throws Exception {
        testReadPolicyTypes(APP_JSON);
    }

    @Test
    void testReadPolicyTypesYaml() throws Exception {
        testReadPolicyTypes(APP_YAML);
    }

    private void testReadPolicyTypes(String mediaType) throws Exception {
        Response rawResponse =
            readResource("policytypes/onap.policies.optimization.resource.HpaPolicy", mediaType,
                apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertNotNull(namingServiceTemplate);
        assertEquals(3, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(5, namingServiceTemplate.getDataTypesAsMap().size());

        rawResponse = readResource(POLICYTYPES, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertFalse(response.getPolicyTypes().isEmpty());

        rawResponse = readResource(POLICYTYPES_TCA, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_VERSION, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_LATEST, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_COLLECTOR, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_COLLECTOR_VERSION, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_COLLECTOR_LATEST, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_VERSION, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_DROOLS_VERSION_LATEST, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_NAMING_VERSION, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    void testDeletePolicyTypeJson() throws Exception {
        testDeletePolicyType(APP_JSON);
    }

    @Test
    void testDeletePolicyTypeYaml() throws Exception {
        testDeletePolicyType(APP_YAML);
    }

    private void testDeletePolicyType(String mediaType) throws Exception {
        Response rawResponse = deleteResource("policytypes/onap.policies.IDoNotExist/versions/1.0.0",
            mediaType, apiPort);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());

        rawResponse = createResource(POLICYTYPES, "policytypes/onap.policies.Test.yaml", apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse =
            readResource("policytypes/onap.policies.Test/versions/1.0.0", mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse =
            deleteResource("policytypes/onap.policies.Test/versions/1.0.0", mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse =
            readResource("policytypes/onap.policies.Test/versions/1.0.0", mediaType, apiPort);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    void testReadPoliciesJson() throws Exception {
        testReadPolicies(APP_JSON);
    }

    @Test
    void testReadPoliciesYaml() throws Exception {
        testReadPolicies(APP_YAML);
    }

    private void testReadPolicies(String mediaType) throws Exception {
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, apiPort);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }

        Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_LATEST, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

    }

    @Test
    void testNamingPolicyGet() throws Exception {

        Response rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/1.0.0", APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/1.0.0?mode=referenced", APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        ToscaServiceTemplate namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(3, namingServiceTemplate.getDataTypesAsMap().size());

        rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/latest?mode=referenced", APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(3, namingServiceTemplate.getDataTypesAsMap().size());

        rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/policies"
                + "?mode=referenced", APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, namingServiceTemplate.getPolicyTypesAsMap().size());
        assertEquals(3, namingServiceTemplate.getDataTypesAsMap().size());

        rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/1.0.0", APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);

        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertNull(namingServiceTemplate.getPolicyTypes());
        assertNull(namingServiceTemplate.getDataTypes());

        rawResponse = readResource("policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/latest", APP_JSON, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertNull(namingServiceTemplate.getPolicyTypes());
        assertNull(namingServiceTemplate.getDataTypes());

        rawResponse =
            readResource("policytypes/onap.policies.Naming/versions/1.0.0/policies", APP_JSON,
                apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        namingServiceTemplate = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertEquals(1, namingServiceTemplate.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertNull(namingServiceTemplate.getPolicyTypes());
        assertNull(namingServiceTemplate.getDataTypes());
    }

    @Test
    void testDeletePoliciesJson() throws Exception {
        testDeletePolicies(APP_JSON);
    }

    @Test
    void testDeletePoliciesYaml() throws Exception {
        testDeletePolicies(APP_YAML);
    }

    private void testDeletePolicies(String mediaType) throws Exception {
        Response rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType, apiPort);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse error = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("policy onap.restart.tca:1.0.0 not found", error.getErrorMessage());
    }

    @Test
    void testDeletePolicyVersionJson() throws Exception {
        testDeletePolicyVersion(APP_JSON);
    }

    @Test
    void testDeletePolicyVersionYaml() throws Exception {
        testDeletePolicyVersion(APP_YAML);
    }

    private void testDeletePolicyVersion(String mediaType) throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName, apiPort);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().isEmpty());
        }
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, apiPort);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
        Response rawResponse = deleteResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1, mediaType, apiPort);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        ErrorResponse errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("policies for onap.restart.tca:1.0.0 do not exist", errorResponse.getErrorMessage());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE, mediaType, apiPort);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("policies for onap.restart.tca:null do not exist", errorResponse.getErrorMessage());

        rawResponse = readResource(POLICYTYPES_TCA_POLICIES_VCPE_LATEST, mediaType, apiPort);
        assertEquals(Response.Status.NOT_FOUND.getStatusCode(), rawResponse.getStatus());
        errorResponse = rawResponse.readEntity(ErrorResponse.class);
        assertEquals("policies for onap.restart.tca:null do not exist", errorResponse.getErrorMessage());
    }

    @Test
    void testGetAllVersionOfPolicyJson() throws Exception {
        testGetAllVersionOfPolicy(APP_JSON);
    }

    @Test
    void testGetAllVersionOfPolicyYaml() throws Exception {
        testGetAllVersionOfPolicy(APP_YAML);
    }

    private void testGetAllVersionOfPolicy(String mediaType) throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName, apiPort);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertNotNull(response);
            assertFalse(response.getPolicyTypes().isEmpty());
        }
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, apiPort);
            assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
        }
        Response rawResponse = readResource(POLICYTYPES_TCA_POLICIES, mediaType, apiPort);
        assertEquals(Response.Status.OK.getStatusCode(), rawResponse.getStatus());
    }

    @Test
    void testGetPoliciesJson() throws Exception {
        getPolicies(APP_JSON);
    }

    @Test
    void testGetPoliciesYaml() throws Exception {
        getPolicies(APP_YAML);
    }

    private void getPolicies(String mediaType) throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName, apiPort);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertThat(response).isNotNull();
            assertThat(response.getPolicyTypes()).isNotEmpty();
        }
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, apiPort);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        }
        Response rawResponse = readResource(POLICIES, mediaType, apiPort);
        assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertThat(response.getToscaTopologyTemplate().getPolicies()).isNotEmpty();
    }

    @Test
    void testGetSpecificPolicyJson() throws Exception {
        getSpecificPolicy(APP_JSON);
    }

    @Test
    void testGetSpecificPolicyYaml() throws Exception {
        getSpecificPolicy(APP_YAML);
    }

    private void getSpecificPolicy(String mediaType) throws Exception {
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES, resrcName, apiPort);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertThat(response).isNotNull();
            assertThat(response.getPolicyTypes()).isNotEmpty();
        }
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            Response rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, apiPort);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        }
        Response rawResponse = readResource(POLICIES_VCPE_VERSION1, mediaType, apiPort);
        assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
        assertThat(response.getToscaTopologyTemplate().getPolicies()).hasSize(1);
    }

    @Test
    void testDeleteSpecificPolicy() throws Exception {
        Response rawResponse;
        for (String resrcName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            rawResponse = createResource(POLICYTYPES, resrcName, apiPort);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
            ToscaServiceTemplate response = rawResponse.readEntity(ToscaServiceTemplate.class);
            assertThat(response).isNotNull();
            assertThat(response.getPolicyTypes()).isNotEmpty();
        }
        for (String resrcName : TOSCA_POLICY_RESOURCE_NAMES) {
            rawResponse = createResource(POLICYTYPES_TCA_POLICIES, resrcName, apiPort);
            assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
        }

        rawResponse = readResource(POLICIES_VCPE_VERSION1, APP_JSON, apiPort);
        assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        // delete a particular policy
        rawResponse = deleteResource(POLICIES_VCPE_VERSION1, APP_JSON, apiPort);
        assertThat(rawResponse.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());

        rawResponse = readResource(POLICIES_VCPE_VERSION1, APP_JSON, apiPort);
        assertThat(rawResponse.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

        rawResponse = deleteResource(POLICIES_VCPE_VERSION1, APP_JSON, apiPort);
        assertThat(rawResponse.getStatus()).isEqualTo(Status.NOT_FOUND.getStatusCode());

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
