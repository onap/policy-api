/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2022 Nordix Foundation.
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

import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.onap.policy.api.main.exception.PolicyApiRuntimeException;
import org.onap.policy.api.main.rest.genapi.ToscaNodeTemplateDesignApi;
import org.onap.policy.api.main.service.ToscaServiceTemplateService;
import org.onap.policy.common.endpoints.event.comm.Topic;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class to provide REST API services for Tosca Node templates.
 */
@RestController
@RequiredArgsConstructor
public class NodeTemplateController extends CommonRestController implements ToscaNodeTemplateDesignApi {

    private final ToscaServiceTemplateService toscaServiceTemplateService;

    /**
     * Creates one or more new tosca node templates in one call.
     *
     * @param body the body of the node templates in TOSCA definition
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> createToscaNodeTemplates(ToscaServiceTemplate body, UUID requestId) {

        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(NetLoggerUtil.EventType.IN, Topic.CommInfrastructure.REST, "/nodetemplates",
                toJson(body));
        }
        try {
            ToscaServiceTemplate nodeTemplates = toscaServiceTemplateService.createToscaNodeTemplates(body);
            return makeOkResponse(requestId, nodeTemplates);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "POST /nodetemplates";
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Updates one or more node templates in one call.
     *
     * @param body the body of the node templates in TOSCA definition
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> updateToscaNodeTemplates(ToscaServiceTemplate body, UUID requestId) {

        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(NetLoggerUtil.EventType.IN, Topic.CommInfrastructure.REST, "/nodetemplates",
                toJson(body));
        }
        try {
            ToscaServiceTemplate nodeTemplates = toscaServiceTemplateService.updateToscaNodeTemplates(body);
            return makeOkResponse(requestId, nodeTemplates);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "PUT /nodetemplates";
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Deletes a node template with specific name and version.
     *
     * @param name the name of node template
     * @param version the version of node template
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<ToscaServiceTemplate> deleteToscaNodeTemplates(String name, String version, UUID requestId) {
        try {
            ToscaServiceTemplate nodeTemplates = toscaServiceTemplateService.deleteToscaNodeTemplate(name, version);
            return makeOkResponse(requestId, nodeTemplates);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = String.format("DELETE /nodetemplates/%s/versions/%s", name, version);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves the specified version of a node template.
     *
     * @param name the name of the node template
     * @param version the version of the node template
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<List<ToscaNodeTemplate>> getSpecificVersionOfNodeTemplate(String name, String version,
        UUID requestId) {
        try {
            List<ToscaNodeTemplate> nodeTemplates = toscaServiceTemplateService.fetchToscaNodeTemplates(name, version);
            return makeOkResponse(requestId, nodeTemplates);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /nodetemplates/%s/versions/%s", name, version);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves all the node templates from the tosca service template.
     *
     * @return the Response object containing the results of the API operation
     */
    @Override
    public ResponseEntity<List<ToscaNodeTemplate>> getAllNodeTemplates(UUID requestId) {
        try {
            List<ToscaNodeTemplate> nodeTemplates = toscaServiceTemplateService.fetchToscaNodeTemplates(null, null);
            return makeOkResponse(requestId, nodeTemplates);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = "GET /nodetemplates";
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }
}
