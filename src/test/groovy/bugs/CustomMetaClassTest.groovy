package groovy.bugs

class CustomMetaClassTest extends GroovyTestCase{
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