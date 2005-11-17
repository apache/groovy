package org.codehaus.groovy.grails.domain;

class OneToManyTest2 {

   @Property int id;
   @Property int version;
   @Property RelationshipsTest other; // many-to-one relationship

}