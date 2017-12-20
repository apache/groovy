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

import java.util.Collections;
import java.util.List;

/**
 * Returns the first of several candidates found.
 * This is useful if several types should be supported but only the first
 * should be the default/inferred type. Other options in the list are
 * obtained through explicitly typing the parameter(s).
 *
 * @since 2.5.0
 */
public class PickFirstResolver extends ClosureSignatureConflictResolver {
    @Override
    public List<ClassNode[]> resolve(List<ClassNode[]> candidates, ClassNode receiver, Expression arguments, ClosureExpression closure,
                                     MethodNode methodNode, SourceUnit sourceUnit, CompilationUnit compilationUnit, String[] options) {
        return Collections.singletonList(candidates.get(0));
    }
}
