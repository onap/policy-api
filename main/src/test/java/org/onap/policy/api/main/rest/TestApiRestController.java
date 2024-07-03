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

package org.onap.policy.api.main.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.onap.policy.api.main.rest.provider.healthcheck.HealthCheckProvider;
import org.onap.policy.api.main.service.ToscaServiceTemplateService;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@WebMvcTest(controllers = ApiRestController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles({"default", "test-mvc"})
class TestApiRestController {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private HealthCheckProvider healthCheckProvider;

    @MockBean
    private ToscaServiceTemplateService toscaServiceTemplateService;

    AutoCloseable autoCloseable;

    private final PfModelRuntimeException pfException =
        new PfModelRuntimeException(Response.Status.BAD_REQUEST, "Error");

    private static final String SOME_POLICY_TYPE = "somePolicyType";
    private static final String SOME_POLICY_NAME = "somePolicyName";
    private static final String SOME_POLICY_TYPE_VERSION = "somePolicyTypeVersion";
    private static final String SOME_POLICY_VERSION = "somePolicyVersion";
    private static final String WRONG_POLICY_EVERYTHING = "wrong";

    private static final String URI_VALID_POLICY_TYPE_AND_VERSION =
        "/policytypes/" + SOME_POLICY_TYPE + "/versions/" + SOME_POLICY_TYPE_VERSION;
    private static final String URI_VALID_POLICY_TYPE_AND_VERSION_FOR_POLICIES =
        URI_VALID_POLICY_TYPE_AND_VERSION + "/policies";

    @BeforeEach
    void setUp(@Autowired WebApplicationContext context) {
        autoCloseable = MockitoAnnotations.openMocks(this);
        this.mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void getHealthCheck() throws Exception {
        var healthCheckReport = new HealthCheckReport();
        healthCheckReport.setCode(200);
        healthCheckReport.setHealthy(true);
        healthCheckReport.setMessage("Health check OK");
        healthCheckReport.setUrl("/healthcheck");
        given(healthCheckProvider.performHealthCheck()).willReturn(healthCheckReport);
        var response = "{\"url\":\"/healthcheck\",\"healthy\":true,\"code\":200,\"message\":\"Health check OK\"}";

        var getRequest = get("/healthcheck").accept(MediaType.APPLICATION_JSON_VALUE);
        mvc.perform(getRequest).andExpect(status().isOk()).andExpect(content().string(response));
    }

    @Test
    void getAllPolicyTypes() throws Exception {
        var policyType = new ToscaServiceTemplate();
        when(toscaServiceTemplateService.fetchPolicyTypes(null, null)).thenReturn(policyType);
        var getRequest = get("/policytypes").accept(MediaType.APPLICATION_JSON_VALUE);
        this.mvc.perform(getRequest).andExpect(status().isOk());

        given(toscaServiceTemplateService.fetchPolicyTypes(null, null)).willThrow(pfException);
        var getExceptionReq = get("/policytypes").accept(MediaType.APPLICATION_JSON_VALUE);
        this.mvc.perform(getExceptionReq).andExpect(status().isBadRequest());
    }

    @Test
    void getAllVersionsOfPolicyType() throws Exception {
        when(toscaServiceTemplateService.fetchPolicyTypes(SOME_POLICY_TYPE, null))
            .thenReturn(new ToscaServiceTemplate());
        var getRequest = get("/policytypes").accept(MediaType.APPLICATION_JSON_VALUE);
        this.mvc.perform(getRequest).andExpect(status().isOk());

        given(toscaServiceTemplateService.fetchPolicyTypes(WRONG_POLICY_EVERYTHING, null))
            .willThrow(pfException);
        this.mvc.perform(get("/policytypes/wrong").accept(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSpecificVersionOfPolicyType() throws Exception {
        when(toscaServiceTemplateService.fetchPolicyTypes(SOME_POLICY_TYPE, SOME_POLICY_TYPE_VERSION))
            .thenReturn(new ToscaServiceTemplate());
        this.mvc.perform(get(URI_VALID_POLICY_TYPE_AND_VERSION)
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

        when(toscaServiceTemplateService.fetchPolicyTypes(WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING))
            .thenThrow(new PfModelException(Response.Status.BAD_REQUEST, "Bad Request"));
        this.mvc.perform(get("/policytypes/wrong/versions/wrong")
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isBadRequest());
    }

    @Test
    void getLatestVersionOfPolicyType() throws Exception {
        when(toscaServiceTemplateService.fetchLatestPolicyTypes(SOME_POLICY_TYPE))
            .thenReturn(new ToscaServiceTemplate());
        this.mvc.perform(get("/policytypes/somePolicyType/versions/latest")
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isOk());

        given(toscaServiceTemplateService.fetchLatestPolicyTypes(WRONG_POLICY_EVERYTHING)).willThrow(pfException);
        this.mvc.perform(get("/policytypes/wrong/versions/latest")
            .accept(MediaType.APPLICATION_JSON_VALUE)).andExpect(status().isBadRequest());
    }

    @Test
    void createPolicyType() throws Exception {
        var toscaTemplate = new ToscaServiceTemplate();
        toscaTemplate.setName(SOME_POLICY_TYPE);
        toscaTemplate.setVersion(SOME_POLICY_TYPE_VERSION);
        when(toscaServiceTemplateService.createPolicyType(any())).thenReturn(toscaTemplate);

        var body = ResourceUtils.getResourceAsString("policytypes/onap.policies.Test.yaml");
        var postRequest = post("/policytypes").content(body).contentType("application/yaml");
        this.mvc.perform(postRequest).andExpect(status().isCreated());
    }

    @Test
    void createPolicyType_Exception() throws Exception {
        when(toscaServiceTemplateService.createPolicyType(any()))
            .thenThrow(new PfModelRuntimeException(Response.Status.BAD_REQUEST, "Bad Request"));

        var body = ResourceUtils.getResourceAsString("policytypes/onap.policies.Test.yaml");
        var postRequest = post("/policytypes").content(body).contentType("application/yaml");
        this.mvc.perform(postRequest).andExpect(status().isBadRequest());
    }

    @Test
    void deleteSpecificVersionOfPolicyType() throws Exception {
        when(toscaServiceTemplateService.deletePolicyType(SOME_POLICY_TYPE, SOME_POLICY_TYPE_VERSION))
            .thenReturn(new ToscaServiceTemplate());
        var deleteRequest = delete(URI_VALID_POLICY_TYPE_AND_VERSION)
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(deleteRequest).andExpect(status().isOk());
    }

    @Test
    void deleteSpecificVersionOfPolicyType_Exception() throws Exception {
        when(toscaServiceTemplateService.deletePolicyType(WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING))
            .thenThrow(new PfModelRuntimeException(Response.Status.BAD_REQUEST, "Error"));
        var deleteExceptionReq = delete("/policytypes/wrong/versions/wrong")
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(deleteExceptionReq).andExpect(status().isBadRequest());
    }

    @Test
    void getAllPolicies() throws Exception {
        when(toscaServiceTemplateService.fetchPolicies(
            SOME_POLICY_TYPE, SOME_POLICY_TYPE_VERSION, null, null, PolicyFetchMode.BARE))
            .thenReturn(new ToscaServiceTemplate());
        var fetchPoliciesReq = get(URI_VALID_POLICY_TYPE_AND_VERSION_FOR_POLICIES)
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesReq).andExpect(status().isOk());

        given(toscaServiceTemplateService.fetchPolicies(
            WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING, null, null, PolicyFetchMode.REFERENCED))
            .willThrow(pfException);
        var fetchPoliciesExcReq = get("/policytypes/wrong/versions/wrong/policies?mode=REFERENCED")
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesExcReq).andExpect(status().isBadRequest());
    }

    @Test
    void getAllVersionsOfPolicy() throws Exception {
        when(toscaServiceTemplateService.fetchPolicies(
            SOME_POLICY_TYPE, SOME_POLICY_TYPE_VERSION, SOME_POLICY_NAME, null, PolicyFetchMode.BARE))
            .thenReturn(new ToscaServiceTemplate());
        var fetchPoliciesReq = get(URI_VALID_POLICY_TYPE_AND_VERSION_FOR_POLICIES + "/somePolicyName")
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesReq).andExpect(status().isOk());
    }

    @Test
    void getAllVersionsOfPolicy_Exception() throws Exception {
        given(toscaServiceTemplateService.fetchPolicies(
            WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING,
            null, PolicyFetchMode.REFERENCED)).willThrow(pfException);
        var fetchPoliciesExcReq = get("/policytypes/wrong/versions/wrong/policies/wrong?mode=REFERENCED")
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesExcReq).andExpect(status().isBadRequest());
    }

    @Test
    void getSpecificVersionOfPolicy() throws Exception {
        when(toscaServiceTemplateService.fetchPolicies(
            SOME_POLICY_TYPE, SOME_POLICY_TYPE_VERSION, SOME_POLICY_NAME, SOME_POLICY_VERSION, PolicyFetchMode.BARE))
            .thenReturn(new ToscaServiceTemplate());
        var fetchPoliciesReq =
            get(URI_VALID_POLICY_TYPE_AND_VERSION_FOR_POLICIES
                + "/somePolicyName/versions/somePolicyVersion").accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesReq).andExpect(status().isOk());

        given(toscaServiceTemplateService.fetchPolicies(
            WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING,
            WRONG_POLICY_EVERYTHING, PolicyFetchMode.REFERENCED)).willThrow(pfException);
        var fetchPoliciesExcReq = get("/policytypes/wrong/versions/wrong/policies/wrong/versions/wrong"
            + "?mode=REFERENCED").accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesExcReq).andExpect(status().isBadRequest());
    }

    @Test
    void getLatestVersionOfPolicy() throws Exception {
        when(toscaServiceTemplateService.fetchLatestPolicies(
            SOME_POLICY_TYPE, SOME_POLICY_TYPE_VERSION, SOME_POLICY_NAME, PolicyFetchMode.BARE))
            .thenReturn(new ToscaServiceTemplate());
        var fetchPoliciesReq = get(URI_VALID_POLICY_TYPE_AND_VERSION_FOR_POLICIES
            + "/somePolicyName/versions/latest").accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesReq).andExpect(status().isOk());
    }

    @Test
    void getLatestVersionOfPolicy_Exception() throws Exception {
        when(toscaServiceTemplateService.fetchLatestPolicies(WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING,
            WRONG_POLICY_EVERYTHING, null))
            .thenThrow(new PfModelException(Response.Status.BAD_REQUEST, "Error"));
        var fetchPoliciesExcReq = get("/policytypes/wrong/versions/wrong/policies/wrong/versions/latest");
        this.mvc.perform(fetchPoliciesExcReq).andExpect(status().isBadRequest());
    }

    @Test
    void createPolicy() throws Exception {
        var toscaTemplate = new ToscaServiceTemplate();
        toscaTemplate.setName(SOME_POLICY_NAME);
        toscaTemplate.setVersion(SOME_POLICY_VERSION);
        when(toscaServiceTemplateService.createPolicy(any())).thenReturn(toscaTemplate);

        var body = ResourceUtils.getResourceAsString("policies/vFirewall.policy.monitoring.input.tosca.v2.yaml");
        var postRequest = post(URI_VALID_POLICY_TYPE_AND_VERSION_FOR_POLICIES)
            .content(body).contentType("application/yaml");
        this.mvc.perform(postRequest).andExpect(status().isCreated());

        // exception scenario
        given(toscaServiceTemplateService.createPolicy(any())).willThrow(pfException);
        var postExceptionReq = post("/policytypes/wrong/versions/wrong/policies")
            .content(body).contentType("application/yaml");
        this.mvc.perform(postExceptionReq).andExpect(status().isBadRequest());
    }

    @Test
    void deleteSpecificVersionOfPolicy() throws Exception {
        when(toscaServiceTemplateService.deletePolicy(SOME_POLICY_NAME, SOME_POLICY_VERSION))
            .thenReturn(new ToscaServiceTemplate());
        var deleteRequest = delete(URI_VALID_POLICY_TYPE_AND_VERSION_FOR_POLICIES
            + "/somePolicyName/versions/somePolicyVersion")
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(deleteRequest).andExpect(status().isOk());
    }

    @Test
    void deleteSpecificVersionOfPolicy_Exception() throws Exception {
        given(toscaServiceTemplateService.deletePolicy(WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING))
            .willThrow(pfException);
        var deleteExceptionReq = delete("/policytypes/wrong/versions/wrong/policies/wrong/versions/wrong")
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(deleteExceptionReq).andExpect(status().isBadRequest());
    }

    @Test
    void getPolicies() throws Exception {
        when(toscaServiceTemplateService.fetchPolicies(null, null, null, null, PolicyFetchMode.BARE))
            .thenReturn(new ToscaServiceTemplate());
        var fetchPoliciesReq = get("/policies").accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesReq).andExpect(status().isOk());

        when(toscaServiceTemplateService.fetchPolicies(
            null, null, null, null, PolicyFetchMode.REFERENCED))
            .thenThrow(new PfModelRuntimeException(Response.Status.BAD_REQUEST, "Random error message"));
        var fetchPoliciesExcReq = get("/policies?mode=REFERENCED")
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesExcReq).andExpect(status().isBadRequest());
    }

    @Test
    void getPolicies_404Exception() throws Exception {
        given(toscaServiceTemplateService.fetchPolicies(
            null, null, null, null, PolicyFetchMode.REFERENCED))
            .willThrow(new PfModelRuntimeException(Response.Status.NOT_FOUND, "Random error message"));
        var fetchPoliciesExcReq = get("/policies?mode=REFERENCED")
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesExcReq).andExpect(status().isNotFound()).andExpect(content()
            .string("{\"code\":\"NOT_FOUND\",\"error\":\"No policies found\",\"details\":[\"No policies found\"]}"));
    }

    @Test
    void getSpecificPolicy() throws Exception {
        when(toscaServiceTemplateService.fetchPolicies(null, null,
            SOME_POLICY_NAME, SOME_POLICY_VERSION, PolicyFetchMode.BARE))
            .thenReturn(new ToscaServiceTemplate());
        var fetchPoliciesReq = get("/policies/somePolicyName/versions/somePolicyVersion")
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesReq).andExpect(status().isOk());

        given(toscaServiceTemplateService.fetchPolicies(null, null,
            WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING, PolicyFetchMode.REFERENCED))
            .willThrow(new PfModelRuntimeException(Response.Status.NOT_FOUND, "Random error message"));
        var fetchPoliciesExcReq = get("/policies/wrong/versions/wrong?mode=REFERENCED")
            .accept(MediaType.APPLICATION_JSON);
        this.mvc.perform(fetchPoliciesExcReq).andExpect(status().isNotFound());
    }

    @Test
    void createPolicies() throws Exception {
        var toscaTemplate = new ToscaServiceTemplate();
        toscaTemplate.setName(SOME_POLICY_NAME);
        toscaTemplate.setVersion(SOME_POLICY_VERSION);
        when(toscaServiceTemplateService.createPolicies(any())).thenReturn(toscaTemplate);

        var body = ResourceUtils.getResourceAsString("policies/vFirewall.policy.monitoring.input.tosca.v2.yaml");
        var postRequest = post("/policies").content(body).contentType("application/yaml");
        this.mvc.perform(postRequest).andExpect(status().isCreated());

        // exception scenario
        given(toscaServiceTemplateService.createPolicies(any())).willThrow(pfException);
        var postExceptionReq = post("/policies").content(body).contentType("application/yaml");
        this.mvc.perform(postExceptionReq).andExpect(status().isBadRequest());
    }

    @Test
    void deleteSpecificPolicy() throws Exception {
        when(toscaServiceTemplateService.deletePolicy(SOME_POLICY_NAME, SOME_POLICY_VERSION))
            .thenReturn(new ToscaServiceTemplate());
        var deleteRequest = delete("/policies/somePolicyName/versions/somePolicyVersion");
        this.mvc.perform(deleteRequest).andExpect(status().isOk());

        given(toscaServiceTemplateService.deletePolicy(WRONG_POLICY_EVERYTHING, WRONG_POLICY_EVERYTHING))
            .willThrow(pfException);
        var deleteExcReq = delete("/policies/wrong/versions/wrong");
        this.mvc.perform(deleteExcReq).andExpect(status().isBadRequest());
    }
}