package org.codehaus.gram;

import junit.framework.TestCase;
import org.codehaus.jam.JAnnotation;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JProperty;
import org.codehaus.jam.JamService;
import org.codehaus.jam.JamServiceFactory;
import org.codehaus.jam.JamServiceParams;
import org.codehaus.jam.JMethod;
import org.codehaus.gram.model.Person;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @version $Revision$
 */
public class JamTest extends TestCase {
    private JamService service;

    public void testJam() throws Exception {
        JamServiceFactory jamServiceFactory = JamServiceFactory.getInstance();

        JamServiceParams params = jamServiceFactory.createServiceParams();
        params.includeSourcePattern(new File[]{new File("src/test")}, "**/*.java");
        //params.includeSourcePattern(new File[]{new File("/workspace/inbox/src/java")}, "**/*.java");
        service = jamServiceFactory.createService(params);

        JClass thisClass = null;
        JClass[] allClasses = service.getAllClasses();
        //String className = "com.raftplc.raftflow.model.RiskIdentification";
        String className = Person.class.getName();
        for (int i = 0; i < allClasses.length; i++) {
            JClass aClass = allClasses[i];
            if (aClass.getQualifiedName().equals(className)) {
                thisClass = aClass;
            }
        }

        assertTrue("Could not find this class", thisClass != null);

        // lets get the properties


        Map propertyMap = dumpClass(thisClass);


        while (true) {
            thisClass = thisClass.getSuperclass();
            if (thisClass == null) {
                break;
            }
            dumpClass(thisClass);
        }

        JProperty property = (JProperty) propertyMap.get("Id");
        assertTrue("Could not find property Id", property != null);

        Map annotations = GramModule.getAnnotationMap(property);
        assertTrue("Should have at least one annotation on property: " + property + " but was: " + annotations, annotations.size() > 0);

        JAnnotation annotation = (JAnnotation) annotations.get("hibernate.id");
        assertTrue("Should have found annotation 'hibernate.id'", annotation != null);

    }

    protected Map dumpClass(JClass aClass) {
        System.out.println("Dumping class: " + aClass.getQualifiedName());
        Map propertyMap = GramModule.getPropertyMap(aClass);
        for (Iterator iter = propertyMap.entrySet().iterator(); iter.hasNext();) {
            Map.Entry entry = (Map.Entry) iter.next();
            JProperty property = (JProperty) entry.getValue();
            System.out.println("property: " + entry.getKey() + " has annoations: " + GramModule.getAnnotationMap(property));
        }
        Map methodMap = GramModule.getMethodMap(aClass);
        JMethod method = (JMethod) methodMap.get("getId");
        if (method != null) {
            System.out.println("getId() has annotations: " + GramModule.getAnnotationMap(method));
        }
        System.out.println();
        return propertyMap;
    }


}
