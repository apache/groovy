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
package groovy.lang

import groovy.transform.CompileStatic
import org.junit.Test

@CompileStatic
class NamedValueTest {
    @Test
    void testGetters() {
        def nv = new NamedValue('foo', 'bar')
        assert 'foo' == nv.name
        assert 'bar' == nv.val
    }

    @Test
    void testEqualsAndHashCode() {
        def nv1 = new NamedValue('foo', 'bar')
        def nv2 = new NamedValue('foo', 'bar')
        assert nv1 == nv2

        def set = new HashSet()
        set.add(nv1)
        set.add(nv2)
        assert 1 == set.size()

        def nv3 = new NamedValue('foo2', 'bar')
        def nv4 = new NamedValue('foo', 'bar2')
        set.add(nv3)
        set.add(nv4)
        assert 3 == set.size()
    }

    @Test
    void testToString() {
        def nv = new NamedValue('foo', 'bar')
        assert "foo='bar'" == nv.toString()
    }

    @Test
    void testSerialize() {
        def nv = new NamedValue('foo', 'bar')
        def baos = new ByteArrayOutputStream()
        baos.withObjectOutputStream { oos ->
            oos.writeObject(nv)
        }
        new ByteArrayInputStream(baos.toByteArray()).withObjectInputStream {ois ->
            assert nv == ois.readObject()
        }
    }
}
