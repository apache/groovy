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
package groovy.util

/**
 *   Test for FactoryBuilderSupport based in BuilderSupportTest
 *   as it should comply with the same contract
 */
class FactoryBuilderSupportTest extends GroovyTestCase {
    void testSimpleNode() {
        def b = new SpoofFactoryBuilder()
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        def node = b.foo()
        def log = b.@log
        def expected = [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null,
                'handle_node_attributes', node,
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
        assert log == expected
    }

    void testSimpleNodeWithValue() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('value')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', 'value',
                'handle_node_attributes', node,
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testSimpleNodeWithOneAttribute() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo(name:'value')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null, 'name', 'value',
                'handle_node_attributes', node, 'name', 'value',
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testSimpleNodeWithClosure() {
        def b = new SpoofFactoryBuilder()
        b.foo(){
            b.bar()
        }
        def log = b.@log
        def expected = [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null,
                'handle_node_attributes', 'x',
                'new_instance', 'bar', null,
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'node_completed', null, 'x',
                'post_node_completion', null, 'x'
        ]
        assert log == expected
    }

    void testSimpleNodeWithOneAttributeAndValue() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo(bar:'baz', 'value')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', 'value', 'bar', 'baz',
                'handle_node_attributes', node, 'bar', 'baz',
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testSimpleNodeWithValueAndOneAttribute() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('value', bar:'baz')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', 'value', 'bar', 'baz',
                'handle_node_attributes', node, 'bar', 'baz',
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testSimpleNodeWithOneAttributeAndValueAndClosure() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo(bar:'baz', 'value') { 1 }
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', 'value', 'bar', 'baz',
                'handle_node_attributes', node, 'bar', 'baz',
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testSimpleNodeWithValueAndOneAttributeAndClosure() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('value', bar:'baz') { 1 }
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', 'value', 'bar', 'baz',
                'handle_node_attributes', node, 'bar', 'baz',
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testSimpleNodeTwoValues() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('arg1', 'arg2')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', ['arg1', 'arg2'],
                'handle_node_attributes',node,
                'node_completed',null,node,
                'post_node_completion',null, node
        ]
    }

    void testSimpleNodeTwoValuesClosure() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('arg1', 'arg2') { 1 }
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', ['arg1', 'arg2'],
                'handle_node_attributes',node,
                'node_completed',null,node,
                'post_node_completion',null, node
        ]
    }

    void testSimpleNodeThreeValues() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('arg1', 'arg2', 'arg3')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', ['arg1', 'arg2', 'arg3'],
                'handle_node_attributes',node,
                'node_completed',null,node,
                'post_node_completion',null, node
        ]
    }

    void testSimpleNodeFourValues() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('arg1', 'arg2', 'arg3', 'arg4')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', ['arg1', 'arg2', 'arg3', 'arg4'],
                'handle_node_attributes',node,
                'node_completed',null,node,
                'post_node_completion',null, node
        ]

    }

    void testNestedMethodCallsResolution() {
        def b = new SpoofFactoryBuilder()
        b.outest {
            b.outer {
                nestedBuilderCall(b)
            }
        }
        assert b.@log.contains('inner')
    }

    void nestedBuilderCall(builder) {
        builder.inner()
    }

    void testExplicitProperty() {
        def b = new SpoofFactoryBuilder();
        // property neither set in the binding or explicitly, should fail
        shouldFail { b.exp }

        // setting first should set the value in the binding and make it usable 
        b.notExp = 1
        assert b.notExp == 1

        int val = 0
        b.registerExplicitProperty ('exp', {val++}, {val = it ^ 1})

        // test getter
        assert b.exp == 0
        assert b.exp == 1
        assert b.exp == val - 1

        // test setter
        b.exp = 3
        assert val == 2
        b.exp = 4
        assert val == 5

        // symbols in the property closure shold also resolve to the builder...
        b.registerExplicitProperty ('exp2', {exp}, {exp = it })

        assert b.exp2 == val - 1
        b.exp2 = 4
        assert val == 5
    }

    void testExplicitMethod() {
        def b = new SpoofFactoryBuilder();
        // property neither set in the binding or explicitly, should fail
        shouldFail { b.exp() }


        int val = 0
        b.registerExplicitMethod ('exp', {it -> val+= it})

        // test calls
        assert b.exp(0) == 0
        assert b.exp(1) == 1
        assert (val - 1) == b.exp(-1)
        b.exp(3 - val)
        assert val == 3
        b.exp(5)
        assert val == 8

        // symbols in the method closure shold also resolve to the builder...
        b.registerExplicitMethod ('exp2', {exp(it)})

        assert b.exp2(-2) == 6
        b.exp2(4 - val)
        assert val == 4
    }

    // ==================================

    void testNestedBuilderSimpleNode() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        assert n.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        def node = b.foo()
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        assert n.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null,
                'handle_node_attributes', node,
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testNestedBuilderSimpleNodeWithValue() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo('value')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        assert n.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', 'value',
                'handle_node_attributes', node,
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testNestedBuilderSimpleNodeWithOneAttribute() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo(name:'value')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        assert n.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null, 'name', 'value',
                'handle_node_attributes', node, 'name', 'value',
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testNestedBuilderSimpleNodeWithClosure() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        b.foo(){
            b.bar()
        }
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        assert n.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null,
                'handle_node_attributes', 'x',
                'new_instance', 'bar', null,
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'node_completed', null, 'x',
                'post_node_completion', null, 'x'
        ]
    }

    void testNestedBuilderSimpleNodeWithOneAttributeAndValue() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo(bar:'baz', 'value')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        assert n.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', 'value', 'bar', 'baz',
                'handle_node_attributes', node, 'bar', 'baz',
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testNestedBuilderSimpleNodeWithValueAndOneAttribute() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo('value', bar:'baz')
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        assert n.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', 'value', 'bar', 'baz',
                'handle_node_attributes', node, 'bar', 'baz',
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testNestedBuilderSimpleNodeWithOneAttributeAndValueAndClosure() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo(bar:'baz', 'value') { 1 }
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        assert n.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', 'value', 'bar', 'baz',
                'handle_node_attributes', node, 'bar', 'baz',
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testNestedBuilderSimpleNodeWithValueAndOneAttributeAndClosure() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo('value', bar:'baz') { 1 }
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
        assert n.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', 'value', 'bar', 'baz',
                'handle_node_attributes', node, 'bar', 'baz',
                'node_completed', null, node,
                'post_node_completion', null, node
        ]
    }

    void testWithBuilder() {
        def b = new SpoofFactoryBuilder()
        def c = new SpoofFactoryBuilder()
        def factory = new XFactory( builder:c )
        c.registerFactory( "fooz", factory )
        c.registerFactory( "baz", factory )
        b.foo(){
            bar()
            withBuilder(c){
               fooz(){
                  baz()
               }   
            }
        }
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null,
                'handle_node_attributes', 'x',
                'new_instance', 'bar', null,
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'node_completed', null, 'x',
                'post_node_completion', null, 'x'
        ]
        assert c.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'register', 'fooz', '',
                'register', 'baz', '',
                'new_instance', 'fooz', null,
                'handle_node_attributes', 'x',
                'new_instance', 'baz', null,
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'node_completed', null, 'x',
                'post_node_completion', null, 'x'
        ]
    }

    void testWithBuilderAndName() {
        def b = new SpoofFactoryBuilder()
        def c = new SpoofFactoryBuilder()
        def factory = new XFactory( builder:c )
        c.registerFactory( "fooz", factory )
        c.registerFactory( "baz", factory )
        b.foo(){
            bar()
            withBuilder(c,'foo'){
               fooz(){
                  baz()
               }   
            }
        }

        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null,
                'handle_node_attributes', 'x',
                'new_instance', 'bar', null,
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'new_instance', 'foo', 'x',
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'node_completed', null, 'x',
                'post_node_completion', null, 'x'
        ]
        assert c.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'register', 'fooz', '',
                'register', 'baz', '',
                'new_instance', 'fooz', null,
                'handle_node_attributes', 'x',
                'new_instance', 'baz', null,
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'node_completed', null, 'x',
                'post_node_completion', null, 'x'
        ]
    }

    void testWithBuilderAndNameAndAttributes() {
        def b = new SpoofFactoryBuilder()
        def c = new SpoofFactoryBuilder()
        def factory = new XFactory( builder:c )
        c.registerFactory( "fooz", factory )
        c.registerFactory( "baz", factory )
        b.foo(){
            bar()
            withBuilder(c,'foo',bar:'baz'){
               fooz(){
                  baz()
               }   
            }
        }

        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null,
                'handle_node_attributes', 'x',
                'new_instance', 'bar', null,
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'new_instance', 'foo', 'x', 'bar', 'baz',
                'handle_node_attributes', 'x', 'bar', 'baz',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'node_completed', null, 'x',
                'post_node_completion', null, 'x'
        ]
        assert c.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'register', 'fooz', '',
                'register', 'baz', '',
                'new_instance', 'fooz', null,
                'handle_node_attributes', 'x',
                'new_instance', 'baz', null,
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'node_completed', null, 'x',
                'post_node_completion', null, 'x'
        ]
    }

    void testWithBuilderAndThrowAnException() {
        def b = new SpoofFactoryBuilder()
        def c = new SpoofFactoryBuilder()

        shouldFail( RuntimeException ) {
            b.foo(){
                bar()
                withBuilder(c){
                   throw new RuntimeException("expected")
                }
            }
        }

        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null,
                'handle_node_attributes', 'x',
                'new_instance', 'bar', null,
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x'/*,
            'node_completed',null,'x',
            'post_node_completion',null, 'x'*/
                // node foo() was not completed successfuly
        ]
        assert c.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
        ]
    }

    void testHandlesChildren() {
        def b = new SpoofFactoryBuilder()
        b.getFactories().foo.handlesNodeChildren = true
        b.foo {
            b.bar()
        }
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null,
                'handle_node_attributes', 'x',
                'node_children', 'foo',
                'new_instance', 'bar', null,
                'handle_node_attributes', 'x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed', 'x', 'x',
                'post_node_completion', 'x', 'x',
                'node_completed', null, 'x',
                'post_node_completion', null, 'x'
        ]
    }

    void testInteceptsChildren() {
        def b = new SpoofFactoryBuilder()
        b.getFactories().foo.handlesNodeChildren = true
        b.getFactories().foo.processNodeChildren = false
        b.foo {
            b.bar()
        }
        assert b.@log == [
                'register', 'outest', 'Layers',
                'register', 'outer', 'Layers',
                'register', 'inner', 'Layers',
                'register', 'foo', 'Meta',
                'register', 'bar', 'Meta',
                'new_instance', 'foo', null,
                'handle_node_attributes', 'x',
                'node_children', 'foo',
                'node_completed', null, 'x',
                'post_node_completion', null, 'x'
        ]
    }

    void testErrorMessage_checkValueIsType() {
        def msg = shouldFail(Exception) {
            FactoryBuilderSupport.checkValueIsType('message', 'prop', Integer)
        }
        assert msg.contains('prop')
        assert msg.contains('Integer')
        assert msg.contains('String')
    }

    void testErrorMessage_checkValueIsTypeNotString() {
        def msg = shouldFail(Exception) {
            FactoryBuilderSupport.checkValueIsTypeNotString(123G, 'prop', Integer)
        }
        assert msg.contains('prop')
        assert msg.contains('Integer')
        assert msg.contains('String')
        assert msg.contains('BigInteger')

    }

//    void testMultiThreaded() {
//        def b = new SpoofFactoryBuilder()
//        Thread t1 = Thread.start {
//            b.foo {
//                Thread.sleep(100)
//                bar()
//            }
//        }
//        Thread t2 = Thread.start {
//            Thread.sleep(50)
//            b.outer()
//        }
//
//        t1.join()
//        t2.join()
//        def log = b.@log
//        assert log == [
//                'register', 'foo', 'Meta',
//                'register', 'bar', 'Meta',
//                'register', 'outest', 'Layers',
//                'register', 'outer', 'Layers',
//                'register', 'inner', 'Layers',
//                'new_instance','foo', null,
//                'handle_node_attributes','x',
//                    'new_instance','bar', null,
//                    'handle_node_attributes','x',
//                'set_parent', 'x', 'x',
//                'set_child', 'x', 'x',
//                    'node_completed','x','x',
//                    'post_node_completion', 'x', 'x',
//                'node_completed', null,'x',
//                'post_node_completion', null, 'x',
//                'new_instance','outer', null,
//                'handle_node_attributes', 'x',
//                'node_completed', null, 'x',
//                'post_node_completion', null, 'x'
//            ]
//    }

    //TODO registration groups test
}

/**
    The SpoofFactoryBuilder is a sample instance of the abstract FactoryBuilderSupport class
    that does nothing but logging how it was called, returning 'x' for each node.
**/
class SpoofFactoryBuilder extends FactoryBuilderSupport{
    protected List log = []

    SpoofFactoryBuilder() {
        autoRegisterNodes()
    }

    public void registerMeta() {
       def factory = new XFactory( builder:this )
       registerFactory( "foo", factory )
       registerFactory( "bar", factory )
    }

    public void registerLayers() {
        def factory = new XFactory( builder:this )
       registerFactory( "outest", factory )
       registerFactory( "outer", factory )
       registerFactory( "inner", factory )
    }
    
    protected Object postNodeCompletion(Object parent, Object node) {
        log << 'post_node_completion'
        log << parent
        log << node
        node
    }
}

class XFactory extends AbstractFactory {
    SpoofFactoryBuilder builder

    boolean leaf = false

    boolean handlesNodeChildren = false

    boolean processNodeChildren = true

    public boolean onNodeChildren(FactoryBuilderSupport builder, Object node, Closure childContent) {
        builder.@log << 'node_children'
        builder.@log << builder.currentName
        return processNodeChildren && super.onNodeChildren(builder, node, childContent)
    }

    public void onFactoryRegistration(FactoryBuilderSupport builder, String registerdName, String groupName) {
        builder.@log << 'register'
        builder.@log << registerdName
        builder.@log << groupName
    }

    public Object newInstance(
        FactoryBuilderSupport builder, Object name, Object value, Map properties
    ) throws InstantiationException, IllegalAccessException {
        builder.@log << 'new_instance'
        builder.@log << name
        builder.@log << value
        properties.each{entry -> builder.@log << entry.key; builder.@log << entry.value}
        return 'x'
    }

    public boolean onHandleNodeAttributes( FactoryBuilderSupport builder, Object node, Map attributes ) {
        builder.@log << 'handle_node_attributes'
        builder.@log << node
        attributes.each{entry -> builder.@log << entry.key; builder.@log << entry.value}
        return false 
    }

    public void onNodeCompleted( FactoryBuilderSupport builder, Object parent, Object node ) {
        builder.@log << 'node_completed'
        builder.@log << parent
        builder.@log << node
    }

    public void setParent( FactoryBuilderSupport builder, Object parent, Object child ) {
        builder.@log << "set_parent"
        builder.@log << parent
        builder.@log << child
    }

    public void setChild( FactoryBuilderSupport builder, Object parent, Object child ) {
        builder.@log << "set_child"
        builder.@log << parent
        builder.@log << child
    }
}
