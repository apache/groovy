package org.codehaus.groovy.macro.transform;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.macro.runtime.MacroContext;
import org.codehaus.groovy.macro.runtime.MacroStub;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.util.ArrayList;
import java.util.List;

class MacroCallTransformingVisitor extends ClassCodeVisitorSupport {

    private static final ClassNode MACRO_CONTEXT_CLASS_NODE = ClassHelper.make(MacroContext.class);

    private static final ClassNode MACRO_STUB_CLASS_NODE = ClassHelper.make(MacroStub.class);

    private static final PropertyExpression MACRO_STUB_INSTANCE = new PropertyExpression(new ClassExpression(MACRO_STUB_CLASS_NODE), "INSTANCE");

    private static final String MACRO_STUB_METHOD_NAME = "macroMethod";

    private final SourceUnit sourceUnit;
    private final CompilationUnit unit;
    private final ClassLoader classLoader;

    public MacroCallTransformingVisitor(SourceUnit sourceUnit, CompilationUnit unit) {
        this.sourceUnit = sourceUnit;
        this.unit = unit;
        this.classLoader = unit.getTransformLoader();
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        super.visitMethodCallExpression(call);

        List<MethodNode> methods = MacroMethodsCache.get(classLoader).get(call.getMethodAsString());

        if (methods == null) {
            // Not a macro call
            return;
        }

        List<Expression> callArguments = InvocationWriter.makeArgumentList(call.getArguments()).getExpressions();

        ClassNode[] argumentsList = new ClassNode[callArguments.size()];

        for (int i = 0; i < callArguments.size(); i++) {
            argumentsList[i] = ClassHelper.make(callArguments.get(i).getClass());
        }

        methods = StaticTypeCheckingSupport.chooseBestMethod(MACRO_CONTEXT_CLASS_NODE, methods, argumentsList);

        for (MethodNode macroMethodNode : methods) {
            if (!(macroMethodNode instanceof ExtensionMethodNode)) {
                // TODO is it even possible?
                continue;
            }

            MethodNode macroExtensionMethodNode = ((ExtensionMethodNode) macroMethodNode).getExtensionMethodNode();

            final Class clazz;
            try {
                clazz = classLoader.loadClass(macroExtensionMethodNode.getDeclaringClass().getName());
            } catch (ClassNotFoundException e) {
                //TODO different reaction?
                continue;
            }

            MacroContext macroContext = new MacroContext(unit, sourceUnit, call);

            List<Object> macroArguments = new ArrayList<>();
            macroArguments.add(macroContext);
            macroArguments.addAll(callArguments);

            Expression result = (Expression) InvokerHelper.invokeStaticMethod(clazz, macroMethodNode.getName(), macroArguments.toArray());

            call.setObjectExpression(MACRO_STUB_INSTANCE);
            call.setMethod(new ConstantExpression(MACRO_STUB_METHOD_NAME));

            // TODO check that we reset everything here
            call.setSpreadSafe(false);
            call.setSafe(false);
            call.setImplicitThis(false);
            call.setArguments(result);
            call.setGenericsTypes(new GenericsType[0]);

            break;
        }
    }
}
