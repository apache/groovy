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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.ReturnsSTCTest

/**
 * Unit tests for static type checking : returns.
 */
class ReturnsStaticCompileTest extends ReturnsSTCTest implements StaticCompilationTestSupport {

    void testReturnTypeInferenceWithInheritance() {
        assertScript '''
interface Greeter {
   public void sayHello()
}

class HelloGreeter implements Greeter {
   public void sayHello() {
       println "Hello world!"
   }
}

class A {
   Greeter createGreeter() {
       new HelloGreeter()
   }

   void sayHello() {
      // also fails: def greeter = createGreeter()
      // successful: def greeter = (Greeter)createGreeter()
      Greeter greeter = createGreeter()
      greeter.sayHello()
   }
}

class HelloThereGreeter implements Greeter {
   public void sayHello() {
       println "Hello there!"
   }
}

class B extends A {
   Greeter createGreeter() {
       new HelloThereGreeter()
   }
}


new B().sayHello()'''
    }

}

