package org.codehaus.gram;

import org.codehaus.jam.JamService;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JProperty;
import org.codehaus.jam.JElement;
import org.codehaus.jam.JAnnotatedElement;

import java.util.Map;
import java.util.HashMap;

/**
 * A set of helper methods for the JamService for use inside Gram
 *
 * @version $Revision$
 */
public class GramModule {

    public static Map getPropertyMap(JClass jclass) {
        return getElementSimpleNameMap(jclass.getProperties());
    }

    public static Map getFieldMap(JClass jclass) {
        return getElementSimpleNameMap(jclass.getFields());
    }

    public static Map getMethodMap(JClass jclass) {
        return getElementSimpleNameMap(jclass.getMethods());
    }

    public static Map getElementSimpleNameMap(JElement[] elements) {
        Map answer = new HashMap();
        for (int i = 0; i < elements.length; i++) {
            JElement element = elements[i];
            answer.put(element.getSimpleName(), element);
        }
        return answer;
    }

    public static Map getAnnotationMap(JAnnotatedElement element) {
        return getElementQualifiedNameMap(element.getAnnotations());
    }

    public static Map getElementQualifiedNameMap(JElement[] elements) {
        Map answer = new HashMap();
        for (int i = 0; i < elements.length; i++) {
            JElement element = elements[i];
            answer.put(element.getQualifiedName(), element);
        }
        return answer;
    }
}
