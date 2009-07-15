package org.codehaus.groovy.tools.gse;

import java.util.Set;

import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.control.SourceUnit;

public class DependencyTracker extends ClassCodeVisitorSupport {
    private Set<String> current;
    private SourceUnit source;
    private StringSetMap cache;

    public DependencyTracker(SourceUnit source, StringSetMap cache) {
        this.source = source;
        this.cache = cache;
    }

    private void addToCache(ClassNode node){
        if (!node.isPrimaryClassNode()) return;
        current.add(node.getName());
    }
    
    private void addToCache(ClassNode[] nodes){
        if (nodes==null) return;
        for (ClassNode node : nodes) addToCache(node);
    }

    @Override
    public void visitClass(ClassNode node) {
        Set<String> old = current;
        current = cache.get(node.getName());
        addToCache(node.getSuperClass());
        addToCache(node.getInterfaces());
        super.visitClass(node);
        current =  old;
    }
    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }
    @Override
    public void visitClassExpression(ClassExpression expression) {
        super.visitClassExpression(expression);
        addToCache(expression.getType());
    }
    @Override
    public void visitField(FieldNode node) {
        super.visitField(node);
        addToCache(node.getType());
    }
    @Override
    public void visitMethod(MethodNode node) {
        for (Parameter p : node.getParameters()) {
            addToCache(p.getType());
        }
        addToCache(node.getReturnType());
        addToCache(node.getExceptions());
        super.visitMethod(node);
    }
    @Override
    public void visitArrayExpression(ArrayExpression expression) {
        super.visitArrayExpression(expression);
        addToCache(expression.getType());
    }
    @Override
    public void visitCastExpression(CastExpression expression) {
        super.visitCastExpression(expression);
        addToCache(expression.getType());
    }
    @Override
    public void visitVariableExpression(VariableExpression expression) {
        super.visitVariableExpression(expression);
        addToCache(expression.getType());
    }
    @Override
    public void visitCatchStatement(CatchStatement statement) {
        super.visitCatchStatement(statement);
        addToCache(statement.getVariable().getType());
    }
    @Override
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode an : node.getAnnotations()) {
            addToCache(an.getClassNode());
        }
    }
    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);
        addToCache(call.getType());
    }
}
