package org.codehaus.groovy.reflection.v7;

import org.codehaus.groovy.reflection.GroovyClassValue;
import org.codehaus.groovy.reflection.GroovyClassValue.ComputeValue;

/** GroovyClassValue implementaion that simply delegates to Java 7's java.lang.ClassValue
 * @see java.lang.ClassValue
 *
 * @param <T>
*/
public class GroovyClassValueJava7<T> extends ClassValue<T> implements GroovyClassValue<T> {
   private final ComputeValue<T> computeValue;
   public GroovyClassValueJava7(ComputeValue<T> computeValue){
      this.computeValue = computeValue;
   }
   @Override
   protected T computeValue(Class<?> type) {
      return computeValue.computeValue(type);
   }
}
