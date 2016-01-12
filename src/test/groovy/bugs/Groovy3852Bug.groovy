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

import gls.CompilableTestSupport

class Groovy3852Bug extends CompilableTestSupport {
    def gcl = new GroovyClassLoader()
    void testDuplicationAnnotationOnClassNoParams() {
        try {
            gcl.parseClass """
                @Deprecated
                @Deprecated
                @Deprecated
                class TooDeprecatedGroovy3852V1 {}
            """
            fail('The class compilation should have failed as it has duplication annotations')
        }catch(ex) {
            assertTrue ex.message.contains('Cannot specify duplicate annotation')
        }
    }

    void testDuplicationAnnotationOnClassWithParams() {
        try {
            gcl.parseClass """
                import java.lang.annotation.*
                @Retention(value=RetentionPolicy.CLASS)
                @Retention(value=RetentionPolicy.CLASS)
                @interface TooDeprecatedGroovy3852V2 {}
            """
            fail('The class compilation should have failed as it has duplication annotations')
        }catch(ex) {
            assertTrue ex.message.contains('Cannot specify duplicate annotation')
        }
    }

    void testDuplicationAnnotationOnOtherTargets() {
        try {
            gcl.parseClass """
                class TooDeprecatedGroovy3852V3 {
                    @Deprecated
                    @Deprecated
                    @Deprecated
                    def m() {}
                }
            """
            fail('The class compilation should have failed as it has duplication annotations on a method')
        }catch(ex) {
            assertTrue ex.message.contains('Cannot specify duplicate annotation')
        }

        try {
            gcl.parseClass """
                class TooDeprecatedGroovy3852V3 {
                    @Deprecated
                    @Deprecated
                    @Deprecated
                    def f
                }
            """
            fail('The class compilation should have failed as it has duplication annotations on a field')
        }catch(ex) {
            assertTrue ex.message.contains('Cannot specify duplicate annotation')
        }
    }

    void testDuplicationNonRuntimeRetentionPolicyAnnotations() {
        try {
            gcl.parseClass """
                @Newify(auto=false, value=String)
                @Newify(auto=false, value=String)
                class Groovy3930 {}
            """
        } catch (ex) {
            fail('The class compilation should have succeeded as it has duplication annotations but with retention policy not at RUNTIME')
        }
    }

    void testDuplicationAnnotationsForImport() {
        // TODO: replace with better test - Newify doesn't really make sense for import
        try {
            gcl.parseClass """
                @Newify(auto=false, value=String)
                @Newify(auto=false, value=String)
                import java.lang.String
                class Groovy3925 {}
            """
        } catch (ex) {
            fail('The class compilation should have succeeded as it has duplication annotations but with retention policy not at RUNTIME')
        }
    }
}