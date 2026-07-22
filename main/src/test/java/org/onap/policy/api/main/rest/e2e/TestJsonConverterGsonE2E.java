/*-
 * ============LICENSE_START=======================================================
 * Copyright (C) 2026 Deutsche Telekom AG. All rights reserved.
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

import org.junit.jupiter.api.Test;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;

/**
 * Regression test for the JSON HttpMessageConverter used by the Policy API.
 *
 * <p>The TOSCA model classes are bound with Gson {@code @SerializedName} annotations that map
 * camelCase Java fields to the snake_case JSON keys the Policy API contract uses (for example
 * {@code toscaTopologyTemplate} &rarr; {@code topology_template}). If Jackson is selected as the
 * JSON converter instead of Gson it binds by field name, so a request body with {@code
 * topology_template} is not bound and the API rejects the create with "topology template not
 * specified on service template" (HTTP 404) — the failure observed in the gating pipeline after the
 * Spring Boot 4 upgrade.
 *
 * <p>The regression was masked because the property that selects the JSON mapper was renamed in
 * Spring Boot 4 from {@code spring.mvc.converters.preferred-json-mapper} to {@code
 * spring.http.converters.preferred-json-mapper}; a config still using the old name is silently
 * ignored and Boot falls back to Jackson. To prove the fix is robust and property-independent this
 * test explicitly sets the mapper preference to {@code jackson} and still requires the snake_case
 * (Gson) contract to hold, because {@link org.onap.policy.api.main.config.WebConfig} now registers
 * the Gson converter explicitly rather than relying on the property.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles({"default", "test"})
@AutoConfigureWebTestClient
// Deliberately do NOT let the property pick Gson: the fix must win regardless.
@TestPropertySource(properties = "spring.http.converters.preferred-json-mapper=jackson")
class TestJsonConverterGsonE2E {

    private static final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;

    // A TOSCA policy type and a policy (both authored with snake_case keys), served from
    // policy-models-examples on the test classpath.
    private static final String DROOLS_POLICYTYPE =
        "policytypes/onap.policies.controlloop.operational.common.Drools.yaml";
    private static final String DROOLS_POLICY_JSON = "policies/vCPE.policy.operational.input.tosca.json";

    private static final String POLICYTYPES = "/policytypes";
    private static final String POLICIES = "/policies";
    private static final String DROOLS_POLICY_URI =
        "/policytypes/onap.policies.controlloop.operational.common.Drools/versions/1.0.0/policies";

    @Autowired
    WebTestClient webClient;

    private WebTestClient authClient() {
        var filter = ExchangeFilterFunctions.basicAuthentication("policyadmin", "zb!XztG34");
        return webClient.mutate().filter(filter).build();
    }

    @Test
    void createDroolsPolicyWithSnakeCaseJsonBody() {
        var client = authClient();

        // The policy type must exist before a policy of that type can be created.
        yamlAsPost(client, POLICYTYPES, DROOLS_POLICYTYPE).expectStatus().is2xxSuccessful();

        // The heart of the regression: a JSON body using snake_case keys (topology_template,
        // tosca_definitions_version, type_version) must be accepted -> 201 Created. With Jackson as
        // the JSON converter these keys are dropped and the API answers 404 "topology template not
        // specified on service template".
        var body = ResourceUtils.getResourceAsString(DROOLS_POLICY_JSON);
        assertThat(body).contains("\"topology_template\"");

        client.post().uri(POLICIES).contentType(APPLICATION_JSON).bodyValue(body)
            .exchange()
            .expectStatus().isCreated();

        // And the response the API renders must itself be snake_case (Gson output), not camelCase.
        var responseBody = new String(client.get().uri(DROOLS_POLICY_URI).accept(APPLICATION_JSON)
            .exchange()
            .expectStatus().isOk()
            .expectBody().returnResult().getResponseBodyContent());

        assertThat(responseBody)
            .contains("\"topology_template\"")
            .doesNotContain("toscaTopologyTemplate");
    }

    private WebTestClient.ResponseSpec yamlAsPost(WebTestClient client, String uri, String resource) {
        var yaml = new MediaType("application", "yaml");
        return client.post().uri(uri).contentType(yaml)
            .bodyValue(ResourceUtils.getResourceAsString(resource))
            .exchange();
    }
}
