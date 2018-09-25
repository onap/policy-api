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

import java.util.Arrays;
import org.onap.policy.api.main.PolicyApiException;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.api.main.parameters.ApiParameterHandler;
import org.onap.policy.common.logging.flexlogger.FlexLogger;
import org.onap.policy.common.logging.flexlogger.Logger;

/**
 * This class initiates ONAP Policy Framework policy api.
 *
 */
public class Main {
    private static final Logger LOGGER = FlexLogger.getLogger(Main.class);

    // The policy api Activator that activates the policy api service
    private ApiActivator activator;

    // The parameters read in from JSON
    private ApiParameterGroup parameterGroup;

    /**
     * Instantiates the policy api service.
     *
     * @param args the command line arguments
     */
    public Main(final String[] args) {
        final String argumentString = Arrays.toString(args);
        LOGGER.info("Starting policy api service with arguments - " + argumentString);

        // Check the arguments
        final ApiCommandLineArguments arguments = new ApiCommandLineArguments();
        try {
            // The arguments return a string if there is a message to print and we should exit
            final String argumentMessage = arguments.parse(args);
            if (argumentMessage != null) {
                LOGGER.info(argumentMessage);
                return;
            }

            // Validate that the arguments are sane
            arguments.validate();
        } catch (final PolicyApiException e) {
            LOGGER.error("start of policy api service failed", e);
            return;
        }

        // Read the parameters
        try {
            parameterGroup = new ApiParameterHandler().getParameters(arguments);
        } catch (final Exception e) {
            LOGGER.error("start of policy api service failed", e);
            return;
        }

        // Now, create the activator for the policy api service
        activator = new ApiActivator(parameterGroup);

        // Start the activator
        try {
            activator.initialize();
        } catch (final PolicyApiException e) {
            LOGGER.error("start of policy api service failed, used parameters are " + Arrays.toString(args),
                    e);
            return;
        }

        // Add a shutdown hook to shut everything down in an orderly manner
        Runtime.getRuntime().addShutdownHook(new PolicyApiShutdownHookClass());
        LOGGER.info("Started policy api service");
    }

    /**
     * Get the parameters specified in JSON.
     *
     * @return the parameters
     */
    public ApiParameterGroup getParameters() {
        return parameterGroup;
    }

    /**
     * Shut down Execution.
     *
     * @throws PolicyApiException on shutdown errors
     */
    public void shutdown() throws PolicyApiException {
        // clear the parameterGroup variable
        parameterGroup = null;

        // clear the api activator
        if (activator != null) {
            activator.terminate();
        }
    }

    /**
     * The Class PolicyApiShutdownHookClass terminates the policy api service when its run method is
     * called.
     */
    private class PolicyApiShutdownHookClass extends Thread {
        /*
         * (non-Javadoc)
         *
         * @see java.lang.Runnable#run()
         */
        @Override
        public void run() {
            try {
                // Shutdown the policy api service and wait for everything to stop
                activator.terminate();
            } catch (final PolicyApiException e) {
                LOGGER.warn("error occured during shut down of the policy api service", e);
            }
        }
    }

    /**
     * The main method.
     *
     * @param args the arguments
     */
    public static void main(final String[] args) {
        new Main(args);
    }
}
