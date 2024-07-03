/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2024 Nordix Foundation. All rights reserved.
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

package org.onap.policy.api.main.rest.e2e;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.File;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles({"default", "test"})
@AutoConfigureWebTestClient
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TestApiRestControllerE2E {

    @Autowired
    WebTestClient webClient;

    protected static final MediaType APPLICATION_YAML = new MediaType("application", "yaml");
    protected static final MediaType APPLICATION_JSON = new MediaType("application", "json");
    protected static final String OP_POLICY_NAME_VCPE = "operational.restart";

    protected static final String[] TOSCA_POLICYTYPE_RESOURCE_NAMES = {
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
    protected static final String POLICYTYPES = "/policytypes";
    protected static final String POLICYTYPES_TCA = "/policytypes/onap.policies.monitoring.tcagen2";
    protected static final String POLICYTYPES_COLLECTOR =
        "/policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server";
    protected static final String POLICYTYPES_TCA_VERSION =
        "/policytypes/onap.policies.monitoring.tcagen2/versions/1.0.0";
    protected static final String POLICYTYPES_TCA_LATEST =
        "/policytypes/onap.policies.monitoring.tcagen2/versions/latest";
    protected static final String POLICYTYPES_COLLECTOR_VERSION =
        "/policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server/versions/1.0.0";
    protected static final String POLICYTYPES_COLLECTOR_LATEST =
        "/policytypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server/versions/latest";

    protected static final String POLICYTYPES_DROOLS =
        "/policytypes/onap.policies.controlloop.operational.common.Drools";
    protected static final String POLICYTYPES_DROOLS_VERSION = POLICYTYPES_DROOLS + "/versions/1.0.0";
    protected static final String POLICYTYPES_DROOLS_VERSION_LATEST = POLICYTYPES_DROOLS + "/versions/latest";

    protected static final String POLICYTYPES_NAMING_VERSION = POLICYTYPES + "/onap.policies.Naming/versions/1.0.0";

    protected static final String POLICYTYPES_TCA_POLICIES =
        "/policytypes/onap.policies.monitoring.tcagen2/versions/1.0.0/policies";
    protected static final String POLICYTYPES_TCA_POLICIES_VCPE =
        "/policytypes/onap.policies.monitoring.tcagen2/versions/1.0.0/policies/onap.restart.tca";
    protected static final String POLICYTYPES_TCA_POLICIES_VCPE_VERSION1 = "/policytypes/"
        + "onap.policies.monitoring.tcagen2/versions/1.0.0/policies/onap.restart.tca/versions/1.0.0";
    protected static final String POLICYTYPES_TCA_POLICIES_VCPE_LATEST = "/policytypes/"
        + "onap.policies.monitoring.tcagen2/versions/1.0.0/policies/onap.restart.tca/versions/latest";

    protected static final String POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION =
        POLICYTYPES_DROOLS_VERSION + "/policies/" + OP_POLICY_NAME_VCPE + "/versions/1.0.0";

    protected static final String POLICIES = "/policies";

    protected static final String[] TOSCA_POLICY_RESOURCE_NAMES = {
        "policies/vCPE.policy.monitoring.input.tosca.json",
        "policies/vCPE.policy.monitoring.input.tosca.yaml",
        "policies/vDNS.policy.monitoring.input.tosca.json",
        "policies/vDNS.policy.monitoring.input.tosca.v2.yaml"};

    protected static final String[] TOSCA_POLICIES_RESOURCE_NAMES = {
        "policies/vCPE.policies.optimization.input.tosca.json",
        "policies/vCPE.policies.optimization.input.tosca.yaml"};

    private static final String TOSCA_POLICYTYPE_OP_RESOURCE =
        "policytypes/onap.policies.controlloop.operational.Common.yaml";

    protected static final String TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_JSON =
        "policies/vCPE.policy.operational.input.tosca.json";

    protected static final String TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_YAML =
        "policies/vCPE.policy.operational.input.tosca.yaml";

    protected static final String POLICIES_VCPE_VERSION1 = "/policies/onap.restart.tca/versions/1.0.0";

    @BeforeEach
    void beforeEach() {
        var filter = ExchangeFilterFunctions.basicAuthentication("policyadmin", "zb!XztG34");
        webClient = webClient.mutate().filter(filter).build();
    }

    @Order(1)
    @Test
    void createPolicyType() {
        for (String resourceName : TOSCA_POLICYTYPE_RESOURCE_NAMES) {
            performPostRequestIsCreated(resourceName, "/policytypes");
        }
    }

    @Order(2)
    @Test
    void createPolicyWithPolicyTypeAndVersion() {
        for (String resourceName : TOSCA_POLICY_RESOURCE_NAMES) {
            performPostRequestIsCreated(resourceName, POLICYTYPES_TCA_POLICIES);
        }

        // try for bad policy
        new File("src/test/resources/policies/BadTestPolicy.yaml").deleteOnExit();

        // Send a policy with no policy type trigger an error
        String toscaPolicy = ResourceUtils
            .getResourceAsString(TOSCA_POLICY_RESOURCE_NAMES[TOSCA_POLICIES_RESOURCE_NAMES.length - 1]);

        toscaPolicy = toscaPolicy.replaceAll("onap.policies.monitoring.tcagen2", "IDontExist");

        webClient.post().uri(POLICYTYPES_TCA_POLICIES).contentType(APPLICATION_YAML).bodyValue(toscaPolicy)
            .exchange().expectStatus().is4xxClientError();
    }

    @Order(3)
    @Test
    void createPoliciesWithPolicyEndpoint() {
        for (String resourceName : TOSCA_POLICIES_RESOURCE_NAMES) {
            performPostRequestIsCreated(resourceName, "/policies");
        }
    }

    @Order(4)
    @Test
    void testPoliciesVersioning() {
        var policyTypePath = "policytypes/onap.policies.monitoring.cdap.tca.hi.lo.app.snapshot.yaml";
        performPostRequestIsCreated(policyTypePath, "/policytypes");

        policyTypePath = "policytypes/onap.restart.tca.snapshot.yaml";
        performPostRequestIsCreated(policyTypePath, "/policies");
    }

    @Order(5)
    @Test
    void testToscaCompliantOpDroolsPolicies() {
        performPostRequestIsCreated(TOSCA_POLICYTYPE_OP_RESOURCE, "/policytypes");

        performGetRequest(POLICYTYPES_DROOLS_VERSION);

        performPostRequestIsCreated(TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_JSON, POLICIES);

        performPostRequestIsCreated(TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_YAML, POLICIES);

        performGetRequest(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION);

        performDeleteRequest(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION);

        performPostRequestIsCreated(TOSCA_POLICY_OP_DROOLS_VCPE_RESOURSE_YAML, POLICIES);

        performGetRequest(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION);

        var response = performGetRequestAndCollectResponse(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION);
        assertEquals(1, response.getToscaTopologyTemplate().getPolicies().size());
        assertEquals(OP_POLICY_NAME_VCPE,
            response.getToscaTopologyTemplate().getPolicies().get(0).get(OP_POLICY_NAME_VCPE).getName());
        Map<String, Object> props = response.getToscaTopologyTemplate().getPolicies()
            .get(0).get(OP_POLICY_NAME_VCPE).getProperties();
        assertNotNull(props);

        if (props.get("operations") instanceof List<?> operations) {
            assertEquals(1, operations.size());
            if (operations.get(0) instanceof Map<?, ?> operation) {
                assertEquals(props.get("trigger"), operation.get("id"));
                if (operation.get("operation") instanceof Map<?, ?> op) {
                    assertEquals("APPC", op.get("actor"));
                    assertEquals("Restart", op.get("operation"));
                }
            }

        }

        performDeleteRequest(POLICYTYPES_DROOLS_POLICIES_VCPE_VERSION);
    }

    @Test
    void getHealthCheck() {
        webClient.get().uri("/healthcheck").accept(APPLICATION_JSON)
            .exchange().expectStatus().isOk();
    }

    @Test
    void getAllPolicyTypes() {
        webClient.get().uri("/policytypes").accept(APPLICATION_JSON)
            .exchange().expectStatus().isOk();
    }

    @Test
    void getAllVersionsOfPolicyType() {
        var uri = "/policytypes/onap.policies.optimization.resource.HpaPolicy";
        var response = performGetRequestAndCollectResponse(uri);
        assertNotNull(response);
        assertEquals(3, response.getPolicyTypesAsMap().size());
        assertEquals(5, response.getDataTypesAsMap().size());

        performGetRequest(POLICYTYPES_TCA);
        performGetRequest(POLICYTYPES_COLLECTOR);
        performGetRequest(POLICYTYPES_DROOLS);
    }

    @Test
    void getSpecificVersionOfPolicyType() {
        performGetRequest(POLICYTYPES_TCA_VERSION);
        performGetRequest(POLICYTYPES_COLLECTOR_VERSION);
        performGetRequest(POLICYTYPES_DROOLS_VERSION);
        performGetRequest(POLICYTYPES_NAMING_VERSION);
    }

    @Test
    void getLatestVersionOfPolicyType() {
        performGetRequest(POLICYTYPES_TCA_LATEST);
        performGetRequest(POLICYTYPES_COLLECTOR_LATEST);
        performGetRequest(POLICYTYPES_DROOLS_VERSION_LATEST);

        webClient.get().uri("/policytypes/wrong/versions/latest")
            .accept(APPLICATION_JSON).exchange()
            .expectStatus().isNotFound();
    }

    @Test
    void deleteSpecificVersionOfPolicyType() {
        performPostRequestIsCreated("policytypes/onap.policies.Test.yaml", POLICYTYPES);
        var uri = "/policytypes/onap.policies.Test/versions/1.0.0";
        performGetRequest(uri);
        performDeleteRequest("/policytypes/onap.policies.Test/versions/1.0.0");

        // tried to delete again
        webClient.delete().uri(uri).exchange().expectStatus().isNotFound();
    }

    @Test
    void getPoliciesWithPolicyTypeAndVersionEndpoint() {
        for (String resourceName : TOSCA_POLICY_RESOURCE_NAMES) {
            performPostRequestIsCreated(resourceName, POLICYTYPES_TCA_POLICIES);
        }

        performGetRequest(POLICYTYPES_TCA_POLICIES);
        performGetRequest(POLICYTYPES_TCA_POLICIES);

        performGetRequest(POLICYTYPES_TCA_POLICIES_VCPE);
        performGetRequest(POLICYTYPES_TCA_POLICIES_VCPE);

        performGetRequest(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1);
        performGetRequest(POLICYTYPES_TCA_POLICIES_VCPE_VERSION1);

        performGetRequest(POLICYTYPES_TCA_POLICIES_VCPE_LATEST);
        performGetRequest(POLICYTYPES_TCA_POLICIES_VCPE_LATEST);
    }

    @Test
    void getPoliciesWithPolicyTypeAndVersionEndpoint_CheckResponses() {
        performGetRequestAndCollectResponse("/policytypes/onap.policies.Naming/versions/1.0.0/"
            + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/1.0.0");

        var response = performGetRequestAndCollectResponse(
            "/policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/1.0.0?mode=referenced");

        assertEquals(1, response.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, response.getPolicyTypesAsMap().size());
        assertEquals(3, response.getDataTypesAsMap().size());

        response = performGetRequestAndCollectResponse(
            "/policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/latest?mode=referenced");
        assertEquals(1, response.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, response.getPolicyTypesAsMap().size());
        assertEquals(3, response.getDataTypesAsMap().size());

        response = performGetRequestAndCollectResponse(
            "/policytypes/onap.policies.Naming/versions/1.0.0/policies"
                + "?mode=referenced");
        assertEquals(1, response.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertEquals(1, response.getPolicyTypesAsMap().size());
        assertEquals(3, response.getDataTypesAsMap().size());

        response = performGetRequestAndCollectResponse(
            "/policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/1.0.0");
        assertEquals(1, response.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertNull(response.getPolicyTypes());
        assertNull(response.getDataTypes());

        response = performGetRequestAndCollectResponse(
            "/policytypes/onap.policies.Naming/versions/1.0.0/"
                + "policies/SDNC_Policy.ONAP_NF_NAMING_TIMESTAMP/versions/latest");
        assertEquals(1, response.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertNull(response.getPolicyTypes());
        assertNull(response.getDataTypes());

        response = performGetRequestAndCollectResponse(
            "/policytypes/onap.policies.Naming/versions"
                + "/1.0.0/policies");
        assertEquals(1, response.getToscaTopologyTemplate().getPoliciesAsMap().size());
        assertNull(response.getPolicyTypes());
        assertNull(response.getDataTypes());
    }

    @Test
    void deleteSpecificVersionOfPolicy() {
        var policyTypeFile = "policytypes/onap.policies.monitoring.tcagen2.yaml";
        performPostRequestIsCreated(policyTypeFile, "/policytypes");

        for (String resourceName : TOSCA_POLICY_RESOURCE_NAMES) {
            performPostRequestIsCreated(resourceName, POLICYTYPES_TCA_POLICIES);
        }

        performDeleteRequest(POLICIES_VCPE_VERSION1);
        webClient.get().uri(POLICIES_VCPE_VERSION1).accept(APPLICATION_JSON)
            .exchange().expectStatus().isNotFound();
    }

    @Test
    void getPolicies() {
        var policyTypeFile = "policytypes/onap.policies.monitoring.tcagen2.yaml";
        performPostRequestIsCreated(policyTypeFile, "/policytypes");

        for (String resourceName : TOSCA_POLICY_RESOURCE_NAMES) {
            performPostRequestIsCreated(resourceName, POLICYTYPES_TCA_POLICIES);
        }

        var response = performGetRequestAndCollectResponse(POLICIES);
        assertThat(response.getToscaTopologyTemplate().getPolicies()).isNotEmpty();
    }

    @Test
    void getPolicies_FetchTypeInvalid() {
        webClient.get().uri("/policies?mode=RANDOM").accept(APPLICATION_JSON, APPLICATION_YAML)
            .exchange().expectStatus().isOk();
    }

    @Test
    void getSpecificPolicy() {
        var policyTypeFile = "policytypes/onap.policies.monitoring.tcagen2.yaml";
        performPostRequestIsCreated(policyTypeFile, "/policytypes");

        for (String resourceName : TOSCA_POLICY_RESOURCE_NAMES) {
            performPostRequestIsCreated(resourceName, POLICYTYPES_TCA_POLICIES);
        }

        var response = performGetRequestAndCollectResponse(POLICIES_VCPE_VERSION1);
        assertThat(response.getToscaTopologyTemplate().getPolicies()).hasSize(1);
    }

    protected MediaType getMediaType(String resourceName) {
        if (resourceName.endsWith(".json")) {
            return MediaType.APPLICATION_JSON;
        } else if (resourceName.endsWith(".yaml") || resourceName.endsWith(".yml")) {
            return APPLICATION_YAML;
        }
        return null;
    }

    private void performPostRequestIsCreated(String resourceName, String urlTemplate) {
        var mediaType = getMediaType(resourceName);
        mediaType = getMediaType(resourceName) == null ? APPLICATION_JSON : mediaType;
        var body = ResourceUtils.getResourceAsString(resourceName);

        webClient.post().uri(urlTemplate).contentType(mediaType)
            .bodyValue(body).exchange().expectStatus().isCreated();
    }

    private ToscaServiceTemplate performGetRequestAndCollectResponse(String urlTemplate) {
        var response = webClient.get().uri(urlTemplate).accept(APPLICATION_JSON, APPLICATION_YAML).exchange()
            .expectStatus().isOk().expectBody(String.class);
        assertNotNull(response.returnResult());
        var contentType = response.returnResult().getResponseHeaders().getContentType();
        assert contentType != null;
        ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();

        try {
            if (contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
                serviceTemplate = new StandardCoder()
                    .decode(response.returnResult().getResponseBody(), ToscaServiceTemplate.class);
            } else if (contentType.isCompatibleWith(APPLICATION_YAML)) {
                serviceTemplate = new StandardYamlCoder()
                    .decode(response.returnResult().getResponseBody(), ToscaServiceTemplate.class);
            }
        } catch (CoderException e) {
            throw new RuntimeException(e);
        }

        return serviceTemplate;
    }

    private void performGetRequest(String urlTemplate) {
        webClient.get().uri(urlTemplate).accept(APPLICATION_JSON, APPLICATION_YAML).exchange()
            .expectStatus().isOk();
    }

    private void performDeleteRequest(String urlTemplate) {
        webClient.delete().uri(urlTemplate).exchange().expectStatus().isOk();
    }
}