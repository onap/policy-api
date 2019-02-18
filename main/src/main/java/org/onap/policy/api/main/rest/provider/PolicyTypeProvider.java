package org.onap.policy.api.main.rest.provider;

import org.onap.policy.api.main.rest.model.PolicyType;
import org.onap.policy.api.main.rest.model.PolicyTypeList;
import org.onap.policy.api.main.rest.model.VersionList;

/**
 * 
 * Class for all kinds of policy type operations
 *
 */
public class PolicyTypeProvider {
   
    private static final String POST_OK = "Successfully created";
    
    /**
     * Retrieves current policy types stored in Policy Framework
     * 
     * @return the PolicyTypeList object containing a list of current policy types
     */
    public PolicyTypeList fetchCurrentPolicyTypes() {
        // placeholder
        return new PolicyTypeList();
    }
    
    /**
     * Creates a new policy type
     * 
     * @param body the policy type body
     * 
     * @return a string message indicating the operation results 
     */
    public String createPolicyType(PolicyType body) {
        // placeholder
        return POST_OK;
    }
   
    /**
     * Retrieves a version list of particular policy type ID
     * 
     * @param policyTypeId the policy type ID
     * 
     * @return the VersionList object containing a list of all available versions for the policy type ID
     */
    public VersionList fetchVersionListPerPolicyType(String policyTypeId) {
        // placeholder
        return new VersionList();
    }
    
    /**
     * Retrieves all versions of particular policy type ID
     * 
     * @param policyTypeId the policy type ID
     * 
     * @return the PolicyTypeList object containing a list of all versions of policy type ID
     */
    public PolicyTypeList fetchAllVersionsPerPolicyType(String policyTypeId) {
        // placeholder
        return new PolicyTypeList();
    }
    
    /**
     * Retrieves one version of policy Type ID 
     * 
     * @param policyTypeId the policy type ID
     * @param versionId the version ID
     * 
     * @return the PolicyTypeList object containing a version of policy type matching policy type ID and version ID
     */
    public PolicyTypeList fetchOneVersionPerPolicyType(String policyTypeId, String versionId) {
        // placeholder
        return new PolicyTypeList();
    }
}
