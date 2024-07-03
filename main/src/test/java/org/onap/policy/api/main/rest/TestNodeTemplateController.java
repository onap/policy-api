/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2022-2024 Nordix Foundation. All rights reserved.
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import jakarta.ws.rs.core.Response;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import org.onap.policy.api.main.service.ToscaServiceTemplateService;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
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

@WebMvcTest(controllers = NodeTemplateController.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@ActiveProfiles({"default", "test-mvc"})
class TestNodeTemplateController {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private ToscaServiceTemplateService toscaServiceTemplateService;

    AutoCloseable autoCloseable;

    private static final PfModelException PF_MODEL_EXCEPTION =
        new PfModelException(Response.Status.BAD_REQUEST, "Error");

    @BeforeEach
    void setUp(@Autowired WebApplicationContext context) {
        autoCloseable = MockitoAnnotations.openMocks(this);
        mvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @AfterEach
    void tearDown() throws Exception {
        autoCloseable.close();
    }

    @Test
    void createToscaNodeTemplates() throws Exception {
        var body = ResourceUtils.getResourceAsString("nodetemplates/nodetemplates.metadatasets.input.tosca.json");
        var createRequest = post("/nodetemplates").contentType(MediaType.APPLICATION_JSON).content(body);
        when(toscaServiceTemplateService.createToscaNodeTemplates(any(ToscaServiceTemplate.class)))
            .thenReturn(new ToscaServiceTemplate());
        mvc.perform(createRequest).andExpect(status().isCreated());
    }

    @Test
    void createToscaNodeTemplates_Exception() throws Exception {
        var body = ResourceUtils.getResourceAsString("nodetemplates/nodetemplates.metadatasets.input.tosca.json");
        var createRequest = post("/nodetemplates").contentType(MediaType.APPLICATION_JSON).content(body);
        when(toscaServiceTemplateService.createToscaNodeTemplates(any(ToscaServiceTemplate.class)))
            .thenThrow(PF_MODEL_EXCEPTION);
        mvc.perform(createRequest).andExpect(status().isBadRequest());
    }

    @Test
    void updateToscaNodeTemplates() throws Exception {
        var body = ResourceUtils.getResourceAsString("nodetemplates/nodetemplates.metadatasets.input.tosca.json");
        var updateRequest = put("/nodetemplates").contentType(MediaType.APPLICATION_JSON).content(body);
        when(toscaServiceTemplateService.updateToscaNodeTemplates(any(ToscaServiceTemplate.class)))
            .thenReturn(new ToscaServiceTemplate());
        mvc.perform(updateRequest).andExpect(status().isOk());
    }

    @Test
    void updateToscaNodeTemplates_Exception() throws Exception {
        var body = ResourceUtils.getResourceAsString("nodetemplates/nodetemplates.metadatasets.input.tosca.json");
        var updateRequest = put("/nodetemplates").contentType(MediaType.APPLICATION_JSON).content(body);
        when(toscaServiceTemplateService.updateToscaNodeTemplates(any(ToscaServiceTemplate.class)))
            .thenThrow(PF_MODEL_EXCEPTION);
        mvc.perform(updateRequest).andExpect(status().isBadRequest());
    }

    @Test
    void deleteToscaNodeTemplates() throws Exception {
        var deleteRequest = delete("/nodetemplates/nodeName/versions/nodeVersion")
            .accept(MediaType.APPLICATION_JSON);
        when(toscaServiceTemplateService.deleteToscaNodeTemplate("nodeName", "nodeVersion"))
            .thenReturn(new ToscaServiceTemplate());
        mvc.perform(deleteRequest).andExpect(status().isOk());
    }

    @Test
    void deleteToscaNodeTemplates_Exception() throws Exception {
        var deleteRequest = delete("/nodetemplates/nodeName/versions/nodeVersion")
            .accept(MediaType.APPLICATION_JSON);
        when(toscaServiceTemplateService.deleteToscaNodeTemplate("nodeName", "nodeVersion"))
            .thenThrow(PF_MODEL_EXCEPTION);
        mvc.perform(deleteRequest).andExpect(status().isBadRequest());
    }

    @Test
    void getSpecificVersionOfNodeTemplate() throws Exception {
        var getRequest = get("/nodetemplates/nodeName/versions/nodeVersion")
            .accept(MediaType.APPLICATION_JSON);
        when(toscaServiceTemplateService.fetchToscaNodeTemplates("nodeName", "nodeVersion"))
            .thenReturn(List.of(new ToscaNodeTemplate()));
        mvc.perform(getRequest).andExpect(status().isOk());
    }

    @Test
    void getSpecificVersionOfNodeTemplate_Exception() throws Exception {
        var getRequest = get("/nodetemplates/nodeName/versions/nodeVersion")
            .accept(MediaType.APPLICATION_JSON);
        when(toscaServiceTemplateService.fetchToscaNodeTemplates("nodeName", "nodeVersion"))
            .thenThrow(PF_MODEL_EXCEPTION);
        mvc.perform(getRequest).andExpect(status().isBadRequest());
    }

    @Test
    void getAllNodeTemplates() throws Exception {
        var getRequest = get("/nodetemplates")
            .accept(MediaType.APPLICATION_JSON);
        when(toscaServiceTemplateService.fetchToscaNodeTemplates(null, null))
            .thenReturn(List.of(new ToscaNodeTemplate()));
        mvc.perform(getRequest).andExpect(status().isOk());
    }

    @Test
    void getAllNodeTemplates_Exception() throws Exception {
        var getRequest = get("/nodetemplates")
            .accept(MediaType.APPLICATION_JSON);
        when(toscaServiceTemplateService.fetchToscaNodeTemplates(null, null))
            .thenThrow(PF_MODEL_EXCEPTION);
        mvc.perform(getRequest).andExpect(status().isBadRequest());
    }
}