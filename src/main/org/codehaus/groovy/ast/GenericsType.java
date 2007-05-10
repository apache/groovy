/*
 * Copyright 2005 Jochen Theodorou
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
 *
 */
 
package org.codehaus.groovy.ast;

/**
 * This class is used to describe generic type signatures
 * for ClassNodes. 
 * @author Jochen Theodorou
 * @see ClassNode
 */
public class GenericsType extends ASTNode {
    ClassNode upperBound;
    ClassNode type;
    String name;
    
    public GenericsType(ClassNode type, ClassNode upperBound) {
        this.type = type;
        this.name = type.getName();
        this.upperBound = upperBound;
    }
    
    public GenericsType(ClassNode basicType) {
        this(basicType,null);
    }

    public ClassNode getType() {
        return type;
    }
    
    public void setType(ClassNode type) {
        this.type = type;
    }
    
    public String toString() {
        String ret = name;
        if (upperBound!=null) ret += " extends "+upperBound.toString();
        return ret;
    }

    public ClassNode getUpperBound() {
        return upperBound;
    }
    
    public String getName(){
        return name;
    }
}
