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

package groovy.grape;

import groovy.lang.Grab;
import groovy.lang.Grapes;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.*;

/**
 * Transformation for declarative dependency management.
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
        grabAliases = new HashSet<String>();
        grapesAliases = new HashSet<String>();
        for (ImportNode im : mn.getImports()) {
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

        List<Map<String,Object>> grabMaps = new ArrayList<Map<String,Object>>();

        for (ClassNode classNode : sourceUnit.getAST().getClasses()) {
            grabAnnotations = new ArrayList<AnnotationNode>();
            grapesAnnotations = new ArrayList<AnnotationNode>();

            visitClass(classNode);

            ClassNode grapeClassNode = new ClassNode(Grape.class);

            if (!grapesAnnotations.isEmpty()) {
                for (AnnotationNode node : grapesAnnotations) {
                    Expression init = node.getMember("initClass");
                    Expression value = node.getMember("value");
                    if (value instanceof ListExpression) {
                        for (Object o : ((ListExpression)value).getExpressions()) {
                            if (o instanceof ConstantExpression) {
                                extractGrab(init, (ConstantExpression) o);
                            }
                        }
                    } else if (value instanceof ConstantExpression) {
                        extractGrab(init, (ConstantExpression) value);
                    }
                    // don't worry if it's not a ListExpression, or AnnotationConstant, etc.
                    // the rest of GroovyC will flag it as a syntax error later, so we don't
                    // need to raise the error ourselves
                }
            }

            if (!grabAnnotations.isEmpty()) {
                grabAnnotationLoop:
                for (AnnotationNode node : grabAnnotations) {
                    Map<String, Object> grabMap = new HashMap<String, Object>();
                    checkForConvenienceForm(node);
                    for (String s : new String[]{"group", "module", "version", "classifier"}) {
                        Expression member = node.getMember(s);
                        if (member == null && !s.equals("classifier")) {
                            addError("The missing attribute \"" + s + "\" is required in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                            continue grabAnnotationLoop;
                        } else if (member != null && !(member instanceof ConstantExpression)) {
                            addError("Attribute \"" + s + "\" has value " + member.getText() + " but should be an inline constant in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                            continue grabAnnotationLoop;
                        }
                    }
                    grabMap.put("group", ((ConstantExpression)node.getMember("group")).getValue());
                    grabMap.put("module", ((ConstantExpression)node.getMember("module")).getValue());
                    grabMap.put("version", ((ConstantExpression)node.getMember("version")).getValue());
                    if (node.getMember("classifier") != null)
                        grabMap.put("classifier", ((ConstantExpression)node.getMember("classifier")).getValue());
                    grabMaps.add(grabMap);

                    if ((node.getMember("initClass") == null)
                        || (node.getMember("initClass") == ConstantExpression.TRUE))
                    {
                        List<Statement> grabInitializers = new ArrayList<Statement>();

                        // add Grape.grab([group:group, module:module, version:version, classifier:classifier])
                        MapExpression me = new MapExpression();
                        me.addMapEntryExpression(new ConstantExpression("group"),node.getMember("group"));
                        me.addMapEntryExpression(new ConstantExpression("module"),node.getMember("module"));
                        me.addMapEntryExpression(new ConstantExpression("version"),node.getMember("version"));
                        if (node.getMember("classifier") != null)
                            me.addMapEntryExpression(new ConstantExpression("classifier"),node.getMember("classifier"));

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
            Map<String, Object> basicArgs = new HashMap<String, Object>();
            basicArgs.put("classLoader", sourceUnit.getClassLoader());

            try {
                Grape.grab(basicArgs, grabMaps.toArray(new Map[grabMaps.size()]));
            } catch (RuntimeException re) {
                // Decided against syntax exception since this is not a syntax error.
                // The down side is we lose line number information for the offending
                // @Grab annotation.
                source.addException(re);
            }
        }
    }

    private void checkForConvenienceForm(AnnotationNode node) {
        Object val = node.getMember("value");
        if (val == null || !(val instanceof ConstantExpression)) return;
        Object allParts = ((ConstantExpression)val).getValue();
        if (!(allParts instanceof String)) return;
        String allstr = (String) allParts;
        if (allstr.contains(":")) {
            String[] parts = allstr.split(":");
            if (parts.length > 4) return;
            if (parts.length > 3) node.addMember("classifier", new ConstantExpression(parts[3]));
            if (parts.length > 2) node.addMember("version", new ConstantExpression(parts[2]));
            else node.addMember("version", new ConstantExpression("*")); // TODO '*' default in @Grab not working?
            node.addMember("module", new ConstantExpression(parts[1]));
            node.addMember("group", new ConstantExpression(parts[0]));
        }
    }

    private void extractGrab(Expression init, ConstantExpression ce) {
        if (ce.getValue() instanceof AnnotationNode) {
            AnnotationNode annotation = (AnnotationNode) ce.getValue();
            if ((init != null) && (annotation.getMember("initClass") != null)) {
                annotation.setMember("initClass", init);
            }
            String name = annotation.getClassNode().getName();
            if ((GRAB_CLASS_NAME.equals(name))
                    || (allowShortGrab && GRAB_SHORT_NAME.equals(name))
                    || (grabAliases.contains(name))) {
                grabAnnotations.add(annotation);
            }
        }
    }

    /**
     * Adds the annotation to the internal target list if a match is found.
     *
     * @param node the AST node we are processing
     */
    public void visitAnnotations(AnnotatedNode node) {
        super.visitAnnotations(node);
        for (AnnotationNode an : node.getAnnotations()) {
            String name = an.getClassNode().getName();
            if ((GRAB_CLASS_NAME.equals(name))
                    || (allowShortGrab && GRAB_SHORT_NAME.equals(name))
                    || (grabAliases.contains(name))) {
                grabAnnotations.add(an);
            }
            if ((GRAPES_CLASS_NAME.equals(name))
                    || (allowShortGrapes && GRAPES_SHORT_NAME.equals(name))
                    || (grapesAliases.contains(name))) {
                grapesAnnotations.add(an);
            }
        }
    }

}
