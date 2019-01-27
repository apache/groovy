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

class Groovy8969Test extends AntTestCase {
    private scriptParamNameCheck = '''
        import org.codehaus.groovy.control.CompilerConfiguration
        def cc = new CompilerConfiguration(parameters: true)
        def shell = new GroovyShell(getClass().classLoader, cc)
        shell.evaluate """
            trait HasUpper {
                def upper(String upperParam) { upperParam.toUpperCase() }
            }

            class Main implements HasLower, HasUpper { }

            assert Main.getMethod('lower', String).parameters[0].name == 'lowerParam'
            assert Main.getMethod('upper', String).parameters[0].name == 'upperParam'

            assert new Main().with{ lower('Foo') + upper('Bar') } == 'fooBAR'
        """
    '''

    void testParameterNamesSeenInAST() {
        if (System.getProperty('java.specification.version') < '1.8') return
//        def debugLogger = new org.apache.tools.ant.DefaultLogger()
//        debugLogger.setMessageOutputLevel(4)
//        debugLogger.setOutputPrintStream(System.out)
//        debugLogger.setErrorPrintStream(System.err)

        doInTmpDir { ant, baseDir ->
            println baseDir
            baseDir.src {
                'HasLower.groovy'('''
                    trait HasLower {
                        def lower(String lowerParam) { lowerParam.toLowerCase() }
                    }
                ''')
            }
//            ant.project.addBuildListener(debugLogger)
            ant.mkdir(dir: 'build')
            ant.taskdef(name: 'groovyc', classname: 'org.codehaus.groovy.ant.Groovyc')
            ant.groovyc(srcdir: 'src', destdir: 'build', parameters: 'true')
            ant.taskdef(name: 'groovy', classname: 'org.codehaus.groovy.ant.Groovy')
            ant.groovy(scriptParamNameCheck) {
                classpath { pathelement(path: 'build') }
            }
        }
    }
}
