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
package org.codehaus.groovy.classgen.asm;

/**
 * @author <a href="mailto:blackdrag@gmx.org">Jochen "blackdrag" Theodorou</a>
 */
public interface BinaryExpressionWriter {

    public boolean write(int operation, boolean simulate);

    public boolean arrayGet(int operation, boolean simulate);
    /*
     *      left.visit(controller.getAcg());
            operandStack.doGroovyCast(BinaryExpressionMultiTypeDispatcher.getType(left,controller.getClassNode()));
            right.visit(controller.getAcg());
            operandStack.doGroovyCast(ClassHelper.int_TYPE);
            MethodVisitor mv = controller.getMethodVisitor();
            intArrayGet.call(mv);
            operandStack.replace(ClassHelper.int_TYPE,2);
     */

    public boolean arraySet(boolean simulate);
    /*
     * intArraySet.call(mv);
     */

}
