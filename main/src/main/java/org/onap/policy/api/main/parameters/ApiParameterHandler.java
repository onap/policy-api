/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 * Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
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

package org.onap.policy.api.main.parameters;

import com.google.gson.GsonBuilder;
import java.io.FileReader;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.startstop.ApiCommandLineArguments;
import org.onap.policy.common.parameters.ValidationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class handles reading, parsing and validating of policy api parameters from JSON files.
 */
public class ApiParameterHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiParameterHandler.class);

    /**
     * Read the parameters from the parameter file.
     *
     * @param arguments the arguments passed to policy api
     * @return the parameters read from the configuration file
     * @throws PolicyApiException on parameter exceptions
     */
    public ApiParameterGroup getParameters(final ApiCommandLineArguments arguments)
            throws PolicyApiException {
        ApiParameterGroup apiParameterGroup = null;

        // Read the parameters
        try {
            // Read the parameters from JSON using Gson
            final var gson = new GsonBuilder().create();
            apiParameterGroup = gson.fromJson(new FileReader(arguments.getFullConfigurationFilePath()),
                    ApiParameterGroup.class);
        } catch (final Exception e) {
            final String errorMessage = "error reading parameters from \"" + arguments.getConfigurationFilePath()
                    + "\"\n" + "(" + e.getClass().getSimpleName() + "):" + e.getMessage();
            throw new PolicyApiException(errorMessage, e);
        }

        // The JSON processing returns null if there is an empty file
        if (apiParameterGroup == null) {
            final String errorMessage = "no parameters found in \"" + arguments.getConfigurationFilePath() + "\"";
            LOGGER.error(errorMessage);
            throw new PolicyApiException(errorMessage);
        }

        // validate the parameters
        final ValidationResult validationResult = apiParameterGroup.validate();
        if (!validationResult.isValid()) {
            String returnMessage =
                    "validation error(s) on parameters from \"" + arguments.getConfigurationFilePath() + "\"\n";
            returnMessage += validationResult.getResult();

            LOGGER.error(returnMessage);
            throw new PolicyApiException(returnMessage);
        }

        return apiParameterGroup;
    }
}
