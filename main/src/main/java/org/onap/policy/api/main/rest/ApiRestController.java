/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.onap.policy.api.main.rest.provider.HealthCheckProvider;
import org.onap.policy.api.main.rest.provider.PolicyProvider;
import org.onap.policy.api.main.rest.provider.PolicyTypeProvider;
import org.onap.policy.api.main.rest.provider.StatisticsProvider;
import org.onap.policy.common.endpoints.report.HealthCheckReport;
import org.onap.policy.models.tosca.ToscaPolicy;
import org.onap.policy.models.tosca.ToscaPolicyList;
import org.onap.policy.models.tosca.ToscaPolicyType;
import org.onap.policy.models.tosca.ToscaPolicyTypeList;

/**
 * Class to provide REST API services.
 */
@Path("/policy/api/v1")
@Api(value = "Policy Design API")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@SwaggerDefinition(info = @Info(
        description = "Policy Design API is publicly exposed for clients to Create/Read/Update/Delete"
                    + " policy types, policy type implementation and policies which can be recognized"
                    + " and executable by incorporated policy engines. It is an"
                    + " independent component running rest service that takes all policy design API calls"
                    + " from clients and then assign them to different API working functions. Besides"
                    + " that, API is also exposed for clients to retrieve healthcheck status of this API"
                    + " rest service and the statistics report including the counters of API invocation.",
        version = "1.0.0",
        title = "Policy Design",
        extensions = {
                @Extension(properties = {
                        @ExtensionProperty(name = "planned-retirement-date", value = "tbd"),
                        @ExtensionProperty(name = "component", value = "Policy Framework")
                })
        }),
        schemes = { SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS },
        securityDefinition = @SecurityDefinition(basicAuthDefinitions = { @BasicAuthDefinition(key = "basicAuth") }))
public class ApiRestController {

    /**
     * Retrieves the healthcheck status of the API component.
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/healthcheck")
    @ApiOperation(value = "Perform a system healthcheck",
            notes = "Returns healthy status of the Policy API component",
            response = HealthCheckReport.class,
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            },
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "HealthCheck", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response getHealthCheck(
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new HealthCheckProvider().performHealthCheck()).build();
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
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            },
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Statistics", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response getStatistics(
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new StatisticsProvider().fetchCurrentStatistics()).build();
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
            response = ToscaPolicyTypeList.class,
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            },
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "PolicyType", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response getAllPolicyTypes(
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyTypeProvider().fetchPolicyTypes(null, null)).build();
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
            response = ToscaPolicyTypeList.class,
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            },
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "PolicyType", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response getAllVersionsOfPolicyType(
            @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyTypeProvider().fetchPolicyTypes(policyTypeId, null)).build();
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
            notes = "Returns a particular version for the specified policy type",
            response = ToscaPolicyTypeList.class,
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            },
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "PolicyType", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response getSpecificVersionOfPolicyType(
            @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
            @PathParam("versionId") @ApiParam(value = "Version of policy type", required = true) String versionId,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyTypeProvider().fetchPolicyTypes(policyTypeId, versionId)).build();
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
    @ApiOperation(value = "Create a new policy type",
            notes = "Client should provide TOSCA body of the new policy type",
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "PolicyType", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Resource successfully created",
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            }),
            @ApiResponse(code = 400, message = "Invalid Body"),
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response createPolicyType(
            @ApiParam(value = "Entity body of policy type", required = true) ToscaPolicyType body,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyTypeProvider().createPolicyType(body)).build();
    }

    /**
     * Deletes all versions of a particular policy type.
     *
     * @param policyTypeId the ID of specified policy type
     *
     * @return the Response object containing the results of the API operation
     */
    @DELETE
    @Path("/policytypes/{policyTypeId}")
    @ApiOperation(value = "Delete all versions of a policy type",
            notes = "Rule 1: pre-defined policy types cannot be deleted;"
                  + "Rule 2: policy types that are in use (parameterized by a TOSCA policy) cannot be deleted."
                  + "The parameterizing TOSCA policies must be deleted first;",
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "PolicyType", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Resources successfully deleted, no content returned",
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            }),
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 409, message = "Delete Conflict, Rule Violation"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response deleteAllVersionsOfPolicyType(
            @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyTypeProvider().deletePolicyTypes(policyTypeId, null)).build();
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
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "PolicyType", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Resource successfully deleted, no content returned",
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            }),
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 409, message = "Delete Conflict, Rule Violation"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response deleteSpecificVersionOfPolicyType(
            @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
            @PathParam("versionId") @ApiParam(value = "Version of policy type", required = true) String versionId,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyTypeProvider().deletePolicyTypes(policyTypeId, versionId)).build();
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
    @ApiOperation(value = "Retrieve all versions of a policy created for a particular policy type version",
            notes = "Returns a list of all versions of specified policy created for the specified policy type version",
            response = ToscaPolicyList.class,
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            },
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Policy", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response getAllPolicies(
            @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
            @PathParam("policyTypeVersion")
                @ApiParam(value = "Version of policy type", required = true) String policyTypeVersion,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyProvider().fetchPolicies(policyTypeId, policyTypeVersion, null, null)).build();
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
            notes = "Returns a list of all version details of the specified policy",
            response = ToscaPolicyList.class,
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            },
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Policy", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response getAllVersionsOfPolicy(
            @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
            @PathParam("policyTypeVersion")
                @ApiParam(value = "Version of policy type", required = true) String policyTypeVersion,
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyProvider().fetchPolicies(policyTypeId, policyTypeVersion, policyId, null)).build();
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
            response = ToscaPolicyList.class,
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            },
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Policy", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response getSpecificVersionOfPolicy(
            @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
            @PathParam("policyTypeVersion")
                @ApiParam(value = "Version of policy type", required = true) String policyTypeVersion,
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @PathParam("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyProvider().fetchPolicies(policyTypeId, policyTypeVersion,
                                                       policyId, policyVersion)).build();
    }

    /**
     * Retrieves either latest or deployed version of a particular policy depending on query parameter.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId the ID of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies/{policyId}/versions")
    @ApiOperation(value = "Retrieve either latest or deployed version of a particular policy depending on query param",
            notes = "Returns either latest or deployed version of specified policy depending on query param",
            response = ToscaPolicyList.class,
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            },
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Policy", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response getEitherLatestOrDeployedVersionOfPolicy(
            @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
            @PathParam("policyTypeVersion")
                @ApiParam(value = "Version of policy type", required = true) String policyTypeVersion,
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @QueryParam("type")
                @ApiParam(value = "Version that can only be 'latest' or 'deployed'", required = true) String type,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyProvider().fetchPolicies(policyTypeId, policyTypeVersion, policyId, type)).build();
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
    @Consumes({"application/json", "application/yaml", "application/json; vnd.onap.guard", "application/yaml; vnd.onap.operational"})
    @Produces({"application/json", "application/yaml"})
    @ApiOperation(value = "Create a new policy for a policy type version",
            notes = "Client should provide TOSCA body of the new policy",
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Policy", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Resource successfully created",
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            }),
            @ApiResponse(code = 400, message = "Invalid Body"),
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response createPolicy(
            @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
            @PathParam("policyTypeVersion")
                @ApiParam(value = "Version of policy type", required = true) String policyTypeVersion,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId,
            @ApiParam(value = "Entity body of policy", required = true) ToscaPolicy body) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyProvider().createPolicy(policyTypeId, policyTypeVersion, body)).build();
    }

    /**
     * Deletes all versions of a particular policy.
     *
     * @param policyTypeId the ID of specified policy type
     * @param policyTypeVersion the version of specified policy type
     * @param policyId the ID of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @DELETE
    @Path("/policytypes/{policyTypeId}/versions/{policyTypeVersion}/policies/{policyId}")
    @ApiOperation(value = "Delete all versions of a policy",
            notes = "Rule: the version that has been deployed in PDP group(s) cannot be deleted",
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Policy", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Resources successfully deleted, no content returned",
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            }),
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 409, message = "Delete Conflict, Rule Violation"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response deleteAllVersionsOfPolicy(
            @PathParam("policyTypeId") @ApiParam(value = "ID of policy type", required = true) String policyTypeId,
            @PathParam("policyTypeVersion")
                @ApiParam(value = "Version of policy type", required = true) String policyTypeVersion,
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyProvider().deletePolicies(policyTypeId, policyTypeVersion, policyId, null)).build();
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
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Policy", },
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 204, message = "Resource successfully deleted, no content returned",
            responseHeaders = {
                    @ResponseHeader(name = "X-MinorVersion",
                                    description = "Used to request or communicate a MINOR version back from the client"
                                                + " to the server, and from the server back to the client",
                                    response = String.class),
                    @ResponseHeader(name = "X-PatchVersion",
                                    description = "Used only to communicate a PATCH version in a response for"
                                                + " troubleshooting purposes only, and will not be provided by"
                                                + " the client on request",
                                    response = String.class),
                    @ResponseHeader(name = "X-LatestVersion",
                                    description = "Used only to communicate an API's latest version",
                                    response = String.class),
                    @ResponseHeader(name = "X-ONAP-RequestID",
                                    description = "Used to track REST transactions for logging purpose",
                                    response = UUID.class)
            }),
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 404, message = "Resource Not Found"),
            @ApiResponse(code = 409, message = "Delete Conflict, Rule Violation"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response deleteSpecificVersionOfPolicy(
            @PathParam("policyTypeId") @ApiParam(value = "PolicyType ID", required = true) String policyTypeId,
            @PathParam("policyTypeVersion")
                @ApiParam(value = "Version of policy type", required = true) String policyTypeVersion,
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @PathParam("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {
        return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
            .entity(new PolicyProvider().deletePolicies(policyTypeId, policyTypeVersion,
                                                        policyId, policyVersion)).build();
    }

    private ResponseBuilder addVersionControlHeaders(ResponseBuilder rb) {
        return rb.header("X-MinorVersion", "0").header("X-PatchVersion", "0").header("X-LatestVersion", "1.0.0");
    }

    private ResponseBuilder addLoggingHeaders(ResponseBuilder rb, UUID requestId) {
        if (requestId == null) {
            // Generate a random uuid if client does not embed requestId in rest request
            return rb.header("X-ONAP-RequestID", UUID.randomUUID());
        }
        return rb.header("X-ONAP-RequestID", requestId);
    }
}
