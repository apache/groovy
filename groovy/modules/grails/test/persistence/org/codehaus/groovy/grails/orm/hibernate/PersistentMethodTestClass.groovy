package org.codehaus.groovy.grails.orm.hibernate;

public class PersistentMethodTestClass {

	@Property List optionals = [ "age" ];
	
	@Property Long id; 
	@Property Long version; 
	
	@Property String firstName; 
	@Property String lastName; 
	@Property Integer age;
}