/*
 * Copyright 2003-2009 the original author or authors.
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

import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.transform.StaticTypesTransformation;

public class StaticInvocationWriter extends InvocationWriter {
    public StaticInvocationWriter(WriterController wc) {
        super(wc);
    }

    @Override
    public void writeInvokeConstructor(final ConstructorCallExpression call) {
        ConstructorNode cn = (ConstructorNode) call.getNodeMetaData(StaticTypesTransformation.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (cn==null) {
            super.writeInvokeConstructor(call);
            return;
        }

        String ownerDescriptor = prepareConstructorCall(cn);
        TupleExpression args = makeArgumentList(call.getArguments());
        loadArguments(args.getExpressions(), cn.getParameters());
        finnishConstructorCall(cn, ownerDescriptor, args.getExpressions().size());

    }
}
