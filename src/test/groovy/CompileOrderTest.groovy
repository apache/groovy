/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package groovy

class CompileOrderTest extends GroovyTestCase {
   public void testCompileOrder() {
      def interfaceFile = File.createTempFile("TestOrderInterface", ".groovy", new File("target"))
      def concreteFile = File.createTempFile("TestOrderConcrete", ".groovy", new File("target"))
      def cl = new GroovyClassLoader(this.class.classLoader);
      def currentDir = concreteFile.parentFile.absolutePath
      cl.addClasspath(currentDir)
      cl.shouldRecompile = true

      try {
         // Create the interface
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

    public void testCompileFileURI() {
        def interfaceFile = File.createTempFile("TestURLOrderInterface", ".groovy", new File("target"))
        def concreteFile = File.createTempFile("TestURLOrderConcrete", ".groovy", new File("target"))

        def cl = new GroovyClassLoader(this.class.classLoader);
        def currentDir = concreteFile.parentFile.absolutePath
        cl.addClasspath(currentDir)
        cl.shouldRecompile = true

        try {
            // Create the interface
            interfaceFile.deleteOnExit()
            def interfaceName = interfaceFile.name - ".groovy"
            interfaceFile.write "interface $interfaceName { }\n"

            // Create a concrete class which implements the interface
            concreteFile.deleteOnExit()
            def concreteName = concreteFile.name - ".groovy"
            concreteFile.write "class $concreteName implements $interfaceName { }\n"

            GroovyCodeSource codeSource = new GroovyCodeSource(concreteFile.toURI())
            // We're testing whether this fails:
            def groovyClass = cl.parseClass(codeSource,false)
            // Create an object, just for good measure.
            def object = groovyClass.newInstance()
        } finally {
            interfaceFile.delete()
            concreteFile.delete()
        }
    }

}
