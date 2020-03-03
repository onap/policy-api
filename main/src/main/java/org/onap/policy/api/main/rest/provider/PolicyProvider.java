/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
 * Modifications Copyright (C) 2020 Nordix Foundation.
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

import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaServiceTemplate;

/**
 * Class to provide all kinds of policy operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class PolicyProvider extends CommonModelProvider {

    /**
     * Default constructor.
     */
    public PolicyProvider() throws PfModelException {
        super();
    }

    /**
     * Retrieves a list of policies matching specified ID and version of both policy type and policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate fetchPolicies(String policyTypeId, String policyTypeVersion, String policyId,
            String policyVersion) throws PfModelException {

        return getFilteredPolicies(policyTypeId, policyTypeVersion, policyId, policyVersion);
    }

    /**
     * Retrieves a list of policies with the latest versions that match specified policy type id and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of the policy
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate fetchLatestPolicies(String policyTypeId, String policyTypeVersion, String policyId)
            throws PfModelException {

        return getFilteredPolicies(policyTypeId, policyTypeVersion, policyId, ToscaPolicyFilter.LATEST_VERSION);
    }

    /**
     * Creates one or more new policies for the same policy type ID and version.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param body the entity body of polic(ies)
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate createPolicy(String policyTypeId, String policyTypeVersion, ToscaServiceTemplate body)
            throws PfModelException {

        return modelsProvider.createPolicies(body);
    }

    /**
     * Creates one or more new policies.
     *
     * @param body the entity body of policy
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate createPolicies(ToscaServiceTemplate body) throws PfModelException {
        return modelsProvider.createPolicies(body);
    }

    /**
     * Deletes the policy matching specified ID and version of both policy type and policy.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the ToscaServiceTemplate object
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public ToscaServiceTemplate deletePolicy(String policyTypeId, String policyTypeVersion, String policyId,
            String policyVersion) throws PfModelException {

        return modelsProvider.deletePolicy(policyId, policyVersion);
    }

    /**
     * Retrieves the specified version of the policy.
     *
     * @param policyTypeName the name of the policy type
     * @param policyTypeVersion the version of the policy type
     * @param policyName the name of the policy
     * @param policyVersion the version of the policy
     *
     * @return the TOSCA service template containing the specified version of the policy
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private ToscaServiceTemplate getFilteredPolicies(String policyTypeName, String policyTypeVersion, String policyName,
            String policyVersion) throws PfModelException {

        ToscaPolicyFilter policyFilter = ToscaPolicyFilter.builder().name(policyName).version(policyVersion)
                .type(policyTypeName).typeVersion(policyTypeVersion).build();
        return modelsProvider.getFilteredPolicies(policyFilter);
    }
}
