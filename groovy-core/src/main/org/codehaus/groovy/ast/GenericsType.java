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

package org.codehaus.groovy.ast;

/**
 * This class is used to describe generic type signatures
 * for ClassNodes. 
 * @author Jochen Theodorou
 * @see ClassNode
 */
public class GenericsType extends ASTNode {
    private final ClassNode[] upperBounds;
    private final ClassNode lowerBound;
    private ClassNode type;
    private String name;
    private boolean placeholder;
    private boolean resolved;
    private boolean wildcard;
    
    public GenericsType(ClassNode type, ClassNode[] upperBounds, ClassNode lowerBound) {
        this.type = type;
        this.name = type.getName();
        this.upperBounds = upperBounds;
        this.lowerBound = lowerBound;
        placeholder = false;
        resolved = false;
    }
    
    public GenericsType(ClassNode basicType) {
        this(basicType,null,null);
    }

    public ClassNode getType() {
        return type;
    }
    
    public void setType(ClassNode type) {
        this.type = type;
    }
    
    public String toString() {
        String ret = name;
        if (upperBounds!=null) {
            ret += " extends ";
            for (int i = 0; i < upperBounds.length; i++) {
                ret += upperBounds[i].toString();
                if (i+1<upperBounds.length) ret += " & ";
            }
        } else if (lowerBound!=null) {
            ret += " super "+lowerBound;
        }
        return ret;
    }

    public ClassNode[] getUpperBounds() {
        return upperBounds;
    }
    
    public String getName(){
        return name;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
    }
    
    public boolean isResolved() {
        return resolved || placeholder;
    }
    
    public void setResolved(boolean res) {
        resolved = res;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public void setWildcard(boolean wildcard) {
        this.wildcard = wildcard;
    }
    
    public ClassNode getLowerBound() {
        return lowerBound;
    }
}
