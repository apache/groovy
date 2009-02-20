/*
 * Copyright 2003-2008 the original author or authors.
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

package groovy.grape;

import groovy.lang.Grab;
import groovy.lang.Grapes;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Danno
 * Date: Jan 18, 2008
 * Time: 9:48:57 PM
 */

@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
public class GrabAnnotationTransformation extends ClassCodeVisitorSupport implements ASTTransformation {
    private static final String GRAB_CLASS_NAME = Grab.class.getName();
    private static final String GRAB_DOT_NAME = GRAB_CLASS_NAME.substring(GRAB_CLASS_NAME.lastIndexOf("."));
    private static final String GRAB_SHORT_NAME = GRAB_DOT_NAME.substring(1);

    private static final String GRAPES_CLASS_NAME = Grapes.class.getName();
    private static final String GRAPES_DOT_NAME = GRAPES_CLASS_NAME.substring(GRAPES_CLASS_NAME.lastIndexOf("."));
    private static final String GRAPES_SHORT_NAME = GRAPES_DOT_NAME.substring(1);

    boolean allowShortGrab;
    Set<String> grabAliases;
    List<AnnotationNode> grabAnnotations;

    boolean allowShortGrapes;
    Set<String> grapesAliases;
    List<AnnotationNode> grapesAnnotations;

    SourceUnit sourceUnit;

    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public void visit(ASTNode[] nodes, SourceUnit source) {
        sourceUnit = source;

        ModuleNode mn = (ModuleNode) nodes[0];

        allowShortGrab = true;
        allowShortGrapes = true;
        grabAliases = new HashSet();
        grapesAliases = new HashSet();
        Iterator i = mn.getImports().iterator();
        while (i.hasNext()) {
            ImportNode im = (ImportNode) i.next();
            String alias = im.getAlias();
            String className = im.getClassName();
            if ((className.endsWith(GRAB_DOT_NAME) && ((alias == null) || (alias.length() == 0)))
                || (GRAB_CLASS_NAME.equals(alias)))
            {
                allowShortGrab = false;
            } else if (GRAB_CLASS_NAME.equals(className)) {
                grabAliases.add(im.getAlias());
            }
            if ((className.endsWith(GRAPES_DOT_NAME) && ((alias == null) || (alias.length() == 0)))
                || (GRAPES_CLASS_NAME.equals(alias)))
            {
                allowShortGrapes = false;
            } else if (GRAPES_CLASS_NAME.equals(className)) {
                grapesAliases.add(im.getAlias());
            }
        }

        List<Map<String,Object>> grabMaps = new ArrayList();

        for (ClassNode classNode : (List<ClassNode>) sourceUnit.getAST().getClasses()) {
            grabAnnotations = new ArrayList<AnnotationNode>();
            grapesAnnotations = new ArrayList<AnnotationNode>();

            visitClass(classNode);

            ClassNode grapeClassNode = new ClassNode(Grape.class);

            if (!grapesAnnotations.isEmpty()) {
                for (int j = 0; j < grapesAnnotations.size(); j++) {
                    AnnotationNode node = grapesAnnotations.get(j);
                    Expression init = node.getMember("initClass");

                    Expression value = node.getMember("value");
                    if (value instanceof ListExpression) {
                        for (Object o : ((ListExpression)value).getExpressions()) {
                            if (o instanceof AnnotationConstantExpression) {
                                if (((AnnotationConstantExpression)o).getValue() instanceof AnnotationNode) {
                                    AnnotationNode annotation = (AnnotationNode) ((AnnotationConstantExpression)o).getValue();
                                    if ((init != null) && (annotation.getMember("initClass") != null)) {
                                        annotation.setMember("initClass", init);
                                    }
                                    String name = annotation.getClassNode().getName();
                                    if ((GRAB_CLASS_NAME.equals(name))
                                        || (allowShortGrab && GRAB_SHORT_NAME.equals(name))
                                        || (grabAliases.contains(name)))
                                    {
                                        grabAnnotations.add(annotation);
                                    }
                                }
                            }
                        }
                    }
                    // don't worry if it's not a ListExpression, or AnnotationConstant, etc.
                    // the rest of GroovyC will flag it as a syntax error later, so we don't
                    // need to raise the error ourselves
                }
            }

            if (!grabAnnotations.isEmpty()) {
                grabAnnotationLoop:
                for (int j = 0; j < grabAnnotations.size(); j++) {
                    AnnotationNode node = grabAnnotations.get(j);
                    Map<String,Object> grabMap = new HashMap();
                    for (String s : new String[] {"group", "module", "version"}) {
                        if (node.getMember(s) == null) {
                            addError("The missing attribute \"" + s + "\" is required in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                            continue grabAnnotationLoop;
                        }
                    }
                    grabMap.put("group", ((ConstantExpression)node.getMember("group")).getValue());
                    grabMap.put("module", ((ConstantExpression)node.getMember("module")).getValue());
                    grabMap.put("version", ((ConstantExpression)node.getMember("version")).getValue());
                    grabMaps.add(grabMap);

                    if ((node.getMember("initClass") == null)
                        || (node.getMember("initClass") == ConstantExpression.TRUE))
                    {
                        List grabInitializers = new ArrayList();


                        // add Grape.grab([group:group, module:module, version:version])
                        MapExpression me = new MapExpression();
                        me.addMapEntryExpression(new ConstantExpression("group"),node.getMember("group"));
                        me.addMapEntryExpression(new ConstantExpression("module"),node.getMember("module"));
                        me.addMapEntryExpression(new ConstantExpression("version"),node.getMember("version"));

                        grabInitializers.add(new ExpressionStatement(
                                new StaticMethodCallExpression(
                                    grapeClassNode,
                                    "grab",
                                    new ArgumentListExpression(me))));

                        // insert at beginning so we have the classloader set up before the class is called
                        classNode.addStaticInitializerStatements(grabInitializers, true);
                    }
                }
            }


        }
        if (!grabMaps.isEmpty()) {
            Map basicArgs = new HashMap();
            basicArgs.put("classLoader", sourceUnit.getClassLoader());

            Grape.grab(basicArgs, grabMaps.toArray(new Map[grabMaps.size()]));
        }
    }

    protected void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        super.visitConstructorOrMethod(node, isConstructor);

        // this should be pushed into the super class...
        for (Parameter param : node.getParameters()) {
            visitAnnotations(param);
        }
    }

    /**
     * Adds the annotation to the internal target list if a match is found
     * @param node
     */
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        Iterator i = node.getAnnotations().iterator();
        while (i.hasNext()) {
            AnnotationNode an = (AnnotationNode) i.next();
            String name = an.getClassNode().getName();
            if ((GRAB_CLASS_NAME.equals(name))
                || (allowShortGrab && GRAB_SHORT_NAME.equals(name))
                || (grabAliases.contains(name)))
            {
                grabAnnotations.add(an);
            }
            if ((GRAPES_CLASS_NAME.equals(name))
                || (allowShortGrapes && GRAPES_SHORT_NAME.equals(name))
                || (grapesAliases.contains(name)))
            {
                grapesAnnotations.add(an);
            }
        }
    }

}
