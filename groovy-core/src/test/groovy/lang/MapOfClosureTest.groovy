/*
 * Copyright 2003-2007 the original author or authors.
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
package groovy.lang

/**
 * Tests maps containing closures coerced to classes by asType
 *   @author Jochen Theodorou
 */
class MapOfClosureTest extends GroovyTestCase {

  void testInterfaceProxy() {
     def outer=1
     def x = [run:{outer++}] as Runnable
     x.run()
     assert x instanceof Runnable
     assert outer==2
  }
  
  void testObject() {
     def m = [run:{}]
     def x = m as Object
     assert x.is(m)
  }
  
  void testAbstractClassSubclassing() {
     def outer=1
     def x = [run:{outer++}] as TimerTask
     x.run()
     assert x instanceof TimerTask
     assert outer==2
  }
}