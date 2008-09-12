/*
 * Copyright 2003-2008 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.transform.vm5

/**
 * @author Alex Tkachman
 */
class SingletonTransformTest extends GroovyTestCase {

    GroovyShell shell;

    protected void setUp() {
        super.setUp();
        shell = new GroovyShell();
    }

    protected void tearDown() {
        shell = null;
        super.tearDown();
    }

    void testSingleton() {
        def res = shell.evaluate("""
              @Singleton
              class X {
                 def getHello () {
                   "Hello, World!"
                 }
              }

              X.instance.hello
        """)

        assertEquals("Hello, World!", res)
    }

    void testLazySingleton() {
        def res = shell.evaluate("""
              @Singleton(lazy=true)
              class X {
                 def getHello () {
                   "Hello, World!"
                 }
              }

              assert X.@instance == null

              X.instance.hello
        """)

        assertEquals("Hello, World!", res)
    }

    void testSingletonInstantiationFails() {
        shouldFail {
            shell.evaluate("""
                  @Singleton
                  class X {
                     def getHello () {
                       "Hello, World!"
                     }
                  }

                  new X ()
            """)
        }
    }

    void testSingletonOverideConstructorFails() {
            def res = shell.evaluate("""
                  @Singleton
                  class X {
                     static hello = "Bye-bye world"

                     X () {
                        hello = "Hello, World!"
                     }
                  }

                  X.instance.hello
            """)

            assertEquals("Hello, World!", res)
    }
}