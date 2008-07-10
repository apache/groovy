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

package org.codehaus.groovy.transform;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.classgen.GeneratorContext;
import org.codehaus.groovy.control.*;
import org.codehaus.groovy.control.messages.SimpleMessage;
import org.codehaus.groovy.control.messages.WarningMessage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.*;

/**
 * This class handles the invocation of the ASTAnnotationTransformation
 * when it is encountered by a tree walk.  One instance of each exists
 * for each phase of the compilation it applies to.  Before invocation the
 * <p/>
 * {@link org.codehaus.groovy.transform.ASTTransformationCollectorCodeVisitor} will add a list
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
    private List<ASTNode[]> targetNodes;
    private Map<ASTNode, List<ASTTransformation>> transforms;
    private Map<Class<? extends ASTTransformation>, ASTTransformation> transformInstances;

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
        Map<Class<? extends ASTTransformation>, Set<ASTNode>> baseTransforms = classNode.getTransforms(phase);
        if (!baseTransforms.isEmpty()) {
            transformInstances = new HashMap<Class<? extends ASTTransformation>, ASTTransformation>();
            for (Class<? extends ASTTransformation> transformClass : baseTransforms.keySet()) {
                try {
                    transformInstances.put(transformClass, transformClass.newInstance());
                } catch (InstantiationException e) {
                    source.getErrorCollector().addError(
                            new SimpleMessage(
                                    "Could not instantiate Transformation Processor " + transformClass
                                    , //+ " declared by " + annotation.getClassNode().getName(),
                                    source));
                } catch (IllegalAccessException e) {
                    source.getErrorCollector().addError(
                            new SimpleMessage(
                                    "Could not instantiate Transformation Processor " + transformClass
                                    , //+ " declared by " + annotation.getClassNode().getName(),
                                    source));
                }
            }



            // invert the map, is now one to many
            transforms = new HashMap<ASTNode, List<ASTTransformation>>();
            for (Map.Entry<Class<? extends ASTTransformation>, Set<ASTNode>> entry : baseTransforms.entrySet()) {
                for (ASTNode node : entry.getValue()) {
                    List<ASTTransformation> list = transforms.get(node);
                    if (list == null)  {
                        list = new ArrayList<ASTTransformation>();
                        transforms.put(node, list);
                    }
                    list.add(transformInstances.get(entry.getKey()));
                }
            }

            targetNodes = new LinkedList<ASTNode[]>();

            // fist pass, collect nodes
            super.visitClass(classNode);

            // second pass, call visit on all of the collected nodes
            for (ASTNode[] node : targetNodes) {
                for (ASTTransformation snt : transforms.get(node[0])) {
                    snt.visit(node, source);
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
        //noinspection unchecked
        for (AnnotationNode annotation : (Collection<AnnotationNode>) node.getAnnotations()) {
            if (transforms.containsKey(annotation)) {
                targetNodes.add(new ASTNode[]{annotation, node});
            }
        }
    }

    public static void addPhaseOperations(CompilationUnit compilationUnit) {
        addGlobalTransforms(compilationUnit);

        compilationUnit.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
            public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                ASTTransformationCollectorCodeVisitor collector = new ASTTransformationCollectorCodeVisitor(source);
                collector.visitClass(classNode);
            }
        }, Phases.SEMANTIC_ANALYSIS);
        for (CompilePhase phase : CompilePhase.values()) {
            final ASTTransformationVisitor visitor = new ASTTransformationVisitor(phase);
            switch (phase) {
                case INITIALIZATION:
                case PARSING:
                case CONVERSION:
                    // with transform detection alone these phases are inaccessible, so don't add it
                    break;

                default:
                    compilationUnit.addPhaseOperation(new CompilationUnit.PrimaryClassNodeOperation() {
                        public void call(SourceUnit source, GeneratorContext context, ClassNode classNode) throws CompilationFailedException {
                            visitor.source = source;
                            visitor.visitClass(classNode);
                        }
                    }, phase.getPhaseNumber());
                    break;

            }
        }
    }

    public static void addGlobalTransforms(CompilationUnit compilationUnit) {
        GroovyClassLoader cuLoader = compilationUnit.getClassLoader();
        LinkedHashMap<String, URL> globalTransformNames = new LinkedHashMap<String, URL>();
        try {
            Enumeration<URL> globalServices = cuLoader.getResources("META-INF/services/org.codehaus.groovy.transform.ASTTransformation");
            while (globalServices.hasMoreElements()) {
                URL service = globalServices.nextElement();
                String className;
                BufferedReader svcIn = new BufferedReader(new InputStreamReader(service.openStream()));
                try {
                    className = svcIn.readLine();
                } catch (IOException ioe) {
                    compilationUnit.getErrorCollector().addError(new SimpleMessage(
                        "IOException reading the service definition at "
                        + service.toExternalForm() + " because of exception " + ioe.toString(), null));
                    continue;
                }
                while (className != null) {
                    if (!className.startsWith("#") && className.length() > 0) {
                        if (globalTransformNames.containsKey(className)) {
                            if (!service.equals(globalTransformNames.get(className))) {
                                compilationUnit.getErrorCollector().addWarning(
                                        WarningMessage.POSSIBLE_ERRORS,
                                        "The global transform for class " + className + " is defined in both "
                                            + globalTransformNames.get(className).toExternalForm()
                                            + " and "
                                            + service.toExternalForm()
                                            + " - the former definition will be used and the latter ignored.",
                                        null,
                                        null);
                            }

                        } else {
                            globalTransformNames.put(className, service);
                        }
                    }
                    try {
                        className = svcIn.readLine();
                    } catch (IOException ioe) {
                        compilationUnit.getErrorCollector().addError(new SimpleMessage(
                            "IOException reading the service definition at "
                            + service.toExternalForm() + " because of exception " + ioe.toString(), null));
                        //noinspection UnnecessaryContinue
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            //FIXME the warning message will NPE with what I have :(
            compilationUnit.getErrorCollector().addError(new SimpleMessage(
                "IO Exception attempting to load global transforms:" + e.getMessage(),
                null));
        }
        for (Map.Entry<String, URL> entry : globalTransformNames.entrySet()) {
            try {
                Class gTransClass = cuLoader.loadClass(entry.getKey());
                //noinspection unchecked
                GroovyASTTransformation transformAnnotation = (GroovyASTTransformation) gTransClass.getAnnotation(GroovyASTTransformation.class);
                if (transformAnnotation == null) {
                    compilationUnit.getErrorCollector().addError(new SimpleMessage(
                        "Transform Class " + entry.getKey() + " is specified as a global transform in " + entry.getValue().toExternalForm()
                        + " but it is not annotated by " + GroovyASTTransformation.class.getName(), null));
                    continue;
                }
                if (ASTTransformation.class.isAssignableFrom(gTransClass)) {
                    final ASTTransformation instance = (ASTTransformation)gTransClass.newInstance();
                    compilationUnit.addPhaseOperation(new CompilationUnit.SourceUnitOperation() {
                        public void call(SourceUnit source) throws CompilationFailedException {
                            instance.visit(new ASTNode[] {source.getAST()}, source);
                        }
                    }, transformAnnotation.phase().getPhaseNumber());
                } else {
                    compilationUnit.getErrorCollector().addError(new SimpleMessage(
                        "Transform Class " + entry.getKey() + " specified at "
                        + entry.getValue().toExternalForm() + " is not an ASTTransformation.", null));
                }
            } catch (Exception e) {
                compilationUnit.getErrorCollector().addError(new SimpleMessage(
                    "Could not instantiate global transform class " + entry.getKey() + " specified at "
                    + entry.getValue().toExternalForm() + "  because of exception " + e.toString(), null));
            }
        }
    }
}

