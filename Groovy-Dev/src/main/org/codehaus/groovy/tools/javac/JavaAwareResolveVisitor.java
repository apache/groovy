/*
 * Copyright 2003-2007 the original author or authors.
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

package org.codehaus.groovy.tools.javac;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ResolveVisitor;

public class JavaAwareResolveVisitor extends ResolveVisitor {

    public JavaAwareResolveVisitor(CompilationUnit cu) {
        super(cu);
    }
    
    protected void visitClassCodeContainer(Statement code) {
        // do nothing here, leave it to the normal resolving
    }
    
    protected void addError(String msg, ASTNode expr) {
        // do nothing here, leave it to the normal resolving        
    }
}
