/*
 * Copyright 2003-2014 the original author or authors.
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

package builder

import groovy.util.GroovyTestCase
import org.junit.Test
import static org.junit.Assert.*

class BuilderSupportTest extends BuilderSupport{
    def log = []
    protected void setParent(Object parent, Object child){
        log << "sp"
        log << parent
        log << child
    }
    protected Object createNode(Object name){
        log << 'cn1'
        log <<  name
        return 'x'
    }
    protected Object createNode(Object name, Object value){
        log << 'cn2'
        log << name
        log << value
        return 'x'
    }
    protected Object createNode(Object name, Map attributes){
        log << 'cn3'
        log << name
        attributes.each{entry -> log << entry.key; log << entry.value}
        return 'x'
    }
    protected Object createNode(Object name, Map attributes, Object value){
        log << 'cn4'
        log << name
        attributes.each{entry -> log << entry.key; log << entry.value}
        log << value
        return 'x'
    }
    protected void nodeCompleted(Object parent, Object node) {
        log << 'nc'
        log << parent
        log << node
    }
    
    public static void main(String[] args)
    {
    
// tag::BuilderSupportTest_default_constructor_object1[]
        def sb1 = new BuilderSupportTest()
        // log should be empty after initialized
        assertEquals sb1.log , []  
// end::BuilderSupportTest_default_constructor_object1[]


// tag::BuilderSupportTest_add_node_object2[]
        def node1 = sb1.foo()
        assert sb1.log == ['cn1','foo','nc',null, node1]
// end::BuilderSupportTest_add_node_object2[]

// tag::BuilderSupportTest_add_node_with_value_object3[]
        // simple node with value
        def sb2 = new BuilderSupportTest()
        def node2 = sb2.foo('value')
        
        // node type 2 creation with name/value pair
        assert sb2.log == ['cn2','foo','value', 'nc',null,node2]
// end::BuilderSupportTest_add_node_with_value_object3[]


// tag::BuilderSupportTest_add_node_with_value_object4[]
        // simple node with one attribute
        def sb3 = new BuilderSupportTest()
        def node3 = sb3.foo(name:'value')
        
        // node type 3 creation with map as name/value pair
        assert sb3.log == ['cn3','foo', 'name','value', 'nc',null,'x']
// end::BuilderSupportTest_add_node_with_value_object4[]
        
        
// tag::BuilderSupportTest_add_nested_nodes_object5[]
        // how is closure applied?
        def sb4 = new BuilderSupportTest()
        sb4.foo(){
            sb4.bar()
        }

        // nested node creation without name/value pairs
        assert sb4.log == [
            'cn1','foo',    // child node 1 is name of method
            'cn1','bar',    // inner child node 2 is name of method
            'sp', 'x', 'x', // set links so inner child points to outer child
            'nc','x','x',   // inner node bar complete points back to outer
            'nc',null,'x']  // outer node complete and no further outer child to link/point back to
// end::BuilderSupportTest_add_nested_nodes_object5[]

    } // end of main
       
} // end of class