package org.codehaus.groovy.tools.groovydoc;

/**
 * Represents a link pair (href, packages).
 * The packages are comma separated.
 */
public class LinkArgument {
    private String href;
    private String packages;

    /**
     * Get the packages attribute.
     *
     * @return the packages attribute.
     */
    public String getPackages() {
        return packages;
    }

    /**
     * Set the packages attribute.
     *
     * @param packages the comma separated package prefixs corresponding to this link
     */
    public void setPackages(String packages) {
        this.packages = packages;
    }

    /**
     * Get the href attribute.
     *
     * @return the href attribute.
     */
    public String getHref() {
        return href;
    }

    /**
     * Set the href attribute.
     *
     * @param hr a <code>String</code> value representing the URL to use for this link
     */
    public void setHref(String hr) {
        href = hr;
    }

}