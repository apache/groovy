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
package groovy.bugs

import groovy.test.GroovyTestCase

class SynchronizedBytecodeBug extends GroovyTestCase {

    /**
     * Groovy's bytecode associated with syncrhonized(foo) construct used to generate invalid bytecode
     * This test method shows that the standard wait()/notify() works.
     */
    void testSynchronized() {
        Integer foo = 0

        Thread.start{
            sleep 100
            synchronized(foo) {
                foo.notify()
            }
        }

        synchronized(foo) {
            foo.wait()
        }

        // if this point is reached, the test worked :-)
        assert true
    }
  
  /* more tests to ensure a monitor exit is done at the right place */
    
  void testBreakInSynchronized() {
    Object lock = new Object()
    while (true) {
        synchronized(lock) {
            break
        }
    }
    checkNotHavingAMonitor(lock)
  }
  
  void testContinueInSynchronized() {
    Object lock = new Object()  
    boolean b = true
    while (b) {
        synchronized(lock) {
            b = false
            continue
        }
    }
    checkNotHavingAMonitor(lock)
  }
  
  void testReturnInSynchronized() {
    Object lock = new Object()  
    methodWithReturn(lock)
    checkNotHavingAMonitor(lock)
  }
  
  def methodWithReturn(lock) {
    synchronized (lock) {
      return
    }
  }
  
  void testBreakInClosureWithSynchronized() {
    Object lock = new Object()
    def c = {
      while (true) {
          synchronized(lock) {
            break
          }
      }
      checkNotHavingAMonitor(lock)
    }
    c()
    checkNotHavingAMonitor(lock)
  }
  
  void testContinueInClosureWithSynchronized() {
    Object lock = new Object()
    def c = {
      boolean b = true
      while (b) {
          synchronized(lock) {
            b = false
            continue
          }
      }
      checkNotHavingAMonitor(lock)
    }
    c()
    checkNotHavingAMonitor(lock)
  }

  // GROOVY-4159
  void testExceptionInSynchronized() {
    def obj = 1
    try {
      synchronized(obj) {
        obj.e
      }
      assert false
    } catch (MissingPropertyException e) {
      assert true
    }
    checkNotHavingAMonitor(obj) 
  }
  
  def checkNotHavingAMonitor(Object lock){
    // if we call notify* or wait without having the
    // monitor, we get an exception.
    try {
      lock.notifyAll()
      assert false,"should have no monitor!"
    } catch (IllegalMonitorStateException imse) {
      assert true
    }
  }
}
