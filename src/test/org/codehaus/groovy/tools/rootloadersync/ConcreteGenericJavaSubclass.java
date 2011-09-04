package org.codehaus.groovy.tools.rootloadersync;

import java.util.Set;

public class ConcreteGenericJavaSubclass extends AbstractGenericGroovySuperclass<String> {

   public ConcreteGenericJavaSubclass(Set<String> notes) {
      super(notes);
   }

   @Override
   protected void doSomething(String note) {
   }
}
