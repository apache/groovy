package org.codehaus.groovy.grails.domain;

class OneToManyTest1 {
   @Property Map relationships = [ "accounts" : OneToManyTest2.class ];
   @Property Map mappedBy = ["accounts" : "holder" ];

   @Property int id;
   @Property int version;
   @Property Set accounts; // one-to-many relationship to Account
}


