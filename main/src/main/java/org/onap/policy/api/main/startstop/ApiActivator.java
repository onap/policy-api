/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
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

import org.onap.policy.api.main.rest.ApiRestServer;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;
import org.onap.policy.common.parameters.ParameterService;
import org.onap.policy.api.main.PolicyApiException;
import org.onap.policy.api.main.parameters.ApiParameterGroup;

/**
 * This class wraps a distributor so that it can be activated as a complete service together with all its api
 * and forwarding handlers.
 */
public class ApiActivator {
    // The logger for this class
    private static final Logger LOGGER = FlexLogger.getLogger(ApiActivator.class);

    // The parameters of this policy api activator
    private final ApiParameterGroup apiParameterGroup;

    private static boolean alive = false;

    private ApiRestServer restServer;

    /**
     * Instantiate the activator for policy api as a complete service.
     *
     * @param apiParameterGroup the parameters for the api service
     */
    public ApiActivator(final ApiParameterGroup apiParameterGroup) {
        this.apiParameterGroup = apiParameterGroup;
    }

    /**
     * Initialize api as a complete service.
     *
     * @throws PolicyApiException on errors in initializing the service
     */
    @SuppressWarnings("unchecked")
    public void initialize() throws PolicyApiException {
        LOGGER.debug("Policy api starting as a service . . .");
        startApiRestServer();
        registerToParameterService(apiParameterGroup);
        ApiActivator.setAlive(true);
        LOGGER.debug("Policy api started as a service");
    }

    /**
     * Starts the api rest server using configuration parameters.
     *
     * @throws PolicyApiException if server start fails
     */
    private void startApiRestServer() throws PolicyApiException {
        apiParameterGroup.getRestServerParameters().setName(apiParameterGroup.getName());
        restServer = new ApiRestServer(apiParameterGroup.getRestServerParameters());
        if (!restServer.start()) {
            throw new PolicyApiException(
                    "Failed to start api rest server. Check log for more details...");
        }
    }

    /**
     * Terminate policy api.
     *
     * @throws PolicyApiException on termination errors
     */
    public void terminate() throws PolicyApiException {
        try {
            deregisterToParameterService(apiParameterGroup);
            ApiActivator.setAlive(false);

            // Stop the api rest server
            restServer.stop();
        } catch (final Exception exp) {
            LOGGER.error("Policy api service termination failed", exp);
            throw new PolicyApiException(exp.getMessage(), exp);
        }
    }

    /**
     * Get the parameters used by the activator.
     *
     * @return the parameters of the activator
     */
    public ApiParameterGroup getParameterGroup() {
        return apiParameterGroup;
    }

    /**
     * Method to register the parameters to Common Parameter Service.
     *
     * @param apiParameterGroup the api parameter group
     */
    public void registerToParameterService(final ApiParameterGroup apiParameterGroup) {
        ParameterService.register(apiParameterGroup);
    }

    /**
     * Method to deregister the parameters from Common Parameter Service.
     *
     * @param apiParameterGroup the api parameter group
     */
    public void deregisterToParameterService(final ApiParameterGroup apiParameterGroup) {
        ParameterService.deregister(apiParameterGroup.getName());
    }

    /**
     * Returns the alive status of api service.
     *
     * @return the alive
     */
    public static boolean isAlive() {
        return alive;
    }

    /**
     * Change the alive status of api service.
     *
     * @param status the status
     */
    public static void setAlive(final boolean status) {
        alive = status;
    }
}
