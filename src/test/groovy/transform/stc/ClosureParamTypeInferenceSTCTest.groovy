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
 * Unit tests for static type checking : closure parameter type inference.
 */
class ClosureParamTypeInferenceSTCTest extends StaticTypeCheckingTestCase {
    void testInferenceForDGM_CollectUsingExplicitIt() {
        assertScript '''
            ['a','b'].collect { it -> it.toUpperCase() }
        '''
    }

    void testInferenceForDGM_CollectUsingExplicitItAndIncorrectType() {
        shouldFailWithMessages '''
            ['a','b'].collect { Date it -> it.toUpperCase() }
        ''', 'Expected parameter of type java.lang.String but got java.util.Date'
    }

    void testInferenceForDGM_CollectUsingImplicitIt() {
        assertScript '''
            ['a','b'].collect { it.toUpperCase() }
        '''
    }

    void testInferenceForDGM_eachUsingExplicitIt() {
        assertScript '''
            ['a','b'].each { it -> it.toUpperCase() }
        '''
    }

    void testInferenceForDGM_eachUsingImplicitIt() {
        assertScript '''
            ['a','b'].each { it.toUpperCase() }
        '''
    }

    void testInferenceForDGM_CollectUsingImplicitItAndLUB() {
        assertScript '''
            assert [1234, 3.14].collect { it.intValue() } == [1234,3]
        '''
    }

    void testInferenceForDGM_countUsingFirstSignature() {
        assertScript '''
            def src = [a: 1, b:2, c:3]
            assert src.count { k,v -> v>1 } == 2
        '''
    }

    void testInferenceForDGM_countUsingSecondSignature() {
        assertScript '''
            def src = [a: 1, b:2, c:3]
            assert src.count { e -> e.value>1 } == 2
        '''
    }

    void testInferenceForDGM_countUsingSecondSignatureAndImplicitIt() {
        assertScript '''
            def src = [a: 1, b:2, c:3]
            assert src.count { it.value>1 } == 2
        '''
    }

    void testInferenceForDGM_collectManyUsingFirstSignature() {
        assertScript '''
def map = [bread:3, milk:5, butter:2]
def result = map.collectMany{ k, v -> k.startsWith('b') ? k.toList() : [] }
assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
'''
    }

    void testInferenceForDGM_collectManyUsingSecondSignature() {
        assertScript '''
def map = [bread:3, milk:5, butter:2]
def result = map.collectMany{ e -> e.key.startsWith('b') ? e.key.toList() : [] }
assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
'''
    }

    void testInferenceForDGM_collectManyUsingSecondSignatureAndImplicitIt() {
        assertScript '''
def map = [bread:3, milk:5, butter:2]
def result = map.collectMany{ it.key.startsWith('b') ? it.key.toList() : [] }
assert result == ['b', 'r', 'e', 'a', 'd', 'b', 'u', 't', 't', 'e', 'r']
'''
    }

    void testInferenceForDGM_Collect2() {
        assertScript '''
def items = []
['a','b','c'].collect(items) { it.toUpperCase() }
'''
    }

    void testInferenceForDGM_CollectMap() {
        assertScript '''
        assert [a: 'foo',b:'bar'].collect { k,v -> k+v } == ['afoo','bbar']
        assert [a: 'foo',b:'bar'].collect { e -> e.key+e.value } == ['afoo','bbar']
        assert [a: 'foo',b:'bar'].collect { it.key+it.value } == ['afoo','bbar']
'''
    }

    void testInferenceForDGM_CollectMapWithCollection() {
        assertScript '''
        assert [a: 'foo',b:'bar'].collect([]) { k,v -> k+v } == ['afoo','bbar']
        assert [a: 'foo',b:'bar'].collect([]) { e -> e.key+e.value } == ['afoo','bbar']
        assert [a: 'foo',b:'bar'].collect([]) { it.key+it.value } == ['afoo','bbar']
'''
    }

    void testInferenceForDGM_collectEntries() {
        assertScript '''
            assert ['a','b','c'].collectEntries { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectEntriesWithCollector() {
        assertScript '''
            assert ['a','b','c'].collectEntries([:]) { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectEntriesIterator() {
        assertScript '''
            assert ['a','b','c'].iterator().collectEntries { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectEntriesIteratorWithCollector() {
        assertScript '''
            assert ['a','b','c'].iterator().collectEntries([:]) { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectEntriesOnMap() {
        assertScript '''
            assert [a:'a',b:'b',c:'c'].collectEntries { k,v -> [k+k, v.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries { e -> [e.key+e.key, e.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries { [it.key+it.key, it.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
'''
    }

    void testInferenceForDGM_collectEntriesOnMapWithCollector() {
        assertScript '''
            assert [a:'a',b:'b',c:'c'].collectEntries([:]) { k,v -> [k+k, v.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries([:]) { e -> [e.key+e.key, e.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
            assert [a:'a',b:'b',c:'c'].collectEntries([:]) { [it.key+it.key, it.value.toUpperCase() ]} == [aa:'A',bb:'B',cc:'C']
'''
    }

    void testInferenceForDGM_collectEntriesOnArray() {
        assertScript '''
            String[] array = ['a','b','c']
            assert array.collectEntries { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectEntriesOnArrayWithCollector() {
        assertScript '''
            String[] array = ['a','b','c']
            assert array.collectEntries([:]) { [it, it.toUpperCase() ]} == [a:'A',b:'B',c:'C']
'''
    }

    void testInferenceForDGM_collectManyOnIterable() {
        assertScript '''
            assert (0..5).collectMany { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
'''
    }

    void testInferenceForDGM_collectManyOnIterator() {
        assertScript '''
            assert (0..5).iterator().collectMany { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
'''
    }

    void testInferenceForDGM_collectManyOnIterableWithCollector() {
        assertScript '''
            assert (0..5).collectMany([]) { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
'''
    }

    void testInferenceForDGM_collectManyOnMap() {
        assertScript '''
            assert [a:0,b:1,c:2].collectMany { k,v -> [v, 2*v ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany { e -> [e.value, 2*e.value ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany { [it.value, 2*it.value ]} == [0,0,1,2,2,4]
'''
    }

    void testInferenceForDGM_collectManyOnMapWithCollector() {
        assertScript '''
            assert [a:0,b:1,c:2].collectMany([]) { k,v -> [v, 2*v ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany([]) { e -> [e.value, 2*e.value ]} == [0,0,1,2,2,4]
            assert [a:0,b:1,c:2].collectMany([]) { [it.value, 2*it.value ]} == [0,0,1,2,2,4]
'''
    }

    void testInferenceForDGM_collectManyOnArray() {
        assertScript '''
            Integer[] arr = (0..5) as Integer[]
            assert arr.collectMany { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
'''
    }

    void testDGM_collectOnArray() {
        assertScript '''
            String[] arr = ['foo', 'bar', 'baz']
            assert arr.collect { it.startsWith('ba') } == [false, true, true]
            List<Boolean> answer = [true]
            arr.collect(answer) { it.startsWith('ba') }
            assert answer == [true, false, true, true]
        '''
    }

    void testDGM_collectOnIterator() {
        assertScript '''
            Iterator<String> itr = ['foo', 'bar', 'baz'].iterator()
            assert itr.collect { it.startsWith('ba') } == [false, true, true]
        '''
    }

    void testInferenceOnNonExtensionMethod() {
        assertScript '''import groovy.transform.stc.ClosureParams
            import groovy.transform.stc.FirstParam
            public <T> T foo(T arg, @ClosureParams(FirstParam) Closure c) { c.call(arg) }
            assert foo('a') { it.toUpperCase() } == 'A'
'''
    }

    void testFromStringWithSimpleType() {
        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

void foo(@ClosureParams(value=FromString,options="java.lang.String") Closure cl) { cl.call('foo') }
foo { String str -> println str.toUpperCase()}
'''

        shouldFailWithMessages '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

void foo(@ClosureParams(value=FromString,options="java.lang.String") Closure cl) { cl.call('foo') }
foo { Date str -> println str}
''', 'Expected parameter of type java.lang.String but got java.util.Date'
    }

    void testFromStringWithGenericType() {
        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

void foo(@ClosureParams(value=FromString,options="java.util.List<java.lang.String>") Closure cl) { cl.call(['foo']) }
foo { List<String> str -> str.each { println it.toUpperCase() } }
'''

        shouldFailWithMessages '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

void foo(@ClosureParams(value=FromString,options="java.util.List<java.lang.String>") Closure cl) { cl.call(['foo']) }
foo { List<Date> d -> d.each { println it } }
''', 'Expected parameter of type java.util.List <java.lang.String> but got java.util.List <Date>'
    }

    void testFromStringWithDirectGenericPlaceholder() {

        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

public <T> void foo(T t, @ClosureParams(value=FromString,options="T") Closure cl) { cl.call(t) }
foo('hey') { println it.toUpperCase() }
'''

    }

    void testFromStringWithGenericPlaceholder() {
        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

public <T> void foo(T t, @ClosureParams(value=FromString,options="java.util.List<T>") Closure cl) { cl.call([t,t]) }
foo('hey') { List<String> str -> str.each { println it.toUpperCase() } }
'''

    }

    void testFromStringWithGenericPlaceholderFromClass() {
        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

    class Foo<T> {
        public void foo(@ClosureParams(value=FromString,options="java.util.List<T>") Closure cl) { cl.call(['hey','ya']) }
    }
    def foo = new Foo<String>()

    foo.foo { List<String> str -> str.each { println it.toUpperCase() } }
'''
    }

    void testFromStringWithGenericPlaceholderFromClassWithTwoGenerics() {
        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

    class Foo<T,U> {
        public void foo(@ClosureParams(value=FromString,options="java.util.List<U>") Closure cl) { cl.call(['hey','ya']) }
    }
    def foo = new Foo<Integer,String>()

    foo.foo { List<String> str -> str.each { println it.toUpperCase() } }
'''
    }

    void testFromStringWithGenericPlaceholderFromClassWithTwoGenericsAndNoExplicitSignature() {
        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

    class Foo<T,U> {
        public void foo(@ClosureParams(value=FromString,options="java.util.List<U>") Closure cl) { cl.call(['hey','ya']) }
    }
    def foo = new Foo<Integer,String>()

    foo.foo { it.each { println it.toUpperCase() } }
'''
    }

    void testFromStringWithGenericPlaceholderFromClassWithTwoGenericsAndNoExplicitSignatureAndNoFQN() {
        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

    class Foo<T,U> {
        public void foo(@ClosureParams(value=FromString,options="List<U>") Closure cl) { cl.call(['hey','ya']) }
    }
    def foo = new Foo<Integer,String>()

    foo.foo { it.each { println it.toUpperCase() } }
'''
    }

    void testFromStringWithGenericPlaceholderFromClassWithTwoGenericsAndNoExplicitSignatureAndNoFQNAndReferenceToSameUnitClass() {
        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

    class Foo {
        void bar() {
            println 'Haha!'
        }
    }

    class Tor<D,U> {
        public void foo(@ClosureParams(value=FromString,options="List<U>") Closure cl) { cl.call([new Foo(), new Foo()]) }
    }
    def tor = new Tor<Integer,Foo>()

    tor.foo { it.each { it.bar() } }
'''
    }

    void testFromStringWithGenericPlaceholderFromClassWithTwoGenericsAndNoExplicitSignatureAndNoFQNAndReferenceToSameUnitClassAndTwoArgs() {
        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

    class Foo {
        void bar() {
            println 'Haha!'
        }
    }

    class Tor<D,U> {
        public void foo(@ClosureParams(value=FromString,options=["D,List<U>"]) Closure cl) { cl.call(3, [new Foo(), new Foo()]) }
    }
    def tor = new Tor<Integer,Foo>()

    tor.foo { r, e -> r.times { e.each { it.bar() } } }
'''
    }

    void testFromStringWithGenericPlaceholderFromClassWithTwoGenericsAndPolymorphicSignature() {
        assertScript '''import groovy.transform.stc.FromString
import groovy.transform.stc.ClosureParams

    class Foo {
        void bar() {
            println 'Haha!'
        }
    }

    class Tor<D,U> {
        public void foo(@ClosureParams(value=FromString,options=["D,List<U>", "D"]) Closure cl) {
            if (cl.maximumNumberOfParameters==2) {
                cl.call(3, [new Foo(), new Foo()])
            } else {
                cl.call(3)
            }
        }
    }
    def tor = new Tor<Integer,Foo>()

    tor.foo { r, e -> r.times { e.each { it.bar() } } }
    tor.foo { it.times { println 'polymorphic' } }
'''
    }

    void testStringGroovyMethodsFindMethodWithVargs() {
        assertScript '''
            "75001 Paris".find(/(\\d{5}\\s(\\w+))/) { all, zip, city -> println all.toUpperCase() }
'''

        assertScript '''
            "75001 Paris".find(/(\\d{5}\\s(\\w+))/) { String all, String zip, String city -> println all.toUpperCase() }
'''
        shouldFailWithMessages '''
            "75001 Paris".find(/(\\d{5}\\s(\\w+))/) { String all, Date zip, String city -> println all.toUpperCase() }
''', 'Expected parameter of type java.lang.String but got java.util.Date'
    }

    void testStringGroovyMethodsFindMethodWithList() {
        assertScript '''
            "75001 Paris".find(/(\\d{5}\\s(\\w+))/) { List<String> all -> println all*.toUpperCase() }
'''
    }

    void testInferenceForDGM_countIterableOrIterator() {
        assertScript '''
            assert ['Groovy','Java'].count { it.length() > 4 } == 1
        '''
        assertScript '''
            assert ['Groovy','Java'].iterator().count { it.length() > 4 } == 1
        '''
    }

    void testInferenceForDGM_countMap() {
        assertScript '''
            assert [G:'Groovy',J:'Java'].count { k,v -> v.length() > 4 } == 1
            assert [G:'Groovy',J:'Java'].count { e -> e.value.length() > 4 } == 1
            assert [G:'Groovy',J:'Java'].count { it.value.length() > 4 } == 1
        '''
    }

    void testInferenceForDGM_countArray() {
        assertScript '''
            String[] array = ['Groovy','Java']
            assert array.count { it.length() > 4 } == 1
        '''
    }

    void testInferenceForDGM_countBy() {
        assertScript '''
            assert ['Groovy','yvoorG'].countBy { it.length() } == [6:2]
        '''
        assertScript '''
            assert ['Groovy','yvoorG'].iterator().countBy { it.length() } == [6:2]
        '''
    }
    void testInferenceForDGM_countByArray() {
        assertScript '''
            String[] array = ['Groovy','yvoorG']
            assert array.countBy { it.length() } == [6:2]
        '''
    }
    void testInferenceForDGM_countByMap() {
        assertScript '''
            assert [langs:['Groovy','Java']].countBy { k,v -> k.length() } == [5:1]
            assert [langs:['Groovy','Java']].countBy { e -> e.key.length() } == [5:1]
            assert [langs:['Groovy','Java']].countBy { it.key.length() } == [5:1]
        '''
    }

    void testInferenceForDGM_downto() {
        assertScript '''
            BigDecimal sum = 0
            10.0.downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            BigInteger sum = 0
            10G.downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            double sum = 0
            10d.downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Double sum = 0
            new Double(10).downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            float sum = 0
            10f.downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Float sum = 0
            new Float(10).downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            long sum = 0
            10L.downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Long sum = 0
            new Long(10).downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            def sum = 0
            new Byte((byte)10).downto(0) {
                sum += 2*it
            }
            assert sum == 110
        '''
    }

    void testInferenceForDGM_upto() {
        assertScript '''
            BigDecimal sum = 0
            0.0.upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            BigInteger sum = 0
            0G.upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            double sum = 0
            0d.upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Double sum = 0
            new Double(0).upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            float sum = 0
            0f.upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Float sum = 0
            new Float(0).upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            long sum = 0
            0L.upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            Long sum = 0
            new Long(0).upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
        assertScript '''
            def sum = 0
            new Byte((byte)0).upto(10) {
                sum += 2*it
            }
            assert sum == 110
        '''
    }

    void testInferenceForDGM_dropWhileOnIterable() {
        assertScript '''
        assert (0..10).dropWhile { it<5 } == (5..10)
        assert (0..10).dropWhile { int i -> i<5 } == (5..10)
        '''
    }

    void testInferenceForDGM_dropWhileOnList() {
        assertScript '''
        assert [0,1,2,3,4,5,6,7,8,9,10].dropWhile { it<5 } == [5,6,7,8,9,10]
        assert [0,1,2,3,4,5,6,7,8,9,10].dropWhile { int i -> i<5 } == [5,6,7,8,9,10]
        '''
    }

    void testInferenceForDGM_dropWhileOnIterator() {
        assertScript '''
        assert [0,1,2,3,4,5,6,7,8,9,10].iterator().dropWhile { it<5 } as List == [5,6,7,8,9,10]
        assert [0,1,2,3,4,5,6,7,8,9,10].iterator().dropWhile { int i -> i<5 } as List == [5,6,7,8,9,10]
        '''
    }

    void testInferenceForDGM_dropWhileOnArray() {
        assertScript '''
        Integer[] array = [0,1,2,3,4,5,6,7,8,9,10]
        assert array.iterator().dropWhile { it<5 } as List == [5,6,7,8,9,10]
        assert array.iterator().dropWhile { int i -> i<5 } as List == [5,6,7,8,9,10]
        '''
    }

    void testInferenceForDGM_eachByte() {
        assertScript '''
            byte[] array = new byte[0]
            array.eachByte { byte b -> b.intValue() }
            array.eachByte { it.intValue() }
        '''
        assertScript '''
            Byte[] array = new Byte[0]
            array.eachByte { Byte b -> b.intValue() }
            array.eachByte { it.intValue() }
        '''
    }

    void testInferenceForEachWithIndexOnMap() {
        assertScript '''
            [a:'A',bb:'B',ccc:'C'].eachWithIndex { k,v,i -> assert k.toUpperCase() == v*(1+i) }
            [a:'A',bb:'B',ccc:'C'].eachWithIndex { e,i -> assert e.key.toUpperCase() == e.value*(1+i) }
        '''
    }
    void testInferenceForEachWithIndexOnIterable() {
        assertScript '''
            ['1','2','3'].eachWithIndex { e,i -> assert e.toUpperCase() == String.valueOf(1+i) }
        '''
    }
    void testInferenceForEachWithIndexOnIterator() {
        assertScript '''
            ['1','2','3'].iterator().eachWithIndex { e,i -> assert e.toUpperCase() == String.valueOf(1+i) }
        '''
    }

    void testInferenceForDGM_everyOnMap() {
        assertScript '''
            assert [a:'A',b:'B',cc:'CC'].every { String k, String v -> k == v.toLowerCase() }
            assert [a:'A',b:'B',cc:'CC'].every { k, v -> k == v.toLowerCase() }
            assert [a:'A',b:'B',cc:'CC'].every { e -> e.key == e.value.toLowerCase() }
            assert [a:'A',b:'B',cc:'CC'].every { it.key == it.value.toLowerCase() }
        '''
    }
    void testInferenceForDGM_everyOnIterable() {
        assertScript '''
            assert ['foo','bar','baz'].every { String it -> it.length() == 3 }
            assert ['foo','bar','baz'].every { it -> it.length() == 3 }
            assert ['foo','bar','baz'].every { it.length() == 3 }
        '''
    }
    void testInferenceForDGM_everyOnIterator() {
        assertScript '''
            assert ['foo','bar','baz'].iterator().every { String it -> it.length() == 3 }
            assert ['foo','bar','baz'].iterator().every { it -> it.length() == 3 }
            assert ['foo','bar','baz'].iterator().every { it.length() == 3 }
        '''
    }
    void testInferenceForDGM_everyOnArray() {
        assertScript '''
            String[] items = ['foo','bar','baz']
            assert items.every { it.length() == 3 }
            assert items.every { String s -> s.length() == 3 }
        '''
    }

    void testInferenceForDGM_findIndexOf() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.findIndexOf { it.startsWith('ba') == 1 }
            assert items1.findIndexOf { String s -> s.startsWith('ba') == 1 }
            def items2 = ['foo','bar','baz']
            assert items2.findIndexOf { it.startsWith('ba') == 1 }
            assert items2.iterator().findIndexOf { it.startsWith('ba') == 1 }
        '''
    }

    void testInferenceForDGM_findLastIndexOf() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.findLastIndexOf { it.startsWith('ba') == 2 }
            assert items1.findLastIndexOf { String s -> s.startsWith('ba') == 2 }
            def items2 = ['foo','bar','baz']
            assert items2.findLastIndexOf { it.startsWith('ba') == 2 }
            assert items2.iterator().findLastIndexOf { it.startsWith('ba') == 2 }
        '''
    }

    void testInferenceForDGM_findIndexValues() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.findIndexValues { it.startsWith('ba') } == [1, 2]
            assert items1.findIndexValues { String s -> s.startsWith('ba') } == [1, 2]
            def items2 = ['foo','bar','baz']
            assert items2.findIndexValues { it.startsWith('ba') } == [1, 2]
            assert items2.iterator().findIndexValues { it.startsWith('ba') } == [1, 2]
        '''
    }

    void testInferenceForDGM_findResult() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.findResult { it.startsWith('ba') ? it : null } == 'bar'
            def items2 = ['foo','bar','baz']
            assert items2.findResult { it.startsWith('ba') ? it : null } == 'bar'
            assert items2.iterator().findResult { it.startsWith('ba') ? it : null } == 'bar'
        '''
    }

    void testInferenceForDGM_findResults() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.findResults { it.startsWith('ba') ? it : null } == ['bar', 'baz']
            def items2 = ['foo','bar','baz']
            assert items2.findResults { it.startsWith('ba') ? it : null } == ['bar', 'baz']
            assert items2.iterator().findResults { it.startsWith('ba') ? it : null } == ['bar', 'baz']
        '''
    }

    void testInferenceForDGM_split() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.split { it.startsWith('ba') } == [['bar', 'baz'], ['foo']]
            Collection items2 = ['foo','bar','baz']
            assert items2.split { it.startsWith('ba') } == [['bar', 'baz'], ['foo']]
        '''
    }

    void testInferenceForDGM_sum() {
        assertScript '''
            String[] items1 = ['foo','bar','baz']
            assert items1.sum { it.toUpperCase() } == 'FOOBARBAZ'
            def items2 = ['fi','fo','fum']
            assert items2.sum('FEE') { it.toUpperCase() } == 'FEEFIFOFUM'
        '''
    }

    void testInferenceForDGM_findOnCollection() {
        assertScript '''
            assert ['a','bbb','ccc'].find { String it -> it.length() == 3 } == 'bbb'
            assert ['a','bbb','ccc'].find { it -> it.length() == 3 } == 'bbb'
            assert ['a','bbb','ccc'].find { it.length() == 3 } == 'bbb'
        '''
    }
    void testInferenceForDGM_findOnArray() {
        assertScript '''
            String[] arraylistOfStrings = ['a','bbb','ccc']
            assert arraylistOfStrings.find { String it -> it.length() == 3 } == 'bbb'
            assert arraylistOfStrings.find { it -> it.length() == 3 } == 'bbb'
            assert arraylistOfStrings.find { it.length() == 3 } == 'bbb'
        '''
    }
    void testInferenceForDGM_findOnMap() {
        assertScript '''
            assert [a:2,b:4,c:6].find { String k, int v -> k.toUpperCase()=='C' && 2*v==12 } instanceof Map.Entry
            assert [a:2,b:4,c:6].find { k, v -> k.toUpperCase()=='C' && 2*v==12 } instanceof Map.Entry
            assert [a:2,b:4,c:6].find { e -> e.key.toUpperCase()=='C' && 2*e.value==12 } instanceof Map.Entry
            assert [a:2,b:4,c:6].find { it.key.toUpperCase()=='C' && 2*it.value==12 } instanceof Map.Entry
        '''
    }

    void testInferenceForDGM_findAllOnCollection() {
        assertScript '''
            assert ['a','bbb','ccc'].findAll { String it -> it.length() == 3 } == ['bbb','ccc']
            assert ['a','bbb','ccc'].findAll { it -> it.length() == 3 } == ['bbb','ccc']
            assert ['a','bbb','ccc'].findAll { it.length() == 3 } == ['bbb','ccc']
        '''
    }
    void testInferenceForDGM_findAllOnArray() {
        assertScript '''
            String[] arraylistOfStrings = ['a','bbb','ccc']
            assert arraylistOfStrings.findAll { String it -> it.length() == 3 } == ['bbb','ccc']
            assert arraylistOfStrings.findAll { it -> it.length() == 3 } == ['bbb','ccc']
            assert arraylistOfStrings.findAll { it.length() == 3 } == ['bbb','ccc']
        '''
    }
    void testInferenceForDGM_findAllOnMap() {
        assertScript '''
            assert [a:2,b:4,c:6].findAll { String k, int v -> k.toUpperCase()=='C' && 2*v==12 } == [c:6]
            assert [a:2,b:4,c:6].findAll { k, v -> k.toUpperCase()=='C' && 2*v==12 } == [c:6]
            assert [a:2,b:4,c:6].findAll { e -> e.key.toUpperCase()=='C' && 2*e.value==12 } == [c:6]
            assert [a:2,b:4,c:6].findAll { it.key.toUpperCase()=='C' && 2*it.value==12 } == [c:6]
        '''
    }

    void testInferenceForDGM_findResultOnCollection() {
        assertScript '''
            assert ['barbar','barbaz','foo'].findResult { it.length() == 3?it.toUpperCase():null } == 'FOO'
            assert ['barbar','barbaz','foo'].findResult { String it -> it.length() == 3?it.toUpperCase():null } == 'FOO'
            assert ['barbar','barbaz','foo'].findResult { it -> it.length() == 3?it.toUpperCase():null } == 'FOO'
            assert ['barbar','barbaz','foo'].findResult(-1) { it.length() == 4?it.toUpperCase():null } == -1
            assert ['barbar','barbaz','foo'].findResult(-1) { String it -> it.length() == 4?it.toUpperCase():null } == -1
            assert ['barbar','barbaz','foo'].findResult(-1) { it -> it.length() == 4?it.toUpperCase():null } == -1
        '''
    }
    void testInferenceForDGM_findResultOnIterable() {
        assertScript '''
            assert (0..10).findResult { it== 3?2*it:null } == 6
            assert (0..10).findResult { int it -> it==3?2*it:null } == 6
            assert (0..10).findResult { it -> it==3?2*it:null } == 6
        '''
    }
    void testInferenceForDGM_findResultOnMap() {
        assertScript '''
            assert [a:1, b:2, c:3].findResult { String k, int v -> "${k.toUpperCase()}$v"=='C3'?2*v:null } == 6
            assert [a:1, b:2, c:3].findResult { k, v -> "${k.toUpperCase()}$v"=='C3'?2*v:null } == 6
            assert [a:1, b:2, c:3].findResult { e -> "${e.key.toUpperCase()}$e.value"=='C3'?2*e.value:null } == 6
            assert [a:1, b:2, c:3].findResult { "${it.key.toUpperCase()}$it.value"=='C3'?2*it.value:null } == 6

            assert [a:1, b:2, c:3].findResult('a') { String k, int v -> "${k.toUpperCase()}$v"=='C4'?2*v:null } == 'a'
            assert [a:1, b:2, c:3].findResult('a') { k, v -> "${k.toUpperCase()}$v"=='C4'?2*v:null } == 'a'
            assert [a:1, b:2, c:3].findResult('a') { e -> "${e.key.toUpperCase()}$e.value"=='C4'?2*e.value:null } == 'a'
            assert [a:1, b:2, c:3].findResult('a') { "${it.key.toUpperCase()}$it.value"=='C4'?2*it.value:null } == 'a'
        '''
    }

    void testInferenceForDGM_findResultsOnIterable() {
        assertScript '''
            assert (0..10).findResults { it<3?2*it:null } == [0,2,4]
            assert (0..10).findResults { int it -> it<3?2*it:null } == [0,2,4]
            assert (0..10).findResults { it -> it<3?2*it:null } == [0,2,4]
        '''
    }
    void testInferenceForDGM_findResultsOnMap() {
        assertScript '''
            assert [a:1, b:2, c:3].findResults { String k, int v -> "${k.toUpperCase()}$v"=='C3'?2*v:null } == [6]
            assert [a:1, b:2, c:3].findResults { k, v -> "${k.toUpperCase()}$v"=='C3'?2*v:null } == [6]
            assert [a:1, b:2, c:3].findResults { e -> "${e.key.toUpperCase()}$e.value"=='C3'?2*e.value:null } == [6]
            assert [a:1, b:2, c:3].findResults { "${it.key.toUpperCase()}$it.value"=='C3'?2*it.value:null } == [6]
        '''
    }

    void testInferenceForDGM_groupByIterable() {
        assertScript '''
            assert ['a','bb','cc','d','eee'].groupBy { it.length() } == [1:['a','d'],2:['bb','cc'],3:['eee']]
        '''
    }
    void testInferenceForDGM_groupByArray() {
        assertScript '''
            String[] array = ['a','bb','cc','d','eee']
            assert array.groupBy { it.length() } == [1:['a','d'],2:['bb','cc'],3:['eee']]
        '''
    }
    void testInferenceForDGM_groupByMap() {
        assertScript '''
            assert [a:'1',b:'2',c:'C'].groupBy { e -> e.key.toUpperCase()==e.value?1:0 } == [0:[a:'1',b:'2'], 1:[c:'C']]
            assert [a:'1',b:'2',c:'C'].groupBy { k, v -> k.toUpperCase()==v?1:0 } == [0:[a:'1',b:'2'], 1:[c:'C']]
            assert [a:'1',b:'2',c:'C'].groupBy { it.key.toUpperCase()==it.value?1:0 } == [0:[a:'1',b:'2'], 1:[c:'C']]
        '''
    }
    void testInferenceForDGM_groupEntriesBy() {
        assertScript '''
            def result = [a:1,b:2,c:3,d:4,e:5,f:6].groupEntriesBy { k,v -> v % 2 }
            result = [a:1,b:2,c:3,d:4,e:5,f:6].groupEntriesBy { it.value % 2 }
            result = [a:1,b:2,c:3,d:4,e:5,f:6].groupEntriesBy { e -> e.value % 2 }
            assert result[0]*.key == ["b", "d", "f"]
            assert result[1]*.value == [1, 3, 5]
     '''
    }

    void testInferenceForDGM_injectOnCollectionWithInitialValue() {
        assertScript '''
            assert ['a','bb','ccc'].inject(0) { acc, str -> acc += str.length(); acc } == 6
        '''
    }

    void testInferenceForDGM_injectOnArrayWithInitialValue() {
        assertScript '''
            String[] array = ['a','bb','ccc']
            assert array.inject(0) { acc, str -> acc += str.length(); acc } == 6
        '''
    }

    void testInferenceForDGM_injectOnIteratorWithInitialValue() {
        assertScript '''
            assert ['a','bb','ccc'].iterator().inject(0) { acc, str -> acc += str.length(); acc } == 6
        '''
    }

    void testInferenceForDGM_injectOnCollection() {
        assertScript '''
            assert ['a','bb','ccc'].inject { acc, str -> acc += str.toUpperCase(); acc } == 'aBBCCC'
        '''
    }

    void testInferenceForDGM_injectOnArray() {
        assertScript '''
            String[] array = ['a','bb','ccc']
            assert array.inject { acc, str -> acc += str.toUpperCase(); acc } == 'aBBCCC'
        '''
    }

    void testInferenceForDGM_injectOnCollectionWithInitialValueDirect() {
        assertScript '''import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM
            assert DGM.inject(['a','bb','ccc'],0) { acc, str -> acc += str.length(); acc } == 6
        '''
    }

    void testInferenceForDGM_injectOnCollectionDirect() {
        assertScript '''import org.codehaus.groovy.runtime.DefaultGroovyMethods as DGM
            assert DGM.inject(['a','bb','ccc']) { acc, str -> acc += str.toUpperCase(); acc } == 'aBBCCC'
        '''
    }

    void testDGM_injectOnMap() {
        assertScript '''
            assert [a:1,b:2].inject(0) { acc, entry -> acc += entry.value; acc} == 3
            assert [a:1,b:2].inject(0) { acc, k, v -> acc += v; acc} == 3
        '''
    }

    void testDGM_max() {
        assertScript '''
            assert ['a','abc', 'defg','hi'].max { it.length() } == 'defg'
            assert ['a','abc', 'defg','hi'].iterator().max { it.length() } == 'defg'
            assert (['a','abc', 'defg','hi'] as String[]).max { it.length() } == 'defg'
        '''
    }

    void testDGM_maxOnMap() {
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].max { a,b -> a.value.length() <=> b.value.length() }
            assert result.key == 'c'
            assert result.value == 'defg'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].max { Map.Entry<String,String> a, Map.Entry<String,String> b -> a.value.length() <=> b.value.length() }
            assert result.key == 'c'
            assert result.value == 'defg'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].max { e -> e.value.length() }
            assert result.key == 'c'
            assert result.value == 'defg'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].max { it.value.length() }
            assert result.key == 'c'
            assert result.value == 'defg'
        '''
    }

    void testDGM_min() {
        assertScript '''
            assert ['a','abc', 'defg','hi'].min { it.length() } == 'a'
            assert ['a','abc', 'defg','hi'].iterator().min { it.length() } == 'a'
            assert (['a','abc', 'defg','hi'] as String[]).min { it.length() } == 'a'
        '''
    }

    void testDGM_maxOnMin() {
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].min { a,b -> a.value.length() <=> b.value.length() }
            assert result.key == 'a'
            assert result.value == 'a'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].min { Map.Entry<String,String> a, Map.Entry<String,String> b -> a.value.length() <=> b.value.length() }
            assert result.key == 'a'
            assert result.value == 'a'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].min { e -> e.value.length() }
            assert result.key == 'a'
            assert result.value == 'a'
        '''
        assertScript '''
            def result = [a:'a',b:'abc', c:'defg',d:'hi'].min { it.value.length() }
            assert result.key == 'a'
            assert result.value == 'a'
        '''
    }

    void testDGM_removeAllOnCollection() {
        assertScript '''
            def list = ['abc','a','groovy','java']
            list.removeAll { it.length() <4 }
            assert list == ['groovy','java']
        '''
    }
    void testDGM_retainAllOnCollection() {
        assertScript '''
            def list = ['abc','a','groovy','java']
            list.retainAll { it.length()>3 }
            assert list == ['groovy','java']
        '''
    }

    void testReverseEachOnList() {
        assertScript '''
            ['a','b'].reverseEach { println it.toUpperCase() }
        '''
    }
    void testReverseEachOnArray() {
        assertScript '''
            (['a','b'] as String[]).reverseEach { println it.toUpperCase() }
        '''
    }
    void testReverseEachOnMap() {
        assertScript '''
            [a:1,b:2].reverseEach { k,v -> println ((k.toUpperCase())*v) }
            [a:1,b:2].reverseEach { e -> println ((e.key.toUpperCase())*e.value) }
            [a:1,b:2].reverseEach { println ((it.key.toUpperCase())*it.value) }
        '''
    }

    void testDGM_sortOnCollection() {
        assertScript '''
            assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { a,b -> a.length() <=> b.length() }
            assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { str -> str.length() }
            assert ["hi","hey","hello"] == ["hello","hi","hey"].sort { it.length() }
        '''
    }
    void testDGM_sortOnArray() {
        assertScript '''
            String[] array = ["hello","hi","hey"]
            assert ["hi","hey","hello"] == array.sort { a,b -> a.length() <=> b.length() }
            assert ["hi","hey","hello"] == array.sort { str -> str.length() }
            assert ["hi","hey","hello"] == array.sort { it.length() }
        '''
    }
    void testDGM_sortOnIterator() {
        assertScript '''
            assert ["hi","hey","hello"] == ["hello","hi","hey"].iterator().sort { a,b -> a.length() <=> b.length() }.collect()
            assert ["hi","hey","hello"] == ["hello","hi","hey"].iterator().sort { str -> str.length() }.collect()
            assert ["hi","hey","hello"] == ["hello","hi","hey"].iterator().sort { it.length() }.collect()
        '''
    }
    void testDGM_sortOnIterable() {
        assertScript '''
            def foo(Iterable<String> iterable) {
                assert ["hi","hey","hello"] == iterable.sort { a,b -> a.length() <=> b.length() }
                assert ["hi","hey","hello"] == iterable.sort { str -> str.length() }
                assert ["hi","hey","hello"] == iterable.sort { it.length() }
            }
            foo(["hello","hi","hey"])
        '''
    }

    void testDGM_sortOnMap() {
        assertScript '''
        def map = [a:5, b:3, c:6, d:4].sort { a, b -> a.value <=> b.value }
        assert map == [b:3, d:4, a:5, c:6]
        '''

        assertScript '''
        def map = [a:5, b:3, c:6, d:4].sort { a -> a.value }
        assert map == [b:3, d:4, a:5, c:6]
        '''

        assertScript '''
        def map = [a:5, b:3, c:6, d:4].sort { it.value }
        assert map == [b:3, d:4, a:5, c:6]
        '''
    }

    void testDGM_slitOnCollection() {
        assertScript '''
            assert [[2,4],[1,3]] == [1,2,3,4].split { it % 2 == 0 }
        '''
    }

    void testDGM_takeWhileOnIterable() {
        assertScript '''
        class AbcIterable implements Iterable<String>  {
           Iterator<String>  iterator() { "abc".iterator() }
       }
       def abc = new AbcIterable()
       assert abc.takeWhile{ it < 'b' } == ['a']
       assert abc.takeWhile{ it <= 'b' } == ['a', 'b']
'''
    }
    void testDGM_takeWhileOnIterator() {
        assertScript '''
        class AbcIterable implements Iterable<String>  {
           Iterator<String>  iterator() { "abc".iterator() }
       }
       def abc = new AbcIterable()
       assert abc.iterator().takeWhile{ it < 'b' }.collect() == ['a']
       assert abc.iterator().takeWhile{ it <= 'b' }.collect() == ['a', 'b']
'''
    }
    void testDGM_takeWhileOnList() {
        assertScript '''
       def abc = ['a','b','c']
       assert abc.iterator().takeWhile{ it < 'b' }.collect() == ['a']
       assert abc.iterator().takeWhile{ it <= 'b' }.collect() == ['a', 'b']'''
    }
    void testDGM_takeWhileOnArray() {
        assertScript '''
            String[] abc = ['a','b','c']
            assert abc.iterator().takeWhile{ it < 'b' }.collect() == ['a']
            assert abc.iterator().takeWhile{ it <= 'b' }.collect() == ['a', 'b']'''
    }
    void testDGM_takeWhileOnMap() {
        assertScript '''
            def shopping = [milk:1, bread:2, chocolate:3]
            assert shopping.takeWhile{ it.key.size() < 6 } == [milk:1, bread:2]
            assert shopping.takeWhile{ it.value % 2 } == [milk:1]
            assert shopping.takeWhile{ k, v -> k.size() + v <= 7 } == [milk:1, bread:2]'''
    }

    void testDGM_times() {
        assertScript '''
            String foo(int x) { "x"*x }
            10.times {
                println foo(it)
            }
        '''
    }

    void testDGM_unique() {
        assertScript '''
       def orig = [1, 3, 4, 5]
       def uniq = orig.unique(false) { it % 2 }
       assert orig == [1, 3, 4, 5]
       assert uniq == [1, 4]'''

       assertScript '''def orig = [2, 3, 3, 4]
       def uniq = orig.unique(false) { a, b -> a <=> b }
       assert orig == [2, 3, 3, 4]
       assert uniq == [2, 3, 4]'''
    }
    void testDGM_uniqueOnCollection() {
        assertScript '''
       def orig = [1, 3, 4, 5]
       def uniq = orig.unique { it % 2 }
       assert uniq == [1, 4]'''

       assertScript '''def orig = [2, 3, 3, 4]
       def uniq = orig.unique { a, b -> a <=> b }
       assert uniq == [2, 3, 4]'''
    }
    void testDGM_uniqueOnIterator() {
        assertScript '''
       def orig = [1, 3, 4, 5].iterator()
       def uniq = orig.unique { it % 2 }.collect()
       assert uniq == [1, 4]'''

       assertScript '''def orig = [2, 3, 3, 4].iterator()
       def uniq = orig.unique { a, b -> a <=> b }.collect()
       assert uniq == [2, 3, 4]'''
    }

    void testDGM_anyOnMap() {
        assertScript '''
            assert [a:10, b:1].any { k,v -> k.length() == v }
            assert [a:10, b:1].any { e -> e.key.length() == e.value }
            assert [a:10, b:1].any {it.key.length() == it.value }
        '''
    }
    void testDGM_anyOnIterable() {
        assertScript '''
            assert ['abc','de','f'].any { it.length() == 2 }
        '''
    }
    void testDGM_anyOnIterator() {
        assertScript '''
            assert ['abc','de','f'].iterator().any { it.length() == 2 }
        '''
    }
    void testDGM_anyOnArray() {
        assertScript '''
            String[] strings = ['abc','de','f']
            assert strings.any { it.length() == 2 }
        '''
    }

    void testDGM_mapWithDefault() {
        assertScript '''
            def map = [a:'A'].withDefault { it.toUpperCase() }
            assert map.b=='B'
        '''
    }

    void testFromStringInSameSourceUnit() {
        assertScript '''import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

public <T> void doSomething(T val, @ClosureParams(value=FromString, options="T") Closure cl) {
    cl(val)
}

doSomething('foo') {
    println it.toUpperCase()
}

doSomething(new Date()) {
    println it.time
}
'''

    }

    void testInferenceWithSAMTypeCoercion() {
        assertScript '''import java.util.concurrent.Callable

interface Action<T> {
    void execute(T thing)
}

class Wrapper<T> {

    private final T thing

    Wrapper(T thing) {
        this.thing = thing
    }

    void contravariantTake(Action<? super T> action) {
        action.execute(thing)
    }

    void invariantTake(Action<T> action) {
        action.execute(thing)
    }

}

static <T> Wrapper<T> wrap(Callable<T> callable) {
    new Wrapper(callable.call())
}

static Integer dub(Integer integer) {
    integer * 2
}

wrap {
    1
} contravariantTake {
    dub(it) // fails static compile, 'it' is not known to be Integer
}

wrap {
    1
} invariantTake {
    dub(it) // passes static compile, 'it' is known to be Integer
}

'''
    }

    void testGroovy6602() {
        shouldFailWithMessages '''import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FromString

void foo(@ClosureParams(value = FromString, options = "java.lang.Number")
         Closure cl) {
    cl.call(4.5)
}

foo { Integer i -> println i }
''', 'Expected parameter of type java.lang.Number but got java.lang.Integer'
    }

    void testGroovy6729() {
        assertScript '''import groovy.transform.stc.ClosureParams
import groovy.transform.stc.FirstParam

    static <T> List<T> callee01(List<T>self, @ClosureParams(FirstParam.FirstGenericType) Closure c) {
        self.each {
            c.call(it)
        }
        return self
    }
        callee01(["a","b","c"]) { a ->
            println(a.toUpperCase()) // [Static type checking] - Cannot find matching method java.lang.Object#toUpperCase(). Please check if the declared type is correct and if the method exists.
        }
    '''
    }

    void testGroovy6735() {
        assertScript '''
def extractInfo(String s) {
  def squareNums = s.findAll(/\\d+/) { String num -> num.toInteger() }.collect{ Integer num -> num ** 2 }
  def wordSizePlusNum = s.findAll(/\\s*(\\w+)\\s*(\\d+)/) { _, String word, String num -> word.size() + num.toInteger() }
  def numPlusWordSize = s.findAll(/\\s*(\\w+)\\s*(\\d+)/) { _, word, num -> num.toInteger() + word.size() }
  [squareNums, wordSizePlusNum, numPlusWordSize]
}
assert extractInfo(" ab 12 cdef 34 jhg ") == [[144, 1156], [14, 38], [14, 38]]
'''
        assertScript '''
def method() {
  assert "foobarbaz".findAll('b(a)([rz])') { full, a, b -> assert "BA"=="B" + a.toUpperCase() }.size() == 2
  assert "foobarbaz".findAll('ba') { String found -> assert "BA" == found.toUpperCase() }.size() == 2
}

method()
'''
    }

    void testGroovy9058() {
        assertScript '''
            List<Object[]> bar() { [['fee', 'fi'] as Object[], ['fo', 'fum'] as Object[]] }

            def foo() {
                def result = []
                List<Object[]> bar = bar()
                bar.each { row -> result << row[0].toString().toUpperCase() }
                result
            }

            assert foo() == ['FEE', 'FO']
        '''
    }

    void testGroovy9518() {
        assertScript '''
            class C {
                C(String s, Comparable<List<Integer>> c) {
                }
            }

            new C('blah', { list -> list.get(0) })
        '''
    }

    void testGroovy9518a() {
        assertScript '''
            class C {
                C(String s, Comparable<List<Integer>> c) {
                }
            }

            new C('blah', { it.get(0) })
        '''
    }

    void testGroovy9518b() {
        assertScript '''
            import groovy.transform.stc.*

            class C {
                C(String s, @ClosureParams(value=SimpleType, options='java.util.List') Closure<Integer> c) {
                }
            }

            new C('blah', { list -> list.get(0) })
        '''
    }

    void testGroovy9570() {
        assertScript '''
            interface Item {}

            class C<I extends Item> {
                Queue<I> queue

                def c = { ->
                    queue.each { I item ->
                        println item
                    }
                }

                def m() {
                    queue.each { I item ->
                        println item
                    }
                }
            }

            new C()
        '''
    }

    void testGroovy9597a() {
        assertScript '''
            import groovy.transform.stc.*

            class A {
                def <T> void proc(Collection<T> values, @ClosureParams(FirstParam.FirstGenericType) Closure<String> block) {
                }
            }

            class B {
                List<Integer> list
                void test(A a) {
                    a.proc(this.list) { it.toBigDecimal().toString() } // works
                    a.with {
                      proc(this.list) { it.toBigDecimal().toString() } // error
                    }
                }
            }

            new B().test(new A())
        '''
    }

    void testGroovy9597b() {
        assertScript '''
            import groovy.transform.stc.*

            class A {
                static A of(@DelegatesTo(A) Closure x) {
                    new A().tap {
                        x.delegate = it
                        x.call()
                    }
                }
                def <T> void proc(Collection<T> values, @ClosureParams(FirstParam.FirstGenericType) Closure<String> block) {
                }
            }

            class B {
              List<Integer> list
              A a = A.of {
                  proc(
                      this.list,
                      { it.toBigDecimal().toString() } // Cannot find matching method java.lang.Object#toBigDecimal()
                  )
              }
            }

            new B()
        '''
    }
}
