/*
 * Copyright 2008-2010 the original author or authors.
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
package groovy.transform

/**
 * Test for {@link ConditionalInterrupt} AST Transformation.
 */
class ConditionalInterruptTest extends GroovyTestCase {

    public void testMethodIsVisited_AndExceptionMessage() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.ConditionalInterrupt
            @ConditionalInterrupt({ visited = true })
            class MyClass {
              boolean visited = false
              def myMethod() { }
            }
        ''')

        def instance = c.newInstance()
        def message = shouldFail(InterruptedException) {
            instance.myMethod()
        }
        assert message == 'Execution interrupted. The following condition failed: { visited = true }'
        assert instance.visited
    }

    public void testMethodIsVisited_AndCustomExceptionMessage() {

        def c = new GroovyClassLoader(this.class.classLoader).parseClass('''
            import groovy.transform.ConditionalInterrupt
            @ConditionalInterrupt(thrown=groovy.transform.CustomException, value={ visited = true })
            class MyClass {
              boolean visited = false
              def myMethod() { }
            }
        ''')

        def instance = c.newInstance()
        def message = shouldFail(CustomException) {
            instance.myMethod()
        }
        assert message == 'Execution interrupted. The following condition failed: { visited = true }'
        assert instance.visited
    }


    public void testStaticMethodIsNotVisited() {

         def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.ConditionalInterrupt
            @ConditionalInterrupt({ visited = true })
            class MyClass {
              boolean visited = false
              static def myMethod() { }
            }
        ''')

        def instance = c.newInstance()
        instance.myMethod()
        assert !instance.visited
    }

    public void testClosureFieldIsVisited() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.ConditionalInterrupt
            @ConditionalInterrupt({ visited = true })
            class MyClass {
              boolean visited = false
              def myMethod = { }
            }
        ''')

        def instance = c.newInstance()
        shouldFail(InterruptedException) {
            instance.myMethod()
        }
        assert instance.visited
    }

    public void testWhileLoopVisited() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.ConditionalInterrupt
            @ConditionalInterrupt({ count > 5 })
            class MyClass {
                int count = 0
                def myMethod = {
                    while (count < 10) {
                        count++
                    }
                }
            }
        ''')

        def instance = c.newInstance()
        shouldFail(InterruptedException) {
            instance.myMethod()
        }
        assert 6 == instance.count
    }

    public void testForLoopVisited() {

        def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.ConditionalInterrupt
            @ConditionalInterrupt({ count > 5 })
            class MyClass {
                int count = 0
                def myMethod = {
                    for (int x = 0; x < 10; x++) {
                        count++
                    }
                }
            }
        ''')

        def instance = c.newInstance()
        shouldFail(InterruptedException) {
            instance.myMethod()
        }
        assert 6 == instance.count
    }

    public void testStaticClosureFieldNotVisited() {

         def c = new GroovyClassLoader().parseClass('''
            import groovy.transform.ConditionalInterrupt
            @ConditionalInterrupt({ visited = true })
            class MyClass {
              boolean visited = false
              static def myMethod = { }
            }
        ''')

        def instance = c.newInstance()
        instance.myMethod()
        assert !instance.visited
    }

    public void testSharedContext() {
        def shell = new GroovyShell()

        def script = shell.parse('''
            import groovy.transform.ConditionalInterrupt

class Helper {
  static int i=0
  static def shouldInterrupt() { i++>1 }
}

@ConditionalInterrupt({ Helper.shouldInterrupt() })
class MyClass {
   def myMethod() { }
}

@ConditionalInterrupt({ Helper.shouldInterrupt() })
class MyOtherClass {
   def myOtherMethod() { new MyClass().myMethod() }
}

new MyOtherClass().myOtherMethod()
''', 'myScript')
        shouldFail(InterruptedException) {
            script.run()
        }
    }
}
