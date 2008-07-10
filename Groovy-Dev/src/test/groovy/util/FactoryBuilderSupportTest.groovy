package groovy.util

import groovy.lang.MissingMethodException

/**
 *   Test for FactoryBuilderSupport based in BuilderSupportTest
 *   as it should comply with the same contract
 *   @author Andres Almiray 
 */

class FactoryBuilderSupportTest extends GroovyTestCase{
    void testSimpleNode() {
        def b = new SpoofFactoryBuilder()
        assert b.@log == [
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
        ]
        def node = b.foo()
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                           'new_instance','foo', null,
                           'handle_node_attributes', node,
                           'node_completed',null, node, 
                           'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeWithValue() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('value')
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                           'new_instance','foo', 'value',
                           'handle_node_attributes', node,
                           'node_completed',null,node,
                           'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeWithOneAttribute() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo(name:'value')
        assert b.@log == [
                           'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                           'new_instance','foo',null,'name','value',
                           'handle_node_attributes',node,'name','value', 
                           'node_completed',null, node,
                           'post_node_completion',null, node 
                        ]
    }

    void testSimpleNodeWithClosure() {
        def b = new SpoofFactoryBuilder()
        b.foo(){
            b.bar()
        }
        assert b.@log == [
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'new_instance','foo',null,
            'handle_node_attributes','x',
                'new_instance','bar',null,
                'handle_node_attributes','x',
            'set_parent', 'x', 'x',
            'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
            ]
    }

    void testSimpleNodeWithOneAttributeAndValue() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo(bar:'baz', 'value')
        assert b.@log == [
                          'register', 'foo',
                          'register', 'bar',
                          'register', 'outest',
                          'register', 'outer',
                          'register', 'inner',
                          'new_instance', 'foo', 'value', 'bar','baz',
                          'handle_node_attributes',node,'bar','baz',
                          'node_completed',null,node,
                          'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeWithValueAndOneAttribute() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('value', bar:'baz')
        assert b.@log == [
                          'register', 'foo',
                          'register', 'bar',
                          'register', 'outest',
                          'register', 'outer',
                          'register', 'inner',
                          'new_instance', 'foo', 'value', 'bar','baz',
                          'handle_node_attributes',node,'bar','baz',
                          'node_completed',null,node,
                          'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeWithOneAttributeAndValueAndClosure() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo(bar:'baz', 'value') { 1 }
        assert b.@log == [
                          'register', 'foo',
                          'register', 'bar',
                          'register', 'outest',
                          'register', 'outer',
                          'register', 'inner',
                          'new_instance', 'foo', 'value', 'bar','baz',
                          'handle_node_attributes',node,'bar','baz',
                          'node_completed',null,node,
                          'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeWithValueAndOneAttributeAndClosure() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('value', bar:'baz') { 1 }
        assert b.@log == [
                          'register', 'foo',
                          'register', 'bar',
                          'register', 'outest',
                          'register', 'outer',
                          'register', 'inner',
                          'new_instance', 'foo', 'value', 'bar','baz',
                          'handle_node_attributes',node,'bar','baz',
                          'node_completed',null,node,
                          'post_node_completion',null, node
                        ]
    }

    void testSimpleNodeTwoValues() {
        def b = new SpoofFactoryBuilder()
        def node = b.foo('arg1', 'arg2')
        assert b.@log == [
                'register', 'foo',
                'register', 'bar',
                'register', 'outest',
                'register', 'outer',
                'register', 'inner',
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
                'register', 'foo',
                'register', 'bar',
                'register', 'outest',
                'register', 'outer',
                'register', 'inner',
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
                'register', 'foo',
                'register', 'bar',
                'register', 'outest',
                'register', 'outer',
                'register', 'inner',
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
                'register', 'foo',
                'register', 'bar',
                'register', 'outest',
                'register', 'outer',
                'register', 'inner',
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

    // ==================================

    void testNestedBuilderSimpleNode() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                         ]
        assert n.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                         ]
        def node = b.foo()
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                         ]
        assert n.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                           'new_instance','foo', null,
                           'handle_node_attributes', node,
                           'node_completed',null, node, 
                           'post_node_completion',null, node
                        ]
    }

    void testNestedBuilderSimpleNodeWithValue() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo('value')
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                         ]
        assert n.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                           'new_instance','foo', 'value',
                           'handle_node_attributes', node,
                           'node_completed',null,node,
                           'post_node_completion',null, node
                        ]
    }

    void testNestedBuilderSimpleNodeWithOneAttribute() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo(name:'value')
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                         ]
        assert n.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                           'new_instance','foo',null,'name','value',
                           'handle_node_attributes',node,'name','value', 
                           'node_completed',null, node,
                           'post_node_completion',null, node 
                        ]
    }

    void testNestedBuilderSimpleNodeWithClosure() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        b.foo(){
            b.bar()
        }
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                         ]
        assert n.@log == [
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'new_instance','foo',null,
            'handle_node_attributes','x',
                'new_instance','bar',null,
                'handle_node_attributes','x',
            'set_parent', 'x', 'x',
            'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
            ]
    }

    void testNestedBuilderSimpleNodeWithOneAttributeAndValue() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo(bar:'baz', 'value')
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                         ]
        assert n.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                           'new_instance', 'foo', 'value', 'bar','baz',
                           'handle_node_attributes',node,'bar','baz',
                           'node_completed',null,node,
                           'post_node_completion',null, node
                         ]
    }

    void testNestedBuilderSimpleNodeWithValueAndOneAttribute() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo('value', bar:'baz')
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                         ]
        assert n.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                          'new_instance', 'foo', 'value', 'bar','baz',
                          'handle_node_attributes',node,'bar','baz',
                          'node_completed',null,node,
                          'post_node_completion',null, node
                        ]
    }

    void testNestedBuilderSimpleNodeWithOneAttributeAndValueAndClosure() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo(bar:'baz', 'value') { 1 }
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                         ]
        assert n.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                           'new_instance', 'foo', 'value', 'bar','baz',
                           'handle_node_attributes',node,'bar','baz',
                           'node_completed',null,node,
                           'post_node_completion',null, node
                        ]
    }

    void testNestedBuilderSimpleNodeWithValueAndOneAttributeAndClosure() {
        def n = new SpoofFactoryBuilder()
        def b = new SpoofFactoryBuilder(proxyBuilder:n)
        def node = b.foo('value', bar:'baz') { 1 }
        assert b.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                         ]
        assert n.@log == [ 'register', 'foo',
                           'register', 'bar',
                           'register', 'outest',
                           'register', 'outer',
                           'register', 'inner',
                           'new_instance', 'foo', 'value', 'bar','baz',
                           'handle_node_attributes',node,'bar','baz',
                           'node_completed',null,node,
                           'post_node_completion',null, node
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
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'new_instance','foo',null,
            'handle_node_attributes','x',
                'new_instance','bar',null,
                'handle_node_attributes','x',
            'set_parent', 'x', 'x',
            'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
            ]
        assert c.@log == [
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'register', 'fooz',
            'register', 'baz',
            'new_instance','fooz',null,
            'handle_node_attributes','x',
                'new_instance','baz',null,
                'handle_node_attributes','x',
            'set_parent', 'x', 'x',
            'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
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
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'new_instance','foo',null,
            'handle_node_attributes','x',
                'new_instance','bar',null,
                'handle_node_attributes','x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
                'new_instance','foo','x',
                'handle_node_attributes','x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
            ]
        assert c.@log == [
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'register', 'fooz',
            'register', 'baz',
            'new_instance','fooz',null,
            'handle_node_attributes','x',
                'new_instance','baz',null,
                'handle_node_attributes','x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
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
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'new_instance','foo',null,
            'handle_node_attributes','x',
                'new_instance','bar',null,
                'handle_node_attributes','x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
                'new_instance','foo','x','bar','baz',
                'handle_node_attributes','x','bar','baz',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
            ]
        assert c.@log == [
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'register', 'fooz',
            'register', 'baz',
            'new_instance','fooz',null,
            'handle_node_attributes','x',
                'new_instance','baz',null,
                'handle_node_attributes','x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
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
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'new_instance','foo',null,
            'handle_node_attributes','x',
                'new_instance','bar',null,
                'handle_node_attributes','x',
                'set_parent', 'x', 'x',
                'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x'/*,
            'node_completed',null,'x',
            'post_node_completion',null, 'x'*/
            // node foo() was not completed successfuly
            ]
        assert c.@log == [
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            ]
    }

    void testHandlesChildren() {
        def b = new SpoofFactoryBuilder()
        b.getFactories().foo.handlesNodeChildren = true
        b.foo {
            b.bar()
        }
        assert b.@log == [
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'new_instance','foo',null,
            'handle_node_attributes','x',
            'node_children', 'foo',
                'new_instance','bar',null,
                'handle_node_attributes','x',
            'set_parent', 'x', 'x',
            'set_child', 'x', 'x',
                'node_completed','x','x',
                'post_node_completion', 'x', 'x',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
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
            'register', 'foo',
            'register', 'bar',
            'register', 'outest',
            'register', 'outer',
            'register', 'inner',
            'new_instance','foo',null,
            'handle_node_attributes','x',
            'node_children', 'foo',
            'node_completed',null,'x',
            'post_node_completion',null, 'x'
            ]
    }
}

/**
    The SpoofFactoryBuilder is a sample instance of the abstract FactoryBuilderSupport class
    that does nothing but logging how it was called, returning 'x' for each node.
**/
class SpoofFactoryBuilder extends FactoryBuilderSupport{
    protected List log = []

    SpoofFactoryBuilder() {
       def factory = new XFactory( builder:this )
       registerFactory( "foo", factory )
       registerFactory( "bar", factory )
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

    public void onFactoryRegistration(FactoryBuilderSupport builder, String registerdName) {
        builder.@log << 'register'
        builder.@log << registerdName
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
