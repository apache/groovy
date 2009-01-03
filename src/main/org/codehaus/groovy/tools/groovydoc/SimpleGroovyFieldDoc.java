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

public class SimpleGroovyFieldDoc extends SimpleGroovyMemberDoc implements GroovyFieldDoc {
    private GroovyType type;
    private String constantValueExpression;

    public SimpleGroovyFieldDoc(String name) {
		super(name);
	}

    public Object constantValue() {/*todo*/return null;}

    public void setConstantValueExpression(String constantValueExpression) {
        this.constantValueExpression = constantValueExpression;
    }

    public String constantValueExpression() {
        return constantValueExpression;
    }

	public boolean isTransient() {/*todo*/return false;}

	public boolean isVolatile() {/*todo*/return false;}

    //	public GroovySerialFieldTag[] serialFieldTags() {/*todo*/return null;}

    public GroovyType type() {
        return type;
    }

    public void setType(SimpleGroovyType type) {
        this.type = type;
    }
}
