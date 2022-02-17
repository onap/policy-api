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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.Authorization;
import io.swagger.annotations.BasicAuthDefinition;
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.Info;
import io.swagger.annotations.ResponseHeader;
import io.swagger.annotations.SecurityDefinition;
import io.swagger.annotations.SwaggerDefinition;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.UUID;
import org.onap.policy.api.main.exception.PolicyApiRuntimeException;
import org.onap.policy.api.main.rest.provider.NodeTemplateProvider;
import org.onap.policy.common.endpoints.event.comm.Topic;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaNodeTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class to provide REST API services for Tosca Node templates.
 */
@RestController
@RequestMapping(path = "/nodetemplate/api/v1", produces = { "application/json", "application/yaml" })
@Api(value = "Tosca Node template Design API")
@SwaggerDefinition(
    info = @Info(
        description = "Tosca Node template Design API is publicly exposed for clients to Create/Read/Update/Delete"
            + " node templates which can be recognized"
            + " and executable by incorporated policy engines. It is an"
            + " independent component running rest service that takes all node templates design API calls"
            + " from clients and then assign them to different API working functions.",
        version = "1.0.0", title = "Tosca Node template Design",
        extensions = {@Extension(properties = {@ExtensionProperty(name = "planned-retirement-date", value = "tbd"),
            @ExtensionProperty(name = "component", value = "Policy Framework")})}),
    schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
    securityDefinition = @SecurityDefinition(basicAuthDefinitions = {@BasicAuthDefinition(key = "basicAuth")}))
public class NodeTemplateController extends CommonRestController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NodeTemplateController.class);

    private static final String EXTENSION_NAME = "interface info";

    private static final String API_VERSION_NAME = "api-version";
    private static final String API_VERSION = "1.0.0";

    private static final String LAST_MOD_NAME = "last-mod-release";

    private static final String AUTHORIZATION_TYPE = "basicAuth";

    private static final String VERSION_MINOR_NAME = "X-MinorVersion";
    private static final String VERSION_MINOR_DESCRIPTION =
        "Used to request or communicate a MINOR version back from the client"
            + " to the server, and from the server back to the client";

    private static final String VERSION_PATCH_NAME = "X-PatchVersion";
    private static final String VERSION_PATCH_DESCRIPTION = "Used only to communicate a PATCH version in a response for"
        + " troubleshooting purposes only, and will not be provided by" + " the client on request";

    private static final String VERSION_LATEST_NAME = "X-LatestVersion";
    private static final String VERSION_LATEST_DESCRIPTION = "Used only to communicate an API's latest version";

    private static final String REQUEST_ID_NAME = "X-ONAP-RequestID";
    private static final String REQUEST_ID_HDR_DESCRIPTION = "Used to track REST transactions for logging purpose";
    private static final String REQUEST_ID_PARAM_DESCRIPTION = "RequestID for http transaction";

    private static final String AUTHENTICATION_ERROR_MESSAGE = "Authentication Error";
    private static final String AUTHORIZATION_ERROR_MESSAGE = "Authorization Error";
    private static final String SERVER_ERROR_MESSAGE = "Internal Server Error";
    private static final String NOT_FOUND_MESSAGE = "Resource Not Found";
    private static final String INVALID_BODY_MESSAGE = "Invalid Body";
    private static final String INVALID_PAYLOAD_MESSAGE = "Not Acceptable Payload";

    @Autowired
    private NodeTemplateProvider provider;

    @Autowired
    private ApiStatisticsManager mgr;

    /**
     * Creates one or more new tosca node templates in one call.
     *
     * @param body the body of the node templates in TOSCA definition
     *
     * @return the Response object containing the results of the API operation
     */
    @PostMapping("/nodetemplates")
    @ApiOperation(value = "Create one or more new node templates",
        notes = "Client should provide TOSCA body of the new node templates",
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"nodeTemplate", },
        response = ToscaServiceTemplate.class,
        responseHeaders = {
            @ResponseHeader(name = VERSION_MINOR_NAME,
                description = VERSION_MINOR_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = VERSION_PATCH_NAME,
                description = VERSION_PATCH_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = VERSION_LATEST_NAME, description = VERSION_LATEST_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = REQUEST_ID_NAME,
                description = REQUEST_ID_HDR_DESCRIPTION, response = UUID.class)},
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Jakarta")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = INVALID_BODY_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_ACCEPTABLE, message = INVALID_PAYLOAD_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> createToscaNodeTemplates(
        @RequestHeader(name = REQUEST_ID_NAME, required = false) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @RequestBody @ApiParam(value = "Entity body of tosca node templates", required = true)
            ToscaServiceTemplate body) {

        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(NetLoggerUtil.EventType.IN, Topic.CommInfrastructure.REST, "/nodetemplates",
                toJson(body));
        }
        try {
            ToscaServiceTemplate nodeTemplates = provider.createNodeTemplates(body);
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
    @PutMapping("/nodetemplates")
    @ApiOperation(value = "Updates one or more new node templates",
        notes = "Client should provide TOSCA body of the updated node templates",
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"nodeTemplate", },
        response = ToscaServiceTemplate.class,
        responseHeaders = {
            @ResponseHeader(name = VERSION_MINOR_NAME,
                description = VERSION_MINOR_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = VERSION_PATCH_NAME,
                description = VERSION_PATCH_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = VERSION_LATEST_NAME, description = VERSION_LATEST_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = REQUEST_ID_NAME,
                description = REQUEST_ID_HDR_DESCRIPTION, response = UUID.class)},
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Jakarta")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = INVALID_BODY_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_ACCEPTABLE, message = INVALID_PAYLOAD_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> updateToscaNodeTemplates(
        @RequestHeader(name = REQUEST_ID_NAME, required = false) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @RequestBody @ApiParam(value = "Entity body of tosca node templates", required = true)
            ToscaServiceTemplate body) {

        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(NetLoggerUtil.EventType.IN, Topic.CommInfrastructure.REST, "/nodetemplates",
                toJson(body));
        }
        try {
            ToscaServiceTemplate nodeTemplates = provider.updateToscaNodeTemplates(body);
            return makeOkResponse(requestId, nodeTemplates);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "PUT /nodetemplates";
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }


    /**
     * Deletes a node template with specific name and version.
     *
     * @param name  the name of node template
     * @param version the version of node template
     * @return the Response object containing the results of the API operation
     */
    @DeleteMapping("/nodetemplates/{name}/versions/{version}")
    @ApiOperation(value = "Updates one or more new node templates",
        notes = "Client should provide TOSCA body of the updated node templates",
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"nodeTemplate", },
        response = ToscaServiceTemplate.class,
        responseHeaders = {
            @ResponseHeader(name = VERSION_MINOR_NAME,
                description = VERSION_MINOR_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = VERSION_PATCH_NAME,
                description = VERSION_PATCH_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = VERSION_LATEST_NAME, description = VERSION_LATEST_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = REQUEST_ID_NAME,
                description = REQUEST_ID_HDR_DESCRIPTION, response = UUID.class)},
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Jakarta")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = INVALID_BODY_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_ACCEPTABLE, message = INVALID_PAYLOAD_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> deleteToscaNodeTemplates(
        @PathVariable("name") @ApiParam(value = "Name of the node template", required = true) String name,
        @PathVariable("version") @ApiParam(value = "Version of the node template",
            required = true) String version,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        try {
            ToscaServiceTemplate nodeTemplates = provider.deleteToscaNodeTemplate(name, version);
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
    @GetMapping("/nodetemplates/{name}/versions/{version}")
    @ApiOperation(value = "Retrieve one version of a tosca node template",
        notes = "Returns a particular version of a node template",
        response = ToscaNodeTemplate.class,
        responseContainer = "List",
        responseHeaders = {
            @ResponseHeader(name = VERSION_MINOR_NAME,
                description = VERSION_MINOR_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = VERSION_PATCH_NAME,
                description = VERSION_PATCH_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = VERSION_LATEST_NAME, description = VERSION_LATEST_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = REQUEST_ID_NAME,
                description = REQUEST_ID_HDR_DESCRIPTION, response = UUID.class)},
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"nodeTemplates", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Jakarta")
            })
        }
    )
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)
    })
    public ResponseEntity<List<ToscaNodeTemplate>> getSpecificVersionOfNodeTemplate(
        @PathVariable("name") @ApiParam(value = "Name of the node template", required = true) String name,
        @PathVariable("version") @ApiParam(value = "Version of the node template",
            required = true) String version,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        try {
            List<ToscaNodeTemplate> nodeTemplates = provider.fetchToscaNodeTemplates(name, version);
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
    @GetMapping("/nodetemplates")
    @ApiOperation(value = "Retrieve all the available tosca node templates",
        notes = "Returns all the node templates from the service template",
        response = ToscaNodeTemplate.class,
        responseContainer = "List",
        responseHeaders = {
            @ResponseHeader(name = VERSION_MINOR_NAME,
                description = VERSION_MINOR_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = VERSION_PATCH_NAME,
                description = VERSION_PATCH_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = VERSION_LATEST_NAME, description = VERSION_LATEST_DESCRIPTION,
                response = String.class),
            @ResponseHeader(name = REQUEST_ID_NAME,
                description = REQUEST_ID_HDR_DESCRIPTION, response = UUID.class)},
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"nodeTemplates", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Jakarta")
            })
        }
    )
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)
    })
    public ResponseEntity<List<ToscaNodeTemplate>> getAllNodeTemplates(
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        try {
            List<ToscaNodeTemplate> nodeTemplates = provider.fetchToscaNodeTemplates(null, null);
            return makeOkResponse(requestId, nodeTemplates);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = "GET /nodetemplates";
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    @ExceptionHandler(value = {PolicyApiRuntimeException.class})
    protected ResponseEntity<Object> handleException(PolicyApiRuntimeException ex) {
        LOGGER.warn(ex.getMessage(), ex.getCause());
        return makeErrorResponse(ex.getRequestId(), ex.getErrorResponse(),
            ex.getErrorResponse().getResponseCode().getStatusCode());
    }

}
