package org.onap.policy.api.main.rest.model;

import java.util.List;
import java.util.Map;

/**
 * Class to represent policy type list 
 */
public class PolicyTypeList {

    private List<Map<String, PolicyTypeLite>> policyTypes;
   
    /**
     * Returns the policyTypes of this {@link PolicyTypeList} instance.
     * 
     * @return the policyTypes
     */
    public List<Map<String, PolicyTypeLite>> getPolicyTypes() {
        return policyTypes;
    }
    
    /**
     * Set policyTypes in this {@link PolicyTypeList} instance.
     * 
     * @param policyTypes the policyTypes to set
     */
    public void setPolicyTypes(List<Map<String, PolicyTypeLite>> policyTypes) {
        this.policyTypes = policyTypes;
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("PolicyTypeList [policyTypes=(");
        for (Map<String, PolicyTypeLite> policyType : getPolicyTypes()) {
            for (Map.Entry<String, PolicyTypeLite> entry : policyType.entrySet()) {
                builder.append(entry.getKey());
                builder.append(":");
                builder.append(entry.getValue().toString());
            }
            builder.append(" ");
        }
        builder.append(")]");
        return builder.toString();
    }
}