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
package org.codehaus.groovy.transform;

import groovy.transform.Sortable;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS;
import static org.codehaus.groovy.transform.StubberSupport.tagAsStub;

/**
 * Adds a minimal {@code Comparable} surface for {@link Sortable} classes in an
 * earlier phase so joint compilation can evolve independently from full Sortable codegen.
 */
@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class SortableASTStubber extends AbstractASTTransformation {

	private static final ClassNode MY_TYPE = make(Sortable.class);

	@Override
	public void visit(ASTNode[] nodes, SourceUnit source) {
		init(nodes, source);
		AnnotationNode annotation = (AnnotationNode) nodes[0];
		AnnotatedNode parent = (AnnotatedNode) nodes[1];
		if (!MY_TYPE.equals(annotation.getClassNode())) return;

		if (parent instanceof ClassNode classNode && !classNode.isInterface()) {
			tagAsStub(SortableASTTransformation.addComparableSurface(classNode, returnS(constX(0))));
		}
	}
}
