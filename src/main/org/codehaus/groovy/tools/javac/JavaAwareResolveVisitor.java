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
