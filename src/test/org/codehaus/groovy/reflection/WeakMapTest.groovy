package org.codehaus.groovy.reflection

class WeakMapTest extends GroovyTestCase{
   void testClassUnload () {
       GroovyShell shell = null
       int SIZE = 1000
       for (int i = 0; i != SIZE; ++i) {
           if (shell == null)
             shell = new GroovyShell ()

           Script s = shell.parse ("""
              class A extends B {
                def String callMe (b) {
                  b instanceof A ? this : b
                }
              }

              class B {
              }

              new A ().callMe ("lambda")
           """)
           s.run()

           ReflectionCache.isAssignableFrom s.class, s.class.superclass
           if (i % 10 == 0) {
               if (i % 50 == 0)
                 shell = null
               System.gc ()
            }

           if (shell != null)
             shell.classLoader.clearCache()
           GroovySystem.metaClassRegistry.removeMetaClass s.class.superclass
           GroovySystem.metaClassRegistry.removeMetaClass s.class
       }

       println "${SIZE} ${ReflectionCache.assignableMap.size()} ${ClassInfo.size()}"
   }
}
