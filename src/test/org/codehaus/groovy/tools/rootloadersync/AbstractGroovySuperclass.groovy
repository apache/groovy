package org.codehaus.groovy.tools.rootloadersync

abstract class AbstractGroovySuperclass {
   public String myMethod() {
      return myAbstractMethod();
   }

   abstract String myAbstractMethod();
}
