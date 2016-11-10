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
package groovy.transform.stc
/**
 * Unit tests for static type checking : closure parameter type resolution
 */
class ClosureParamTypeResolverSTCTest extends StaticTypeCheckingTestCase {
    void testInferenceForDGM_CollectUsingExplicitIt() {
        assertScript '''
            import groovy.transform.stc.*

            def transform(item, @ClosureParams(value=FromString, conflictResolutionStrategy=PickFirstResolver, options=["Integer", "String"]) Closure condition) {
              if (condition.parameterTypes[0].simpleName == 'String')
                condition(item instanceof String ? item : item.toString())
              else
                condition(item instanceof Integer ? item : item.toString().size())
            }

            assert transform('dog') { String s -> s * 2 } == 'dogdog'
            assert transform('dog') { Integer i -> i * 2 } == 6
            assert transform('dog') { it.class.simpleName[0..it] } == 'Inte'
            assert transform(35) { String s -> s * 2 } == '3535'
            assert transform(35) { Integer i -> i * 2 } == 70
            assert transform(35) { it * 2 } == 70
        '''
    }
}
