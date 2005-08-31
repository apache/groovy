package org.codehaus.groovy.grails.orm.hibernate;

import groovy.lang.GroovyClassLoader;

import org.codehaus.groovy.grails.orm.hibernate.metaclass.FindByPersistentMethod;

import junit.framework.TestCase;

public class PersistentMethodTests extends TestCase {

	
	public void testMethodSignatures() {
		
		FindByPersistentMethod findBy = new FindByPersistentMethod( null,new GroovyClassLoader());
		assertTrue(findBy.isMethodMatch("findByFirstName"));
		assertTrue(findBy.isMethodMatch("findByFirstNameAndLastName"));
		assertFalse(findBy.isMethodMatch("rubbish"));
	}
}
