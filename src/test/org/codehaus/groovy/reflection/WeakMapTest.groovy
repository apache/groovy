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
           if (i % 50 == 0)
             shell = null

           if (shell != null)
             shell.classLoader.clearCache()
           GroovySystem.metaClassRegistry.removeMetaClass s.class.superclass
           GroovySystem.metaClassRegistry.removeMetaClass s.class
       }

       println "${SIZE} ${ClassInfo.size()}"
   }
}
