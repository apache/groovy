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

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Under static compilation, {@code toString()} on a receiver whose static type
 * does not declare it resolves to a {@code DefaultGroovyMethods} extension. The
 * result must match dynamic dispatch, which picks a {@code toString()} override
 * declared below {@code AbstractCollection}, {@code AbstractMap} or {@code Object}.
 */
final class Groovy12476 {

    private static final String CLASSES = '''
        import groovy.transform.CompileStatic

        interface Element {}
        class JsonList extends ArrayList<Object> implements Element { String toString() { 'JSONLIST' } }
        class JsonMap extends HashMap<String, Object> implements Element { String toString() { 'JSONMAP' } }
        class Plain implements Element { String toString() { 'PLAIN' } }

        @CompileStatic
        class SC {
            static String element(Element e) { e.toString() }
            static String list(List e) { e.toString() }
            static String collection(Collection e) { e.toString() }
            static String iterable(Iterable e) { e.toString() }
            static String map(Map e) { e.toString() }
            static String object(Object e) { e.toString() }
            static String arrayList(ArrayList e) { e.toString() }
            static String hashMap(HashMap e) { e.toString() }
        }
        String dyn(Object e) { e.toString() }
    '''

    @Test
    void testOverrideReachedThroughInterfaceOrObject() {
        assertScript CLASSES + '''
            def list = new JsonList() << [k: 1]
            def map = new JsonMap(k: [1])
            assert SC.element(list) == 'JSONLIST'
            assert SC.list(list) == 'JSONLIST'
            assert SC.collection(list) == 'JSONLIST'
            assert SC.iterable(list) == 'JSONLIST'
            assert SC.object(list) == 'JSONLIST'
            assert SC.element(map) == 'JSONMAP'
            assert SC.map(map) == 'JSONMAP'
            assert SC.object(map) == 'JSONMAP'
            assert SC.element(new Plain()) == 'PLAIN'
        '''
    }

    @Test
    void testOverrideReachedThroughSuperclassWithoutOwnToString() {
        assertScript CLASSES + '''
            assert SC.arrayList(new JsonList() << 1) == 'JSONLIST'
            assert SC.hashMap(new JsonMap(k: 1)) == 'JSONMAP'
        '''
    }

    @Test
    void testStaticAndDynamicAgree() {
        assertScript CLASSES + '''
            import java.util.concurrent.ConcurrentHashMap
            import java.util.concurrent.CopyOnWriteArrayList

            def nested = [k: 1]
            def lists = [new ArrayList([nested]), Arrays.asList(nested), List.of(nested), new HashSet([nested]),
                         Collections.unmodifiableList([nested]), Collections.synchronizedList([nested]),
                         new CopyOnWriteArrayList([nested]), new JsonList() << nested,
                         new ArrayList([nested]) { String toString() { 'ANON' } }]
            def maps = [new HashMap(k: [1]), new TreeMap(k: [1]), Map.of('k', [1]),
                        Collections.unmodifiableMap([k: [1]]), new ConcurrentHashMap(k: [1]),
                        new Properties().tap { put('k', 'v') }, new JsonMap(k: [1])]
            lists.each { v ->
                String d = dyn(v)
                assert SC.collection(v) == d
                assert SC.iterable(v) == d
                assert SC.object(v) == d
                if (v instanceof List) assert SC.list(v) == d
            }
            maps.each { v ->
                String d = dyn(v)
                assert SC.map(v) == d
                assert SC.object(v) == d
            }
            [1..3, 'abc', 1.50G, new Object(), [1, 2] as int[], Optional.of(nested)].each { v ->
                assert SC.object(v) == dyn(v)
            }
        '''
    }

    @Test
    void testStringConcatenationMatchesInterpolation() {
        assertScript '''
            import java.util.concurrent.ConcurrentHashMap
            import java.util.concurrent.CopyOnWriteArrayList

            def values = [new Hashtable(k: [1]), new ConcurrentHashMap(k: [1]), new CopyOnWriteArrayList([[k: 1]])]
            values.each { v ->
                assert 'x' + v == "x$v"
                if (v instanceof Map) assert v + 'x' == "${v}x"
            }
            assert 'x' + new Hashtable(k: 1) == 'x[k:1]'
            assert new Hashtable(k: 1) + 'x' == '[k:1]x'
            assert 'x[k:1]' - new Hashtable(k: 1) == 'x'
        '''
    }
}
