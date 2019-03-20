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

package org.onap.policy.api.main.rest.provider;

import java.io.IOException;
import java.util.Properties;
import javax.ws.rs.core.Response;
import org.onap.policy.common.utils.resources.ResourceUtils;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.provider.PolicyModelsProvider;
import org.onap.policy.models.provider.PolicyModelsProviderFactory;
import org.onap.policy.models.provider.PolicyModelsProviderParameters;
import org.onap.policy.models.tosca.authorative.concepts.PlainToscaServiceTemplate;
import org.onap.policy.models.tosca.legacy.concepts.LegacyGuardPolicy;

/**
 * Class to provide all kinds of legacy guard policy operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class LegacyGuardPolicyProvider {

    private PolicyModelsProvider modelsProvider;

    /**
     * Default constructor.
     */
    public LegacyGuardPolicyProvider() throws PfModelException {

        PolicyModelsProviderParameters parameters = new PolicyModelsProviderParameters();
        Properties prop = new Properties();
        try {
            prop.load(ResourceUtils.getResourceAsStream("PolicyModelsProviderParameters.properties"));
        } catch (IOException e) {
            throw new PfModelException(
                    Response.Status.INTERNAL_SERVER_ERROR, "error reading models provider config file", e);
        }
        parameters.setImplementation(prop.getProperty("implementation"));
        parameters.setDatabaseUrl(prop.getProperty("databaseUrl"));
        parameters.setPersistenceUnit(prop.getProperty("persistenceUnit"));

        modelsProvider = new PolicyModelsProviderFactory().createPolicyModelsProvider(parameters);
        modelsProvider.init();
    }

    /**
     * Retrieves a list of guard policies matching specified ID and version.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the PlainToscaServiceTemplate object
     */
    public PlainToscaServiceTemplate fetchGuardPolicies(String policyId, String policyVersion)
            throws PfModelException {
        // placeholder
        return new PlainToscaServiceTemplate();
    }

    /**
     * Creates a new guard policy.
     *
     * @param body the entity body of policy
     *
     * @return the PlainToscaServiceTemplate object
     */
    public PlainToscaServiceTemplate createGuardPolicy(LegacyGuardPolicy body) throws PfModelException {
        // placeholder
        return new PlainToscaServiceTemplate();
    }

    /**
     * Deletes the guard policies matching specified ID and version.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the PlainToscaServiceTemplate object
     */
    public PlainToscaServiceTemplate deleteGuardPolicies(String policyId, String policyVersion)
            throws PfModelException {
        // placeholder
        return new PlainToscaServiceTemplate();
    }
}