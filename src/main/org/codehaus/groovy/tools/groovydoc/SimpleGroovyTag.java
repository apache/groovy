package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyTag;

/**
 * Stores info about GroovyDoc tags.
 */
public class SimpleGroovyTag implements GroovyTag {
    private String name;
    private String param;
    private String text;

    public SimpleGroovyTag(String name, String param, String text) {
        this.name = name;
        this.param = param;
        this.text = text;
    }

    public String name() {
        return name;
    }

    public String param() {
        return param;
    }

    public String text() {
        return text;
    }
}
