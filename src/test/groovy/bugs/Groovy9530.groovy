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
package bugs

import org.codehaus.groovy.classgen.asm.AbstractBytecodeTestCase

final class Groovy9530 extends AbstractBytecodeTestCase {

    static class StaticClass {
        public static final int STATIC_VALUE = getStaticValue()

        private static int getStaticValue() {
            getClassLoader().getResources('absent thingy').toList().size()
        }
    }

    void testConstantInlining() {
        def bytecode = compile '''import bugs.Groovy9530.StaticClass
            class C {
                public static final int VALUE = StaticClass.STATIC_VALUE
            }
        '''
        assert bytecode.hasSequence([
            'public final static I VALUE'
        ])
        assert !bytecode.hasSequence([
            'public final static I VALUE = 0' // no initializer should exist!
        ])
    }
}
