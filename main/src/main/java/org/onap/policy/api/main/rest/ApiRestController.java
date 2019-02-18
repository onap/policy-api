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
import io.swagger.annotations.Info;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.onap.policy.api.main.rest.model.PolicyType;
import org.onap.policy.api.main.rest.model.PolicyTypeList;
import org.onap.policy.api.main.rest.model.StatisticsReport;
import org.onap.policy.api.main.rest.model.VersionList;
import org.onap.policy.api.main.rest.provider.HealthCheckProvider;
import org.onap.policy.api.main.rest.provider.PolicyTypeProvider;
import org.onap.policy.api.main.rest.provider.StatisticsProvider;
import org.onap.policy.common.endpoints.report.HealthCheckReport;

/**
 * Class to provide REST API services.
 */
@Path("/policy/api/v1")
@Api(value = "Policy Public API", authorizations = { @Authorization(value = "basicAuth") })
@SwaggerDefinition(
        info = @Info(description = "Policy Api Service", version = "v2.0", title = "Policy Api"),
        consumes = { MediaType.APPLICATION_JSON }, produces = { MediaType.APPLICATION_JSON },
        schemes = { SwaggerDefinition.Scheme.HTTP, SwaggerDefinition.Scheme.HTTPS },
        tags = { @Tag(name = "policy-api", description = "Policy Api Service Operations") })
public class ApiRestController {

    @GET
    @Path("/healthcheck")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Perform a system healthcheck",
            notes = "Returns healthy status of the Policy Api component", response = HealthCheckReport.class,
            tags= { "HealthCheck", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = HealthCheckReport.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
            @ApiResponse(code = 500, message = "Error", response = Void.class)
    })
    public Response getHealthCheck() {
        return Response.status(Response.Status.OK).entity(new HealthCheckProvider().performHealthCheck()).build();
    }
    
    @GET
    @Path("/statistics")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch current statistics",
            notes = "Returns current statistics of the Policy API component",
            response = StatisticsReport.class,
            tags= { "Statistics", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successsful", response = StatisticsReport.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
            @ApiResponse(code = 500, message = "Error", response = Void.class)
    })
    public Response getStatistics() {
        return Response.status(Response.Status.OK).entity(new StatisticsProvider().fetchCurrentStatistics()).build();
    }
    
    @GET
    @Path("/policytypes")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch existing policy types",
            notes = "Returns existing policy types stored in Policy Framework",
            response = PolicyTypeList.class,
            tags= { "PolicyType", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = PolicyTypeList.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
            @ApiResponse(code = 500, message = "Error", response = Void.class)
    })
    public Response getAllPolicyTypes() {
        return Response.status(Response.Status.OK).entity(new PolicyTypeProvider().fetchCurrentPolicyTypes()).build();
    }
    
    @GET
    @Path("/policytypes/{policyTypeId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch all available versions of a policy type",
            notes = "Returns a list of available versions for the specified policy type",
            response = VersionList.class,
            tags= { "PolicyType", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = VersionList.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
            @ApiResponse(code = 404, message = "Policy Type ID Not Found", response = Void.class),
            @ApiResponse(code = 500, message = "Error", response = Void.class)
    })
    public Response getVersionListPerPolicyType(@PathParam("policyTypeId") @ApiParam("ID of policy type") String policyTypeId) {
        return Response.status(Response.Status.OK).entity(new PolicyTypeProvider().fetchVersionListPerPolicyType(policyTypeId)).build();
    }
   
    @GET
    @Path("/policytypes/{policyTypeId}/versions")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch all versions of a policy type",
            notes = "Returns all versions for the specified policy type",
            response = PolicyTypeList.class,
            tags= { "PolicyType", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = PolicyTypeList.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
            @ApiResponse(code = 404, message = "Policy Type ID Not Found", response = Void.class),
            @ApiResponse(code = 500, message = "Error", response = Void.class)
    })
    public Response getAllVersionsPerPolicyType(@PathParam("policyTypeId") @ApiParam("ID of policy type") String policyTypeId) {
        return Response.status(Response.Status.OK).entity(new PolicyTypeProvider().fetchAllVersionsPerPolicyType(policyTypeId)).build();
    }
    
    @GET
    @Path("/policytypes/{policyTypeId}/versions/{versionId}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Fetch one particular version of a policy type",
            notes = "Returns a particular version for the specified policy type",
            response = PolicyTypeList.class,
            tags= { "PolicyType", })
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Successful", response = PolicyTypeList.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
            @ApiResponse(code = 404, message = "Policy Type ID or version ID Not Found", response = Void.class),
            @ApiResponse(code = 500, message = "Error", response = Void.class)
    })
    public Response getOneVersionPerPolicyType(@PathParam("policyTypeId") @ApiParam("ID of policy type") String policyTypeId,
                                               @PathParam("versionId") @ApiParam("ID of version") String versionId) {
        return Response.status(Response.Status.OK).entity(new PolicyTypeProvider().fetchOneVersionPerPolicyType(policyTypeId, versionId)).build();
    }
   
    @POST
    @Path("/policytypes")
    @Consumes(MediaType.APPLICATION_JSON)
    @ApiOperation(value = "Create a new policy type",
            notes = "Client should provide TOSCA body for the new policy type",
            response = Void.class,
            tags= { "PolicyType", })
    @ApiResponses(value = {
            @ApiResponse(code = 201, message = "Created", response = Void.class),
            @ApiResponse(code = 400, message = "Invalid Body", response = Void.class),
            @ApiResponse(code = 401, message = "Unauthorized", response = Void.class),
            @ApiResponse(code = 500, message = "Error", response = Void.class)
    })
    public Response createPolicyType(PolicyType body) {
        return Response.status(Response.Status.OK).entity(new PolicyTypeProvider().createPolicyType(body)).build();
    }
}
