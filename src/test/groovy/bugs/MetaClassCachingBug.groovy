package groovy.bugs

class MetaClassCachingBug extends GroovyTestCase {
   void testOne() {
       StringMocker.mockString("test")
       def collaborator = new MyCollaborator()
       assertEquals "test", collaborator.getDummy("testOne")
   }

   void testTwo() {
       StringMocker.mockString("dog")
       def collaborator = new MyCollaborator()
       assertEquals "dog", collaborator.getDummy("testTwo")
   }
}

class MyCollaborator {
   def getDummy(String str) {
       return str.dummy()
   }
}

class StringMocker {
   static mockString(String retval) {
       String.metaClass.dummy = {->
           return retval
       }
   }
}
