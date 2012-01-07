package org.codehaus.groovy.tools.groovydoc;

public class GroovyDocToolTestSampleGroovy {
  /**
    * First method. Simple case
    */
  def firstMethod() {
  }

  /** Second method. Has return type */
  String secondMethod() {}

  /** Third method. Has primitive return type */
  char thirdMethod() {}

  /** Fourth method. Has fully qualified return type */
  java.util.Date fourthMethod() {}

  /*
    * Fifth method. Doesn't have any groovydoc (note: single * at beginning)
    */
  def fifthMethod() {
  }

}
