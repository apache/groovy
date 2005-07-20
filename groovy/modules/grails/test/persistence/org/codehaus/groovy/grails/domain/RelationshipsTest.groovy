package org.codehaus.groovy.grails.domain;

class RelationshipsTest {
   @Property Map relationships = [ 	"ones" : OneToManyTest2.class,
   									"manys" : ManyToManyTest.class,
   									"uniones" :  UniOneToManyTest.class ];
   									
   @Property int id;
   @Property int version;
   @Property Set ones; // bi-directional one-to-many relationship 
   @Property Set uniones; // uni-directional one-to-many
   @Property Set manys; // many-to-many relationship
   @Property OneToOneTest one; // bi-directional one-to-one
}


