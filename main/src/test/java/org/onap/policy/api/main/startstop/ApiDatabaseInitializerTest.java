/*-
 * ============LICENSE_START=======================================================
 * ONAP
 * ================================================================================
 * Copyright (C) 2020-2021 AT&T Intellectual Property. All rights reserved.
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
 * ============LICENSE_END=========================================================
 */

package org.onap.policy.api.main.startstop;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.fail;

import java.io.File;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.api.main.parameters.CommonTestData;
import org.onap.policy.common.parameters.ValidationResult;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;

public class ApiDatabaseInitializerTest {
    private static final String PARAM_FILE = "src/test/resources/parameters/ApiConfigParameters_Https.json";
    private static final CommonTestData COMMON_TEST_DATA = new CommonTestData();
    private static ApiParameterGroup params;
    private static PolicyModelsProvider provider;

    /**
     * Creates the DB and keeps it open.
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        COMMON_TEST_DATA.makeParameters(PARAM_FILE, "src/test/resources/parameters/ApiConfigParametersXXX.json", 6969);

        params = new StandardCoder().decode(new File(PARAM_FILE), ApiParameterGroup.class);
        ValidationResult result = params.validate();
        if (!result.isValid()) {
            fail(result.getResult());
        }

        // keep the DB open until the test completes
        provider = new PolicyModelsProviderFactory().createPolicyModelsProvider(params.getDatabaseProviderParameters());
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        provider.close();
    }

    @Test
    public void testInitializeApiDatabase() throws PolicyApiException {
        ApiDatabaseInitializer adi = new ApiDatabaseInitializer();
        assertThatCode(() -> adi.initializeApiDatabase(params)).doesNotThrowAnyException();

        // invoke it again - should still be OK
        assertThatCode(() -> adi.initializeApiDatabase(params)).doesNotThrowAnyException();
    }
}
