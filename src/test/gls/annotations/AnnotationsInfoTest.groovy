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
package gls.annotations

class AnnotationsInfoTest extends AnnotationsTestBase {

    void testClassWithoutParameterExtendsClassWithFixedParameter() {
        createClassInfo """
            import gls.annotations.*

            @HasRuntimeRetention
            class B extends ArrayList<Long> {
                @HasSourceRetention
                def fieldS
                
                @HasRuntimeRetention
                def fieldR
                
                @HasDefaultClassRetention
                def methodDC() {}
                
                @HasExplicitClassRetention
                def methodEC() {}
            }
        """
        assert !annotations.any{ it.contains('SourceRetention') }
        annotations.findAll { it.contains('RuntimeRetention') }.with { annos ->
            assert annos.size() == 2
            assert annos.every{ it.contains('visible=true') }
            assert annos.every{ it.contains('class B') || it.contains('field B#fieldR') }
        }
        annotations.findAll { it.contains('ClassRetention') }.with { annos ->
            assert annos.size() == 2
            assert annos.every{ it.contains('visible=false') }
            assert annos.every{ it.contains('method B#methodDC') || it.contains('method B#methodEC') }
        }
    }
}
