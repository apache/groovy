/*
 * Copyright 2003-2011 the original author or authors.
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

import java.util.HashSet;
import java.util.Set;

/**
 * This class is used to describe generic type signatures for ClassNodes.
 *
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
        this.name = type.isGenericsPlaceHolder() ? type.getUnresolvedName() : type.getName();
        this.upperBounds = upperBounds;
        this.lowerBound = lowerBound;
        placeholder = type.isGenericsPlaceHolder();
        resolved = false;
    }

    public GenericsType(ClassNode basicType) {
        this(basicType, null, null);
    }

    public ClassNode getType() {
        return type;
    }

    public void setType(ClassNode type) {
        this.type = type;
    }

    public String toString() {
        Set<String> visited = new HashSet<String>();
        return toString(visited);
    }

    private String toString(Set<String> visited) {
        if (placeholder) visited.add(name);
        String ret = (type == null || placeholder || wildcard) ? name : genericsBounds(type, visited);
        if (upperBounds != null) {
            ret += " extends ";
            for (int i = 0; i < upperBounds.length; i++) {
                ret += genericsBounds(upperBounds[i], visited);
                if (i + 1 < upperBounds.length) ret += " & ";
            }
        } else if (lowerBound != null) {
            ret += " super " + genericsBounds(lowerBound, visited);
        }
        return ret;
    }

    private String genericsBounds(ClassNode theType, Set<String> visited) {

        StringBuilder ret = new StringBuilder();

        if (theType.isArray()) {
            ret.append(theType.getComponentType().getName());
            ret.append("[]");
        } else if (theType.redirect() instanceof InnerClassNode) {
            InnerClassNode innerClassNode = (InnerClassNode) theType.redirect();
            String parentClassNodeName = innerClassNode.getOuterClass().getName();
            ret.append(genericsBounds(innerClassNode.getOuterClass(), new HashSet<String>()));
            ret.append(".");
            String typeName = theType.getName();
            ret.append(typeName.substring(parentClassNodeName.length() + 1));
        } else {
            ret.append(theType.getName());
        }

        GenericsType[] genericsTypes = theType.getGenericsTypes();
        if (genericsTypes == null || genericsTypes.length == 0)
            return ret.toString();

        // TODO instead of catching Object<T> here stop it from being placed into type in first place
        if (genericsTypes.length == 1 && genericsTypes[0].isPlaceholder() && theType.getName().equals("java.lang.Object")) {
            return genericsTypes[0].getName();
        }

        ret.append("<");
        for (int i = 0; i < genericsTypes.length; i++) {
            if (i != 0) ret.append(", ");

            GenericsType type = genericsTypes[i];
            if (type.isPlaceholder() && visited.contains(type.getName())) {
                ret.append(type.getName());
            }
            else {
                ret.append(type.toString(visited));
            }
        }
        ret.append(">");

        return ret.toString();
    }

    public ClassNode[] getUpperBounds() {
        return upperBounds;
    }

    public String getName() {
        return name;
    }

    public boolean isPlaceholder() {
        return placeholder;
    }

    public void setPlaceholder(boolean placeholder) {
        this.placeholder = placeholder;
        type.setGenericsPlaceHolder(placeholder);
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
