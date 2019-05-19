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
package groovy.transform.stc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

/**
 * If multiple candidate signatures are found after applying type hints,
 * a conflict resolver can attempt to resolve the ambiguity.
 *
 * @since 2.5.0
 */
public class ClosureSignatureConflictResolver {
    /**
     *
     * @param candidates the list of signatures as determined after applying type hints and performing initial inference calculations
     * @param receiver the receiver the method is being called on
     * @param arguments the arguments for the closure
     * @param closure the closure expression under analysis
     * @param methodNode the method for which a {@link groovy.lang.Closure} parameter was annotated with {@link ClosureParams}
     * @param sourceUnit the source unit of the file being compiled
     * @param compilationUnit the compilation unit of the file being compiled
     * @param options the options, corresponding to the {@link ClosureParams#options()} found on the annotation
     * @return a non-null list of signatures, where a signature corresponds to an array of class nodes, each of them matching a parameter. A list with more than one element indicates that all ambiguities haven't yet been resolved.
     */
    public List<ClassNode[]> resolve(List<ClassNode[]> candidates, ClassNode receiver, Expression arguments, ClosureExpression closure,
                                     MethodNode methodNode, SourceUnit sourceUnit, CompilationUnit compilationUnit, String[] options) {
        // do nothing by default
        return candidates;
    }
}
