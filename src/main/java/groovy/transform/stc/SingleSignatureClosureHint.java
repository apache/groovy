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

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

import java.util.Collections;
import java.util.List;

/**
 * A simplified version of a {@link groovy.transform.stc.ClosureSignatureHint} which is suitable
 * for monomorphic closures, that is to say closures which only respond to a single signature.
 *
 * @since 2.3.0
 */
public abstract class SingleSignatureClosureHint extends ClosureSignatureHint {

    public abstract ClassNode[] getParameterTypes(final MethodNode node, final String[] options, final SourceUnit sourceUnit, final CompilationUnit unit, final ASTNode usage);

    public List<ClassNode[]> getClosureSignatures(final MethodNode node, final SourceUnit sourceUnit, final CompilationUnit compilationUnit, final String[] options, final ASTNode usage) {
        return Collections.singletonList(getParameterTypes(node, options, sourceUnit, compilationUnit, usage));
    }
}
