package org.codehaus.groovy.grails.domain;

class ManyToManyTest {
   @Property Map relationships = [ "manys" : RelationshipsTest.class ];
   @Property Long id;
   @Property Long version;
   @Property Set manys; // many-to-many relationship 
}