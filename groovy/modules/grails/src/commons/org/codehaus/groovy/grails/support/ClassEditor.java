/*
 * Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.codehaus.groovy.grails.support;

import java.beans.PropertyEditorSupport;

/**
 * 
 * 
 * @author Steven Devijver
 * @since Aug 8, 2005
 */
public class ClassEditor extends PropertyEditorSupport {

	private ClassLoader classLoader = null;
	
	public ClassEditor() {
		super();
	}

	public ClassEditor(Object arg0) {
		super(arg0);
	}

	public void setClassLoader(ClassLoader classLoader) {
		this.classLoader = classLoader;
	}
	
	public String getAsText() {
		return ((Class)getValue()).getName();
	}
	
	public void setAsText(String className) throws IllegalArgumentException {
		try {
			setValue(this.classLoader.loadClass(className));
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException("Could not load class [" + className + "]!");
		}
	}
}
