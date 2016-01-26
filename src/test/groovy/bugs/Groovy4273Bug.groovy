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
package groovy.bugs

import org.codehaus.groovy.control.MultipleCompilationErrorsException

class Groovy4273Bug extends GroovyShellTestCase {
    void testAnnotationWithWrongConstOnAPrecompiledJavaEnum() {
        try {
            shell.parse """
                import java.lang.annotation.*
                
                @Retention(RetentionPolicy.Runtime)
                @interface MyAnn {
                  String foo()  
                }
            """
            fail("The compilation should have failed as enum const Runtime does not exist on enum RetentionPolicy (should be RUNTIME)")
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("No enum const java.lang.annotation.RetentionPolicy.Runtime")
        }
    }

    void testAnnotationWithWrongConstOnAGroovyEnum() {
        try {
            shell.parse """
                import java.lang.annotation.*
                
                enum RetentionPolicy4273 {
                    SOURCE, CLASS, RUNTIME
                }
                
                @Retention(RetentionPolicy.RUNTIME)
                @Target(ElementType.TYPE)
                @interface Retention4273 {
                    RetentionPolicy4273 value();
                }
                
                @Retention4273(RetentionPolicy4273.Runtime)
                class Foo {}
            """
            fail("The compilation should have failed as enum const Runtime does not exist on enum RetentionPolicy4273 (should be RUNTIME)")
        } catch (MultipleCompilationErrorsException e) {
            def syntaxError = e.errorCollector.getSyntaxError(0)
            assert syntaxError.message.contains("No enum const RetentionPolicy4273.Runtime")
        }
    }
}
