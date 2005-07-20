package org.codehaus.groovy.grails.domain;

class ManyToManyTest {
   @Property Map relationships = [ "manys" : RelationshipsTest.class ];
   @Property int id;
   @Property int version;
   @Property Set manys; // many-to-many relationship to 

}