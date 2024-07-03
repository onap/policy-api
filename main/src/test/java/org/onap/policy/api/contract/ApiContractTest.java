/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2023-2024 Nordix Foundation.
 *  Modifications Copyright (C) 2023 Bell Canada. All rights reserved.
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

package org.onap.policy.api.contract;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.onap.policy.api.main.PolicyApiApplication;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.ExchangeFilterFunctions;

@SpringBootTest(classes = PolicyApiApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles({"test", "stub"})
@AutoConfigureWebTestClient
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class ApiContractTest {

    @Autowired
    WebTestClient webClient;

    protected static final MediaType APPLICATION_YAML = new MediaType("application", "yaml");
    protected static final MediaType APPLICATION_JSON = new MediaType("application", "json");

    private static final String TOSCA_NODE_TEMPLATE_RESOURCE =
        "nodetemplates/nodetemplates.metadatasets.input.tosca.json";

    @BeforeEach
    void beforeEach() {
        var filter = ExchangeFilterFunctions.basicAuthentication("policyadmin", "zb!XztG34");
        webClient = webClient.mutate().filter(filter).build();
    }

    @Test
    void testStubPolicyDesign() {
        checkStubJsonGet("/policies");
        checkStubJsonGet("/policies/policyname/versions/1.0.2");
        checkStubJsonGet("/policytypes");
        checkStubJsonGet("/policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16");
        checkStubJsonGet("/policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/latest");
        checkStubJsonGet("/policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0");
        checkStubJsonGet("/policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/policies");
        checkStubJsonGet("/policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/policies/"
            + "9c65fa1f-2833-4076-a64d-5b62e35cd09b");
        checkStubJsonGet("/policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/policies/"
            + "9c65fa1f-2833-4076-a64d-5b62e35cd09b/versions/latest");
        checkStubJsonGet("/policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/policies/"
            + "9c65fa1f-2833-4076-a64d-5b62e35cd09b/versions/1.2.3");
        checkStubJsonGet("/healthcheck");

        checkStubJsonPost("/policies");
        checkStubJsonPost("/policytypes");
        checkStubJsonPost("/policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.2.3/policies");

        checkStubJsonDelete("/policies/policyname/versions/1.0.2");
        checkStubJsonDelete("/policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0");
        checkStubJsonDelete("/policytypes/380d5cb1-e43d-45b7-b10b-ebd15dfabd16/versions/1.0.0/policies/"
            + "9c65fa1f-2833-4076-a64d-5b62e35cd09b/versions/1.2.3");
    }

    @Test
    void testStubNodeTemplateDesign() {
        checkStubJsonGet("/nodetemplates");
        checkStubJsonGet("/nodetemplates/k8stemplate/versions/1.0.0");
        checkStubJsonPost("/nodetemplates");
        checkStubJsonPut();
        checkStubJsonDelete("/nodetemplates/k8stemplate/versions/1.0.0");
    }

    @Test
    void testErrors() {
        webClient.get().uri("/policies").accept(APPLICATION_YAML)
            .exchange().expectStatus().isEqualTo(HttpStatus.NOT_IMPLEMENTED);

        webClient.get().uri("/nodetemplates").accept(APPLICATION_YAML)
            .exchange().expectStatus().isEqualTo(HttpStatus.NOT_IMPLEMENTED);

    }

    private void checkStubJsonGet(String url) {
        webClient.get().uri(url).accept(APPLICATION_JSON)
            .exchange().expectStatus().isOk();
    }

    private void checkStubJsonPost(String url) {
        var body = ResourceUtils.getResourceAsString(TOSCA_NODE_TEMPLATE_RESOURCE);
        webClient.post().uri(url)
            .contentType(MediaType.APPLICATION_JSON).bodyValue(body)
            .accept(APPLICATION_JSON)
            .exchange().expectStatus().isCreated();
    }

    private void checkStubJsonPut() {
        var body = ResourceUtils.getResourceAsString(TOSCA_NODE_TEMPLATE_RESOURCE);
        webClient.put().uri("/nodetemplates")
            .contentType(APPLICATION_JSON).bodyValue(body)
            .accept(APPLICATION_JSON)
            .exchange().expectStatus().isOk();
    }

    private void checkStubJsonDelete(String url) {
        webClient.delete().uri(url).accept(APPLICATION_JSON)
            .exchange().expectStatus().isOk();
    }

}
