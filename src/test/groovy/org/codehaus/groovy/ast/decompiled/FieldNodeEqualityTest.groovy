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
package org.codehaus.groovy.ast.decompiled

import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Test

final class FieldNodeEqualityTest {
    @Test
    void testEquality() {
        FieldNode fn1 = FieldNode.newStatic(CompilerConfiguration, 'JDK11')
        assert fn1.equals(fn1)
        FieldNode fn2 = new LazyFieldNode(() -> fn1, 'JDK11')
        assert fn1.equals(fn2)
        assert fn2.equals(fn1)
        assert fn2.equals(fn2)

        List nodes1 = [fn1]
        assert nodes1.contains(fn1)
        List nodes2 = [fn2]
        assert nodes2.contains(fn2)
    }
}
