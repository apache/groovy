package org.codehaus.groovy.tools.rootloadersync

import static junit.framework.Assert.assertEquals

public class SubclassingInGroovyTest extends GroovyTestCase{

   public void testSubclass() {
      OtherConcreteJavaSubclass unrelatedInstance = new OtherConcreteJavaSubclass();
      ConcreteJavaSubclass instance = new ConcreteJavaSubclass();
      assertEquals("this one works", OtherConcreteJavaSubclass, unrelatedInstance.metaClass.theClass)
      assertEquals("but this one is wrong", ConcreteJavaSubclass, instance.metaClass.theClass)
      assertEquals("string from subclass", instance.myMethod());  // works fine in both languages
      assertEquals("string from subclass", instance.myAbstractMethod());  // works fine in java; throws ClassCastException in groovy
   }

   public void testGenericSubclassWithBafflingSymptom() {
      OtherConcreteGenericJavaSubclass unrelatedInstance = new OtherConcreteGenericJavaSubclass(new HashSet<String>())
      ConcreteGenericJavaSubclass instance = new ConcreteGenericJavaSubclass(new HashSet<String>());
      instance.addNote("abcd");
   }
}
