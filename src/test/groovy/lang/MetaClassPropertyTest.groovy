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

class MetaClassPropertyTest extends GroovyTestCase {

    void testForJavaClass() {
        checkMetaClassBehavior(Short.valueOf('1'))
    }

    void testForGroovyClass() {
        checkMetaClassBehavior(new MCPTest1())
    }

    private checkMetaClassBehavior(Object foo) {
        // check metaclass points to correct class
        assert foo.metaClass.theClass.equals(foo.getClass())

        // check defaults
        assert foo.metaClass.adaptee instanceof MetaClassImpl
        assert foo.getClass().metaClass.adaptee instanceof MetaClassImpl

        // use metaclass builder on instance
        foo.metaClass.dummy = {}
        assert foo.metaClass.adaptee instanceof ExpandoMetaClass
        assert foo.getClass().metaClass.adaptee instanceof MetaClassImpl

        // use metaclass builder on class
        foo.class.metaClass.dummy = {}
        assert foo.metaClass.adaptee instanceof ExpandoMetaClass
        // a little fragile but ExpandoMetaProperty is not public
        assert foo.getClass().metaClass.adaptee.getClass().name.contains('ExpandoMetaClass$ExpandoMetaProperty')

        // remove class-based metaclass
        GroovySystem.metaClassRegistry.removeMetaClass(foo.getClass())
        assert foo.metaClass.adaptee instanceof ExpandoMetaClass
        assert foo.getClass().metaClass.adaptee instanceof MetaClassImpl
    }
}

class MCPTest1 {
}
