/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2018 Samsung Electronics Co., Ltd. All rights reserved.
 *  Copyright (C) 2019, 2021 AT&T Intellectual Property. All rights reserved.
 *  Modifications Copyright (C) 2022 Nordix Foundation.
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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.startstop.ApiCommandLineArguments;
import org.onap.policy.common.utils.cmd.CommandLineException;

/**
 * Class to perform unit test of ApiParameterHandler.
 *
 */
public class TestApiParameterHandler {
    @Test
    public void testParameterHandlerNoParameterFile() throws PolicyApiException, CommandLineException {
        final String[] noArgumentString = {"-c", "parameters/NoParameterFile.json"};
        final ApiCommandLineArguments noArguments = new ApiCommandLineArguments();
        noArguments.parse(noArgumentString);

        try {
            new ApiParameterHandler().getParameters(noArguments);
            fail("test should throw an exception here");
        } catch (final Exception e) {
            assertTrue(e.getMessage().contains("FileNotFoundException"));
        }
    }

    @Test
    public void testParameterHandlerEmptyParameters() throws PolicyApiException, CommandLineException {
        final String[] emptyArgumentString = {"-c", "parameters/EmptyParameters.json"};
        final ApiCommandLineArguments emptyArguments = new ApiCommandLineArguments();
        emptyArguments.parse(emptyArgumentString);

        try {
            new ApiParameterHandler().getParameters(emptyArguments);
            fail("test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("no parameters found in \"parameters/EmptyParameters.json\"", e.getMessage());
        }
    }

    @Test
    public void testParameterHandlerBadParameters() throws PolicyApiException, CommandLineException {
        final String[] badArgumentString = {"-c", "parameters/BadParameters.json"};
        final ApiCommandLineArguments badArguments = new ApiCommandLineArguments();
        badArguments.parse(badArgumentString);

        try {
            new ApiParameterHandler().getParameters(badArguments);
            fail("test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("error reading parameters from \"parameters/BadParameters.json\"\n"
                    + "(JsonSyntaxException):java.lang.IllegalStateException: "
                    + "Expected a string but was BEGIN_ARRAY at line 2 column 15 path $.name", e.getMessage());
        }
    }

    @Test
    public void testParameterHandlerInvalidParameters() throws PolicyApiException, CommandLineException {
        final String[] invalidArgumentString = {"-c", "parameters/InvalidParameters.json"};
        final ApiCommandLineArguments invalidArguments = new ApiCommandLineArguments();
        invalidArguments.parse(invalidArgumentString);

        try {
            new ApiParameterHandler().getParameters(invalidArguments);
            fail("test should throw an exception here");
        } catch (final Exception e) {
            assertEquals("error reading parameters from \"parameters/InvalidParameters.json\"\n"
                    + "(JsonSyntaxException):java.lang.IllegalStateException: "
                    + "Expected a string but was BEGIN_ARRAY at line 2 column 15 path $.name", e.getMessage());
        }
    }

    @Test
    public void testParameterHandlerNoParameters() throws PolicyApiException, CommandLineException {
        final String[] noArgumentString = {"-c", "parameters/NoParameters.json"};
        final ApiCommandLineArguments noArguments = new ApiCommandLineArguments();
        noArguments.parse(noArgumentString);

        assertThatThrownBy(() -> new ApiParameterHandler().getParameters(noArguments))
            .hasMessageContaining("\"name\"", "is null");
    }

    @Test
    public void testParameterHandlerMinumumParameters() throws PolicyApiException, CommandLineException {
        final String[] minArgumentString = {"-c", "parameters/MinimumParameters.json"};
        final ApiCommandLineArguments minArguments = new ApiCommandLineArguments();
        minArguments.parse(minArgumentString);
        final ApiParameterGroup parGroup = new ApiParameterHandler().getParameters(minArguments);
        assertEquals(CommonTestData.API_GROUP_NAME, parGroup.getName());
    }

    @Test
    public void testApiParameterGroup() throws PolicyApiException, CommandLineException {
        final String[] apiConfigParameters = {"-c", "parameters/ApiConfigParameters_Https.json"};
        final ApiCommandLineArguments arguments = new ApiCommandLineArguments();
        arguments.parse(apiConfigParameters);
        final ApiParameterGroup parGroup = new ApiParameterHandler().getParameters(arguments);
        assertTrue(arguments.checkSetConfigurationFilePath());
        assertEquals(CommonTestData.API_GROUP_NAME, parGroup.getName());
    }

    @Test
    public void testApiParameterGroup_Postgres() throws PolicyApiException, CommandLineException {
        final String[] apiConfigParameters = {"-c", "parameters/ApiConfigParameters_Postgres.json"};
        final ApiCommandLineArguments arguments = new ApiCommandLineArguments();
        arguments.parse(apiConfigParameters);
        final ApiParameterGroup parGroup = new ApiParameterHandler().getParameters(arguments);
        assertTrue(arguments.checkSetConfigurationFilePath());
        assertEquals(CommonTestData.API_GROUP_NAME, parGroup.getName());
    }

    @Test
    public void testApiParameterGroup_Invalid() throws PolicyApiException, CommandLineException {
        final String[] apiConfigParameters = {"-c", "parameters/ApiConfigParameters_InvalidName.json"};
        final ApiCommandLineArguments arguments = new ApiCommandLineArguments();
        arguments.parse(apiConfigParameters);

        assertThatThrownBy(() -> new ApiParameterHandler().getParameters(arguments)).hasMessageContaining("\"name\"",
                        "is null");
    }

    @Test
    public void testApiVersion() throws PolicyApiException, CommandLineException {
        final String[] apiConfigParameters = {"-v"};
        final ApiCommandLineArguments arguments = new ApiCommandLineArguments();
        final String version = arguments.parse(apiConfigParameters);
        assertTrue(version.startsWith("ONAP Policy Framework Api Service"));
    }

    @Test
    public void testApiHelp() throws PolicyApiException, CommandLineException {
        final String[] apiConfigParameters = {"-h"};
        final ApiCommandLineArguments arguments = new ApiCommandLineArguments();
        final String help = arguments.parse(apiConfigParameters);
        assertTrue(help.startsWith("usage:"));
    }

    @Test
    public void testApiInvalidOption() throws PolicyApiException {
        final String[] apiConfigParameters = {"-d"};
        final ApiCommandLineArguments arguments = new ApiCommandLineArguments();
        try {
            arguments.parse(apiConfigParameters);
        } catch (final Exception exp) {
            assertTrue(exp.getMessage().startsWith("invalid command line arguments specified"));
        }
    }
}
