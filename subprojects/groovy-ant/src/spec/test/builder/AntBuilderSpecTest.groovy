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
package builder

import groovy.ant.AntTestCase

/**
 * Test cases for the Ant builder documentation.
 */
class AntBuilderSpecTest extends AntTestCase {

    void testEcho() {
        /*
        // tag::create_zip_builder[]
        def ant = new AntBuilder()
        // end::create_zip_builder[]
        */
        // tag::example_echo[]
        def ant = new groovy.ant.AntBuilder()          // <1>
        ant.echo('hello from Ant!')         // <2>
        // end::example_echo[]
    }

    void testCreateZip() {
        doInTmpDir { ant, baseDir ->
            baseDir.src {
                'test.groovy'("println 'Hello'")
            }
            // tag::create_zip_zip[]
            ant.zip(destfile: 'sources.zip', basedir: 'src')
            // end::create_zip_zip[]
        }
    }

    void testCopyFiles() {
        doInTmpDir {ant, baseDir ->
            baseDir.src {
                test {
                    some {
                        'test1.groovy'('assert 1+1==2')
                        'test2.groovy'('assert 1+1==2')
                        pkg {
                            'MyTest.groovy'('assert 1+1==2')
                        }
                    }
                }
            }
            // tag::copy_files[]
            // let's just call one task
            ant.echo("hello")

            // here is an example of a block of Ant inside GroovyMarkup
            ant.sequential {
                echo("inside sequential")
                def myDir = "target/AntTest/"
                mkdir(dir: myDir)
                copy(todir: myDir) {
                    fileset(dir: "src/test") {
                        include(name: "**/*.groovy")
                    }
                }
                echo("done")
            }

            // now let's do some normal Groovy again
            def file = new File(ant.project.baseDir,"target/AntTest/some/pkg/MyTest.groovy")
            assert file.exists()
            // end::copy_files[]
        }
    }

    void testFileScanner() {
        doInTmpDir {ant, baseDir ->
            baseDir.src {
                test {
                    some {
                        'test1.groovy'('assert 1+1==2')
                        'test2.groovy'('assert 1+1==2')
                        pkg {
                            'MyTest.groovy'('assert 1+1==2')
                        }
                    }
                }
            }
            // tag::filescanner[]
            // let's create a scanner of filesets
            def scanner = ant.fileScanner {
                fileset(dir:"src/test") {
                    include(name:"**/My*.groovy")
                }
            }

            // now let's iterate over
            def found = false
            for (f in scanner) {
                println("Found file $f")
                found = true
                assert f instanceof File
                assert f.name.endsWith(".groovy")
            }
            assert found
            // end::filescanner[]
        }
    }

    void testExecuteJUnit() {
        doInTmpDir {ant, baseDir ->
            baseDir.some {
                pkg {
                    'MyTest.java'('''
                        package some.pkg;
                        import junit.framework.TestCase;
                        public class MyTest extends TestCase {
                            public void testAddition() {
                                assertEquals(1+1, 2);
                            }
                        }
                    ''')
                }
            }
            ant.javac(srcdir:'.', includes:'**/*.java', fork:'true')
            // tag::run_junit[]
            ant.junit {
                classpath { pathelement(path: '.') }
                test(name:'some.pkg.MyTest')
            }
            // end::run_junit[]
        }
    }

    void testCompileRunJava() {
        doInTmpDir {ant, baseDir ->
            // tag::compile_java[]
            ant.echo(file:'Temp.java', '''
                class Temp {
                    public static void main(String[] args) {
                        System.out.println("Hello");
                    }
                }
            ''')
            ant.javac(srcdir:'.', includes:'Temp.java', fork:'true')
            ant.java(classpath:'.', classname:'Temp', fork:'true')
            ant.echo('Done')
            // end::compile_java[]
        }
    }
}
