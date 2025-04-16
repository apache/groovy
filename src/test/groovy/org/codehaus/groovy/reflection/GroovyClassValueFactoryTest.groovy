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

import groovy.test.GroovyTestCase

import java.util.concurrent.atomic.AtomicInteger

import org.codehaus.groovy.reflection.GroovyClassValue.ComputeValue

class GroovyClassValueFactoryTest extends GroovyTestCase {
	void testCreateGroovyClassValue(){
		final AtomicInteger counter = new AtomicInteger()
		GroovyClassValue<String> classValue = GroovyClassValueFactory.createGroovyClassValue(new ComputeValue<String>(){
			String computeValue(Class<?> type){
				counter.incrementAndGet()
				return type.name
			}
		})
		assertEquals("retrieved String class value", String.name, classValue.get(String))
		assertEquals("computeValue correctly invoked 1 time", 1, counter.get())
		assertEquals("retrieved String class value", String.name, classValue.get(String))
		assertEquals("computeValue correctly invoked 1 time", 1, counter.get())
		assertEquals("retrieved Integer class value", Integer.name, classValue.get(Integer))
		assertEquals("computeValue correctly invoked 2 times", 2, counter.get())
		classValue.remove(String)
		assertEquals("retrieved String class value", String.name, classValue.get(String))
		assertEquals("computeValue correctly invoked 3 times", 3, counter.get())
	}
}
