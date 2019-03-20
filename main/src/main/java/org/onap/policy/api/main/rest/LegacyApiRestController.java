/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
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
import io.swagger.annotations.Extension;
import io.swagger.annotations.ExtensionProperty;
import io.swagger.annotations.ResponseHeader;
import java.util.List;
import java.util.UUID;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import org.onap.policy.api.main.rest.provider.LegacyGuardPolicyProvider;
import org.onap.policy.api.main.rest.provider.LegacyOperationalPolicyProvider;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicy;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

/**
 * Class to provide legacy REST API services.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
@Path("/policy/api/v1")
@Api(value = "Legacy Policy Design API")
@Produces({"application/json; vnd.onap.guard", "application/json; vnd.onap.operational"})
@Consumes({"application/json; vnd.onap.guard", "application/json; vnd.onap.operational"})
public class LegacyApiRestController {

    /**
     * Retrieves all versions of guard policies.
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policytypes/onap.policy.controlloop.guard/versions/1.0.0/policies")
    @Produces("application/json; vnd.onap.guard")
    @ApiOperation(value = "Retrieve all versions of guard policies",
            notes = "Returns a list of all versions of guard policies",
            response = LegacyGuardPolicy.class, responseContainer = "List",
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
            tags = { "Legacy Guard Policy", },
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
    public Response getAllGuardPolicies(
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {

        try {
            List<LegacyGuardPolicy> policies = new LegacyGuardPolicyProvider().fetchGuardPolicies(null, null);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policies).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Retrieves all versions of a particular guard policy.
     *
     * @param policyId the ID of specified guard policy
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policytypes/onap.policy.controlloop.guard/versions/1.0.0/policies/{policyId}")
    @Produces("application/json; vnd.onap.guard")
    @ApiOperation(value = "Retrieve all versions of a particular guard policy",
            notes = "Returns a list of all versions of the specified guard policy",
            response = LegacyGuardPolicy.class, responseContainer = "List",
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
            tags = { "Legacy Guard Policy", },
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
    public Response getAllVersionsOfGuardPolicy(
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {

        try {
            List<LegacyGuardPolicy> policies = new LegacyGuardPolicyProvider()
                    .fetchGuardPolicies(policyId, null);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policies).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Retrieves the specified version of a particular guard policy.
     *
     * @param policyId the ID of specified policy
     * @param policyVersion the version of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policytypes/onap.policy.controlloop.guard/versions/1.0.0/policies/{policyId}/versions/{policyVersion}")
    @Produces("application/json; vnd.onap.guard")
    @ApiOperation(value = "Retrieve one version of a particular guard policy",
            notes = "Returns a particular version of a specified guard policy",
            response = LegacyGuardPolicy.class, responseContainer = "List",
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
            tags = { "Legacy Guard Policy", },
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
    public Response getSpecificVersionOfGuardPolicy(
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @PathParam("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {

        try {
            List<LegacyGuardPolicy> policies = new LegacyGuardPolicyProvider()
                    .fetchGuardPolicies(policyId, policyVersion);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policies).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Creates a new guard policy.
     *
     * @param body the body of policy
     *
     * @return the Response object containing the results of the API operation
     */
    @POST
    @Path("/policytypes/onap.policy.controlloop.guard/versions/1.0.0/policies")
    @Consumes("application/json; vnd.onap.guard")
    @Produces("application/json; vnd.onap.guard")
    @ApiOperation(value = "Create a new guard policy",
            notes = "Client should provide entity body of the new guard policy",
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Legacy Guard Policy", },
            response = LegacyGuardPolicy.class,
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
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid Body"),
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response createGuardPolicy(
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId,
            @ApiParam(value = "Entity body of policy", required = true) LegacyGuardPolicy body) {

        try {
            LegacyGuardPolicy policy = new LegacyGuardPolicyProvider().createGuardPolicy(body);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policy).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Deletes all versions of a particular guard policy.
     *
     * @param policyId the ID of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @DELETE
    @Path("/policytypes/onap.policy.controlloop.guard/versions/1.0.0/policies/{policyId}")
    @ApiOperation(value = "Delete all versions of a guard policy",
            notes = "Rule: the version that has been deployed in PDP group(s) cannot be deleted",
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Legacy Guard Policy", },
            response = LegacyGuardPolicy.class, responseContainer = "List",
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
            @ApiResponse(code = 409, message = "Delete Conflict, Rule Violation"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response deleteAllVersionsOfGuardPolicy(
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {

        try {
            List<LegacyGuardPolicy> policies = new LegacyGuardPolicyProvider()
                    .deleteGuardPolicies(policyId, null);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policies).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Deletes the specified version of a particular guard policy.
     *
     * @param policyId the ID of specified policy
     * @param policyVersion the version of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @DELETE
    @Path("/policytypes/onap.policy.controlloop.guard/versions/1.0.0/policies/{policyId}/versions/{policyVersion}")
    @ApiOperation(value = "Delete a particular version of a guard policy",
            notes = "Rule: the version that has been deployed in PDP group(s) cannot be deleted",
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Legacy Guard Policy", },
            response = LegacyGuardPolicy.class, responseContainer = "List",
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
            @ApiResponse(code = 409, message = "Delete Conflict, Rule Violation"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response deleteSpecificVersionOfGuardPolicy(
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @PathParam("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {

        try {
            List<LegacyGuardPolicy> policies = new LegacyGuardPolicyProvider()
                    .deleteGuardPolicies(policyId, policyVersion);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policies).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Retrieves all versions of operational policies.
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policytypes/onap.policy.controlloop.operational/versions/1.0.0/policies")
    @Produces("application/json; vnd.onap.operational")
    @ApiOperation(value = "Retrieve all versions of operational policies",
            notes = "Returns a list of all versions of operational policies",
            response = LegacyOperationalPolicy.class, responseContainer = "List",
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
            tags = { "Legacy Operational Policy", },
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
    public Response getAllOperationalPolicies(
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {

        try {
            List<LegacyOperationalPolicy> policies = new LegacyOperationalPolicyProvider()
                    .fetchOperationalPolicies(null, null);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policies).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Retrieves all versions of a particular operational policy.
     *
     * @param policyId the ID of specified operational policy
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policytypes/onap.policy.controlloop.operational/versions/1.0.0/policies/{policyId}")
    @Produces("application/json; vnd.onap.operational")
    @ApiOperation(value = "Retrieve all versions of a particular operational policy",
            notes = "Returns a list of all versions of the specified operational policy",
            response = LegacyOperationalPolicy.class, responseContainer = "List",
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
            tags = { "Legacy Operational Policy", },
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
    public Response getAllVersionsOfOperationalPolicy(
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {

        try {
            List<LegacyOperationalPolicy> policies = new LegacyOperationalPolicyProvider()
                    .fetchOperationalPolicies(policyId, null);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policies).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Retrieves the specified version of a particular operational policy.
     *
     * @param policyId the ID of specified policy
     * @param policyVersion the version of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @GET
    @Path("/policytypes/onap.policy.controlloop.operational/versions/1.0.0/"
         + "policies/{policyId}/versions/{policyVersion}")
    @Produces("application/json; vnd.onap.operational")
    @ApiOperation(value = "Retrieve one version of a particular operational policy",
            notes = "Returns a particular version of a specified operational policy",
            response = LegacyOperationalPolicy.class, responseContainer = "List",
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
            tags = { "Legacy Operational Policy", },
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
    public Response getSpecificVersionOfOperationalPolicy(
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @PathParam("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {

        try {
            List<LegacyOperationalPolicy> policies = new LegacyOperationalPolicyProvider()
                    .fetchOperationalPolicies(policyId, policyVersion);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policies).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Creates a new operational policy.
     *
     * @param body the body of policy
     *
     * @return the Response object containing the results of the API operation
     */
    @POST
    @Path("/policytypes/onap.policy.controlloop.operational/versions/1.0.0/policies")
    @Consumes("application/json; vnd.onap.operational")
    @Produces("application/json; vnd.onap.operational")
    @ApiOperation(value = "Create a new operational policy",
            notes = "Client should provide entity body of the new operational policy",
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Legacy Operational Policy", },
            response = LegacyOperationalPolicy.class,
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
            extensions = {
                    @Extension(name = "interface info", properties = {
                            @ExtensionProperty(name = "api-version", value = "1.0.0"),
                            @ExtensionProperty(name = "last-mod-release", value = "Dublin")
                    })
            })
    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Invalid Body"),
            @ApiResponse(code = 401, message = "Authentication Error"),
            @ApiResponse(code = 403, message = "Authorization Error"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response createOperationalPolicy(
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId,
            @ApiParam(value = "Entity body of policy", required = true) LegacyOperationalPolicy body) {

        try {
            LegacyOperationalPolicy policy = new LegacyOperationalPolicyProvider()
                    .createOperationalPolicy(body);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policy).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Deletes all versions of a particular operational policy.
     *
     * @param policyId the ID of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @DELETE
    @Path("/policytypes/onap.policy.controlloop.operational/versions/1.0.0/policies/{policyId}")
    @ApiOperation(value = "Delete all versions of a operational policy",
            notes = "Rule: the version that has been deployed in PDP group(s) cannot be deleted",
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Legacy Operational Policy", },
            response = LegacyOperationalPolicy.class, responseContainer = "List",
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
            @ApiResponse(code = 409, message = "Delete Conflict, Rule Violation"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response deleteAllVersionsOfOperationalPolicy(
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {

        try {
            List<LegacyOperationalPolicy> policies = new LegacyOperationalPolicyProvider()
                    .deleteOperationalPolicies(policyId, null);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policies).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
    }

    /**
     * Deletes the specified version of a particular operational policy.
     *
     * @param policyId the ID of specified policy
     * @param policyVersion the version of specified policy
     *
     * @return the Response object containing the results of the API operation
     */
    @DELETE
    @Path("/policytypes/onap.policy.controlloop.operational/versions/1.0.0/"
         + "policies/{policyId}/versions/{policyVersion}")
    @ApiOperation(value = "Delete a particular version of a specified operational policy",
            notes = "Rule: the version that has been deployed in PDP group(s) cannot be deleted",
            authorizations = @Authorization(value = "basicAuth"),
            tags = { "Legacy Operational Policy", },
            response = LegacyOperationalPolicy.class, responseContainer = "List",
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
            @ApiResponse(code = 409, message = "Delete Conflict, Rule Violation"),
            @ApiResponse(code = 500, message = "Internal Server Error")
        })
    public Response deleteSpecificVersionOfOperationalPolicy(
            @PathParam("policyId") @ApiParam(value = "ID of policy", required = true) String policyId,
            @PathParam("policyVersion") @ApiParam(value = "Version of policy", required = true) String policyVersion,
            @HeaderParam("X-ONAP-RequestID") @ApiParam("RequestID for http transaction") UUID requestId) {

        try {
            List<LegacyOperationalPolicy> policies = new LegacyOperationalPolicyProvider()
                    .deleteOperationalPolicies(policyId, policyVersion);
            return addLoggingHeaders(addVersionControlHeaders(Response.status(Response.Status.OK)), requestId)
                    .entity(policies).build();
        } catch (PfModelException | PfModelRuntimeException pfme) {
            return addLoggingHeaders(addVersionControlHeaders(
                    Response.status(pfme.getErrorResponse().getResponseCode())), requestId)
                    .entity(pfme.getErrorResponse()).build();
        }
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