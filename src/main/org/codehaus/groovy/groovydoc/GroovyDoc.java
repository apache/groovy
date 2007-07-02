/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.groovydoc;

public interface GroovyDoc extends Comparable {

	public String commentText();
//	public GroovyTag[] firstSentenceTags();
	public String getRawCommentText();
//	public GroovyTag[] inlineTags();
	public boolean isAnnotationType();
	public boolean isAnnotationTypeElement();
	public boolean isClass();
	public boolean isConstructor();
	public boolean isEnum();
	public boolean isEnumConstant();
	public boolean isError();
	public boolean isException();
	public boolean isField();
	public boolean isIncluded();
	public boolean isInterface();
	public boolean isMethod();
	public boolean isOrdinaryClass();
	public String name();
//	public GroovySourcePosition position();
//	public GroovySeeTag[] seeTags();
	public void setRawCommentText(String arg0);
//	public GroovyTag[] tags();
//	public GroovyTag[] tags(String arg0);
	
	public String firstSentenceCommentText();
}
