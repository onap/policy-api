/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2019-2020 Nordix Foundation.
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.api.main.parameters.ApiParameterGroup;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardYamlCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.tosca.authorative.concepts.ToscaDataType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.onap.policy.models.tosca.authorative.concepts.ToscaTopologyTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates initial policy types in the database.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ApiDatabaseInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDatabaseInitializer.class);

    private static StandardYamlCoder coder = new StandardYamlCoder();
    private PolicyModelsProviderFactory factory;

    /**
     * Constructs the object.
     */
    public ApiDatabaseInitializer() {
        factory = new PolicyModelsProviderFactory();
    }

    /**
     * Initializes database by preloading policy types and policies.
     *
     * @param apiParameterGroup the apiParameterGroup parameters
     * @throws PolicyApiException in case of errors.
     */
    public void initializeApiDatabase(final ApiParameterGroup apiParameterGroup) throws PolicyApiException {

        try (PolicyModelsProvider databaseProvider =
                factory.createPolicyModelsProvider(apiParameterGroup.getDatabaseProviderParameters())) {
            ToscaServiceTemplate serviceTemplate = new ToscaServiceTemplate();
            serviceTemplate.setDataTypes(new LinkedHashMap<String, ToscaDataType>());
            serviceTemplate.setPolicyTypes(new LinkedHashMap<String, ToscaPolicyType>());
            serviceTemplate.setToscaDefinitionsVersion("tosca_simple_yaml_1_0_0");

            ToscaServiceTemplate createdPolicyTypes =
                    preloadPolicyTypes(serviceTemplate, apiParameterGroup.getPreloadPolicyTypes(), databaseProvider);

            createdPolicyTypes.setToscaTopologyTemplate(new ToscaTopologyTemplate());
            createdPolicyTypes.getToscaTopologyTemplate().setPolicies(new LinkedList<Map<String, ToscaPolicy>>());

            preloadPolicies(createdPolicyTypes, apiParameterGroup.getPreloadPolicies(), databaseProvider);
        } catch (final PolicyApiException | PfModelException | CoderException exp) {
            throw new PolicyApiException(exp);
        }
    }

    private ToscaServiceTemplate preloadPolicyTypes(ToscaServiceTemplate serviceTemplate, List<String> policyTypes,
            PolicyModelsProvider databaseProvider) throws PolicyApiException, CoderException, PfModelException {

        for (String pt : policyTypes) {
            String policyTypeAsStringYaml = ResourceUtils.getResourceAsString(pt);
            if (policyTypeAsStringYaml == null) {
                throw new PolicyApiException("Preloading policy type cannot be found: " + pt);
            }

            ToscaServiceTemplate singlePolicyType =
                    coder.decode(policyTypeAsStringYaml, ToscaServiceTemplate.class);
            if (singlePolicyType == null) {
                throw new PolicyApiException("Error deserializing policy type from file: " + pt);
            }

            // Consolidate data types and policy types
            if (singlePolicyType.getDataTypes() != null) {
                serviceTemplate.getDataTypes().putAll(singlePolicyType.getDataTypes());
            }
            serviceTemplate.getPolicyTypes().putAll(singlePolicyType.getPolicyTypes());
        }
        ToscaServiceTemplate createdPolicyTypes = databaseProvider.createPolicyTypes(serviceTemplate);
        LOGGER.debug("Created initial policy types in DB - {}", createdPolicyTypes);
        return createdPolicyTypes;
    }

    private void preloadPolicies(ToscaServiceTemplate serviceTemplate, List<String> policies,
            PolicyModelsProvider databaseProvider) throws PolicyApiException, CoderException, PfModelException {

        for (String policy : policies) {
            String policyAsStringYaml = ResourceUtils.getResourceAsString(policy);
            if (policyAsStringYaml == null) {
                throw new PolicyApiException("Preloading policy cannot be found: " + policy);
            }

            ToscaServiceTemplate singlePolicy =
                    coder.decode(policyAsStringYaml, ToscaServiceTemplate.class);
            if (singlePolicy == null) {
                throw new PolicyApiException("Error deserializing policy from file: " + policy);
            }

            // Consolidate policies
            serviceTemplate.getToscaTopologyTemplate().getPolicies()
                .addAll(singlePolicy.getToscaTopologyTemplate().getPolicies());
        }
        ToscaServiceTemplate createdPolicies = databaseProvider.createPolicies(serviceTemplate);
        LOGGER.debug("Created initial policies in DB - {}", createdPolicies);
    }
}
