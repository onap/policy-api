/*-
 * ============LICENSE_START=======================================================
 * ONAP Policy API
 * ================================================================================
 * Copyright (C) 2019-2020 AT&T Intellectual Property. All rights reserved.
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
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.tuple.Pair;
import org.onap.policy.models.base.PfConceptKey;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.base.PfModelRuntimeException;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.pdp.concepts.PdpGroupFilter;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicyIdentifier;
import org.onap.policy.models.tosca.legacy.concepts.LegacyOperationalPolicy;

/**
 * Class to provide all kinds of legacy operational policy operations.
 *
 * @author Chenfei Gao (cgao@research.att.com)
 */
public class LegacyOperationalPolicyProvider extends CommonModelProvider {

    private static final String INVALID_POLICY_VERSION = "legacy policy version is not an integer";
    private static final String LEGACY_MINOR_PATCH_SUFFIX = ".0.0";
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

        if (policyVersion != null) {
            validNumber(policyVersion, INVALID_POLICY_VERSION);
        }
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

        return collectDeployedPolicies(
                policyId, LEGACY_OPERATIONAL_TYPE, modelsProvider::getOperationalPolicy, List::add, new ArrayList<>());
    }

    /**
     * Creates a new operational policy.
     *
     * @param body the entity body of policy
     *
     * @return the LegacyOperationalPolicy object
     */
    public LegacyOperationalPolicy createOperationalPolicy(LegacyOperationalPolicy body) throws PfModelException {

        validateOperationalPolicyVersion(body);
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

        validNumber(policyVersion, INVALID_POLICY_VERSION);
        validateDeleteEligibility(policyId, policyVersion);

        return modelsProvider.deleteOperationalPolicy(policyId, policyVersion);
    }

    /**
     * Validates whether specified policy can be deleted based on the rule that deployed policy cannot be deleted.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     *
     * @throws PfModelException the PfModel parsing exception
     */
    private void validateDeleteEligibility(String policyId, String policyVersion) throws PfModelException {

        List<ToscaPolicyIdentifier> policies = new ArrayList<>();
        policies.add(new ToscaPolicyIdentifier(policyId, policyVersion + LEGACY_MINOR_PATCH_SUFFIX));
        PdpGroupFilter pdpGroupFilter = PdpGroupFilter.builder().policyList(policies).build();

        List<PdpGroup> pdpGroups = modelsProvider.getFilteredPdpGroups(pdpGroupFilter);

        if (!pdpGroups.isEmpty()) {
            throw new PfModelException(Response.Status.CONFLICT,
                    constructDeletePolicyViolationMessage(policyId, policyVersion, pdpGroups));
        }
    }

    /**
     * Validates the specified version of the operational policy provided in the payload.
     *
     * @param body the operational policy payload
     *
     * @throws PfModelException on errors parsing PfModel
     */
    private void validateOperationalPolicyVersion(LegacyOperationalPolicy body) throws PfModelException {

        validateOperationalPolicyVersionExist(body);
        validateNoDuplicateVersionInDb(body);
    }

    /**
     * Validates whether the version of the operational policy is specified in the payload.
     *
     * @param body the operational policy payload
     *
     * @throws PfModelException on errors parsing PfModel
     */
    private void validateOperationalPolicyVersionExist(LegacyOperationalPolicy body) throws PfModelException {

        if (body.getPolicyVersion() == null) {
            String errMsg = "mandantory field 'policy-version' is missing in the policy: " + body.getPolicyId();
            throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errMsg);
        }
    }

    /**
     * Validates that there is no duplicate version of the operational policy which is already stored in the database.
     *
     * @param body the operational policy payload
     *
     * @throws PfModelException on errors parsing PfModel
     */
    private void validateNoDuplicateVersionInDb(LegacyOperationalPolicy body) throws PfModelException {

        try {
            modelsProvider.getOperationalPolicy(body.getPolicyId(), body.getPolicyVersion());
        } catch (PfModelRuntimeException exc) {
            if (exc.getErrorResponse().getResponseCode() == Response.Status.BAD_REQUEST
                    && exc.getErrorResponse().getErrorMessage().contains("no policy found")) {
                return;
            } else {
                throw new PfModelException(Response.Status.INTERNAL_SERVER_ERROR, "unexpected runtime error", exc);
            }
        }

        // There is one duplicate version stored in the DB.
        // Try to get the latest version
        LegacyOperationalPolicy latest = modelsProvider.getOperationalPolicy(body.getPolicyId(), null);
        final String[] versionArray = latest.getPolicyVersion().split("\\.");
        String errMsg = "operational policy " + body.getPolicyId() + ":" + body.getPolicyVersion()
            + " already exists; its latest version is " + versionArray[0];
        throw new PfModelException(Response.Status.NOT_ACCEPTABLE, errMsg);
    }
}