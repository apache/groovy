package groovy.bugs

class Groovy2391Bug extends GroovyTestCase{
    void testBug () {
      ArrayList.metaClass.asType = { Class clazz ->
          if (clazz.isInstance(delegate[1]))
            return delegate[1]
          fail ()
      }
      ArrayList.metaClass.initialize()

      assertEquals("Boom", [1,"Boom",3] as String)

      GroovySystem.metaClassRegistry.removeMetaClass ArrayList
    }
}