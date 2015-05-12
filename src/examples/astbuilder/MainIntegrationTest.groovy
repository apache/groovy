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
package examples.astbuilder

import org.codehaus.groovy.tools.ast.TransformTestHelper
import org.codehaus.groovy.control.CompilePhase

/**
 *
 * This TestCase shows how to invoke an AST Transformation from a unit test.
 * An IDE will let you step through the AST Transformation using this approach. 
 *
 * @author Hamlet D'Arcy
 */

class MainIntegrationTest extends GroovyTestCase {

     public void testInvokeUnitTest() {
        def invoker = new TransformTestHelper(new MainTransformation(), CompilePhase.CANONICALIZATION)

        def file = new File('./MainExample.groovy')
        assert file.exists()

        def clazz = invoker.parse(file)
        def tester = clazz.newInstance()
        tester.main(null)       // main method added with AST transform
    }
}
