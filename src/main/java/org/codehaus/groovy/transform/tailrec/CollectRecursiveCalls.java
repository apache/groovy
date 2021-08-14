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
package org.codehaus.groovy.transform.tailrec;

import org.apache.groovy.util.Maps;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;

import java.util.ArrayList;
import java.util.List;

/**
 * Collect all recursive calls within method
 */
class CollectRecursiveCalls extends CodeVisitorSupport {
	private final List<Expression> recursiveCalls = new ArrayList<>();
	private MethodNode method;

	@Override
	public void visitMethodCallExpression(MethodCallExpression call) {
		if (isRecursive(call)) {
			recursiveCalls.add(call);
		}
        super.visitMethodCallExpression(call);
    }

	@Override
	public void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
		if (isRecursive(call)) {
            recursiveCalls.add(call);
        }
		super.visitStaticMethodCallExpression(call);
	}
	
	public synchronized List<Expression> collect(MethodNode method) {
		recursiveCalls.clear();
		this.method = method;
		this.method.getCode().visit(this);
		return recursiveCalls;
	}

	private boolean isRecursive(Expression call) {
		return new RecursivenessTester().isRecursive(Maps.of("method", method, "call", call));
	}
}
