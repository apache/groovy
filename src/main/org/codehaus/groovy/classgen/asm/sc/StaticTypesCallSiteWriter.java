/*
 * Copyright 2003-2010 the original author or authors.
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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.classgen.asm.CallSiteWriter;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.TypeChooser;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.runtime.MetaClassHelper;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * A call site writer which is able to switch between two modes :
 * <ul>
 *     <li>dynamic</li> mode which makes use of call site caching
 *     <li>static</li> mode which produces optimized bytecode with direct method calls
 * </ul>
 * The static mode is used when a method is annotated with {@link groovy.transform.CompileStatic}.
 *
 * @author Cedric Champeau
 */
public class StaticTypesCallSiteWriter extends CallSiteWriter implements Opcodes {

    private WriterController controller;

    public StaticTypesCallSiteWriter(final StaticTypesWriterController controller) {
        super(controller);
        this.controller = controller;
    }

    @Override
    public void generateCallSiteArray() {
    }

    @Override
    public void makeCallSite(final Expression receiver, final String message, final Expression arguments, final boolean safe, final boolean implicitThis, final boolean callCurrent, final boolean callStatic) {
    }

    @Override
    public void makeGetPropertySite(final Expression receiver, final String methodName, final boolean safe, final boolean implicitThis) {
    }

    @Override
    public void makeGroovyObjectGetPropertySite(final Expression receiver, final String methodName, final boolean safe, final boolean implicitThis) {
    }


    @Override
    public void makeSiteEntry() {
    }

    @Override
    public void prepareCallSite(final String message) {
    }

    @Override
    public void makeSingleArgumentCall(final Expression receiver, final String message, final Expression arguments) {
        TypeChooser typeChooser = controller.getTypeChooser();
        ClassNode classNode = controller.getClassNode();
        ClassNode rType = ClassHelper.getWrapper(typeChooser.resolveType(receiver, classNode));
        ClassNode aType = ClassHelper.getWrapper(typeChooser.resolveType(arguments, classNode));
        if (rType.isDerivedFrom(ClassHelper.Number_TYPE) && aType.isDerivedFrom(ClassHelper.Number_TYPE)) {
            OperandStack operandStack = controller.getOperandStack();
            int m1 = operandStack.getStackLength();
            //slow Path
            prepareSiteAndReceiver(receiver, message, false, controller.getCompileStack().isLHS());
            visitBoxedArgument(arguments);
            int m2 = operandStack.getStackLength();
            MethodVisitor mv = controller.getMethodVisitor();
            mv.visitMethodInsn(INVOKESTATIC,
                    "org/codehaus/groovy/runtime/dgmimpl/NumberNumber"+ MetaClassHelper.capitalize(message),
                    message,
                    "(Ljava/lang/Number;Ljava/lang/Number;)Ljava/lang/Number;");
            controller.getOperandStack().replace(ClassHelper.Number_TYPE, m2-m1);
            return;
        }

        // todo: more cases
        throw new GroovyBugError("This method should not have been called. Please try to create a simple example reproducing this error and file" +
                "a bug report at http://jira.codehaus.org/browse/GROOVY");
    }
}
