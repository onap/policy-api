package org.onap.policy.api.main.rest.model;

/**
 * Class to represent policy type in lite
 */
public class PolicyTypeLite {
    
    private String version;
    private String description;
    private String derivedFrom;
   
    /**
     * Returns the version of this {@link PolicyTypeLite} instance.
     * 
     * @return the version
     */
    public String getVersion() {
        return version;
    }
    
    /**
     * Set version in this {@link PolicyTypeLite} instance.
     * 
     * @param version the version to set
     */
    public void setVersion(String version) {
        this.version = version;
    }
    
    /**
     * Returns the description of this {@link PolicyTypeLite} instance.
     * 
     * @return the description
     */
    public String getDescription() {
        return description;
    }
    
    /**
     * Set description in this {@link PolicyTypeLite} instance.
     * 
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Returns the derivedFrom of this {@link PolicyTypeLite} instance.
     * 
     * @return the derivedFrom
     */
    public String getDerivedFrom() {
        return derivedFrom;
    }
    
    /**
     * Set derivedFrom in this {@link PolicyTypeLite} instance.
     * 
     * @param derivedFrom the derivedFrom to set
     */
    public void setDerivedFrom(String derivedFrom) {
        this.derivedFrom = derivedFrom;
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("PolicyTypeLite [version=");
        builder.append(getVersion());
        builder.append(", description=");
        builder.append(getDescription());
        builder.append(", derivedFrom=");
        builder.append(getDerivedFrom());
        builder.append("]");
        return builder.toString(); 
    }
}


