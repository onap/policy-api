package org.onap.policy.api.main.rest.provider;

import java.util.ArrayList;
import java.util.List;
import org.onap.policy.models.base.PfModelException;
import org.onap.policy.models.pdp.concepts.PdpGroup;
import org.onap.policy.models.tosca.authorative.concepts.ToscaPolicy;

public class CommonDeleteModelProvider extends CommonModelProvider {

    /**
     * Constructs the object.
     * @throws PfModelException if an error occurs
     */
    public CommonDeleteModelProvider() throws PfModelException {
        super();
    }

    /**
     * Constructs returned message for policy delete rule violation.
     *
     * @param policyId the ID of policy
     * @param policyVersion the version of policy
     * @param pdpGroups the list of pdp groups
     *
     * @return the constructed message
     */
    protected String constructDeleteViolationMessageGroups(
            String policyId, String policyVersion, List<PdpGroup> pdpGroups) {

        List<String> pdpGroupNameVersionList = new ArrayList<>();
        for (PdpGroup pdpGroup : pdpGroups) {
            pdpGroupNameVersionList.add(pdpGroup.getName() + ":" + pdpGroup.getVersion());
        }
        String deployedPdpGroups = String.join(",", pdpGroupNameVersionList);
        return "policy with ID " + policyId + ":" + policyVersion
                + " cannot be deleted as it is deployed in pdp groups " + deployedPdpGroups;
    }

    /**
     * Constructs returned message for policy type delete rule violation.
     *
     * @param policyTypeId the ID of policy type
     * @param policyTypeVersion the version of policy type
     * @param policies the list of policies that parameterizes specified policy type
     *
     * @return the constructed message
     */
    protected String constructDeleteViolationMessagePolicies(
            String policyTypeId, String policyTypeVersion, List<ToscaPolicy> policies) {

        List<String> policyNameVersionList = new ArrayList<>();
        for (ToscaPolicy policy : policies) {
            policyNameVersionList.add(policy.getName() + ":" + policy.getVersion());
        }
        String parameterizedPolicies = String.join(",", policyNameVersionList);
        return "policy type with ID " + policyTypeId + ":" + policyTypeVersion
                + " cannot be deleted as it is parameterized by policies " + parameterizedPolicies;
    }
}
