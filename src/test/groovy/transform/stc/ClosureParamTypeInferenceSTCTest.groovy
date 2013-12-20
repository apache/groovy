/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc

import groovy.transform.NotYetImplemented

/**
 * Unit tests for static type checking : closure parameter type inference.
 *
 * @author Cedric Champeau
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
            Integer[] arr = (0..5)
            assert arr.collectMany { [it, 2*it ]} == [0,0,1,2,2,4,3,6,4,8,5,10]
'''
    }

    void testInferenceOnNonExtensionMethod() {
        assertScript '''import groovy.transform.stc.ClosureParams
            import groovy.transform.stc.FirstArg
            public <T> T foo(T arg, @ClosureParams(FirstArg) Closure c) { c.call(arg) }
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
}
