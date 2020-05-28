/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy.grape

import groovy.test.GroovyTestCase

class GrapeClassLoaderTest extends GroovyTestCase {

    public GrapeClassLoaderTest() {
        // insure files are installed locally
        Grape.resolve([autoDownload:true, classLoader:new GroovyClassLoader()],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'],
            [groupId:'org.testng', artifactId:'testng', version:'5.8', classifier:'jdk15'])
    }

    public void testGrapes() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

@Grapes([@Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')])
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

    @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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

    @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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

    @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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
//            @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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
//@Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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
    @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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
    @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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

@groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
public static String testMethod () {
    return JideSplitButton.class.name
}""")
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    public void testScriptMethodAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import com.jidesoft.swing.JideSplitButton

@groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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
@groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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
@groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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

@Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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

@groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
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

@require_gem(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
public class TestAliasedAnnotation {
    public static String testMethod () {
        return JideSplitButton.class.name
    }
}""")
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    void testClassifier() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass("""
import org.testng.TestNG

@Grab(group = 'org.testng', module = 'testng', version = '5.8', classifier = 'jdk15')
class TestTypeAnnotation {
    static String testMethod() {
        TestNG.name
    }
}""")
        assert testClass.testMethod() == 'org.testng.TestNG'
    }

}
