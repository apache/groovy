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
package org.apache.groovy.contracts.tests.other

import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test

import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

class MissingLineNumberTests extends BaseTestClass {

    def source = '''
    @Contracted
    package tests

    import groovy.contracts.*

    @Invariant({ str?.size() > 0 })
    class MissingLineNumber {

        private String str = "test"

        @Requires({ param1 != null })
        void operation1(def param1)  {
            // noop
        }

        void operation2(def param1)  {
            str = param1
        }

    }
    '''

    @Test
    void line_number_in_precondition_stacktrace() {

        def var = create_instance_of(source)

        try {
            var.operation1 null
        } catch (AssertionError ex) {
            ByteArrayOutputStream output = new ByteArrayOutputStream()
            PrintWriter writer = new PrintWriter(output)

            ex.printStackTrace(writer)
            writer.flush()
            writer.close()

            String stacktrace = new String(output.toByteArray())
            String errorline = ""

            stacktrace.eachLine {
                String line ->
                    if (line.contains("MissingLineNumber.operation1(")) {
                        errorline = line
                    }
            }

            println errorline

            assertTrue "error line must not be empty", errorline.size() > 0

            assertFalse "line number of assertion must not be missing", errorline.endsWith("(MissingLineNumber.groovy)")
        }
    }

    @Test
    void line_number_in_class_invariant_stacktrace() {

        def var = create_instance_of(source)

        try {
            var.operation2 ""
        } catch (AssertionError ex) {
            ByteArrayOutputStream output = new ByteArrayOutputStream()
            PrintWriter writer = new PrintWriter(output)

            ex.printStackTrace(writer)
            writer.flush()
            writer.close()

            String stacktrace = new String(output.toByteArray())
            String errorline = ""

            stacktrace.eachLine {
                String line ->
                    if (line.contains("MissingLineNumber.operation2(")) {
                        errorline = line
                    }
            }

            println errorline

            assertTrue "error line must not be empty", errorline.size() > 0

            assertFalse "line number of assertion must not be missing", errorline.endsWith("(MissingLineNumber.groovy)")
        }
    }
}
