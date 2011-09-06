package org.codehaus.groovy.tools.rootloadersync;

public class OtherConcreteJavaSubclass extends AbstractGroovySuperclass {
   @Override
   public String myAbstractMethod() {
      return "string from unrelated subclass";
   }
}
