package groovy.inspect;

import junit.framework.TestCase;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Set;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Comparator;

public class InspectorTest extends TestCase implements Serializable {
    public String someField = "only for testing";             
    public static final String SOME_CONST = "only for testing";

    public InspectorTest(String name) {
        super(name);
    }

    public void testCtor() {
        new Inspector(new Object());
        try {
            new Inspector(null);
            fail("should have thown IllegalArgumentException");
        } catch (Exception expected) {
        }
    }

    public void testClassProps() {
        Inspector insp = new Inspector(this);
        String[] classProps = insp.getClassProps();
        assertEquals("package groovy.inspect",classProps[Inspector.CLASS_PACKAGE_IDX]);
        assertEquals("public class InspectorTest",classProps[Inspector.CLASS_CLASS_IDX]);
        assertEquals("implements Serializable ",classProps[Inspector.CLASS_INTERFACE_IDX]);
        assertEquals("extends TestCase",classProps[Inspector.CLASS_SUPERCLASS_IDX]);
        assertEquals("is Primitive: false, is Array: false, is Groovy: false",classProps[Inspector.CLASS_OTHER_IDX]);
    }
    public void testMethods() {
        Inspector insp = new Inspector(new Object());
        Object[] methods = insp.getMethods();
        assertEquals(10, methods.length);
        String[] names = {"hashCode","getClass","wait","wait","wait","equals","notify","notifyAll","toString","java.lang.Object"};
        assertNameEquals(names, methods);
        String[] details = {"JAVA","public final","Object","void","wait","long, int","InterruptedException"};
        assertContains(methods, details);
        // ctors are not considered static !
        String[] ctorDetails = {"JAVA","public","Object","Object","java.lang.Object","",""};
        assertContains(methods, ctorDetails);
    }

    public void testStaticMethods() {
        Inspector insp = new Inspector(this);
        Object[] methods = insp.getMethods();
        for (int i = 0; i < methods.length; i++) {
            String[] strings = (String[]) methods[i];
            if(strings[1].indexOf("static") > -1) return; // ok, found one static method
        }
        fail("there should have been at least one static method in this TestCase, e.g. 'fail'.");
    }
    public void testMetaMethods() {
        Inspector insp = new Inspector(new Object());
        Object[] metaMethods = insp.getMetaMethods();
        assertEquals(28, metaMethods.length);
        String[] names = { "println", "println", "println", "find", "print", "print", "each", "invokeMethod",
"inspect", "isCase", "identity", "getAt", "putAt", "dump", "eachPropertyName", "eachProperty", "allProperties",
"use", "use", "printf", "eachWithIndex", "every", "any", "grep", "collect", "collect", "findAll", "findIndexOf"};
        assertNameEquals(names, metaMethods);
        String[] details = {"GROOVY","public","Object","void","println","Object","n/a"};
        assertContains(metaMethods, details);
    }

    public void testStaticMetaMethods() {
        Matcher matcher = Pattern.compile("").matcher("");
        Inspector insp = new Inspector(matcher);
        Object[] metaMethods = insp.getMetaMethods();

        // todo: this currently fails under JDK 1.5 for whatever reason...
        if (! System.getProperty("java.version").startsWith("1.5")){
            assertUnique(Inspector.sort(metaMethods));
        }
        String[] details = {"GROOVY","public static","Matcher","Matcher","getLastMatcher","","n/a"};
        assertContains(metaMethods, details);
    }

    public void testFields() {
        Inspector insp = new Inspector(this);
        Object[] fields = insp.getPublicFields();
        assertEquals(2, fields.length);
        String[] names = { "someField","SOME_CONST" };
        assertNameEquals(names, fields);
        String[] details = {"JAVA","public","InspectorTest","String","someField","only for testing"};
        assertContains(fields, details);
    }
    public void testProperties() {
        Inspector insp = new Inspector(this);
        Object[] fields = insp.getProperties();
        assertEquals(4, fields.length);
        String[] names = { "SOME_CONST","someField","class","name"};
        assertNameEquals(names, fields);
        String[] details = {"GROOVY","public","n/a","String","name","testProperties"};
        assertContains(fields, details);
    }

    private void assertNameEquals(String[] names, Object[] metaMethods) {
        Set metaSet = new HashSet();
        for (int i = 0; i < metaMethods.length; i++) {
            String[] strings = (String[]) metaMethods[i];
            metaSet.add(strings[Inspector.MEMBER_NAME_IDX]);
        }
        Set nameSet = new HashSet(Arrays.asList(names));
        assertEquals(nameSet, metaSet);
    }

    private void assertContains(Object[] candidates, String[] sample) {
        String sampleBuffer = concat(sample);
        for (int i = 0; i < candidates.length; i++) {
            String[] entry = (String[]) candidates[i];
            if (sampleBuffer.equals(concat(entry))) return;
        }
        fail("should have found sample: " + sampleBuffer);
    }

    private void assertUnique(Object[] sortedMembers){
        Comparator comp = new Inspector.MemberComparator();
        for (int i = 1; i < sortedMembers.length; i++) {
            if (0 == comp.compare(sortedMembers[i - 1], sortedMembers[i])){
                Inspector.print(sortedMembers);
                fail("found duplication at pos "+ (i-1)+" and "+i);
            }
        }
    }

    private String concat(String[] details) {
        StringBuffer detailBuffer = new StringBuffer();
        for (int i = 0; i < details.length; i++) {
            detailBuffer.append(details[i]);
            detailBuffer.append(" ");
        }
        return detailBuffer.toString();
    }

}