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

import groovy.test.GroovyTestCase

class FileTreeBuilderTest extends GroovyTestCase {
    File tmpDir

    void tearDown() {
        tmpDir.deleteDir()
    }

    void testFileTreeBuilder() {
        // tag::example[]
        tmpDir = File.createTempDir()
        def fileTreeBuilder = new FileTreeBuilder(tmpDir)
        fileTreeBuilder.dir('src') {
            dir('main') {
               dir('groovy') {
                  file('Foo.groovy', 'println "Hello"')
               }
            }
            dir('test') {
               dir('groovy') {
                  file('FooTest.groovy', 'class FooTest extends groovy.test.GroovyTestCase {}')
               }
            }
         }
         // end::example[]
         
         // tag::example_assert[]
         assert new File(tmpDir, '/src/main/groovy/Foo.groovy').text == 'println "Hello"'
         assert new File(tmpDir, '/src/test/groovy/FooTest.groovy').text == 'class FooTest extends groovy.test.GroovyTestCase {}'
         // end::example_assert[]
    }
    
    void testFileTreeBuilderShortHandSyntax() {
        // tag::shorthand_syntax[]
        tmpDir = File.createTempDir()
        def fileTreeBuilder = new FileTreeBuilder(tmpDir)
        fileTreeBuilder.src {
            main {
               groovy {
                  'Foo.groovy'('println "Hello"')
               }
            }
            test {
               groovy {
                  'FooTest.groovy'('class FooTest extends groovy.test.GroovyTestCase {}')
               }
            }
         }
         // end::shorthand_syntax[]
         
         // tag::shorthand_syntax_assert[]
         assert new File(tmpDir, '/src/main/groovy/Foo.groovy').text == 'println "Hello"'
         assert new File(tmpDir, '/src/test/groovy/FooTest.groovy').text == 'class FooTest extends groovy.test.GroovyTestCase {}'
         // end::shorthand_syntax_assert[]
    }    
 }
