package org.codehaus.groovy.reflection

class WeakMapTest extends GroovyTestCase{
   void testClassUnload () {
       GroovyShell shell = new GroovyShell()
       int SIZE = 100
       for (int i = 0; i != SIZE; ++i) {
           Object s = shell.parse ("""
              class A extends B {
                def B callMe (b) {
                  b instanceof A ? this : b
                }
              }

              class B {
              }

              new A ().callMe ("lambda")
           """)

           ReflectionCache.isAssignableFrom s.class, s.class.superclass
           if (i % 10 == 0) {
               System.gc ()
             println "${i} ${ReflectionCache.assignableMap.size()} ${ReflectionCache.CACHED_CLASS_MAP.size()}"
           }

           shell.classLoader.clearCache()
           GroovySystem.metaClassRegistry.removeMetaClass s.class.superclass
           GroovySystem.metaClassRegistry.removeMetaClass s.class
       }

       println "${SIZE} ${ReflectionCache.assignableMap.size()} ${ReflectionCache.CACHED_CLASS_MAP.size()}"
   }
}