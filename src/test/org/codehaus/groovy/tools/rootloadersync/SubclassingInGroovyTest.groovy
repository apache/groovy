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
package org.codehaus.groovy.tools.rootloadersync

import groovy.test.GroovyTestCase

import static junit.framework.Assert.assertEquals

public class SubclassingInGroovyTest extends GroovyTestCase{

   public void testSubclass() {
      OtherConcreteJavaSubclass unrelatedInstance = new OtherConcreteJavaSubclass();
      ConcreteJavaSubclass instance = new ConcreteJavaSubclass();
      assertEquals("this one works", OtherConcreteJavaSubclass, unrelatedInstance.metaClass.theClass)
      assertEquals("but this one is wrong", ConcreteJavaSubclass, instance.metaClass.theClass)
      assertEquals("string from subclass", instance.myMethod());  // works fine in both languages
      assertEquals("string from subclass", instance.myAbstractMethod());  // works fine in java; throws ClassCastException in groovy
   }

   public void testGenericSubclassWithBafflingSymptom() {
      OtherConcreteGenericJavaSubclass unrelatedInstance = new OtherConcreteGenericJavaSubclass(new HashSet<String>())
      ConcreteGenericJavaSubclass instance = new ConcreteGenericJavaSubclass(new HashSet<String>());
      instance.addNote("abcd");
   }
}
