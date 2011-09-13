package org.codehaus.groovy.tools.rootloadersync;

import org.junit.Test;
import java.util.HashSet;
import static junit.framework.Assert.assertEquals;


public class SubclassingInJavaTest {
   @Test
   public void testSubclass() {
      OtherConcreteJavaSubclass unrelatedInstance = new OtherConcreteJavaSubclass();
      ConcreteJavaSubclass instance = new ConcreteJavaSubclass();
      assertEquals("string from subclass", instance.myMethod());  // works fine in both languages
      assertEquals("string from subclass", instance.myAbstractMethod());  // works fine in java; throws ClassCastException in groovy
   }

   @Test
   public void testGenericSubclassWithBafflingSymptom() {
      OtherConcreteGenericJavaSubclass unrelatedInstance = new OtherConcreteGenericJavaSubclass(new HashSet<String>());
      ConcreteGenericJavaSubclass instance = new ConcreteGenericJavaSubclass(new HashSet<String>());
      instance.addNote("abcd");
   }
}
