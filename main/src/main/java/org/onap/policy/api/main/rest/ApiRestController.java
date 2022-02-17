/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020,2022 Nordix Foundation.
 * Modifications Copyright (C) 2020-2022 Bell Canada. All rights reserved.
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
import javax.ws.rs.core.Response.Status;
import org.onap.policy.api.main.exception.PolicyApiRuntimeException;
import org.onap.policy.api.main.rest.provider.HealthCheckProvider;
import org.onap.policy.api.main.rest.provider.PolicyProvider;
import org.onap.policy.api.main.rest.provider.PolicyTypeProvider;
import org.onap.policy.api.main.rest.provider.StatisticsProvider;
import org.onap.policy.common.endpoints.event.comm.Topic.CommInfrastructure;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil;
import org.onap.policy.common.endpoints.utils.NetLoggerUtil.EventType;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Class to provide REST API services.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@RestController
@RequestMapping(path = "/policy/api/v1", produces = { "application/json", "application/yaml" })
@Api(value = "Policy Design API")
@SwaggerDefinition(
    info = @Info(
        description = "Policy Design API is publicly exposed for clients to Create/Read/Update/Delete"
            + " policy types, policy type implementation and policies which can be recognized"
            + " and executable by incorporated policy engines. It is an"
            + " independent component running rest service that takes all policy design API calls"
            + " from clients and then assign them to different API working functions. Besides"
            + " that, API is also exposed for clients to retrieve healthcheck status of this API"
            + " rest service and the statistics report including the counters of API invocation.",
        version = "1.0.0", title = "Policy Design",
        extensions = {@Extension(properties = {@ExtensionProperty(name = "planned-retirement-date", value = "tbd"),
            @ExtensionProperty(name = "component", value = "Policy Framework")})}),
    schemes = {SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS},
    securityDefinition = @SecurityDefinition(basicAuthDefinitions = {@BasicAuthDefinition(key = "basicAuth")}))
public class ApiRestController extends CommonRestController {

    private enum Target {
        POLICY,
        POLICY_TYPE,
        OTHER
    }

    @Autowired
    private PolicyProvider policyProvider;

    @Autowired
    private HealthCheckProvider healthCheckProvider;

    @Autowired
    private PolicyTypeProvider policyTypeProvider;

    @Autowired
    private ApiStatisticsManager mgr;

    @Autowired
    private StatisticsProvider statisticsProvider;

    /**
     * Retrieves the healthcheck status of the API component.
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/healthcheck")
    @ApiOperation(value = "Perform a system healthcheck", notes = "Returns healthy status of the Policy API component",
        response = HealthCheckReport.class,
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"HealthCheck", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<HealthCheckReport> getHealthCheck(
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        updateApiStatisticsCounter(Target.OTHER, HttpStatus.OK, HttpMethod.GET);
        return makeOkResponse(requestId, healthCheckProvider.performHealthCheck());
    }

    /**
     * Retrieves the statistics report of the API component.
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/statistics")
    @ApiOperation(value = "Retrieve current statistics",
        notes = "Returns current statistics including the counters of API invocation",
        response = StatisticsReport.class,
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Statistics", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<StatisticsReport> getStatistics(
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        updateApiStatisticsCounter(Target.OTHER, HttpStatus.OK, HttpMethod.GET);
        return makeOkResponse(requestId, statisticsProvider.fetchCurrentStatistics());
    }

    /**
     * Retrieves all available policy types.
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/policytypes")
    @ApiOperation(value = "Retrieve existing policy types",
        notes = "Returns a list of existing policy types stored in Policy Framework",
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"PolicyType", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> getAllPolicyTypes(
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.fetchPolicyTypes(null, null);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "GET /policytypes";
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves all versions of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/policytypes/{policyTypeId}")
    @ApiOperation(value = "Retrieve all available versions of a policy type",
        notes = "Returns a list of all available versions for the specified policy type",
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"PolicyType", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> getAllVersionsOfPolicyType(
        @PathVariable("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.fetchPolicyTypes(policyTypeId, null);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s", policyTypeId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves specified version of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     * @param versionId the version of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/policytypes/{policyTypeId}/versions/{versionId}")
    @ApiOperation(value = "Retrieve one particular version of a policy type",
        notes = "Returns a particular version for the specified policy type", response = ToscaServiceTemplate.class,
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"PolicyType", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> getSpecificVersionOfPolicyType(
        @PathVariable("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathVariable("versionId") @ApiParam(value = "Version of policy type", required = true) String versionId,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.fetchPolicyTypes(policyTypeId, versionId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s", policyTypeId, versionId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves latest version of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/policytypes/{policyTypeId}/versions/latest")
    @ApiOperation(value = "Retrieve latest version of a policy type",
        notes = "Returns latest version for the specified policy type", response = ToscaServiceTemplate.class,
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"PolicyType", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> getLatestVersionOfPolicyType(
        @PathVariable("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.fetchLatestPolicyTypes(policyTypeId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/latest", policyTypeId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Creates a new policy type.
     *
     * @param body the body of policy type following TOSCA definition
     *
     * @return the Response object containing the results of the API operation
     */
    @PostMapping("/policytypes")
    @ApiOperation(value = "Create a new policy type", notes = "Client should provide TOSCA body of the new policy type",
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"PolicyType", },
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
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = INVALID_BODY_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_ACCEPTABLE, message = INVALID_PAYLOAD_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> createPolicyType(
        @RequestBody @ApiParam(value = "Entity body of policy type", required = true) ToscaServiceTemplate body,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, "/policytypes", toJson(body));
        }
        try {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.createPolicyType(body);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.POST);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "POST /policytypes";
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.POST);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Deletes specified version of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     * @param versionId the version of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @DeleteMapping("/policytypes/{policyTypeId}/versions/{versionId}")
    @ApiOperation(value = "Delete one version of a policy type",
        notes = "Rule 1: pre-defined policy types cannot be deleted;"
            + "Rule 2: policy types that are in use (parameterized by a TOSCA policy) cannot be deleted."
            + "The parameterizing TOSCA policies must be deleted first;",
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"PolicyType", },
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
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = HTTP_CONFLICT_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificVersionOfPolicyType(
        @PathVariable("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathVariable("versionId") @ApiParam(value = "Version of policy type", required = true) String versionId,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.deletePolicyType(policyTypeId, versionId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.OK, HttpMethod.DELETE);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("DELETE /policytypes/%s/versions/%s", policyTypeId, versionId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.DELETE);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves all versions of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies")
    @ApiOperation(
        value = "Retrieve all versions of a policy created for a particular policy type version",
        notes = "Returns a list of all versions of specified policy created for the specified policy type version",
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Policy,"},
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")
            })
        }
    )
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)
    })
    public ResponseEntity<ToscaServiceTemplate> getAllPolicies(
        @PathVariable("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathVariable("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @RequestParam(name = "mode", defaultValue = "bare") @ApiParam("Fetch mode for policies, BARE for bare"
            + " policies (default), REFERENCED for fully referenced policies") PolicyFetchMode mode) {
        try {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchPolicies(policyTypeId, policyTypeVersion, null, null, mode);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s/policies", policyTypeId, policyTypeVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves all versions of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId the ID of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies/{policyId}")
    @ApiOperation(value = "Retrieve all version details of a policy created for a particular policy type version",
        notes = "Returns a list of all version details of the specified policy", response = ToscaServiceTemplate.class,
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Policy", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")
            })
        }
    )
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)
    })
    public ResponseEntity<ToscaServiceTemplate> getAllVersionsOfPolicy(
        @PathVariable("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathVariable("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @PathVariable("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
        @RequestHeader(name = REQUEST_ID_NAME, required = false) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @RequestParam(name = "mode", defaultValue = "bare")
        @ApiParam("Fetch mode for policies, BARE for bare policies (default),"
            + " REFERENCED for fully referenced policies") PolicyFetchMode mode) {
        try {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchPolicies(policyTypeId, policyTypeVersion, policyId, null, mode);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("/policytypes/%s/versions/$s/policies/%s",
                policyTypeId, policyTypeVersion, policyId);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves the specified version of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId the ID of specified policy
     * @param policyVersion the version of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies/{policyId}/versions/{policyVersion}")
    @ApiOperation(value = "Retrieve one version of a policy created for a particular policy type version",
        notes = "Returns a particular version of specified policy created for the specified policy type version",
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Policy", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")
            })
        }
    )
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)
    })
    public ResponseEntity<ToscaServiceTemplate> getSpecificVersionOfPolicy(
        @PathVariable("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathVariable("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @PathVariable("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
        @PathVariable("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @RequestParam(name = "mode", defaultValue = "bare") @ApiParam("Fetch mode for policies, BARE for bare policies"
            + "  (default), REFERENCED for fully referenced policies") PolicyFetchMode mode) {
        try {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchPolicies(policyTypeId, policyTypeVersion, policyId, policyVersion, mode);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s/policies/%s/versions/%s",
                policyTypeId, policyTypeVersion, policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves the latest version of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId the ID of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies/{policyId}/versions/latest")
    @ApiOperation(value = "Retrieve the latest version of a particular policy",
        notes = "Returns the latest version of specified policy", response = ToscaServiceTemplate.class,
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Policy", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> getLatestVersionOfPolicy(
        @PathVariable("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathVariable("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @PathVariable("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @RequestParam(name = "mode", defaultValue = "bare") @ApiParam("Fetch mode for policies, TERSE for bare "
            + "policies (default), REFERENCED for fully referenced policies") PolicyFetchMode mode) {
        try {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchLatestPolicies(policyTypeId, policyTypeVersion, policyId, mode);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policytypes/%s/versions/%s/policies/%s/versions/latest",
                policyTypeId, policyTypeVersion, policyId);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Creates a new policy for a particular policy type and version.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param body the body of policy following TOSCA definition
     *
     * @return the Response object containing the results of the API operation
     */
    @PostMapping("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies")
    @ApiOperation(value = "Create a new policy for a policy type version",
        notes = "Client should provide TOSCA body of the new policy",
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Policy", },
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
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = INVALID_BODY_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_ACCEPTABLE, message = INVALID_PAYLOAD_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> createPolicy(
        @PathVariable("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathVariable("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @RequestBody @ApiParam(value = "Entity body of policy", required = true) ToscaServiceTemplate body) {
        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST,
                "/policytypes/" + policyTypeId + "/versions/" + policyTypeVersion + "/policies", toJson(body));
        }
        try {
            ToscaServiceTemplate serviceTemplate = policyProvider.createPolicy(policyTypeId, policyTypeVersion, body);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.POST);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("POST /policytypes/%s/versions/%s/policies", policyTypeId, policyTypeVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.POST);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Deletes the specified version of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId the ID of specified policy
     * @param policyVersion the version of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @DeleteMapping("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies/{policyId}/"
        + "versions/{policyVersion}")
    @ApiOperation(value = "Delete a particular version of a policy",
        notes = "Rule: the version that has been deployed in PDP group(s) cannot be deleted",
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Policy", },
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
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Dublin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = HTTP_CONFLICT_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificVersionOfPolicy(
        @PathVariable("policyTypeId") @ApiParam(value = "PolicyType ID", required = true) String policyTypeId,
        @PathVariable("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @PathVariable("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
        @PathVariable("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.deletePolicy(policyTypeId, policyTypeVersion, policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.DELETE);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("DELETE /policytypes/%s/versions/%s/policies/%s/versions/%s",
                policyTypeId, policyTypeVersion, policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.DELETE);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves all the available policies.
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/policies")
    @ApiOperation(value = "Retrieve all versions of available policies",
        notes = "Returns all version of available policies",
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Policy", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Guilin")
            })
        }
    )
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)
    })
    public ResponseEntity<ToscaServiceTemplate> getPolicies(
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @RequestParam(name = "mode", defaultValue = "bare") @ApiParam("Fetch mode for policies, BARE for bare"
            + "  policies (default), REFERENCED for fully referenced policies") PolicyFetchMode mode) {
        try {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchPolicies(null, null, null, null, mode);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "GET /policies/ --";
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            if (pfme.getErrorResponse().getResponseCode().equals(Status.NOT_FOUND)) {
                pfme.getErrorResponse().setErrorMessage(ERROR_MESSAGE_NO_POLICIES_FOUND);
                pfme.getErrorResponse().setErrorDetails(List.of(ERROR_MESSAGE_NO_POLICIES_FOUND));
            }
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Retrieves the specified version of a particular policy.
     *
     * @param policyId the Name of specified policy
     * @param policyVersion the version of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @GetMapping("/policies/{policyId}/versions/{policyVersion}")
    @ApiOperation(value = "Retrieve specific version of a specified policy",
        notes = "Returns a particular version of specified policy",
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
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Policy", },
        extensions = {
            @Extension(name = EXTENSION_NAME, properties = {
                @ExtensionProperty(name = API_VERSION_NAME, value = API_VERSION),
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Guilin")
            })
        }
    )
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)
    })
    public ResponseEntity<ToscaServiceTemplate> getSpecificPolicy(
        @PathVariable("policyId") @ApiParam(value = "Name of policy", required = true) String policyId,
        @PathVariable("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @RequestParam(name = "mode", defaultValue = "bare") @ApiParam("Fetch mode for policies, BARE for bare"
            + "  policies (default), REFERENCED for fully referenced policies") PolicyFetchMode mode) {
        try {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchPolicies(null, null, policyId, policyVersion, mode);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("GET /policies/%s/versions/%s", policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.GET);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Creates one or more new policies in one call.
     *
     * @param body the body of policy following TOSCA definition
     *
     * @return the Response object containing the results of the API operation
     */
    @PostMapping("/policies")
    @ApiOperation(value = "Create one or more new policies",
        notes = "Client should provide TOSCA body of the new polic(ies)",
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Policy", },
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
                @ExtensionProperty(name = LAST_MOD_NAME, value = "El Alto")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_BAD_REQUEST, message = INVALID_BODY_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_ACCEPTABLE, message = INVALID_PAYLOAD_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> createPolicies(
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @RequestBody @ApiParam(value = "Entity body of policy", required = true) ToscaServiceTemplate body) {
        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, "/policies", toJson(body));
        }
        try {
            ToscaServiceTemplate serviceTemplate = policyProvider.createPolicies(body);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.POST);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            final var msg = "POST /policies";
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.POST);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    /**
     * Deletes the specified version of a particular policy.
     *
     * @param policyId the ID of specified policy
     * @param policyVersion the version of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @DeleteMapping("/policies/{policyId}/versions/{policyVersion}")
    @ApiOperation(value = "Delete a particular version of a policy",
        notes = "Rule: the version that has been deployed in PDP group(s) cannot be deleted",
        authorizations = @Authorization(value = AUTHORIZATION_TYPE), tags = {"Policy", },
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
                @ExtensionProperty(name = LAST_MOD_NAME, value = "Guilin")})})
    @ApiResponses(value = {
        @ApiResponse(code = HttpURLConnection.HTTP_UNAUTHORIZED, message = AUTHENTICATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_FORBIDDEN, message = AUTHORIZATION_ERROR_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_NOT_FOUND, message = NOT_FOUND_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_CONFLICT, message = HTTP_CONFLICT_MESSAGE),
        @ApiResponse(code = HttpURLConnection.HTTP_INTERNAL_ERROR, message = SERVER_ERROR_MESSAGE)})
    public ResponseEntity<ToscaServiceTemplate> deleteSpecificPolicy(
        @PathVariable("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
        @PathVariable("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
        @RequestHeader(name = REQUEST_ID_NAME, required = false)
        @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {
        try {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.deletePolicy(null, null, policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.OK, HttpMethod.DELETE);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            var msg = String.format("DELETE /policies/%s/versions/%s", policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, HttpStatus.resolve(pfme.getErrorResponse().getResponseCode()
                .getStatusCode()), HttpMethod.DELETE);
            throw new PolicyApiRuntimeException(msg, pfme.getCause(), pfme.getErrorResponse(), requestId);
        }
    }

    private void updateApiStatisticsCounter(Target target, HttpStatus result, HttpMethod http) {
        mgr.updateTotalApiCallCount();
        switch (target) {
            case POLICY:
                updatePolicyStats(result, http);
                break;
            case POLICY_TYPE:
                updatePolicyTypeStats(result, http);
                break;
            default:
                mgr.updateApiCallSuccessCount();
                break;
        }
    }

    private void updatePolicyStats(HttpStatus result, HttpMethod http) {
        if (result.equals(HttpStatus.OK)) {
            switch (http) {
                case GET:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyGetCount();
                    mgr.updatePolicyGetSuccessCount();
                    break;
                case POST:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyPostCount();
                    mgr.updatePolicyPostSuccessCount();
                    break;
                case DELETE:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyDeleteCount();
                    mgr.updatePolicyDeleteSuccessCount();
                    break;
                default:
                    mgr.updateApiCallSuccessCount();
                    break;
            }
        } else {
            switch (http) {
                case GET:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyGetCount();
                    mgr.updatePolicyGetFailureCount();
                    break;
                case POST:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyPostCount();
                    mgr.updatePolicyPostFailureCount();
                    break;
                case DELETE:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyDeleteCount();
                    mgr.updatePolicyDeleteFailureCount();
                    break;
                default:
                    mgr.updateApiCallFailureCount();
                    break;
            }
        }
    }

    private void updatePolicyTypeStats(HttpStatus result, HttpMethod http) {
        if (result.equals(HttpStatus.OK)) {
            switch (http) {
                case GET:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyTypeGetCount();
                    mgr.updatePolicyTypeGetSuccessCount();
                    break;
                case POST:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyTypePostCount();
                    mgr.updatePolicyTypePostSuccessCount();
                    break;
                case DELETE:
                    mgr.updateApiCallSuccessCount();
                    mgr.updateTotalPolicyTypeDeleteCount();
                    mgr.updatePolicyTypeDeleteSuccessCount();
                    break;
                default:
                    mgr.updateApiCallSuccessCount();
                    break;
            }
        } else {
            switch (http) {
                case GET:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyTypeGetCount();
                    mgr.updatePolicyTypeGetFailureCount();
                    break;
                case POST:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyTypePostCount();
                    mgr.updatePolicyTypePostFailureCount();
                    break;
                case DELETE:
                    mgr.updateApiCallFailureCount();
                    mgr.updateTotalPolicyTypeDeleteCount();
                    mgr.updatePolicyTypeDeleteFailureCount();
                    break;
                default:
                    mgr.updateApiCallFailureCount();
                    break;
            }
        }
    }
}