package org.onap.policy.api.main.rest.model;

import java.util.List;

/**
 * Class to represent policy version list
 */
public class VersionList {
    
    private List<String> versions;
    
    /**
     * Returns the versions of this {@link VersionList} instance.
     * 
     * @return the versions
     */
    public List<String> getVersions() {
        return versions;
    }
    
    /**
     * Set versions in this {@link VersionList} instance.
     * 
     * @param versions the versions to set
     */
    public void setVersions(List<String> versions) {
        this.versions = versions;
    }
    
    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("VersionList [versions=(");
        for (String version : getVersions()) {
            builder.append(version);
            builder.append(" ");  
        }
        builder.append(")]");
        return builder.toString();
    }
}