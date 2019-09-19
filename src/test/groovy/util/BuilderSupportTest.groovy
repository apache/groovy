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

import groovy.test.GroovyTestCase

/**
 * Testing BuilderSupport and reveal how calling methods on it result in implementation callbacks.
 * Using the SpoofBuilder (see below) to call it in various ways and analyze the "spoofed" logs.
 * This is especially useful when designing subclasses of BuilderSupport.
 */
class BuilderSupportTest extends GroovyTestCase{
    void testSimpleNode() {
        def b = new SpoofBuilder()
        assert b.log == []
        def node = b.foo()
        assert b.log == [  'create_with_name','foo',
                           'node_completed',null, node, 
                           'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeWithValue() {
        def b = new SpoofBuilder()
        def node = b.foo('value')
        assert b.log == [  'create_with_name_and_value','foo',
                           'value', 'node_completed',null,node,
                           'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeWithOneAttribute() {
        def b = new SpoofBuilder()
        def node = b.foo(name:'value')
        assert b.log == [
                           'create_with_name_and_map','foo', 
                           'name','value', 
                           'node_completed',null,'x',
                           'post_node_completion',null, 'x'
                        ]
    }

    void testSimpleNodeWithClosure() {
        def b = new SpoofBuilder()
        b.foo(){
            b.bar()
        }
        assert b.log == [
            'create_with_name','foo',
                'create_with_name','bar',
            'set_parent', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
            ]
    }

    void testSimpleNodeWithOneAttributeAndValue() {
        def b = new SpoofBuilder()
        def node = b.foo(bar:'baz', 'value')
        assert b.log == [
                          'create_with_name_map_and_value', 'foo', 'bar', 'baz','value', 
                          'node_completed',null,node,
                          'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeWithValueAndOneAttribute() {
        def b = new SpoofBuilder()
        def node = b.foo('value', bar:'baz')
        assert b.log == [
                          'create_with_name_map_and_value', 'foo', 'bar', 'baz','value', 
                          'node_completed',null,node,
                          'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeWithOneAttributeAndValueAndClosure() {
        def b = new SpoofBuilder()
        def node = b.foo(bar:'baz', 'value') { 1 }
        assert b.log == [
                          'create_with_name_map_and_value', 'foo', 'bar', 'baz','value', 
                          'node_completed',null,node,
                          'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeWithValueAndOneAttributeAndClosure() {
        def b = new SpoofBuilder()
        def node = b.foo('value', bar:'baz') { 1 }
        assert b.log == [
                          'create_with_name_map_and_value', 'foo', 'bar', 'baz','value', 
                          'node_completed',null,node,
                          'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeTwoValues() {
        def b = new SpoofBuilder()
        shouldFail(MissingMethodException, {def node = b.foo('arg1', 'arg2')})
    }

    void testSimpleNodeTwoValuesClosure() {
        def b = new SpoofBuilder()
        shouldFail(MissingMethodException, {def node = b.foo('arg1', 'arg2') { 1 } })
    }

    void testSimpleNodeThreeValues() {
        def b = new SpoofBuilder()
        shouldFail(MissingMethodException, {def node = b.foo('arg1', 'arg2', 'arg3') })
    }

    void testSimpleNodeFourValues() {
        def b = new SpoofBuilder()
        shouldFail(MissingMethodException, {def node = b.foo('arg1', 'arg2', 'arg3', 'arg4') })
    }

    void testNestedMethodCallsResolution() {
        def b = new SpoofBuilder()
        b.outest {
            b.outer {
                nestedBuilderCall(b)
            }
        }
        assert b.log.contains('inner') 
    }

    void nestedBuilderCall(builder) {
        builder.inner()
    }

    // GROOVY-3341
    void testSimpleNodeWithClosureThatThrowsAMissingMethodException() {
      def builder = new SpoofBuilder()
      String errorMessage = shouldFail(MissingMethodException, {
          builder.a {
              b {
                  error('xy'.foo())
              }
          }
      })
      assert errorMessage.contains('No signature of method: java.lang.String.foo()')
  }
}

/**
    The SpoofBuilder is a sample instance of the abstract BuilderSupport class
    that does nothing but logging how it was called, returning 'x' for each node.
**/
class SpoofBuilder extends BuilderSupport{
    def log = []
    
    protected void setParent(Object parent, Object child){
        log << "set_parent"
        log << parent
        log << child
    }
    protected Object createNode(Object name){
        log << 'create_with_name'
        log <<  name
        return 'x'
    }
    protected Object createNode(Object name, Object value){
        log << 'create_with_name_and_value'
        log << name
        log << value
        return 'x'
    }
    protected Object createNode(Object name, Map attributes){
        log << 'create_with_name_and_map'
        log << name
        attributes.each{entry -> log << entry.key; log << entry.value}
        return 'x'
    }
    protected Object createNode(Object name, Map attributes, Object value){
        log << 'create_with_name_map_and_value'
        log << name
        attributes.each{entry -> log << entry.key; log << entry.value}
        log << value
        return 'x'
    }
    protected void nodeCompleted(Object parent, Object node) {
        log << 'node_completed'
        log << parent
        log << node
    }
    
    protected Object postNodeCompletion(Object parent, Object node) {
        log << 'post_node_completion'
        log << parent
        log << node
        node
    }
}
