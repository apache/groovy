/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.reflection;

import org.codehaus.groovy.reflection.GroovyClassValue.ComputeValue;

import java.lang.reflect.Constructor;

class GroovyClassValueFactory {
	/**
	 * This flag is introduced as a (hopefully) temporary workaround for a JVM bug, that is to say that using
	 * ClassValue prevents the classes and classloaders from being unloaded.
	 * See https://bugs.openjdk.java.net/browse/JDK-8136353
	 * This issue does not exist on IBM Java (J9) so use ClassValue by default on that JVM. 
	 */
	private final static boolean USE_CLASSVALUE = Boolean.valueOf(System.getProperty("groovy.use.classvalue", "IBM J9 VM".equals(System.getProperty("java.vm.name"))?"true":"false"));

	private static final Constructor groovyClassValueConstructor;

	static {
		Class groovyClassValueClass;
		if (USE_CLASSVALUE) {
			try {
				Class.forName("java.lang.ClassValue");
				try {
					groovyClassValueClass = Class.forName("org.codehaus.groovy.reflection.v7.GroovyClassValueJava7");
				} catch (Exception e) {
					throw new RuntimeException(e); // this should never happen, but if it does, let it propagate and be fatal
				}
			} catch (ClassNotFoundException e) {
				groovyClassValueClass = GroovyClassValuePreJava7.class;
			}
		} else {
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
