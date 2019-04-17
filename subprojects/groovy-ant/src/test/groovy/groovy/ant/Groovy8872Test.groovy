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

import static groovy.test.GroovyAssert.isAtLeastJdk

class Groovy8872Test extends AntTestCase {
    private scriptParamNameCheck = '''
        @ExtractParamNames
        abstract class DummyClass implements JavaInterface, GroovyInterface {}

        assert DummyClass.paramNames.addPerson == ['name', 'dob', 'vip']
        assert DummyClass.paramNames.addEvent == ['id', 'eventName', 'dateOfEvent']
    '''

    void testParameterNamesSeenInAST() {
        // parameter name inclusion in bytecode is a JDK8+ feature
        if (!isAtLeastJdk('1.8')) return
//        def debugLogger = new org.apache.tools.ant.DefaultLogger()
//        debugLogger.setMessageOutputLevel(4)
//        debugLogger.setOutputPrintStream(System.out)
//        debugLogger.setErrorPrintStream(System.err)

        doInTmpDir { ant, baseDir ->
            baseDir.src {
                'JavaInterface.java'('''
                    import java.util.Date;

                    public interface JavaInterface {
                        void addPerson(String name, Date dob, boolean vip);
                    }
                ''')
                'GroovyInterface.groovy'('''
                    interface GroovyInterface {
                        void addEvent(int id, String eventName, Date dateOfEvent)
                    }
                ''')
                'ExtractParamNames.groovy'('''
                    import org.codehaus.groovy.transform.GroovyASTTransformationClass
                    import java.lang.annotation.*

                    /**
                     * Test transform adds a static field to a class that returns a map
                     * from the name for each found method to its parameter names.
                     */
                    @java.lang.annotation.Documented
                    @Retention(RetentionPolicy.SOURCE)
                    @Target(ElementType.TYPE)
                    @GroovyASTTransformationClass("ExtractParamNamesTransformation")
                    @interface ExtractParamNames { }
                ''')
                'ExtractParamNamesTransformation.groovy'('''
                    import org.codehaus.groovy.ast.*
                    import org.codehaus.groovy.transform.*
                    import org.codehaus.groovy.control.*
                    import static org.codehaus.groovy.ast.tools.GeneralUtils.*

                    @GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
                    class ExtractParamNamesTransformation extends AbstractASTTransformation {
                        void visit(ASTNode[] nodes, SourceUnit source) {
                            init(nodes, source)
                            def classNode = nodes[1]
                            assert classNode instanceof ClassNode
                            def result = mapX(classNode.allDeclaredMethods.collect{
                                entryX(constX(it.name), list2args(it.parameters.name))
                            })
                            classNode.addField(new FieldNode("paramNames", ACC_PUBLIC + ACC_STATIC + ACC_FINAL,
                                               ClassHelper.MAP_TYPE.plainNodeReference, classNode, result))
                        }
                    }
                ''')
            }
//            ant.project.addBuildListener(debugLogger)
            ant.mkdir(dir: 'build')
            ant.javac(classpath: '.', destdir: 'build', srcdir: 'src',
                    includes: '*.java', includeantruntime: 'false', fork: 'true') {
                compilerarg(value: '-parameters')
            }
            ant.taskdef(name: 'groovyc', classname: 'org.codehaus.groovy.ant.Groovyc')
            ant.groovyc(srcdir: 'src', destdir: 'build', parameters: 'true')
            ant.taskdef(name: 'groovy', classname: 'org.codehaus.groovy.ant.Groovy')
            ant.groovy(scriptParamNameCheck) {
                classpath { pathelement(path: 'build') }
            }
        }
    }
}
