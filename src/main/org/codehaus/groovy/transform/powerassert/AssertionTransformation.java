/*
 * Copyright 2008 the original author or authors.
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

package org.codehaus.groovy.transform.powerassert;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

/**
 * Entry point for the transformation of assertions. Since this is a global
 * AST transformation, it must be listed in org.codehaus.groovy.transform.ASTTransformation.
 *
 * @author Peter Niederwieser
 */
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
public class AssertionTransformation implements ASTTransformation {
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (!(nodes[0] instanceof ModuleNode))
            throw new GroovyBugError("tried to apply AssertionTransformation to " + nodes[0]);

        AssertionRewriter.rewrite(sourceUnit);
    }
}
