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
package groovy.transform

import groovy.mock.interceptor.StubFor
import groovy.test.GroovyTestCase

import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.transform.TimedInterruptibleASTTransformation

/**
 * Test for TimedInterrupt.
 */
class TimedInterruptTest extends GroovyTestCase {

  void testClassMethodIsVisited() {
    def c = new GroovyClassLoader().parseClass('''
      import groovy.transform.TimedInterrupt

      @TimedInterrupt(value = 1L)
      class MyClass {
        def myMethod() { }
      }
    ''')
    assertPassesNormalFailsSlowExecution(c)
  }

  void testClassMethodIsVisitedAndCustomExceptionThrown() {
    def c = new GroovyClassLoader(this.class.classLoader).parseClass('''
      import groovy.transform.TimedInterrupt

      @TimedInterrupt(thrown=groovy.transform.CustomException,value = 1L)
      class MyClass {
        def myMethod() { }
      }
    ''')
    assertPassesNormalFailsSlowExecution(c, 1000000666L, '1', 'myMethod', CustomException)
  }

  void testScriptMethodIsVisited() {
    def c = new GroovyClassLoader().parseClass('''
      import groovy.transform.TimedInterrupt

      @TimedInterrupt(value = 1L)
      def myMethod() { }
    ''')
    assertPassesNormalFailsSlowExecution(c)
  }

  void testStaticMethodIsNotVisited() {
    def c = new GroovyClassLoader().parseClass('''
      import groovy.transform.TimedInterrupt

      @TimedInterrupt(value = 1L)
      class MyClass {
        static def myMethod() { }
      }
    ''')
    assertPassesSlowExecution(c)
  }

  void testClosureFieldIsVisited() {
    def c = new GroovyClassLoader().parseClass('''
      import groovy.transform.TimedInterrupt

      @TimedInterrupt(value = 1L)
      class MyClass {
        def myMethod = { }
      }
    ''')
    assertPassesNormalFailsSlowExecution(c)
  }

  void testClosureInScriptIsVisited_CheckOnMethodStartIsFalse() {
    def c = new GroovyClassLoader().parseClass('''
      import groovy.transform.TimedInterrupt

      @TimedInterrupt(checkOnMethodStart = false, value = 1L)
      def myMethod = { }
      myMethod()
    ''')
    assertPassesNormalFailsSlowExecution(c, 1000000666L, '1', 'run')
  }

  void testWhileInScriptIsVisited_CheckOnMethodStartIsFalse() {
    def c = new GroovyClassLoader().parseClass('''
      @TimedInterrupt(checkOnMethodStart = false, value = 1L)
      import groovy.transform.TimedInterrupt
      import java.util.concurrent.TimeUnit

      int x = 1
      while (x < 2) { x = 2 }
    ''')
    assertPassesNormalFailsSlowExecution(c, 1000000666L, '1', 'run')
  }

  void testForInScriptIsVisited_CheckOnMethodStartIsFalse() {
    def c = new GroovyClassLoader().parseClass('''
      @TimedInterrupt(checkOnMethodStart = false, value = 1L)
      import groovy.transform.TimedInterrupt

      def x = [1]
      for (def o : x) { o++ }
    ''')
    assertPassesNormalFailsSlowExecution(c, 1000000666L, '1', 'run')
  }

  void testStaticClosureFieldNotVisited() {
    def c = new GroovyClassLoader().parseClass('''
      import groovy.transform.TimedInterrupt

      @TimedInterrupt(value = 1L)
      class MyClass {
        static def myMethod = { }
      }
    ''')
    assertPassesSlowExecution(c)
  }

  void testAnnotationParameters() {
    def c = new GroovyClassLoader().parseClass('''
      import groovy.transform.TimedInterrupt
      import java.util.concurrent.TimeUnit

      @TimedInterrupt(value = 18000000L, unit = TimeUnit.MILLISECONDS)
      def myMethod() { }
    ''')
    assertPassesNormalFailsSlowExecution(c, 18000000000666, '18000000', 'myMethod', TimeoutException, 'milliseconds') //5 hours in future
  }

  // TODO not sure all these tests are pulling their weight - testing Groovy annotation type handing not subject
  void testErrorHandling() {
    shouldFail(MultipleCompilationErrorsException) {
      new GroovyClassLoader().parseClass('''
        import groovy.transform.TimedInterrupt
        @TimedInterrupt(value = "5")
        def myMethod() { }
      ''')
    }

    shouldFail(MultipleCompilationErrorsException) {
      new GroovyClassLoader().parseClass('''
        import groovy.transform.TimedInterrupt
        @TimedInterrupt(value = foo())
        def myMethod() { }
      ''')
    }

    shouldFail(MultipleCompilationErrorsException) {
      new GroovyClassLoader().parseClass('''
        import groovy.transform.TimedInterrupt
        @TimedInterrupt(value = 5L, applyToAllClasses = 5)
        def myMethod() { }
      ''')
    }

    shouldFail(MultipleCompilationErrorsException) {
      new GroovyClassLoader().parseClass('''
        import groovy.transform.TimedInterrupt
        @TimedInterrupt(value = 5L, applyToAllClasses = foo())
        def myMethod() { }
      ''')
    }

    shouldFail(MultipleCompilationErrorsException) {
      new GroovyClassLoader().parseClass('''
        import groovy.transform.TimedInterrupt
        @TimedInterrupt(value = 5L, checkOnMethodStart = 5)
        def myMethod() { }
      ''')
    }

    shouldFail(MultipleCompilationErrorsException) {
      new GroovyClassLoader().parseClass('''
        import groovy.transform.TimedInterrupt
        @TimedInterrupt(value = 5L, checkOnMethodStart = foo())
        def myMethod() { }
      ''')
    }

    shouldFail(MultipleCompilationErrorsException) {
      new GroovyClassLoader().parseClass('''
        import groovy.transform.TimedInterrupt
        @TimedInterrupt(value = 5L, unit = 5)
        def myMethod() { }
      ''')
    }

    shouldFail(MultipleCompilationErrorsException) {
      new GroovyClassLoader().parseClass('''
        import groovy.transform.TimedInterrupt
        @TimedInterrupt(value = 5L, unit = foo())
        def myMethod() { }
      ''')
    }
  }

  void testTimedInterruptOnAbstractClass() {
    def script = '''
      @groovy.transform.TimedInterrupt(value = 1L)
      abstract class MyAbstractClass {
        abstract void myMethod()
      }

      class Concrete extends MyAbstractClass {
        void myMethod() {
          99.times {
            // do something
          }
        }
      }
      new Concrete()
    '''

    def system = new StubFor(System)

    // start time initialized to the Long of the Beast
    system.demand.nanoTime(4) { 666L } // 2 times to cover full instantiation
    system.demand.nanoTime() { 1000000667L }

    system.use {
      def instance = new GroovyShell(TimedInterruptibleASTTransformation.getClassLoader()).evaluate(script)
      // may get false positives if multiple annotations with the same expireTime defined in test script
      assert instance.dump().matches('.*timedInterrupt\\S+\\$expireTime=1000000666 .*')

      shouldFail(TimeoutException) {
        instance.myMethod()
      }
    }
  }

  private void assertPassesNormalFailsSlowExecution(c, long expireTime=1000000666L, units='1', methodName='myMethod', exception=TimeoutException, timeUnitName='seconds') {
    def system = new StubFor(System)
    // start time initialized to the Long of the Beast
    system.demand.nanoTime() { 666L }
    def instance
    system.use {
      instance = c.newInstance()
    }
    // may get false positives if multiple annotations with the same expireTime defined in test script
    assert instance.dump().matches('.*timedInterrupt\\S+\\$expireTime=' + expireTime + ' .*')

    system.demand.nanoTime() { expireTime }
    system.use {
      instance."$methodName"()
    }

    // one nanosecond too slow
    system.demand.nanoTime() { expireTime + 1 }
    system.use {
      def e = shouldFail(exception) {
        instance."$methodName"()
      }
      assert e.contains('Execution timed out after ' + units + ' ' + timeUnitName)
    }
  }

  private void assertPassesSlowExecution(c) {
    def system = new StubFor(System)
    // start time initialized to the Long of the Beast
    system.demand.nanoTime() { 666L }
    def instance
    system.use {
      instance = c.newInstance()
    }
    system.demand.nanoTime() { 1000000667L }
    system.use {
      instance.myMethod()
    }
  }
}
