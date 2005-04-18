package org.codehaus.gram;

import groovy.lang.Binding;
import groovy.lang.Script;
import org.codehaus.jam.JAnnotatedElement;
import org.codehaus.jam.JAnnotation;
import org.codehaus.jam.JAnnotationValue;

/**
 * A useful base class for Gram scripts
 *
 * @version $Revision$
 */
public abstract class GramSupport extends Script {

    public GramSupport() {
    }

    public GramSupport(Binding binding) {
        super(binding);
    }

    /**
     * Returns the string value of the annotation or "" if the annotation is null
     * or there is no value for the given name
     */
    public String stringValue(JAnnotation annotation, String name) {
        return stringValue(annotation, name, "");
    }

    /**
     * Returns the string value of the annotation or the defaultValue if the annotation is null
     * or there is no value for the given name
     */
    public String stringValue(JAnnotation annotation, String name, String defaultValue) {
        if (annotation != null) {
            JAnnotationValue value = annotation.getValue(name);
            if (value != null) {
                return value.asString();
            }
        }
        return defaultValue;
    }

    /**
     * Returns the integer value of the annotation or 0 if the annotation is null
     * or there is no value for the given name
     */
    public int intValue(JAnnotation annotation, String name) {
        if (annotation != null) {
            JAnnotationValue value = annotation.getValue(name);
            if (value != null) {
                return value.asInt();
            }
        }
        return 0;
    }

    /**
     * Returns the boolean value of the annotation or false if the annotation is null
     * or there is no value for the given name
     */
    public boolean booleanValue(JAnnotation annotation, String name) {
        if (annotation != null) {
            JAnnotationValue value = annotation.getValue(name);
            if (value != null) {
                return value.asBoolean();
            }
        }
        return false;
    }


    /**
     * Decaptalizes the first character of the given string; particularly useful for turning JAM property names
     * into field names etc.
     */
    public String decapitalize(String text) {
        if (text == null) {
            return null;
        }
        return text.substring(0, 1).toLowerCase() + text.substring(1);
    }

    /**
     * Captalizes the first character of the given string; particularly useful for turning JAM field names
     * into property names etc.
     */
    public String capitalize(String text) {
        if (text == null) {
            return null;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1);
    }

    /**
     * Returns the string value of the named annotation
     */
    public String annotationValue(JAnnotatedElement element, String annotationName, String valueName) {
        return annotationValue(element, annotationName, valueName, "");
    }

    /**
     * Returns the string value of the named annotation or the default value is returned if the element, annotation or value
     * is null
     */
    public String annotationValue(JAnnotatedElement element, String annotationName, String valueName, String defaultValue) {
        if (element != null) {
            JAnnotation annotation = element.getAnnotation(annotationName);
            return stringValue(annotation, valueName, defaultValue);
        }
        return defaultValue;
    }
}
