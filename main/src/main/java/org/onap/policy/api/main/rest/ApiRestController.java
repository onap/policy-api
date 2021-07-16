/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2019-2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Nordix Foundation.
 * Modifications Copyright (C) 2020 Bell Canada.
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
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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

/**
 * Class to provide REST API services.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Path("/policy/api/v1")
@Api(value = "Policy Design API")
@Produces({"application/json", "application/yaml"})
@Consumes({"application/json", "application/yaml"})
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

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRestController.class);

    private static final String ERROR_MESSAGE_NO_POLICIES_FOUND = "No policies found";

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
    private static final String HTTP_CONFLICT_MESSAGE = "Delete Conflict, Rule Violation";


    /**
     * Retrieves the healthcheck status of the API component.
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/healthcheck")
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
    public Response
        getHealthCheck(@HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {

        updateApiStatisticsCounter(Target.OTHER, Result.SUCCESS, HttpMethod.GET);
        return makeOkResponse(requestId, new HealthCheckProvider().performHealthCheck());
    }

    /**
     * Retrieves the statistics report of the API component.
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/statistics")
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
    public Response
        getStatistics(@HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {

        updateApiStatisticsCounter(Target.OTHER, Result.SUCCESS, HttpMethod.GET);

        return makeOkResponse(requestId, new StatisticsProvider().fetchCurrentStatistics());
    }

    /**
     * Retrieves all available policy types.
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policytypes")
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
    public Response
        getAllPolicyTypes(@HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {

        try (var policyTypeProvider = new PolicyTypeProvider()) {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.fetchPolicyTypes(null, null);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.SUCCESS, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("GET /policytypes", pfme);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.FAILURE, HttpMethod.GET);
            return makeErrorResponse(requestId, pfme);
        }
    }

    /**
     * Retrieves all versions of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policytypes/{policyTypeId}")
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
    public Response getAllVersionsOfPolicyType(
        @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {

        try (var policyTypeProvider = new PolicyTypeProvider()) {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.fetchPolicyTypes(policyTypeId, null);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.SUCCESS, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("GET /policytypes/{}", policyTypeId, pfme);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.FAILURE, HttpMethod.GET);
            return makeErrorResponse(requestId, pfme);
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
    @GET
    @Path("/policytypes/{policyTypeId}/versions/{versionId}")
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
    public Response getSpecificVersionOfPolicyType(
        @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathParam("versionId") @ApiParam(value = "Version of policy type", required = true) String versionId,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {

        try (var policyTypeProvider = new PolicyTypeProvider()) {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.fetchPolicyTypes(policyTypeId, versionId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.SUCCESS, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("GET /policytypes/{}/versions/{}", policyTypeId, versionId, pfme);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.FAILURE, HttpMethod.GET);
            return makeErrorResponse(requestId, pfme);
        }
    }

    /**
     * Retrieves latest version of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policytypes/{policyTypeId}/versions/latest")
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
    public Response getLatestVersionOfPolicyType(
        @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {

        try (var policyTypeProvider = new PolicyTypeProvider()) {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.fetchLatestPolicyTypes(policyTypeId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.SUCCESS, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("GET /policytypes/{}/versions/latest", policyTypeId, pfme);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.FAILURE, HttpMethod.GET);
            return makeErrorResponse(requestId, pfme);
        }
    }

    /**
     * Creates a new policy type.
     *
     * @param body the body of policy type following TOSCA definition
     *
     * @return the Response object containing the results of the API operation
     */
    @POST
    @Path("/policytypes")
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
    public Response createPolicyType(
        @ApiParam(value = "Entity body of policy type", required = true) ToscaServiceTemplate body,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {

        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, "/policytypes", toJson(body));
        }

        try (var policyTypeProvider = new PolicyTypeProvider()) {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.createPolicyType(body);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.SUCCESS, HttpMethod.POST);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("POST /policytypes", pfme);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.FAILURE, HttpMethod.POST);
            return makeErrorResponse(requestId, pfme);
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
    @DELETE
    @Path("/policytypes/{policyTypeId}/versions/{versionId}")
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
    public Response deleteSpecificVersionOfPolicyType(
        @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathParam("versionId") @ApiParam(value = "Version of policy type", required = true) String versionId,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {

        try (var policyTypeProvider = new PolicyTypeProvider()) {
            ToscaServiceTemplate serviceTemplate = policyTypeProvider.deletePolicyType(policyTypeId, versionId);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.SUCCESS, HttpMethod.DELETE);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("DELETE /policytypes/{}/versions/{}", policyTypeId, versionId, pfme);
            updateApiStatisticsCounter(Target.POLICY_TYPE, Result.FAILURE, HttpMethod.DELETE);
            return makeErrorResponse(requestId, pfme);
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
    @GET
    @Path("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies")
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
    public Response getAllPolicies(
        @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathParam("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @QueryParam("mode") @DefaultValue("bare") @ApiParam("Fetch mode for policies, BARE for bare policies (default),"
            + " REFERENCED for fully referenced policies") PolicyFetchMode mode
    ) {

        try (var policyProvider = new PolicyProvider()) {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchPolicies(policyTypeId, policyTypeVersion, null, null, mode);
            updateApiStatisticsCounter(Target.POLICY, Result.SUCCESS, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("GET /policytypes/{}/versions/{}/policies", policyTypeId, policyTypeVersion, pfme);
            updateApiStatisticsCounter(Target.POLICY, Result.FAILURE, HttpMethod.GET);
            return makeErrorResponse(requestId, pfme);
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
    @GET
    @Path("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies/{policyId}")
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
    public Response getAllVersionsOfPolicy(
        @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathParam("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @QueryParam("mode") @DefaultValue("bare") @ApiParam("Fetch mode for policies, BARE for bare policies (default),"
            + " REFERENCED for fully referenced policies") PolicyFetchMode mode
    ) {
        try (var policyProvider = new PolicyProvider()) {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchPolicies(policyTypeId, policyTypeVersion, policyId, null, mode);
            updateApiStatisticsCounter(Target.POLICY, Result.SUCCESS, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("/policytypes/{}/versions/{}/policies/{}", policyTypeId, policyTypeVersion, policyId, pfme);
            updateApiStatisticsCounter(Target.POLICY, Result.FAILURE, HttpMethod.GET);
            return makeErrorResponse(requestId, pfme);
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
    @GET
    @Path("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies/{policyId}/versions/{policyVersion}")
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
    public Response getSpecificVersionOfPolicy(
        @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathParam("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
        @PathParam("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @QueryParam("mode") @DefaultValue("bare") @ApiParam("Fetch mode for policies, BARE for bare policies (default),"
            + " REFERENCED for fully referenced policies") PolicyFetchMode mode
    ) {
        try (var policyProvider = new PolicyProvider()) {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchPolicies(policyTypeId, policyTypeVersion, policyId, policyVersion, mode);
            updateApiStatisticsCounter(Target.POLICY, Result.SUCCESS, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("GET /policytypes/{}/versions/{}/policies/{}/versions/{}", policyTypeId, policyTypeVersion,
                policyId, policyVersion, pfme);
            updateApiStatisticsCounter(Target.POLICY, Result.FAILURE, HttpMethod.GET);
            return makeErrorResponse(requestId, pfme);
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
    @GET
    @Path("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies/{policyId}/versions/latest")
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
    public Response getLatestVersionOfPolicy(
        @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathParam("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @QueryParam("mode") @ApiParam("Fetch mode for policies, TERSE for bare policies (default), "
            + "REFERENCED for fully referenced policies") PolicyFetchMode mode) {

        try (var policyProvider = new PolicyProvider()) {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchLatestPolicies(policyTypeId, policyTypeVersion, policyId, mode);
            updateApiStatisticsCounter(Target.POLICY, Result.SUCCESS, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("GET /policytypes/{}/versions/{}/policies/{}/versions/latest", policyTypeId, policyTypeVersion,
                policyId, pfme);
            updateApiStatisticsCounter(Target.POLICY, Result.FAILURE, HttpMethod.GET);
            return makeErrorResponse(requestId, pfme);
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
    @POST
    @Path("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies")
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
    public Response createPolicy(
        @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
        @PathParam("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @ApiParam(value = "Entity body of policy", required = true) ToscaServiceTemplate body) {

        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST,
                "/policytypes/" + policyTypeId + "/versions/" + policyTypeVersion + "/policies", toJson(body));
        }

        try (var policyProvider = new PolicyProvider()) {
            ToscaServiceTemplate serviceTemplate = policyProvider.createPolicy(policyTypeId, policyTypeVersion, body);
            updateApiStatisticsCounter(Target.POLICY, Result.SUCCESS, HttpMethod.POST);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("POST /policytypes/{}/versions/{}/policies", policyTypeId, policyTypeVersion, pfme);
            updateApiStatisticsCounter(Target.POLICY, Result.FAILURE, HttpMethod.POST);
            return makeErrorResponse(requestId, pfme);
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
    @DELETE
    @Path("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies/{policyId}/versions/{policyVersion}")
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
    public Response deleteSpecificVersionOfPolicy(
        @PathParam("policyTypeId") @ApiParam(value = "PolicyType ID", required = true) String policyTypeId,
        @PathParam("policyTypeVersion") @ApiParam(value = "Version of policy type",
            required = true) String policyTypeVersion,
        @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
        @PathParam("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {

        try (var policyProvider = new PolicyProvider()) {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.deletePolicy(policyTypeId, policyTypeVersion, policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, Result.SUCCESS, HttpMethod.DELETE);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("DELETE /policytypes/{}/versions/{}/policies/{}/versions/{}", policyTypeId, policyTypeVersion,
                policyId, policyVersion, pfme);
            updateApiStatisticsCounter(Target.POLICY, Result.FAILURE, HttpMethod.DELETE);
            return makeErrorResponse(requestId, pfme);
        }
    }

    /**
     * Retrieves all the available policies.
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policies")
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
    public Response getPolicies(
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @QueryParam("mode") @DefaultValue("bare") @ApiParam("Fetch mode for policies, BARE for bare policies (default),"
            + " REFERENCED for fully referenced policies") PolicyFetchMode mode
    ) {
        try (var policyProvider = new PolicyProvider()) {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchPolicies(null, null, null, null, mode);
            updateApiStatisticsCounter(Target.POLICY, Result.SUCCESS, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("GET /policies/ --", pfme);
            updateApiStatisticsCounter(Target.POLICY, Result.FAILURE, HttpMethod.GET);
            if (pfme.getErrorResponse().getResponseCode().equals(Status.NOT_FOUND)) {
                pfme.getErrorResponse().setErrorMessage(ERROR_MESSAGE_NO_POLICIES_FOUND);
                pfme.getErrorResponse().setErrorDetails(List.of(ERROR_MESSAGE_NO_POLICIES_FOUND));
            }
            return makeErrorResponse(requestId, pfme);
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
    @GET
    @Path("/policies/{policyId}/versions/{policyVersion}")
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
    public Response getSpecificPolicy(
        @PathParam("policyId") @ApiParam(value = "Name of policy", required = true) String policyId,
        @PathParam("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @QueryParam("mode") @DefaultValue("bare") @ApiParam("Fetch mode for policies, BARE for bare policies (default),"
            + " REFERENCED for fully referenced policies") PolicyFetchMode mode
    ) {
        try (var policyProvider = new PolicyProvider()) {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.fetchPolicies(null, null, policyId, policyVersion, mode);
            updateApiStatisticsCounter(Target.POLICY, Result.SUCCESS, HttpMethod.GET);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("GET /policies/{}/versions/{}", policyId, policyVersion, pfme);
            updateApiStatisticsCounter(Target.POLICY, Result.FAILURE, HttpMethod.GET);
            return makeErrorResponse(requestId, pfme);
        }
    }

    /**
     * Creates one or more new policies in one call.
     *
     * @param body the body of policy following TOSCA definition
     *
     * @return the Response object containing the results of the API operation
     */
    @POST
    @Path("/policies")
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
    public Response createPolicies(
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId,
        @ApiParam(value = "Entity body of policy", required = true) ToscaServiceTemplate body) {

        if (NetLoggerUtil.getNetworkLogger().isInfoEnabled()) {
            NetLoggerUtil.log(EventType.IN, CommInfrastructure.REST, "/policies", toJson(body));
        }

        try (var policyProvider = new PolicyProvider()) {
            ToscaServiceTemplate serviceTemplate = policyProvider.createPolicies(body);
            updateApiStatisticsCounter(Target.POLICY, Result.SUCCESS, HttpMethod.POST);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("POST /policies", pfme);
            updateApiStatisticsCounter(Target.POLICY, Result.FAILURE, HttpMethod.POST);
            return makeErrorResponse(requestId, pfme);
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
    @DELETE
    @Path("/policies/{policyId}/versions/{policyVersion}")
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
    public Response deleteSpecificPolicy(
        @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
        @PathParam("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
        @HeaderParam(REQUEST_ID_NAME) @ApiParam(REQUEST_ID_PARAM_DESCRIPTION) UUID requestId) {

        try (var policyProvider = new PolicyProvider()) {
            ToscaServiceTemplate serviceTemplate =
                policyProvider.deletePolicy(null, null, policyId, policyVersion);
            updateApiStatisticsCounter(Target.POLICY, Result.SUCCESS, HttpMethod.DELETE);
            return makeOkResponse(requestId, serviceTemplate);
        } catch (PfModelException | PfModelRuntimeException pfme) {
            LOGGER.warn("DELETE /policies/{}/versions/{}", policyId, policyVersion, pfme);
            updateApiStatisticsCounter(Target.POLICY, Result.FAILURE, HttpMethod.DELETE);
            return makeErrorResponse(requestId, pfme);
        }
    }



    private enum Target {
        POLICY,
        POLICY_TYPE,
        OTHER
    }

    private enum Result {
        SUCCESS,
        FAILURE
    }

    private enum HttpMethod {
        POST,
        GET,
        DELETE
    }

    private void updateApiStatisticsCounter(Target target, Result result, HttpMethod http) {

        var mgr = ApiStatisticsManager.getInstance();
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

    private void updatePolicyStats(Result result, HttpMethod http) {
        var mgr = ApiStatisticsManager.getInstance();

        if (result == Result.SUCCESS) {
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

    private void updatePolicyTypeStats(Result result, HttpMethod http) {
        var mgr = ApiStatisticsManager.getInstance();

        if (result == Result.SUCCESS) {
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
