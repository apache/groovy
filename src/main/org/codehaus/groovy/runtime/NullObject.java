/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */
package org.codehaus.groovy.runtime;

import java.util.Collections;
import java.util.Iterator;

import groovy.lang.GroovyObjectSupport;

public class NullObject extends GroovyObjectSupport {
  private static NullObject ref;

  /**
   * private constructor
   **/
  private NullObject() {
  }

  /**
   * get the NullObject reference
   **/
  public synchronized static NullObject getNullObject() {
    if (ref == null)
      ref = new NullObject();
    return ref;
  }

  /**
   * Since this is implemented as a singleton, we should avoid the 
   * use of the clone method
   **/
  public Object clone() throws CloneNotSupportedException {
    throw new CloneNotSupportedException();
  }

  /**
   * Tries to get a property on null, which will always fail
   * @param property - the property to get
   * @returns a NPE
   **/
  public Object getProperty(String property) {
    throw new NullPointerException("Cannot get property " + property + "() on null object");
  }
  
  /**
   * Tries to set a property on null, which will always fail
   * @param property - the proprty to set
   * @param newValue - the new value of the property
   * @returns a NPE
   */
  public void setProperty(String property, Object newValue) {
      throw new NullPointerException("Cannot set property " + property + "() on null object");
  }

  /**
   * Tries to invoke a method on null, which will always fail
   * @param name the name of the method to invoke
   * @param args - arguments to the method
   * @returns a NPE
   * 
   **/
  public Object invokeMethod(String name, Object args) {
    throw new NullPointerException("Cannot invoke method " + name + "() on null object");
  }

  /**
   * null is only equal to null
   * @param to - the reference object with which to compare
   * @returns - true if this object is the same as the to argument
   **/
  public boolean equals(Object to) {
    return to == null;
  }

  /**
   * iterator() method to be able to iterate on null. 
   * Note: this part is from Invoker
   * @return an iterator for an empty list
   */
  public Iterator iterator() {
      return Collections.EMPTY_LIST.iterator();
  }
  
  /**
   * Allows to add a String to null. 
   * The result is concatenated String of "null" and the
   * String in the parameter. 
   * @param s - the String to concatenate
   * @return the concatenated string
   */
  public Object plus(String s){
      return "null"+s;
  }
  
  /**
   * The method "is" is used to test for equal references.
   * This method will return true only if the given parameter
   * is null
   * @param other - the object to test
   * @return true if other is null
   */
  public boolean is(Object other) {
      return other==null;
  }
  
  /**
   * Type conversion method for null.
   * @param c - the class to convert to
   * @return always null
   */
  public Object asType(Class c) {
      return null;
  }

  public String toString(){
      throw new NullPointerException("toString() not allowed on null");
  }
  
  public int hashCode() {
      throw new NullPointerException("hashCode() not allowed on null");
  }
  
}
