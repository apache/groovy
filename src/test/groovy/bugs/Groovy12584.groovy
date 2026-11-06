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

import org.codehaus.groovy.runtime.typehandling.GroovyCastException
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * A cast of a collection to a collection type gives the same result, or the
 * same {@link GroovyCastException}, whatever the target type.
 */
final class Groovy12584 {

    @Test
    void testCastToAbstractSubclassOfHashSet() {
        def err = shouldFail GroovyCastException, '''
            abstract class ASet extends HashSet {}
            Object list = [3, 1, 2, 1]
            ASet s = list
        '''
        assert err.message.contains("to class 'ASet'")
    }

    @Test
    void testCastToAbstractSubclassOfArrayList() {
        def err = shouldFail GroovyCastException, '''
            abstract class AList extends ArrayList {}
            Object set = [3, 1, 2] as LinkedHashSet
            AList l = set
        '''
        assert err.message.contains("to class 'AList'")
    }

    @Test
    void testCastToCollectionInterfaceOrAbstractType() {
        assertScript '''
            Object list = [3, 1, 2, 1]
            Set s = list
            assert s instanceof LinkedHashSet
            assert s as List == [3, 1, 2]

            AbstractSet a = list
            assert a instanceof LinkedHashSet
            assert a as List == [3, 1, 2]

            Object iterable = 'ab'.chars().boxed()
            Collection c = iterable
            assert c instanceof ArrayList
            assert c == [97, 98]
        '''
    }
}
