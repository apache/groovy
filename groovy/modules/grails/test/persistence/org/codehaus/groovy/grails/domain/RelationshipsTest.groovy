package org.codehaus.groovy.grails.domain;

class RelationshipsTest {
  @Property relationships = [ 	"ones" : OneToManyTest2.class,
  								"manys" : ManyToManyTest.class,
  								"uniones" : UniOneToManyTest.class ];

  @Property optionals = [ "ones","uniones" ]
   
   @Property Long id;
   @Property Long version;

   @Property Set manys; // many-to-many relationship
   @Property OneToOneTest one; // uni-directional one-to-one
   @Property Set ones; // bi-directional one-to-many relationship    
   @Property Set uniones; // uni-directional one-to-many relationship       
}


