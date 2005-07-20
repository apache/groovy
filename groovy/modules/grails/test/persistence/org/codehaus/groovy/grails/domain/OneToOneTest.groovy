package org.codehaus.groovy.grails.domain;

class OneToOneTest {

   @Property int id;
   @Property int version;
   @Property RelationshipsTest other; // one-to-one relationship

}