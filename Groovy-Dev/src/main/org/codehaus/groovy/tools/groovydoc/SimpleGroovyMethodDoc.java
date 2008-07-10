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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.*;

import java.util.ArrayList;
import java.util.List;

public class SimpleGroovyMethodDoc extends SimpleGroovyExecutableMemberDoc implements GroovyMethodDoc {
	public SimpleGroovyMethodDoc(String name) {
		this(name, new ArrayList());
	}
	public SimpleGroovyMethodDoc(String name, List links) {
		super(name, links);
	}
	private GroovyType returnType;
	public GroovyType returnType() {return returnType;}

    // TODO need returnType.qualifiedTypeName() here

    // TODO - is this needed, I'm not sure this is the best way?  public String returnTypeName() {return getDocUrl(returnType.typeName());}

    public boolean isReturnTypeAvailable() {
        if (returnType != null) {
            if (returnType instanceof SimpleGroovyClassDoc) {
                return true;
            }
        }
        return false;
    }
    
    public void setReturnType(GroovyType returnType) {
		this.returnType = returnType;
	}
	

	public boolean isAbstract() {/*todo*/return false;}
	public GroovyClassDoc overriddenClass() {/*todo*/return null;}
	public GroovyMethodDoc overriddenMethod() {/*todo*/return null;}
	public GroovyType overriddenType() {/*todo*/return null;}
	public boolean overrides(GroovyMethodDoc arg0) {/*todo*/return false;}
}
