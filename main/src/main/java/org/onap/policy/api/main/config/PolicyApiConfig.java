/*-
 * ============LICENSE_START=======================================================
 *  Copyright (C) 2022 Bell Canada. All rights reserved.
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

package org.onap.policy.api.main.config;

import org.onap.policy.api.main.rest.StatisticsReport;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PolicyApiConfig {

    @Value("${policy-api.name}")
    private String groupName;
    @Value("${database.name}")
    private String databaseName;
    @Value("${database.implementation}")
    private String databaseImplementation;
    @Value("${database.driver}")
    private String databaseDriver;
    @Value("${database.url}")
    private String databaseUrl;
    @Value("${database.user}")
    private String databaseUser;
    @Value("${database.password}")
    private String databasePassword;
    @Value("${database.persistenceUnit}")
    private String databasePersistenceUnit;

    /**
     * Initialize database configuration.
     *
     * @return PolicyModelsProvider
     * @throws PfModelException Policy exception
     */
    @Bean(destroyMethod = "close")
    public PolicyModelsProvider policyModelsProvider() throws PfModelException {
        PolicyModelsProviderParameters modelsProviderParameters = new PolicyModelsProviderParameters();
        modelsProviderParameters.setName(databaseName);
        modelsProviderParameters.setImplementation(databaseImplementation);
        modelsProviderParameters.setDatabaseDriver(databaseDriver);
        modelsProviderParameters.setDatabaseUrl(databaseUrl);
        modelsProviderParameters.setDatabaseUser(databaseUser);
        modelsProviderParameters.setDatabasePassword(databasePassword);
        modelsProviderParameters.setPersistenceUnit(databasePersistenceUnit);
        modelsProviderParameters.setDatabaseDriver(databaseDriver);
        return new PolicyModelsProviderFactory().createPolicyModelsProvider(modelsProviderParameters);
    }

    @Bean
    public StatisticsReport createStatisticsReport() {
        return new StatisticsReport();
    }
}