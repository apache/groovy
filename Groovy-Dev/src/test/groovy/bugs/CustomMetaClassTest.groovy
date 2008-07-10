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
         * instance of the string.
         */
        assertEquals "changed first", firstInstance.toString()
        assertEquals "changed second", secondInstance.toString()

        registry.removeMetaClass String.class
        stored = registry.getMetaClass(String.class)
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
    assert getMetaClass() instanceof org.codehaus.groovy.runtime.HandleMetaClass
    assertEquals groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, getMetaClass().delegate.class
  }

  void testStaticMetaClass () {
      // Custom metaclass created
      assertEquals  groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, metaClass.class
      // delegated to MCImpl
      assertEquals  MetaClassImpl, metaClass.delegate.class

      MetaClass handle = CustomMetaClassTest.metaClass

      // It still to be custom
      assertEquals org.codehaus.groovy.runtime.HandleMetaClass, handle.class
      // delegated to CustomMetaClassTestMetaClass
      assertEquals  groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, handle.delegate.class

      // But object still to hold reference to old one
      assertEquals  MetaClassImpl, metaClass.delegate.class
      // let give it chance to reinitialize
      metaClass = null
      // it is still to be default one
      assertEquals  org.codehaus.groovy.runtime.HandleMetaClass, getMetaClass().class
      assertEquals  groovy.runtime.metaclass.groovy.bugs.CustomMetaClassTestMetaClass, getMetaClass().delegate.class

      handle.toString = {
          -> "I am modified"
      }
      // let give it chance to reinitialize
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

