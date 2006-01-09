package org.codehaus.groovy.grails.scaffolding;

import groovy.text.Template;

/**
 * An interface that defines methods for retrieving templates for specific scaffolded types
 *
 * @author Graeme Rocher
 * @since 06-Jan-2006
 */
public interface TemplateFactory {

    /**
     * Locates a template for the given type
     * @param type The type to locate a a template for
     * @return A Template instance or null if non was found for the specified type
     *
     */
    Template findTemplateForType(Class type);

    /**
     * Locates a named template for the given type
     * @param type The type of the template to locate
     * @param name The name of the template
     * @return A Template instance or null if none was found
     */
    Template findNamedTemplateForType(Class type, String name );
}
