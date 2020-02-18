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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

/**
 * Class to provide all kinds of legacy operational policy operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class LegacyOperationalPolicyProvider extends CommonModelProvider {

    private static final PfConceptKey LEGACY_OPERATIONAL_TYPE =
            new PfConceptKey("onap.policies.controlloop.Operational", "1.0.0");

    /**
     * Default constructor.
     */
    public LegacyOperationalPolicyProvider() throws PfModelException {
        super();
    }

    /**
     * Retrieves a list of operational policies matching specified ID and version.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the LegacyOperationalPolicy object
     */
    public LegacyOperationalPolicy fetchOperationalPolicy(String policyId, String policyVersion)
            throws PfModelException {

        return modelsProvider.getOperationalPolicy(policyId, policyVersion);
    }

    /**
     * Retrieves a list of deployed operational policies in each pdp group.
     *
     * @param policyId the ID of the policy
     *
     * @return a list of deployed policies in each pdp group
     *
     * @throws PfModelException the PfModel parsing exception
     */
    public Map<Pair<String, String>, List<LegacyOperationalPolicy>> fetchDeployedOperationalPolicies(String policyId)
            throws PfModelException {

        return collectDeployedPolicies(policyId, LEGACY_OPERATIONAL_TYPE, modelsProvider::getOperationalPolicy,
                List::add, new ArrayList<>(5));
    }

    /**
     * Creates a new operational policy.
     *
     * @param body the entity body of policy
     *
     * @return the LegacyOperationalPolicy object
     */
    public LegacyOperationalPolicy createOperationalPolicy(LegacyOperationalPolicy body) throws PfModelException {

        return modelsProvider.createOperationalPolicy(body);
    }

    /**
     * Deletes the operational policies matching specified ID and version.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @return the LegacyOperationalPolicy object
     */
    public LegacyOperationalPolicy deleteOperationalPolicy(String policyId, String policyVersion)
            throws PfModelException {

        return modelsProvider.deleteOperationalPolicy(policyId, policyVersion);
    }
}