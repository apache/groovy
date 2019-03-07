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
package org.codehaus.groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Represents method nodes which are used by the static type checker to virtually add methods
 * coming from {@link org.codehaus.groovy.runtime.DefaultGroovyMethods DGM-like} methods.
 */
public class ExtensionMethodNode extends MethodNode {
    private final MethodNode extensionMethodNode;
    private final boolean isStaticExtension; // true if it's a static method
    
    public ExtensionMethodNode(
            MethodNode extensionMethodNode,
            String name, int modifiers, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code, boolean isStaticExtension) {
        super(name, modifiers, returnType, parameters, exceptions, code);
        this.extensionMethodNode = extensionMethodNode;
        this.isStaticExtension = isStaticExtension;
    }

    public ExtensionMethodNode(
            MethodNode extensionMethodNode,
            String name, int modifiers, ClassNode returnType, Parameter[] parameters, ClassNode[] exceptions, Statement code) {
        this(extensionMethodNode, name, modifiers, returnType, parameters, exceptions, code, false);
    }

    public MethodNode getExtensionMethodNode() {
        return extensionMethodNode;
    }

    public boolean isStaticExtension() {
        return isStaticExtension;
    }
}
