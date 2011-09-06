package org.codehaus.groovy.tools.rootloadersync;

import java.util.Set;

public class OtherConcreteGenericJavaSubclass extends AbstractGenericGroovySuperclass<String> {

   public OtherConcreteGenericJavaSubclass(Set<String> notes) {
      super(notes);
   }

   @Override
   protected void doSomething(String note) {
   }
}
