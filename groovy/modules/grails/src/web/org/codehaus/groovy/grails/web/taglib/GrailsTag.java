package org.codehaus.groovy.grails.web.taglib;

import java.io.Writer;
import java.util.Map;

/**
 * An interface that allows to tag implementation to be abstracted from the JSP custom tag spec.. hence allowing
 * them to be used in direct method calls etc.
 * 
 * @author Graeme Rocher
 * @since 11-Jan-2006
 */
public interface GrailsTag {

    void init(Map tagContext);

    /**
     * Sets the writer that processes the tag
     * @param w
     */
    void setWriter(Writer w);

    /**
     * Sets the attributes of the tag
     * @param attributes
     */
    void setAttributes(Map attributes);

    /**
     * Sets an attribute of the tag
     * @param name
     * @param value
     */
    void setAttribute(String name, Object value);

    /**
     * Process the start tag
     */
    void doStartTag();

    /**
     * process the end tag
     */
    void doEndTag();

    /**
     * In GSP files grails tag attributes can be dynamic in that a groovy expression could be placed
     * within the tag value. This method needs to return true for all tag attributes that need to be dynamic.
     * Otherwise only a String or GString value is possible within the attribute.
     *
     * @param attr
     * @return True if the specified attribute is dynamic
     */
    boolean isDynamicAttribute(String attr);

    /**
     * @return The name of the tag
     */
    String getName();
}
                            