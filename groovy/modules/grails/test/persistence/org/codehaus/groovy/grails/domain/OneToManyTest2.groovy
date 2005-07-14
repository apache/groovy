package org.codehaus.groovy.grails.domain;

class OneToManyTest2 {
   @Property int id;
   @Property int version;
   @Property OneToManyTest1 holder; // many-to-one relationship to User
}