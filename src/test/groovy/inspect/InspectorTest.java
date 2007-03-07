package groovy.inspect;

import junit.framework.TestCase;

import java.io.Serializable;
import java.io.StringWriter;
import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.*;

public class InspectorTest extends TestCase implements Serializable {
    public String someField = "only for testing";
    public static final String SOME_CONST = "only for testing";

    public InspectorTest(String name) {
        super(name);
    }

    public void testCtor() {
        Object object = new Object();
        Inspector inspector = new Inspector(object);
        assertEquals(object, inspector.getObject());
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
        String[] names = { "sleep", "sleep", "println", "println", "println", "find", "print", "print", "each", "invokeMethod", "asType",
                           "inspect", "is", "isCase", "identity", "getAt", "putAt", "dump", "getMetaPropertyValues",  "getProperties",
                           "use", "use", "use", "printf", "printf", "eachWithIndex", "every", "every", "any", "any", "grep", "collect", "collect", "findAll",
                           "findIndexOf", "iterator", "addShutdownHook", "sprintf", "sprintf"
                         };
        assertEquals(names.length, metaMethods.length);
        assertNameEquals(names, metaMethods);
        String[] details = {"GROOVY","public","Object","void","println","Object","n/a"};
        assertContains(metaMethods, details);
    }

    public void testStaticMetaMethods() {
        Matcher matcher = Pattern.compile("").matcher("");
        Inspector insp = new Inspector(matcher);
        Object[] metaMethods = insp.getMetaMethods();
        assertUnique(Inspector.sort(Arrays.asList(metaMethods)));
        String[] details = {"GROOVY","public static","Matcher","Matcher","getLastMatcher","","n/a"};
        assertContains(metaMethods, details);
    }

    public void testFields() {
        Inspector insp = new Inspector(this);
        Object[] fields = insp.getPublicFields();
        assertEquals(2, fields.length);
        String[] names = { "someField","SOME_CONST" };
        assertNameEquals(names, fields);
        String[] details = {"JAVA","public","InspectorTest","String","someField","\"only for testing\""};
        assertContains(fields, details);
    }

    public void testProperties() {
        Inspector insp = new Inspector(this);
        Object[] properties = insp.getPropertyInfo();
        assertEquals(2, properties.length);
        String[] names = {"class","name" };
        assertNameEquals(names, properties);
        String[] details = {"GROOVY", "public", "n/a", "Class", "class", "class groovy.inspect.InspectorTest"};
        assertContains(properties, details);
    }

    public void testPrint() {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(bytes);
        String ls = System.getProperty("line.separator");
        String[] first = {"a", "b"};
        String[] second = {"x", "y"};
        Object[] memberInfo = {first, second};
        Inspector.print(printStream, memberInfo);
        assertEquals("0:\ta b " + ls + "1:\tx y " + ls, bytes.toString());
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

    private void assertUnique(Collection sortedMembers){
        if (sortedMembers.size() < 2) return;
        Comparator comp = new Inspector.MemberComparator();
        Iterator iter = sortedMembers.iterator();
        Object last = iter.next();
        while (iter.hasNext()) {
            Object element = iter.next();
            if (0 == comp.compare(last, element)){
                fail("found duplication for element "+element);
            }
            last = element;
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