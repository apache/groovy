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

        def invoker = InvokerHelper.instance
        def stored = invoker.metaRegistry.getMetaClass(String.class)
        assert stored instanceof MetaClassImpl
        def myMetaClass = new MyDelegatingMetaClass(String.class)
        invoker.metaRegistry.removeMetaClass String.class
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

        invoker.metaRegistry.removeMetaClass String.class
        stored = invoker.metaRegistry.getMetaClass(String.class)
        assert stored instanceof MetaClassImpl
    }

  void testNormalCreated () {
      assertEquals groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, metaClass.class
      assertEquals  MetaClassImpl, metaClass.delegate.class
  }

  void testEmcCreated () {
    GroovySystem.metaClassRegistry.removeMetaClass metaClass.theClass
    ExpandoMetaClass.enableGlobally()
        metaClass = GroovySystem.metaClassRegistry.getMetaClass(CustomMetaClassTest)
        assertTrue metaClass instanceof groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass
        assertEquals  ExpandoMetaClass, metaClass.delegate.class
    ExpandoMetaClass.disableGlobally()

    GroovySystem.metaClassRegistry.removeMetaClass metaClass.theClass
    metaClass = null
    assert getMetaClass() instanceof groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass
    assert MetaClassImpl == getMetaClass().delegate.class
  }

  void testStaticMetaClass () {
      // Custom metaclass created
      assertEquals  groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, metaClass.class
      // delegated to MCImpl
      assertEquals  MetaClassImpl, metaClass.delegate.class

      MetaClass expandoMetaClass = CustomMetaClassTest.metaClass

      // It still to be custom
      assertEquals groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, expandoMetaClass.class
      // But delegated to EMC
      assertEquals  ExpandoMetaClass, expandoMetaClass.delegate.class

      // But object still to hold reference to old one
      assertEquals  MetaClassImpl, metaClass.delegate.class
      // let give it chance to reinitialize
      metaClass = null
      // Now it should be OK
      assertEquals  ExpandoMetaClass, expandoMetaClass.delegate.class

      expandoMetaClass.toString = {
          -> "I am modified"
      }

      assertEquals "I am modified", toString()

      assertEquals "I am modified", metaClass.invokeMethod(this, "toString", null)

      expandoMetaClass.static.toString = {
          -> "I am modified static"
      }

      assertEquals "I am modified static", getClass().toString()
      
      GroovySystem.metaClassRegistry.removeMetaClass metaClass.theClass
      metaClass = null
      assert getMetaClass() instanceof groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass
      assert MetaClassImpl == getMetaClass().delegate.class
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

