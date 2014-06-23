package org.codehaus.groovy.reflection;

/** Abstraction for Java version dependent ClassValue implementations.
 * @see java.lang.ClassValue
 *
 * @param <T>
 */
public interface GroovyClassValue<T> {
	
	public static interface ComputeValue<T>{
		T computeValue(Class<?> type);
	}
	
	T get(Class<?> type);
	
	void remove(Class<?> type);
	
}
