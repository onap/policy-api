/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Modifications Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2021 Bell Canada. All rights reserved.
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

package org.onap.policy.api.main.startstop;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.api.main.rest.ApiRestController;
import org.onap.policy.api.main.rest.aaf.AafApiFilter;
import org.onap.policy.common.endpoints.http.server.RestServer;
import org.onap.policy.common.parameters.ParameterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class wraps a distributor so that it can be activated as a complete service together with all its api and
 * forwarding handlers.
 */
@RequiredArgsConstructor
public class ApiActivator {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiActivator.class);

    @Getter
    @Setter
    private static boolean alive = false;

    @Getter
    private final ApiParameterGroup parameterGroup;

    private RestServer restServer;

    /**
     * Initialize api as a complete service.
     *
     * @throws PolicyApiException on errors in initializing the service
     */
    public void initialize() throws PolicyApiException {
        LOGGER.debug("Policy api starting as a service . . .");
        startApiRestServer();
        registerToParameterService(parameterGroup);
        ApiActivator.setAlive(true);
        LOGGER.debug("Policy api started as a service");
    }

    /**
     * Starts the api rest server using configuration parameters.
     *
     * @throws PolicyApiException if server start fails
     */
    private void startApiRestServer() throws PolicyApiException {
        parameterGroup.getRestServerParameters().setName(parameterGroup.getName());
        restServer = new RestServer(parameterGroup.getRestServerParameters(), AafApiFilter.class,
                        ApiRestController.class);
        if (!restServer.start()) {
            throw new PolicyApiException("Failed to start api rest server. Check log for more details...");
        }
    }

    /**
     * Terminate policy api.
     *
     * @throws PolicyApiException on termination errors
     */
    public void terminate() throws PolicyApiException {
        try {
            deregisterToParameterService(parameterGroup);

            if (ApiActivator.isAlive()) {
                ApiActivator.setAlive(false);
            }

            // Stop the api rest server
            if (restServer.isAlive()) {
                restServer.stop();
            }
        } catch (final Exception exp) {
            throw new PolicyApiException("Policy api service termination failed", exp);
        }
    }

    /**
     * Method to register the parameters to Common Parameter Service.
     *
     * @param apiParameterGroup the api parameter group
     */
    public void registerToParameterService(final ApiParameterGroup apiParameterGroup) {
        ParameterService.register(apiParameterGroup, true);
    }

    /**
     * Method to deregister the parameters from Common Parameter Service.
     *
     * @param apiParameterGroup the api parameter group
     */
    public void deregisterToParameterService(final ApiParameterGroup apiParameterGroup) {
        if (ParameterService.contains(apiParameterGroup.getName())) {
            ParameterService.deregister(apiParameterGroup.getName());
        }
    }
}
