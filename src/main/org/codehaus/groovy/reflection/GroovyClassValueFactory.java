package org.codehaus.groovy.reflection;

import java.lang.reflect.Constructor;

import org.codehaus.groovy.reflection.GroovyClassValue.ComputeValue;

class GroovyClassValueFactory {
	private static final Constructor groovyClassValueConstructor;

	static {
		Class groovyClassValueClass;
		try{
			Class.forName("java.lang.ClassValue");
			try{
				groovyClassValueClass = Class.forName("org.codehaus.groovy.reflection.v7.GroovyClassValueJava7");
			}catch(Exception e){
				throw new RuntimeException(e); // this should never happen, but if it does, let it propagate and be fatal
			}
		}catch(ClassNotFoundException e){
			groovyClassValueClass = GroovyClassValuePreJava7.class;
		}
		try{
			groovyClassValueConstructor = groovyClassValueClass.getConstructor(ComputeValue.class);
		}catch(Exception e){
			throw new RuntimeException(e); // this should never happen, but if it does, let it propagate and be fatal
		}
	}

	public static <T> GroovyClassValue<T> createGroovyClassValue(ComputeValue<T> computeValue){
		try {
			return (GroovyClassValue<T>) groovyClassValueConstructor.newInstance(computeValue);
		} catch (Exception e) {
			throw new RuntimeException(e); // this should never happen, but if it does, let it propagate and be fatal
		}
	}
}
