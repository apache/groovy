package org.codehaus.groovy.grails.domain;

class OneToOneTest {
	
   @Property Long id;
   @Property Long version;
   
   @Property RelationshipsTest other
}