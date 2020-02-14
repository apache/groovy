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
package org.codehaus.groovy.ast;

import org.codehaus.groovy.GroovyBugError;

import java.util.Collections;
import java.util.List;

/**
 * A {@link ClassNode} where the {@link GenericsType} information is immutable.
 */
public class ImmutableClassNode extends ClassNode {

    private volatile boolean genericsInitialized;
    private volatile boolean writeProtected;

    public ImmutableClassNode(final Class<?> c) {
        super(c);
    }

    // ASTNode overrides:

    @Override
    public void setColumnNumber(int n) {}

    @Override
    public void setLastColumnNumber(int n) {}

    @Override
    public void setLastLineNumber(int n) {}

    @Override
    public void setLineNumber(int n) {}

    @Override
    public void setNodeMetaData(Object k, Object v) {}

    @Override
    public Object putNodeMetaData(Object k, Object v) {
        return getNodeMetaData(k);
    }

    @Override
    public void setSourcePosition(ASTNode n) {}

    // AnnotatedNode overrides:

    @Override
    public void setDeclaringClass(ClassNode cn) {}

    @Override
    public void setHasNoRealSourcePosition(boolean b) {}

    @Override
    public void setSynthetic(boolean b) {}

    // ClassNode overrides:

    @Override
    public List<MethodNode> getDeclaredMethods(final String name) {
        if (lazyInitDone && !writeProtected) {
            synchronized (methods) {
                if (!writeProtected) {
                    writeProtected = true;
                    if (methods.map == null || methods.map.isEmpty()) {
                        methods.map = Collections.emptyMap();
                    } else {
                        for (Object key : methods.map.keySet()) {
                            List<MethodNode> list = methods.get(key);
                            methods.map.put(key, Collections.unmodifiableList(list));
                        }
                        methods.map = Collections.unmodifiableMap(methods.map);
                    }
                }
            }
        }
        return super.getDeclaredMethods(name);
    }

    @Override
    public void setAnnotated(boolean b) {}

    @Override
    protected void setCompileUnit(CompileUnit cu) {}

    @Override
    public void setEnclosingMethod(MethodNode mn) {}

    @Override
    public void setGenericsPlaceHolder(boolean b) {}

    //public void setInterfaces(ClassNode[] cn) {}

    @Override
    public void setModifiers(int bf) {}

    @Override
    public void setModule(ModuleNode mn) {}

    @Override
    public String setName(String s) {
        return getName();
    }

    //public void setRedirect(ClassNode cn) {}

    @Override
    public void setSuperClass(ClassNode cn) {}

    @Override
    public void setScript(boolean b) {}

    @Override
    public void setScriptBody(boolean b) {}

    @Override
    public void setStaticClass(boolean b) {}

    @Override
    public void setSyntheticPublic(boolean b) {}

    //public void setUnresolvedSuperClass(ClassNode cn) {}

    @Override
    public void setUsingGenerics(boolean b) {}

    @Override
    public void setGenericsTypes(GenericsType[] genericsTypes) {
        if (genericsInitialized && genericsTypes != super.getGenericsTypes()) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + getName());
        }
        if (genericsTypes != null) {
            GenericsType[] immutable = new GenericsType[genericsTypes.length];
            for (int i = 0, n = genericsTypes.length; i < n; i += 1) {
                immutable[i] = new ImmutableGenericsType(genericsTypes[i], getName());
            }
            genericsTypes = immutable;
        }
        super.setGenericsTypes(genericsTypes);
        genericsInitialized = true;
    }

    static class ImmutableGenericsType extends GenericsType {

        ImmutableGenericsType(final GenericsType delegate, final String typeName) {
            super(delegate.getUpperBounds(), delegate.getLowerBound());
            this.typeName = typeName;
            super.setName(delegate.getName());
            super.setType(delegate.getType());
            super.setResolved(delegate.isResolved());
            super.setWildcard(delegate.isWildcard());
            super.setPlaceholder(delegate.isPlaceholder());
        }

        private final String typeName;

        @Override
        public void setType(ClassNode cn) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }

        @Override
        public void setPlaceholder(boolean b) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }

        @Override
        public void setResolved(boolean b) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }

        @Override
        public void setName(String s) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }

        @Override
        public void setWildcard(boolean b) {
            throw new GroovyBugError("Attempt to change an immutable Groovy class: " + typeName);
        }
    }
}
