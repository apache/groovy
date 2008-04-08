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

package org.codehaus.groovy.control;

import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ASTSingleNodeTransformation;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.AnnotatedNode;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;

/**
 * This class handles the invocation of the ASTAnnotationTransformation
 * when it is encountered by a tree walk.  One instance of each exists
 * for each phase of the compilation it applies to.  Before invocation the
 * <p/>
 * {@link ASTTransformationCollectorCodeVisitor} will add a list
 * of annotations that this visitor should be concerned about.  All other
 * annotations are ignored, whether or not they are GroovyASTTransformation
 * annotated or not.
 * <p/>
 * A Two-pass method is used. First all candidate annotations are added to a
 * list then the transformations are called on those collected annotations.
 * This is done to avoid concurrent modification exceptions during the AST tree
 * walk and allows the transformations to alter any portion of the AST tree.
 * Hence annotations that are added in this phase will not be processed as
 * transformations.  They will only be handled in later phases (and then only
 * if the type was in the AST prior to any AST transformations being run
 * against it).
 *
 * @author Danno Ferrin (shemnon)
 */
public class ASTTransformationVisitor extends ClassCodeVisitorSupport {

    private CompilePhase phase;
    private SourceUnit source;
    private GeneratorContext context;
    private List<ASTNode[]> targetNodes;
    private Map<ASTNode, List<ASTSingleNodeTransformation>> transforms;

    private ASTTransformationVisitor(CompilePhase phase) {
        this.phase = phase;
    }

    protected SourceUnit getSourceUnit() {
        return source;
    }

    /**
     * Main loop entry.
     * <p/>
     * First, it delegates to the super visitClass so we can collect the
     * relevant annotations in an AST tree walk.
     * <p/>
     * Second, it calls the visit method on the transformation for each relevant
     * annotation found.
     *
     * @param classNode the class to visit
     */
    public void visitClass(ClassNode classNode) {
        // only descend if we have annotations to look for
        Map<ASTSingleNodeTransformation, ASTNode> baseTransforms = classNode.getSingleNodeTransforms(phase);
        if (!baseTransforms.isEmpty()) {
            // invert the map, is now one to many
            transforms = new HashMap<ASTNode, List<ASTSingleNodeTransformation>>();
            for (Map.Entry<ASTSingleNodeTransformation, ASTNode> entry : baseTransforms.entrySet()) {
                List<ASTSingleNodeTransformation> list = transforms.get(entry.getValue());
                if (list == null)  {
                    list = new ArrayList<ASTSingleNodeTransformation>();
                    transforms.put(entry.getValue(), list);
                }
                list.add(entry.getKey());
            }

            targetNodes = new LinkedList<ASTNode[]>();

            // fist pass, collect nodes
            super.visitClass(classNode);

            // second pass, call visit on all of the collected nodes
            for (ASTNode[] node : targetNodes) {
                for (ASTSingleNodeTransformation snt : transforms.get(node[0])) {
                    snt.visit(node, source, context);
                }
            }
        }
    }

    /**
     * Adds the annotation to the internal target list if a match is found.
     *
     * @param node the node to be processed
     */
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations()) {
            if (transforms.containsKey(annotation)) {
                targetNodes.add(new ASTNode[]{annotation, node});
            }
        }
    }

    /**
     * Wraps itself as a PrimaryClassNodeOperation, suitable for insertion into
     * the CompilationUnit's phase operations.
     *
     * @return the resulting PrimaryClassNodeOperation
     */
    CompilationUnit.PrimaryClassNodeOperation getPrimaryClassOperation() {
        return new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTTransformationVisitor.this.source = source;
                ASTTransformationVisitor.this.context = context;
                for (CompilationUnit.PrimaryClassNodeOperation pcno : classNode.getClassTransforms(phase)) {
                    pcno.call(source, context, classNode);
                }
                visitClass(classNode);
            }
        };
    }

    CompilationUnit.SourceUnitOperation getSourceOperation() {
        return new CompilationUnit.SourceUnitOperation() {
            public void call(SourceUnit source) throws CompilationFailedException {
                for (CompilationUnit.SourceUnitOperation suo : source.getAST().getSourceUnitTransforms(phase)) {
                    suo.call(source);
                }
            }
        };
    }


    static void addPhaseOperations(CompilationUnit compilationUnit) {
        compilationUnit.addPhaseOperation(new ASTTransformationCollectorCodeVisitor().getOperation(), Phases.SEMANTIC_ANALYSIS);
        for (CompilePhase phase : CompilePhase.values()) {
            ASTTransformationVisitor visitor = new ASTTransformationVisitor(phase);
            switch (phase) {
                case INITIALIZATION:
                case PARSING:
                case CONVERSION:
                    // with transform detection alone these phases are inaccessible, so don't add it
                    break;

                default:
                    compilationUnit.addPhaseOperation(visitor.getSourceOperation(), phase.getPhaseNumber());
                    compilationUnit.addPhaseOperation(visitor.getPrimaryClassOperation(), phase.getPhaseNumber());
                    break;

            }
        }
    }

}

