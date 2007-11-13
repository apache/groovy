package groovy.bugs

import org.codehaus.groovy.runtime.InvokerHelper

class CustomMetaClassTest extends GroovyTestCase{
    void testReplaceMetaClass() {
        /*
         * Constructing first instance before meta class replacment
         * is made.
         */
        def firstInstance = "first"
        assertEquals "first", firstInstance.toString()

        def myMetaClass = new MyDelegatingMetaClass(String.class)
        def invoker = InvokerHelper.instance
        invoker.metaRegistry.setMetaClass(String.class, myMetaClass)

        /*
         * Constructing second instance after meta class replacment
         * is made.
         */
        def secondInstance = "second"

        /*
         * Since we are replacing a meta class at the class level
         * we are changing the behavior of the first and second
         * instance of the string.
         */
        assertEquals "changed first", firstInstance.toString()
        assertEquals "changed second", secondInstance.toString()
    }

  void testNormalCreated () {
      assertTrue metaClass instanceof groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass
      assertEquals  MetaClassImpl, metaClass.delegate.class
  }

  void testEmcCreated () {
    GroovySystem.metaClassRegistry.removeMetaClass metaClass.theClass
    ExpandoMetaClass.enableGlobally()
        metaClass = GroovySystem.metaClassRegistry.getMetaClass(CustomMetaClassTest)
        assertTrue metaClass instanceof groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass
        assertEquals  ExpandoMetaClass, metaClass.delegate.class
    ExpandoMetaClass.disableGlobally()
  }
}

class MyDelegatingMetaClass extends groovy.lang.DelegatingMetaClass
{
    MyDelegatingMetaClass(final Class a_class)
    {
        super(a_class);
        initialize()
    }

    public Object invokeMethod(Object a_object, String a_methodName, Object[] a_arguments)
    {
        return "changed ${super.invokeMethod(a_object, a_methodName, a_arguments)}"
    }
}

