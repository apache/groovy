package groovy.grape
/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Jan 20, 2008
 * Time: 5:14:11 PM
 */
class GrapeClassLoaderTest extends GroovyTestCase {

    public GrapeClassLoaderTest() {
        GrapeIvy.initGrape()
    }

    public void testGrapes() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

@Grapes([@Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')])
public class TestConstructorAnnotation {

    String field

    public TestConstructorAnnotation() {
        field = JideSplitButton.class.name
    }
}""")
        assert testClass.newInstance().field  == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testConstructorAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

public class TestConstructorAnnotation {

    String field

    @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
    public TestConstructorAnnotation() {
        field = JideSplitButton.class.name
    }
}""")
        assert testClass.newInstance().field  == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testClassFieldAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

public class TestClassFieldAnnotation {

    @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
    private String field

    public TestClassFieldAnnotation() {
        field = JideSplitButton.class.name
    }

    public String testMethod() {
        return field
    }
}""")
        assert testClass.newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testClassPropertyAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

public class TestClassPropertyAnnotation {

    @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
    String field

    public TestClassPropertyAnnotation() {
        field = JideSplitButton.class.name
    }

    public String testMethod() {
        return field
    }
}""")
        assert testClass.newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

//  annotations are discarded on local vars currently
//    public void testClassLocalVariableAnnotation() {
//        GroovyClassLoader loader = new GroovyClassLoader()
//        Class testClass = loader.parseClass("""
//import com.jidesoft.swing.JideSplitButton
//
//    public class TestClassLocalVariableAnnotation {
//
//        public String testMethod() {
//            @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
//            String field = JideSplitButton.class.name
//            return field
//        }
//}""")
//        assert testClass.newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
//    }
//
//  annotations are discarded on local vars currently
//    public void testScriptLocalVariableAnnotation() {
//        GroovyClassLoader loader = new GroovyClassLoader()
//        Class testClass = loader.parseClass("""
//import com.jidesoft.swing.JideSplitButton
//
//@Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
//String field = JideSplitButton.class.name
//
//
//String testMethod() {
//    return field
//}""")
//        assert testClass.newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
//    }

    public void testClassStaticMethodAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

public class TestClassStaticMethodAnnotation {
    @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
    public static String testMethod () {
        return JideSplitButton.class.name
    }
}""")
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testClassMethodAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

public class TestClassMethodAnnotation {
    @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
    public String testMethod () {
        return JideSplitButton.class.name
    }
}""")
        assert testClass.newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testScriptStaticMethodAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

@groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
public static String testMethod () {
    return JideSplitButton.class.name
}""")
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testScriptMethodAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

@groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
public String testMethod () {
    return JideSplitButton.class.name
}""")
        assert testClass.newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testClassParameterAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

public class TestClassStaticMethodAnnotation {
    public String testMethod (
@groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
String bogus
) {
        return JideSplitButton.class.name
    }
}""")
        assert testClass.newInstance().testMethod('x') == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testScriptParameterAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

public String testMethod (
@groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
String bogus
) {
    println "Foo"
    return JideSplitButton.class.name
}""")
        assert testClass.newInstance().testMethod('x') == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testTypeAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

@Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
public class TestTypeAnnotation {
    public static String testMethod () {
        return JideSplitButton.class.name
    }
}""")
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testFQNAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

@groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
public class TestFQNAnnotation {
    public static String testMethod () {
        return JideSplitButton.class.name
    }
}""")
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testAliasedAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton
import groovy.lang.Grab as require_gem

@require_gem(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,)')
public class TestAliasedAnnotation {
    public static String testMethod () {
        return JideSplitButton.class.name
    }
}""")
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

}