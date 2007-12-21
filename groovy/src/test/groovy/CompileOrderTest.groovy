package groovy

class CompileOrderTest extends GroovyTestCase {
   public void testCompileOrder() {
      def interfaceFile = File.createTempFile("TestOrderInterface", ".groovy", new File("target"))
      def concreteFile = File.createTempFile("TestOrderConcrete", ".groovy", new File("target"))

      def cl = new GroovyClassLoader(this.class.classLoader);
      def currentDir = concreteFile.parentFile.absolutePath
      println currentDir
      cl.addClasspath(currentDir)
      cl.shouldRecompile = true

      try {
         // Create the interface
         println "a"
         interfaceFile.deleteOnExit()
         def interfaceName = interfaceFile.name - ".groovy"
         interfaceFile.write "interface $interfaceName { }\n"

         // Create a concrete class which implements the interface
         concreteFile.deleteOnExit()
         def concreteName = concreteFile.name - ".groovy"
         concreteFile.write "class $concreteName implements $interfaceName { }\n"

         // We're testing whether this fails:
         def groovyClass = cl.loadClass(concreteName,true,false)
         // Create an object, just for good measure.
         def object = groovyClass.newInstance()
      } finally {
         interfaceFile.delete()
         concreteFile.delete()
      }
   }
}
