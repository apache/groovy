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
package semantics

import gls.CompilableTestSupport

class OptionalityTest extends CompilableTestSupport {

    void testOptionalParentheses() {
        // tag::optional_parentheses[]
        println 'Hello World'
        def maximum = Math.max 5, 10
        // end::optional_parentheses[]
    }
    
    void testRequiredParentheses() {
        // tag::required_parentheses[]
        println()
        println(Math.max(5, 10))
        // end::required_parentheses[]
    }
    
    void testOptionalSemicolons() {
        // tag::single_statement_with_semicolon[]
        assert true;
        // end::single_statement_with_semicolon[]
        
        // tag::single_statement_without_semicolon[]
        assert true
        // end::single_statement_without_semicolon[]
    }
    
    void testRequiredSemicolons() {
        // tag::statements_separated_by_semicolon[]
        boolean a = true; assert a
        // end::statements_separated_by_semicolon[]
    }
    
    void testOptionalReturn() {
        assertScript '''
            // tag::return_keyword[]
            int add(int a, int b) {
                return a+b
            }
            assert add(1, 2) == 3
            // end::return_keyword[]
        '''
        assertScript '''
            // tag::omitted_return_keyword[]
            int add(int a, int b) {
                a+b
            }
            assert add(1, 2) == 3
            // end::omitted_return_keyword[]
        '''
    }
    
    void testOptionalPublic() {
        shouldCompile '''
            // tag::public_keyword[]
            public class Server {
                public String toString() { "a server" }
            }
            // end::public_keyword[]
        '''
        shouldCompile '''
            // tag::omitted_public[]
            class Server {
                String toString() { "a server" }
            }
            // end::omitted_public[]
        '''
    }
}
