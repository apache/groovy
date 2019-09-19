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
package groovy.bugs

import groovy.test.GroovyTestCase

class CustomMetaClassTest extends GroovyTestCase {

    @Override
    protected void setUp() {
        super.setUp()

        ExpandoMetaClass.disableGlobally()

        def reg = GroovySystem.metaClassRegistry
        reg.removeMetaClass(String)
        reg.removeMetaClass(this.class)
        reg.removeMetaClass(this.metaClass.class)
    }

    void testReplaceMetaClass() {
        /*
         * Constructing first instance before meta class replacement
         * is made.
         */
        def firstInstance = "first"
        assert "first" == firstInstance.toString()

        MetaClassRegistry registry = GroovySystem.metaClassRegistry
        def stored = registry.getMetaClass(String.class)
        assert stored instanceof MetaClassImpl
        def myMetaClass = new MyDelegatingMetaClass(String.class)
        registry.removeMetaClass String.class
        registry.setMetaClass(String.class, myMetaClass)

        /*
         * Constructing second instance after meta class replacment
         * is made.
         */
        def secondInstance = "second"

        /*
         * Since we are replacing a meta class at the class level
         * we are changing the behavior of the first and second
         * instances of the string.
         */
        assert "changed first" == firstInstance.toString()
        assert "changed second" == secondInstance.toString()

        registry.removeMetaClass String.class
        stored = registry.getMetaClass(String.class)
        assert stored instanceof MetaClassImpl
    }

    void testNormalCreated() {
        assert groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass == metaClass.class
        assert MetaClassImpl == metaClass.delegate.class
    }

    void testEmcCreated() {
        GroovySystem.metaClassRegistry.removeMetaClass metaClass.theClass
        ExpandoMetaClass.enableGlobally()
        metaClass = GroovySystem.metaClassRegistry.getMetaClass(CustomMetaClassTest)
        assert metaClass instanceof groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass
        assert ExpandoMetaClass == metaClass.delegate.class
        ExpandoMetaClass.disableGlobally()

        GroovySystem.metaClassRegistry.removeMetaClass metaClass.theClass
        metaClass = null
        assert getMetaClass() instanceof org.codehaus.groovy.runtime.HandleMetaClass
        assert groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass == getMetaClass().delegate.class
    }

    void testStaticMetaClass() {
        // Custom metaclass created
        assert groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass == metaClass.class
        // delegated to MCImpl
        assert MetaClassImpl == metaClass.delegate.class

        MetaClass handle = CustomMetaClassTest.metaClass

        // It should still be custom
        assert org.codehaus.groovy.runtime.HandleMetaClass == handle.class
        // delegated to CustomMetaClassTestMetaClass
        assert groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass == handle.delegate.class

        // object should still hold reference to old one
        assert MetaClassImpl == metaClass.delegate.class
        // let's give it a chance to reinitialize
        metaClass = null
        // should still be default one
        assert org.codehaus.groovy.runtime.HandleMetaClass == getMetaClass().class
        assert groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass == getMetaClass().delegate.class

        handle.toString = {
            -> "I am modified"
        }
        // let's give it a chance to reinitialize
        metaClass = null

        assert  "I am modified" == toString()

        assert "I am modified" == metaClass.invokeMethod(this, "toString", null)

        handle.static.toString = {
            -> "I am modified static"
        }

        assert "I am modified static" == getClass().toString()

        GroovySystem.metaClassRegistry.removeMetaClass metaClass.theClass
        metaClass = null
        assert getMetaClass() instanceof org.codehaus.groovy.runtime.HandleMetaClass
        assert groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass == getMetaClass().delegate.class
    }

}

class MyDelegatingMetaClass extends groovy.lang.DelegatingMetaClass {
    MyDelegatingMetaClass(final Class a_class) {
        super(a_class);
        initialize()
    }

    public Object invokeMethod(Object a_object, String a_methodName, Object[] a_arguments) {
        return "changed ${super.invokeMethod(a_object, a_methodName, a_arguments)}"
    }
}

