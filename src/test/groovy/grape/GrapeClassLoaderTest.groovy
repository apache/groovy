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

import org.junit.BeforeClass
import org.junit.Test

class GrapeClassLoaderTest {

    @BeforeClass
    static void downloadToCache() {
        // ensure files are installed locally
        Grape.resolve([autoDownload:true, classLoader:new GroovyClassLoader()],
            [groupId:'com.jidesoft', artifactId:'jide-oss', version:'[2.2.1,2.3)'],
            [groupId:'org.testng', artifactId:'testng', version:'5.8', classifier:'jdk15'])
    }

    @Test
    void testGrapes() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            @Grapes([@Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')])
            class TestConstructorAnnotation {

                String field

                TestConstructorAnnotation() {
                    field = JideSplitButton.class.name
                }
            }
        ''')
        assert testClass.getConstructor().newInstance().field  == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testConstructorAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            class TestConstructorAnnotation {

                String field

                @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
                TestConstructorAnnotation() {
                    field = JideSplitButton.class.name
                }
            }
            ''')
        assert testClass.getConstructor().newInstance().field  == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testClassFieldAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            class TestClassFieldAnnotation {

                @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
                private String field

                TestClassFieldAnnotation() {
                    field = JideSplitButton.class.name
                }

                String testMethod() {
                    field
                }
            }
        ''')
        assert testClass.getConstructor().newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testClassPropertyAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            class TestClassPropertyAnnotation {

                @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
                String field

                TestClassPropertyAnnotation() {
                    field = JideSplitButton.class.name
                }

                String testMethod() {
                    field
                }
            }
        ''')
        assert testClass.getConstructor().newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

//  annotations are discarded on local vars currently
//    @Test
//    void testClassLocalVariableAnnotation() {
//        GroovyClassLoader loader = new GroovyClassLoader()
//        Class testClass = loader.parseClass('''
//            import com.jidesoft.swing.JideSplitButton
//
//            class TestClassLocalVariableAnnotation {
//                String testMethod() {
//                    @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
//                    String localVar = JideSplitButton.class.name
//                    localVar
//                }
//            }
//        ''')
//        assert testClass.getConstructor().newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
//    }

//  annotations are discarded on local vars currently
//    @Test
//    void testScriptLocalVariableAnnotation() {
//        GroovyClassLoader loader = new GroovyClassLoader()
//        Class testClass = loader.parseClass('''
//            import com.jidesoft.swing.JideSplitButton
//
//            @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
//            String localVar = JideSplitButton.class.name
//            localVar
//        ''')
//        assert testClass.main() == 'com.jidesoft.swing.JideSplitButton'
//    }

    @Test
    void testClassStaticMethodAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            class TestClassStaticMethodAnnotation {
                @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
                static String testMethod () {
                    JideSplitButton.class.name
                }
            }
        ''')
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testClassMethodAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            class TestClassMethodAnnotation {
                @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
                String testMethod () {
                    JideSplitButton.class.name
                }
            }
        ''')
        assert testClass.getConstructor().newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testScriptStaticMethodAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
            static String testMethod () {
                JideSplitButton.class.name
            }
        ''')
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testScriptMethodAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
            String testMethod () {
                return JideSplitButton.class.name
            }
        ''')
        assert testClass.getConstructor().newInstance().testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testClassMethodParameterAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            class TestClassStaticMethodAnnotation {
                String testMethod (
                    @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
                    String bogus
                ) {
                    JideSplitButton.class.name
                }
            }
        ''')
        assert testClass.getConstructor().newInstance().testMethod('x') == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testScriptMethodParameterAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            String testMethod (
                @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
                String bogus
            ) {
                JideSplitButton.class.name
            }
        ''')
        assert testClass.getConstructor().newInstance().testMethod('x') == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testTypeAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            @Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
            class TestTypeAnnotation {
                static String testMethod () {
                    JideSplitButton.class.name
                }
            }
        ''')
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testFQNAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton

            @groovy.lang.Grab(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
            class TestFQNAnnotation {
                static String testMethod () {
                    JideSplitButton.class.name
                }
            }
        ''')
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testAliasedAnnotation() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import com.jidesoft.swing.JideSplitButton
            import groovy.lang.Grab as require_gem

            @require_gem(group = 'com.jidesoft', module = 'jide-oss', version = '[2.2.1,2.3)')
            class TestAliasedAnnotation {
                static String testMethod () {
                    JideSplitButton.class.name
                }
            }
        ''')
        assert testClass.testMethod() == 'com.jidesoft.swing.JideSplitButton'
    }

    @Test
    void testClassifier() {
        GroovyClassLoader loader = new GroovyClassLoader()
        Class testClass = loader.parseClass('''
            import org.testng.TestNG

            @Grab(group = 'org.testng', module = 'testng', version = '5.8', classifier = 'jdk15')
            class TestTypeAnnotation {
                static String testMethod() {
                    TestNG.name
                }
            }
        ''')
        assert testClass.testMethod() == 'org.testng.TestNG'
    }

}
