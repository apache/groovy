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
package org.codehaus.groovy.tools.javac;

import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.ast.ClassNode;

import javax.tools.SimpleJavaFileObject;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

/**
 * Represents a Java source file in memory to compile
 * @since 3.0.0
 */
public class MemJavaFileObject extends SimpleJavaFileObject {
    private final String className;
    private final String src;

    /**
     * Construct a MemJavaFileObject instance with given class node and source code
     *
     * @param classNode  the class node
     * @param src  the source code
     */
    public MemJavaFileObject(ClassNode classNode, String src) {
        this(classNode.getName(), src);
    }

    /**
     * Construct a MemJavaFileObject instance with given class name and source code
     *
     * @param className the class name
     * @param src the source code
     */
    public MemJavaFileObject(String className, String src) {
        super(createURI(className), Kind.SOURCE);
        this.className = className;
        this.src = src;
    }

    private static URI createURI(String className) {
        try {
            return new URI("string:///" + className.replace('.', '/') + Kind.SOURCE.extension);
        } catch (URISyntaxException e) {
            throw new GroovyRuntimeException(e);
        }
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
        return src;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MemJavaFileObject)) return false;
        MemJavaFileObject that = (MemJavaFileObject) o;
        return Objects.equals(className, that.className);
    }

    @Override
    public int hashCode() {
        return Objects.hash(className);
    }

    @Override
    public String toString() {
        return "MemJavaFileObject{" +
                "className=" + className +
                '}';
    }
}
