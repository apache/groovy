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

import groovy.transform.NotYetImplemented

class Groovy8497Test extends AntTestCase {
    @NotYetImplemented
    void testGetProperty() {
//        def debugLogger = new org.apache.tools.ant.DefaultLogger()
//        debugLogger.setMessageOutputLevel(4)
//        debugLogger.setOutputPrintStream(System.out)
//        debugLogger.setErrorPrintStream(System.err)

        doInTmpDir { ant, baseDir ->
            baseDir.src {
                'GroovyClass8497.groovy'('''
                    class GroovyClass8497 {
                        String getFoo() { 'FOO!' }
                    }
                ''')
                'AccessGetProperty.java'('''
                    public class AccessGetProperty {
                        public static void main(String[] args) {
                            System.out.println(new GroovyClass8497().getProperty("foo"));
                        }
                    }
                ''')
            }
//            ant.project.addBuildListener(debugLogger)
            ant.mkdir(dir: 'build')
            def cp = System.getProperty('java.class.path') + ':build'
            ant.taskdef(name: 'groovyc', classname: 'org.codehaus.groovy.ant.Groovyc')
            ant.groovyc(srcdir: 'src', destdir: 'build')
            ant.javac(classpath: cp, destdir: 'build', srcdir: 'src', includeantruntime: 'false', fork: 'true')
            ant.java(classpath: cp, outputproperty: 'result', classname: 'AccessGetProperty')
            assert ant.project.properties.result == 'FOO!'
        }
    }
}
