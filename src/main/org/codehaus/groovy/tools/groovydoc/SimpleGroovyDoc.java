/*
 *
 * Copyright 2007 Jeremy Rayner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.*;

public class SimpleGroovyDoc implements GroovyDoc {
	public SimpleGroovyDoc(String name) {
		this.name = name;
	}
	private String name;
	private String rawCommentText;
	public String name() {
		return name;
	}

	public String toString() {
		return "" + getClass() + "(" + name + ")";
	}
	public String commentText() {
		/*todo*/return rawCommentText;
	}
	public String getRawCommentText() {
		return rawCommentText;
	}
	public void setRawCommentText(String rawCommentText) {
		this.rawCommentText = rawCommentText;
	}

	
	
	
	// Methods from Comparable
	public int compareTo(Object that) {
		if (that instanceof SimpleGroovyDoc) {
			return name.compareTo(((SimpleGroovyDoc) that).name);
		} else {
			throw new ClassCastException();
		}
	}

	// Methods from GroovyDoc
//	public GroovyTag[] firstSentenceTags() {/*todo*/return null;}
//	public GroovyTag[] inlineTags() {/*todo*/return null;}
	public boolean isAnnotationType() {/*todo*/return false;}
	public boolean isAnnotationTypeElement() {/*todo*/return false;}
	public boolean isClass() {/*todo*/return false;}
	public boolean isConstructor() {/*todo*/return false;}
	public boolean isEnum() {/*todo*/return false;}
	public boolean isEnumConstant() {/*todo*/return false;}
	public boolean isError() {/*todo*/return false;}
	public boolean isException() {/*todo*/return false;}
	public boolean isField() {/*todo*/return false;}
	public boolean isIncluded() {/*todo*/return false;}
	public boolean isInterface() {/*todo*/return false;}
	public boolean isMethod() {/*todo*/return false;}
	public boolean isOrdinaryClass() {/*todo*/return false;}
//	public GroovySourcePosition position() {/*todo*/return null;}
//	public GroovySeeTag[] seeTags() {/*todo*/return null;}
//	public GroovyTag[] tags() {/*todo*/return null;}
//	public GroovyTag[] tags(String arg0) {/*todo*/return null;}

}
