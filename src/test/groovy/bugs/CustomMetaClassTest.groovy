/*
 * Copyright 2003-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.bugs

import org.codehaus.groovy.runtime.InvokerHelper

class CustomMetaClassTest extends GroovyTestCase {
    void testReplaceMetaClass() {
        /*
         * Constructing first instance before meta class replacement
         * is made.
         */
        def firstInstance = "first"
        assertEquals "first", firstInstance.toString()

        MetaClassRegistry registry = InvokerHelper.metaRegistry
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
        assertEquals "changed first", firstInstance.toString()
        assertEquals "changed second", secondInstance.toString()

        registry.removeMetaClass String.class
        stored = registry.getMetaClass(String.class)
        assert stored instanceof MetaClassImpl
    }

    void testNormalCreated() {
        assertEquals groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, metaClass.class
        assertEquals MetaClassImpl, metaClass.delegate.class
    }

    void testEmcCreated() {
        GroovySystem.metaClassRegistry.removeMetaClass metaClass.theClass
        ExpandoMetaClass.enableGlobally()
        metaClass = GroovySystem.metaClassRegistry.getMetaClass(CustomMetaClassTest)
        assertTrue metaClass instanceof groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass
        assertEquals ExpandoMetaClass, metaClass.delegate.class
        ExpandoMetaClass.disableGlobally()

        GroovySystem.metaClassRegistry.removeMetaClass metaClass.theClass
        metaClass = null
        assert getMetaClass() instanceof org.codehaus.groovy.runtime.HandleMetaClass
        assertEquals groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, getMetaClass().delegate.class
    }

    void testStaticMetaClass() {
        // Custom metaclass created
        assertEquals groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, metaClass.class
        // delegated to MCImpl
        assertEquals MetaClassImpl, metaClass.delegate.class

        MetaClass handle = CustomMetaClassTest.metaClass

        // It should still be custom
        assertEquals org.codehaus.groovy.runtime.HandleMetaClass, handle.class
        // delegated to CustomMetaClassTestMetaClass
        assertEquals groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, handle.delegate.class

        // object should still hold reference to old one
        assertEquals MetaClassImpl, metaClass.delegate.class
        // let's give it a chance to reinitialize
        metaClass = null
        // should still be default one
        assertEquals org.codehaus.groovy.runtime.HandleMetaClass, getMetaClass().class
        assertEquals groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, getMetaClass().delegate.class

        handle.toString = {
            -> "I am modified"
        }
        // let's give it a chance to reinitialize
        metaClass = null

        assertEquals "I am modified", toString()

        assertEquals "I am modified", metaClass.invokeMethod(this, "toString", null)

        handle.static.toString = {
            -> "I am modified static"
        }

        assertEquals "I am modified static", getClass().toString()

        GroovySystem.metaClassRegistry.removeMetaClass metaClass.theClass
        metaClass = null
        assert getMetaClass() instanceof org.codehaus.groovy.runtime.HandleMetaClass
        assertEquals groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, getMetaClass().delegate.class
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

