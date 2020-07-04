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
package org.codehaus.groovy.transform;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.control.SourceUnit;

/**
 * This class is instantiated and invoked when an AST transformation is
 * activated. For Global AST Transformations, this interface is called once per SourceUnit, which is usually a
 * Groovy source file. For Local AST Transformations, this interface is invoked once every time the Local annotation
 * marker is encountered.
 * <p>
 * You must annotate this class with {@link GroovyASTTransformation} so that Groovy knows which
 * {@link org.codehaus.groovy.control.CompilePhase} to run in.
 *
 * @see GroovyASTTransformation
 */
public interface ASTTransformation {

    /**
     * The method is invoked when an AST Transformation is active. For local transformations, it is invoked once
     * each time the local annotation is encountered. For global transformations, it is invoked once for every source
     * unit, which is typically a source file.
     *
     * @param nodes The ASTnodes when the call was triggered. Element 0 is the AnnotationNode that triggered this
     *      annotation to be activated. Element 1 is the AnnotatedNode decorated, such as a MethodNode or ClassNode. For
     *      global transformations it is usually safe to ignore this parameter.
     * @param source The source unit being compiled. The source unit may contain several classes. For global transformations,
     *      information about the AST can be retrieved from this object. 
     */
    void visit(ASTNode[] nodes, SourceUnit source);
}
