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
package groovy.annotations

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript

@CompileStatic
final class PackageAndImportAnnotationTest {

    @Test
    void testTransformationOfPropertyInvokedOnThis() {
        assertScript '''
            def x = new groovy.annotations.MyClass()
            assert x.class.annotations[0].value() == 60
            assert x.class.package.annotations[0].value() == 30
            new groovy.ant.AntBuilder().with {
                mkdir(dir:'temp')
                delete(file:'temp/log.txt')
                taskdef(name:'groovyc', classname:'org.codehaus.groovy.ant.Groovyc')
                groovyc(srcdir:'src/test', destdir:'temp', includes:'groovy/annotations/MyClass.groovy')
                def text = new File('temp/log.txt').text
                delete(dir:'temp')

                assert text.contains('ClassNode 60')
                assert text.contains('PackageNode 40')
                assert text.contains('ImportNode 50')
            }
        '''
    }
}
