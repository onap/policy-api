/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
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

import java.util.ArrayList;
import java.util.Map;
import org.onap.policy.api.main.exception.PolicyApiException;
import org.onap.policy.common.utils.coder.CoderException;
import org.onap.policy.common.utils.coder.StandardCoder;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyType;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class creates initial policy types in the database.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class ApiDatabaseInitializer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiDatabaseInitializer.class);

    private StandardCoder standardCoder;
    private PolicyModelsProviderFactory factory;

    private static final String[] PRELOAD_POLICYTYPES = {
        "preloadedPolicyTypes/onap.policies.monitoring.cdap.tca.hi.lo.app.json",
        "preloadedPolicyTypes/onap.policies.monitoring.dcaegen2.collectors.datafile.datafile-app-server.json",
        "preloadedPolicyTypes/onap.policies.optimization.AffinityPolicy.json",
        "preloadedPolicyTypes/onap.policies.optimization.DistancePolicy.json",
        "preloadedPolicyTypes/onap.policies.optimization.HpaPolicy.json",
        "preloadedPolicyTypes/onap.policies.optimization.OptimizationPolicy.json",
        "preloadedPolicyTypes/onap.policies.optimization.PciPolicy.json",
        "preloadedPolicyTypes/onap.policies.optimization.QueryPolicy.json",
        "preloadedPolicyTypes/onap.policies.optimization.SubscriberPolicy.json",
        "preloadedPolicyTypes/onap.policies.optimization.Vim_fit.json",
        "preloadedPolicyTypes/onap.policies.optimization.VnfPolicy.json",
        "preloadedPolicyTypes/onap.policies.controlloop.guard.Blacklist.json",
        "preloadedPolicyTypes/onap.policies.controlloop.guard.FrequencyLimiter.json",
        "preloadedPolicyTypes/onap.policies.controlloop.guard.MinMax.json",
        "preloadedPolicyTypes/onap.policies.controlloop.Operational.json"
    };

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
     * @param policyModelsProviderParameters the database parameters
     * @throws PolicyApiException in case of errors.
     */
    public void initializeApiDatabase(final PolicyModelsProviderParameters policyModelsProviderParameters)
            throws PolicyApiException {

        try (PolicyModelsProvider databaseProvider =
                factory.createPolicyModelsProvider(policyModelsProviderParameters)) {
            ToscaServiceTemplate policyTypes = new ToscaServiceTemplate();
            policyTypes.setPolicyTypes(new ArrayList<Map<String,ToscaPolicyType>>());
            policyTypes.setToscaDefinitionsVersion("tosca_simple_yaml_1_0_0");
            for (String pt : PRELOAD_POLICYTYPES) {
                String policyTypeAsString = ResourceUtils.getResourceAsString(pt);
                if (policyTypeAsString == null) {
                    throw new PolicyApiException("Preloading policy type cannot be found: " + pt);
                }
                ToscaServiceTemplate singlePolicyType = standardCoder.decode(policyTypeAsString,
                        ToscaServiceTemplate.class);
                if (singlePolicyType == null) {
                    throw new PolicyApiException("Error deserializing policy type from file: " + pt);
                }
                // Consolidate policy types
                for (Map<String, ToscaPolicyType> eachPolicyType : singlePolicyType.getPolicyTypes()) {
                    policyTypes.getPolicyTypes().add(eachPolicyType);
                }
            }
            ToscaServiceTemplate createdPolicyTypes = databaseProvider.createPolicyTypes(policyTypes);
            if (createdPolicyTypes == null) {
                throw new PolicyApiException("Error preloading policy types: " + policyTypes);
            } else {
                LOGGER.debug("Created initial policy types in DB - {}", createdPolicyTypes);
            }
        } catch (final PfModelException | CoderException exp) {
            throw new PolicyApiException(exp);
        }
    }
}
