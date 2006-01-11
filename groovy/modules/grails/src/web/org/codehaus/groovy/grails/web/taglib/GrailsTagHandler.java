package org.codehaus.groovy.grails.web.taglib;

import java.io.Writer;
import java.io.IOException;
import java.util.Map;

/**
 * An interface that allows to tag implementation to be abstracted from the JSP custom tag spec.. hence allowing
 * them to be used in direct method calls etc.
 * 
 * @author Graeme Rocher
 * @since 11-Jan-2006
 */
public interface GrailsTagHandler {

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
     * Process the tag
     */
    void doTag() throws IOException;
}
