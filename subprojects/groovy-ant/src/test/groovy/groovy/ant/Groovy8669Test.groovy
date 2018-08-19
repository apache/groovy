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
package groovy.ant

class Groovy8669Test extends AntTestCase {
    private scriptAllOnPath = '''
    def anno = AnnotatedClass.annotations[0]
    def type = anno.annotationType()
    assert type.name == 'MyAnnotation'
    assert type.getDeclaredMethod('value').invoke(anno).name == 'ValueClass'
    '''

    private scriptNoValueClass = '''
    // using annotations will cause ValueClass not to be found
    // but should still be able to use the class otherwise
    assert AnnotatedClass.name.size() == 14
    '''

    private scriptNoAnnotationOnPath = '''
    // class should be usable but won't have annotations
    assert !AnnotatedClass.annotations
    '''

    void testCreateZip() {
//        def debugLogger = new org.apache.tools.ant.DefaultLogger()
//        debugLogger.setMessageOutputLevel(4)
//        debugLogger.setOutputPrintStream(System.out)
//        debugLogger.setErrorPrintStream(System.err)

        doInTmpDir { ant, baseDir ->
            baseDir.src {
                'ValueClass.java'('''
                    public class ValueClass{ }
                ''')
                'MyAnnotation.java'('''
                    import java.lang.annotation.*;

                    @Target(ElementType.TYPE)
                    @Retention(RetentionPolicy.RUNTIME)
                    public @interface MyAnnotation {
                        Class<ValueClass> value();
                    }
                ''')
                'AnnotatedClass.java'('''
                    @MyAnnotation(ValueClass.class)
                    class AnnotatedClass { }
                ''')
            }
//            ant.project.addBuildListener(debugLogger)
            ant.mkdir(dir: 'build')
            ant.javac(classpath: '.', destdir: 'build', srcdir: 'src',
                    includes: '*.java', includeantruntime: 'false', fork: 'true')
            ['ValueClass', 'MyAnnotation', 'AnnotatedClass'].each { name ->
                ant.mkdir(dir: "build$name")
                ant.copy(file: "build/${name}.class", todir: "build$name")
            }
            ant.taskdef(name: 'groovy', classname: 'org.codehaus.groovy.ant.Groovy')
            ant.groovy(scriptAllOnPath) {
                classpath { pathelement(path: 'buildValueClass') }
                classpath { pathelement(path: 'buildMyAnnotation') }
                classpath { pathelement(path: 'buildAnnotatedClass') }
            }
            ant.groovy(scriptNoValueClass) {
                classpath { pathelement(path: 'buildMyAnnotation') }
                classpath { pathelement(path: 'buildAnnotatedClass') }
            }
            ant.groovy(scriptNoAnnotationOnPath) {
                classpath { pathelement(path: 'buildAnnotatedClass') }
            }
        }
    }
}
