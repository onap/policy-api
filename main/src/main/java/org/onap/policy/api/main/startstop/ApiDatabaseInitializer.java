/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019 Nordix Foundation.
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

import java.util.LinkedHashMap;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

/**
 * This class creates initial policy types in the database.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ApiDatabaseInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDatabaseInitializer.class);

    private StandardCoder standardCoder;
    private PolicyModelsProviderFactory factory;

    /**
     * Constructs the object.
     */
    public ApiDatabaseInitializer() {
        factory = new PolicyModelsProviderFactory();
        standardCoder = new StandardCoder();
    }

    /**
     * Initializes database by preloading policy types.
     *
     * @param apiParameterGroup the apiParameterGroup parameters
     * @throws PolicyApiException in case of errors.
     */
    public void initializeApiDatabase(final ApiParameterGroup apiParameterGroup)
            throws PolicyApiException {

        try (PolicyModelsProvider databaseProvider =
                factory.createPolicyModelsProvider(apiParameterGroup.getDatabaseProviderParameters())) {
            ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();
            serviceTemplate.setPolicyTypes(new LinkedHashMap<String, ToscaPolicyType>());
            serviceTemplate.setToscaDefinitionsVersion("tosca_simple_yaml_1_0_0");
            for (String pt : apiParameterGroup.getPreloadPolicyTypes()) {
                String policyTypeAsStringYaml = ResourceUtils.getResourceAsString(pt);
                if (policyTypeAsStringYaml == null) {
                    throw new PolicyApiException("Preloading policy type cannot be found: " + pt);
                }

                Object yamlObject = new Yaml().load(policyTypeAsStringYaml);
                String policyTypeAsString = new StandardCoder().encode(yamlObject);

                ToscaServiceTemplate singlePolicyType =
                        standardCoder.decode(policyTypeAsString, ToscaServiceTemplate.class);
                if (singlePolicyType == null) {
                    throw new PolicyApiException("Error deserializing policy type from file: " + pt);
                }
                // Consolidate policy types
                serviceTemplate.getPolicyTypes().putAll(singlePolicyType.getPolicyTypes());
            }
            ToscaServiceTemplate createdPolicyTypes = databaseProvider.createPolicyTypes(serviceTemplate);
            if (createdPolicyTypes == null) {
                throw new PolicyApiException("Error preloading policy types: " + serviceTemplate);
            } else {
                LOGGER.debug("Created initial policy types in DB - {}", createdPolicyTypes);
            }
        } catch (final PfModelException | CoderException exp) {
            throw new PolicyApiException(exp);
        }
    }
}
