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

import org.codehaus.groovy.reflection.GroovyClassValue.ComputeValue
import org.junit.jupiter.api.Test

import java.util.concurrent.atomic.AtomicInteger

import static org.junit.jupiter.api.Assertions.assertEquals

class GroovyClassValueFactoryTest {
 @Test
	void testCreateGroovyClassValue(){
		final AtomicInteger counter = new AtomicInteger()
		GroovyClassValue<String> classValue = GroovyClassValueFactory.createGroovyClassValue(new ComputeValue<String>(){
			String computeValue(Class<?> type){
				counter.incrementAndGet()
				return type.name
			}
		})
		assertEquals(String.name, classValue.get(String), "retrieved String class value")
		assertEquals(1, counter.get(), "computeValue correctly invoked 1 time")
		assertEquals(String.name, classValue.get(String), "retrieved String class value")
		assertEquals(1, counter.get(), "computeValue correctly invoked 1 time")
		assertEquals(Integer.name, classValue.get(Integer), "retrieved Integer class value")
		assertEquals(2, counter.get(), "computeValue correctly invoked 2 times")
		classValue.remove(String)
		assertEquals(String.name, classValue.get(String), "retrieved String class value")
		assertEquals(3, counter.get(), "computeValue correctly invoked 3 times")
	}
}
