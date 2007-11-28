package org.codehaus.groovy.runtime

import groovy.util.GroovyTestCase


class NestedCategoryTest extends GroovyTestCase{
   void testGreeter_plain(){
	   def greeter = new Greeter();
	   assertEquals "Hello Groovy!", greeter.greet();
	}

	void testGreeter_withOne(){
	   def greeter = new Greeter();
	   assertEquals "Hello Groovy!", greeter.greet();
	   use( CategoryOne.class ){
	      assertEquals "Hello from One", greeter.greet();
	   }
	   assertEquals "Hello Groovy!", greeter.greet();
	}

	void testGreeter_withTwo(){
	   def greeter = new Greeter();
	   assertEquals "Hello Groovy!", greeter.greet();
	   use( CategoryTwo.class ){
	      assertEquals "Hello from Two", greeter.greet();
	   }
	   assertEquals "Hello Groovy!", greeter.greet();
	}

	void testGreeter_withOneAndTwo_nested(){
	   // fails!
	   def greeter = new Greeter();
	   assertEquals "Hello Groovy!", greeter.greet();
	   use( CategoryOne.class ){
	      assertEquals "Hello from One", greeter.greet();
	      use( CategoryTwo.class ){
	         assertEquals "Hello from Two", greeter.greet();
	      }
	      assertEquals "Hello from One", greeter.greet();
	   }
	   assertEquals "Hello Groovy!", greeter.greet();
	}
}

class Greeter{
    String greet(){
       return "Hello Groovy!";
    }

    String say( String s ){
       return "I say: "+ s;
    }
 }

 class CategoryOne{
    static String greet( Greeter self ){
       return "Hello from One";
    }
 }

 class CategoryTwo{
    static String greet( Greeter self ){
       return "Hello from Two";
    }
 }