package org.codehaus.groovy.reflection

import java.util.concurrent.atomic.AtomicInteger;

import groovy.util.GroovyTestCase;

import org.codehaus.groovy.reflection.GroovyClassValue.ComputeValue

class GroovyClassValueFactoryTest extends GroovyTestCase {
	public void testCreateGroovyClassValue(){
		final AtomicInteger counter = new AtomicInteger();
		GroovyClassValue<String> classValue = GroovyClassValueFactory.createGroovyClassValue(new ComputeValue<String>(){
			String computeValue(Class<?> type){
				counter.incrementAndGet()
				return type.name;
			}
		});
		assertEquals("retrieved String class value", String.class.getName(), classValue.get(String.class))
		assertEquals("computeValue correctly invoked 1 time", 1, counter.value)
		assertEquals("retrieved String class value", String.class.getName(), classValue.get(String.class))
		assertEquals("computeValue correctly invoked 1 time", 1, counter.value)
		assertEquals("retrieved Integer class value", Integer.class.getName(), classValue.get(Integer.class))
		assertEquals("computeValue correctly invoked 2 times", 2, counter.value)
		classValue.remove(String.class)
		assertEquals("retrieved String class value", String.class.getName(), classValue.get(String.class))
		assertEquals("computeValue correctly invoked 3 times", 3, counter.value)
	}
}
