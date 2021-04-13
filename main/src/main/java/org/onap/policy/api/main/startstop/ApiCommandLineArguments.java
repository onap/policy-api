/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Modifications Copyright (C) 2021 Nordix Foundation.
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

import org.onap.policy.api.main.exception.PolicyApiRuntimeException;
import org.onap.policy.common.utils.cmd.CommandLineArgumentsHandler;
import org.onap.policy.common.utils.cmd.CommandLineException;

/**
 * This class reads and handles command line parameters for the policy api main program.
 */
public class ApiCommandLineArguments extends CommandLineArgumentsHandler {
    private static final String API_COMPONENT = "policy api";

    /**
     * Construct the options for the CLI editor from super.
     */
    public ApiCommandLineArguments() {
        super(Main.class.getName(), API_COMPONENT);
    }

    /**
     * Construct the options for the CLI editor and parse in the given arguments.
     *
     * @param args The command line arguments
     */
    public ApiCommandLineArguments(final String[] args) {
        this();

        try {
            parse(args);
        } catch (final CommandLineException e) {
            throw new PolicyApiRuntimeException("parse error on policy api parameters", e);
        }
    }
}
