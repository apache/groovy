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
package groovy.generated

import org.junit.Test

final class AutoExternalizeGeneratedTest extends AbstractGeneratedAstTestCase {

    @Test
    void testImplicit() {
        def implicitAutoExternalize = parseClass '''
            @groovy.transform.AutoExternalize
            class ClassUnderTest {
            }
        '''
        assert implicitAutoExternalize.annotations.size() == 0
        assert implicitAutoExternalize.declaredAnnotations.size() == 0
        assertMethodIsAnnotated(implicitAutoExternalize, 'readExternal', ObjectInput)
        assertMethodIsAnnotated(implicitAutoExternalize, 'writeExternal', ObjectOutput)
    }

    @Test
    void testExplicit() {
        def explicitAutoExternalize = parseClass '''
            @groovy.transform.AutoExternalize
            class ClassUnderTest {
                void readExternal(ObjectInput in) {
                }
                void writeExternal(ObjectOutput out) throws IOException {
                }
            }
        '''
        assert explicitAutoExternalize.annotations.size() == 0
        assert explicitAutoExternalize.declaredAnnotations.size() == 0
        assertMethodIsNotAnnotated(explicitAutoExternalize, 'readExternal', ObjectInput)
        assertMethodIsNotAnnotated(explicitAutoExternalize, 'writeExternal', ObjectOutput)
    }
}
