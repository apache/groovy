/*
 * Copyright 2003-2012 the original author or authors.
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
import groovy.lang.GrabConfig;
import groovy.lang.GrabExclude;
import groovy.lang.GrabResolver;
import groovy.lang.Grapes;
import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ImportNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.tools.GrapeUtil;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.ASTTransformationVisitor;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Transformation for declarative dependency management.
 */
@GroovyASTTransformation(phase=CompilePhase.CONVERSION)
public class GrabAnnotationTransformation extends ClassCodeVisitorSupport implements ASTTransformation, CompilationUnitAware {
    private static final String GRAB_CLASS_NAME = Grab.class.getName();
    private static final String GRAB_DOT_NAME = GRAB_CLASS_NAME.substring(GRAB_CLASS_NAME.lastIndexOf("."));
    private static final String GRAB_SHORT_NAME = GRAB_DOT_NAME.substring(1);

    private static final String GRABEXCLUDE_CLASS_NAME = GrabExclude.class.getName();
    private static final String GRABEXCLUDE_DOT_NAME = dotName(GRABEXCLUDE_CLASS_NAME);
    private static final String GRABEXCLUDE_SHORT_NAME = shortName(GRABEXCLUDE_DOT_NAME);

    private static final String GRABCONFIG_CLASS_NAME = GrabConfig.class.getName();
    private static final String GRABCONFIG_DOT_NAME = dotName(GRABCONFIG_CLASS_NAME);
    private static final String GRABCONFIG_SHORT_NAME = shortName(GRABCONFIG_DOT_NAME);

    private static final String GRAPES_CLASS_NAME = Grapes.class.getName();
    private static final String GRAPES_DOT_NAME = dotName(GRAPES_CLASS_NAME);
    private static final String GRAPES_SHORT_NAME = shortName(GRAPES_DOT_NAME);

    private static final String GRABRESOLVER_CLASS_NAME = GrabResolver.class.getName();
    private static final String GRABRESOLVER_DOT_NAME = dotName(GRABRESOLVER_CLASS_NAME);
    private static final String GRABRESOLVER_SHORT_NAME = shortName(GRABRESOLVER_DOT_NAME);

    private static final ClassNode THREAD_CLASSNODE = ClassHelper.make(Thread.class);
    private static final List<String> GRABEXCLUDE_REQUIRED = Arrays.asList("group", "module");
    private static final List<String> GRABRESOLVER_REQUIRED = Arrays.asList("name", "root");
    private static final List<String> GRAB_REQUIRED = Arrays.asList("group", "module", "version");
    private static final List<String> GRAB_OPTIONAL = Arrays.asList("classifier", "transitive", "conf", "ext", "type", "changing", "force", "initClass");
    private static final List<String> GRAB_BOOLEAN = Arrays.asList("transitive", "changing", "force", "initClass");
    private static final Collection<String> GRAB_ALL = DefaultGroovyMethods.plus(GRAB_REQUIRED, GRAB_OPTIONAL);
    private static final Pattern IVY_PATTERN = Pattern.compile("([a-zA-Z0-9-/._+=]+)#([a-zA-Z0-9-/._+=]+)(;([a-zA-Z0-9-/.\\(\\)\\[\\]\\{\\}_+=,:@][a-zA-Z0-9-/.\\(\\)\\]\\{\\}_+=,:@]*))?(\\[([a-zA-Z0-9-/._+=,]*)\\])?");
    private static final Pattern ATTRIBUTES_PATTERN = Pattern.compile("(.*;|^)([a-zA-Z0-9]+)=([a-zA-Z0-9.*\\[\\]\\-\\(\\),]*)$");

    private static String dotName(String className) {
        return className.substring(className.lastIndexOf("."));
    }

    private static String shortName(String className) {
        return className.substring(1);
    }

    boolean allowShortGrab;
    Set<String> grabAliases;
    List<AnnotationNode> grabAnnotations;

    boolean allowShortGrabExcludes;
    Set<String> grabExcludeAliases;
    List<AnnotationNode> grabExcludeAnnotations;

    boolean allowShortGrabConfig;
    Set<String> grabConfigAliases;
    List<AnnotationNode> grabConfigAnnotations;

    boolean allowShortGrapes;
    Set<String> grapesAliases;
    List<AnnotationNode> grapesAnnotations;

    boolean allowShortGrabResolver;
    Set<String> grabResolverAliases;
    List<AnnotationNode> grabResolverAnnotations;

    CompilationUnit compilationUnit;
    SourceUnit sourceUnit;
    ClassLoader loader;
    boolean initContextClassLoader;

    public SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public void setCompilationUnit(final CompilationUnit compilationUnit) {
        this.compilationUnit = compilationUnit;
    }

    public void visit(ASTNode[] nodes, SourceUnit source) {
        sourceUnit = source;
        loader = null;
        initContextClassLoader = false;

        ModuleNode mn = (ModuleNode) nodes[0];

        allowShortGrab = true;
        allowShortGrabExcludes = true;
        allowShortGrabConfig = true;
        allowShortGrapes = true;
        allowShortGrabResolver = true;
        grabAliases = new HashSet<String>();
        grabExcludeAliases = new HashSet<String>();
        grabConfigAliases = new HashSet<String>();
        grapesAliases = new HashSet<String>();
        grabResolverAliases = new HashSet<String>();
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
            if ((className.endsWith(GRABRESOLVER_DOT_NAME) && ((alias == null) || (alias.length() == 0)))
                || (GRABRESOLVER_CLASS_NAME.equals(alias)))
            {
                allowShortGrabResolver = false;
            } else if (GRABRESOLVER_CLASS_NAME.equals(className)) {
                grabResolverAliases.add(im.getAlias());
            }
        }

        List<Map<String,Object>> grabMaps = new ArrayList<Map<String,Object>>();
        List<Map<String,Object>> grabExcludeMaps = new ArrayList<Map<String,Object>>();

        for (ClassNode classNode : sourceUnit.getAST().getClasses()) {
            grabAnnotations = new ArrayList<AnnotationNode>();
            grabExcludeAnnotations = new ArrayList<AnnotationNode>();
            grabConfigAnnotations = new ArrayList<AnnotationNode>();
            grapesAnnotations = new ArrayList<AnnotationNode>();
            grabResolverAnnotations = new ArrayList<AnnotationNode>();

            visitClass(classNode);

            ClassNode grapeClassNode = ClassHelper.make(Grape.class);

            List<Statement> grabResolverInitializers = new ArrayList<Statement>();

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

            if (!grabResolverAnnotations.isEmpty()) {
                grabResolverAnnotationLoop:
                for (AnnotationNode node : grabResolverAnnotations) {
                    Map<String, Object> grabResolverMap = new HashMap<String, Object>();
                    Expression value = node.getMember("value");
                    ConstantExpression ce = null;
                    if (value != null && value instanceof ConstantExpression) {
                        ce = (ConstantExpression) value;
                    }
                    String sval = null;
                    if (ce != null && ce.getValue() instanceof String) {
                        sval = (String) ce.getValue();
                    }
                    if (sval != null && sval.length() > 0) {
                        for (String s : GRABRESOLVER_REQUIRED) {
                            Expression member = node.getMember(s);
                            if (member != null) {
                                addError("The attribute \"" + s + "\" conflicts with attribute 'value' in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                                continue grabResolverAnnotationLoop;
                            }
                        }
                        grabResolverMap.put("name", sval);
                        grabResolverMap.put("root", sval);
                    } else {
                        for (String s : GRABRESOLVER_REQUIRED) {
                            Expression member = node.getMember(s);
                            if (member == null) {
                                addError("The missing attribute \"" + s + "\" is required in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                                continue grabResolverAnnotationLoop;
                            } else if (member != null && !(member instanceof ConstantExpression)) {
                                addError("Attribute \"" + s + "\" has value " + member.getText() + " but should be an inline constant in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                                continue grabResolverAnnotationLoop;
                            }
                            grabResolverMap.put(s, ((ConstantExpression) member).getValue());
                        }
                    }
                    Grape.addResolver(grabResolverMap);
                    addGrabResolverAsStaticInitIfNeeded(grapeClassNode, node, grabResolverInitializers, grabResolverMap);
                }
            }

            if (!grabConfigAnnotations.isEmpty()) {
                for (AnnotationNode node : grabConfigAnnotations) {
                    checkForClassLoader(node);
                    checkForInitContextClassLoader(node);
                }
                addInitContextClassLoaderIfNeeded(classNode);
            }

            if (!grabExcludeAnnotations.isEmpty()) {
                grabExcludeAnnotationLoop:
                for (AnnotationNode node : grabExcludeAnnotations) {
                    Map<String, Object> grabExcludeMap = new HashMap<String, Object>();
                    checkForConvenienceForm(node, true);
                    for (String s : GRABEXCLUDE_REQUIRED) {
                        Expression member = node.getMember(s);
                        if (member == null) {
                            addError("The missing attribute \"" + s + "\" is required in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                            continue grabExcludeAnnotationLoop;
                        } else if (member != null && !(member instanceof ConstantExpression)) {
                            addError("Attribute \"" + s + "\" has value " + member.getText() + " but should be an inline constant in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                            continue grabExcludeAnnotationLoop;
                        }
                        grabExcludeMap.put(s, ((ConstantExpression)member).getValue());
                    }
                    grabExcludeMaps.add(grabExcludeMap);
                }
            }

            if (!grabAnnotations.isEmpty()) {
                grabAnnotationLoop:
                for (AnnotationNode node : grabAnnotations) {
                    Map<String, Object> grabMap = new HashMap<String, Object>();
                    checkForConvenienceForm(node, false);
                    for (String s : GRAB_ALL) {
                        Expression member = node.getMember(s);
                        if (member == null && !GRAB_OPTIONAL.contains(s)) {
                            addError("The missing attribute \"" + s + "\" is required in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                            continue grabAnnotationLoop;
                        } else if (member != null && !(member instanceof ConstantExpression)) {
                            addError("Attribute \"" + s + "\" has value " + member.getText() + " but should be an inline constant in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                            continue grabAnnotationLoop;
                        }
                        if (node.getMember(s) != null)
                            grabMap.put(s, ((ConstantExpression)member).getValue());
                    }
                    grabMaps.add(grabMap);
                    callGrabAsStaticInitIfNeeded(classNode, grapeClassNode, node, grabExcludeMaps);
                }
            }
            if (!grabResolverInitializers.isEmpty()) {
                classNode.addStaticInitializerStatements(grabResolverInitializers, true);
            }
        }

        if (!grabMaps.isEmpty()) {
            Map<String, Object> basicArgs = new HashMap<String, Object>();
            basicArgs.put("classLoader", loader != null ? loader : sourceUnit.getClassLoader());
            if (!grabExcludeMaps.isEmpty()) basicArgs.put("excludes", grabExcludeMaps);

            try {
                Grape.grab(basicArgs, grabMaps.toArray(new Map[grabMaps.size()]));
                // grab may have added more transformations through new URLs added to classpath, so do one more scan
                if (compilationUnit!=null) {
                    ASTTransformationVisitor.addGlobalTransformsAfterGrab(compilationUnit.getASTTransformationsContext());
                }
            } catch (RuntimeException re) {
                // Decided against syntax exception since this is not a syntax error.
                // The down side is we lose line number information for the offending
                // @Grab annotation.
                source.addException(re);
            }
        }
    }

    private void callGrabAsStaticInitIfNeeded(ClassNode classNode, ClassNode grapeClassNode, AnnotationNode node, List<Map<String, Object>> grabExcludeMaps) {
        if ((node.getMember("initClass") == null)
            || (node.getMember("initClass") == ConstantExpression.TRUE))
        {
            List<Statement> grabInitializers = new ArrayList<Statement>();

            // add Grape.grab(excludeArgs, [group:group, module:module, version:version, classifier:classifier])
            // or Grape.grab([group:group, module:module, version:version, classifier:classifier])
            MapExpression me = new MapExpression();
            for (String s : GRAB_REQUIRED) {
                me.addMapEntryExpression(new ConstantExpression(s),node.getMember(s));
            }

            for (String s : GRAB_OPTIONAL) {
                if (node.getMember(s) != null)
                    me.addMapEntryExpression(new ConstantExpression(s),node.getMember(s));
            }

            ArgumentListExpression grabArgs;
            if (grabExcludeMaps.isEmpty()) {
                grabArgs = new ArgumentListExpression(me);
            } else {
                MapExpression args = new MapExpression();
                ListExpression list = new ListExpression();
                for (Map<String, Object> map : grabExcludeMaps) {
                    Set<Map.Entry<String, Object>> entries = map.entrySet();
                    MapExpression inner = new MapExpression();
                    for (Map.Entry<String, Object> entry : entries) {
                        inner.addMapEntryExpression(new ConstantExpression(entry.getKey()), new ConstantExpression(entry.getValue()));
                    }
                    list.addExpression(inner);
                }
                args.addMapEntryExpression(new ConstantExpression("excludes"), list);
                grabArgs = new ArgumentListExpression(args, me);
            }
            grabInitializers.add(new ExpressionStatement(
                    new StaticMethodCallExpression(grapeClassNode, "grab", grabArgs)));

            // insert at beginning so we have the classloader set up before the class is called
            classNode.addStaticInitializerStatements(grabInitializers, true);
        }
    }

    private void addGrabResolverAsStaticInitIfNeeded(ClassNode grapeClassNode, AnnotationNode node,
                                                      List<Statement> grabResolverInitializers, Map<String, Object> grabResolverMap) {
        if ((node.getMember("initClass") == null)
            || (node.getMember("initClass") == ConstantExpression.TRUE))
        {
            MapExpression resolverArgs = new MapExpression();
            for (Map.Entry<String, Object> next : grabResolverMap.entrySet()) {
                resolverArgs.addMapEntryExpression(new ConstantExpression(next.getKey()), new ConstantExpression(next.getValue()));
            }
            grabResolverInitializers.add(new ExpressionStatement(
                    new StaticMethodCallExpression(grapeClassNode, "addResolver", new ArgumentListExpression(resolverArgs))));
        }
    }

    private void addInitContextClassLoaderIfNeeded(ClassNode classNode) {
        if (initContextClassLoader) {
            Statement initStatement = new ExpressionStatement(
                    new MethodCallExpression(
                            new StaticMethodCallExpression(THREAD_CLASSNODE, "currentThread", ArgumentListExpression.EMPTY_ARGUMENTS),
                            "setContextClassLoader",
                            new MethodCallExpression(
                                    new MethodCallExpression(VariableExpression.THIS_EXPRESSION, "getClass", MethodCallExpression.NO_ARGUMENTS),
                                    "getClassLoader",
                                    ArgumentListExpression.EMPTY_ARGUMENTS
                            )
                    )
            );

            classNode.addObjectInitializerStatements(initStatement);
        }
    }

    private void checkForClassLoader(AnnotationNode node) {
        Object val = node.getMember("systemClassLoader");
        if (val == null || !(val instanceof ConstantExpression)) return;
        Object systemClassLoaderObject = ((ConstantExpression)val).getValue();
        if (!(systemClassLoaderObject instanceof Boolean)) return;
        Boolean systemClassLoader = (Boolean) systemClassLoaderObject;
        if (systemClassLoader) loader = ClassLoader.getSystemClassLoader();
    }

    private void checkForInitContextClassLoader(AnnotationNode node) {
        Object val = node.getMember("initContextClassLoader");
        if (val == null || !(val instanceof ConstantExpression)) return;
        Object initContextClassLoaderObject = ((ConstantExpression)val).getValue();
        if (!(initContextClassLoaderObject instanceof Boolean)) return;
        initContextClassLoader = (Boolean) initContextClassLoaderObject;
    }

    private void checkForConvenienceForm(AnnotationNode node, boolean exclude) {
        Object val = node.getMember("value");
        if (val == null || !(val instanceof ConstantExpression)) return;
        Object allParts = ((ConstantExpression)val).getValue();
        if (!(allParts instanceof String)) return;
        String allstr = (String) allParts;

        // strip off trailing attributes
        boolean done = false;
        while (!done) {
            Matcher attrs = ATTRIBUTES_PATTERN.matcher(allstr);
            if (attrs.find()) {
                String attrName = attrs.group(2);
                String attrValue = attrs.group(3);
                if (attrName == null || attrValue == null) continue;
                boolean isBool = GRAB_BOOLEAN.contains(attrName);
                ConstantExpression value = new ConstantExpression(isBool ? Boolean.valueOf(attrValue) : attrValue);
                value.setSourcePosition(node);
                node.addMember(attrName, value);
                int lastSemi = allstr.lastIndexOf(';');
                if (lastSemi == -1) {
                    allstr = "";
                    break;
                }
                allstr = allstr.substring(0, lastSemi);
            } else {
                done = true;
            }
        }

        if (allstr.contains("#")) {
            // see: http://ant.apache.org/ivy/history/latest-milestone/textual.html
            Matcher m = IVY_PATTERN.matcher(allstr);
            if (!m.find()) return;
            if (m.group(1) == null || m.group(2) == null) return;
            node.addMember("module", new ConstantExpression(m.group(2)));
            node.addMember("group", new ConstantExpression(m.group(1)));
            if (m.group(6) != null) node.addMember("conf", new ConstantExpression(m.group(6)));
            if (m.group(4) != null) node.addMember("version", new ConstantExpression(m.group(4)));
            else if (!exclude && node.getMember("version") == null) node.addMember("version", new ConstantExpression("*"));
            node.getMembers().remove("value");
        } else if (allstr.contains(":")) {
            // assume gradle syntax
            // see: http://www.gradle.org/latest/docs/userguide/dependency_management.html#sec:how_to_declare_your_dependencies
            Map<String, Object> parts = GrapeUtil.getIvyParts(allstr);
            for (String key : parts.keySet()) {
                String value = parts.get(key).toString();
                if (!key.equals("version") || !value.equals("*") || !exclude) {
                    node.addMember(key, new ConstantExpression(value));
                }
            }
            node.getMembers().remove("value");
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
            if ((GRABEXCLUDE_CLASS_NAME.equals(name))
                    || (allowShortGrabExcludes && GRABEXCLUDE_SHORT_NAME.equals(name))
                    || (grabExcludeAliases.contains(name))) {
                grabExcludeAnnotations.add(annotation);
            }
            if ((GRABCONFIG_CLASS_NAME.equals(name))
                    || (allowShortGrabConfig && GRABCONFIG_SHORT_NAME.equals(name))
                    || (grabConfigAliases.contains(name))) {
                grabConfigAnnotations.add(annotation);
            }
            if ((GRABRESOLVER_CLASS_NAME.equals(name))
                    || (allowShortGrabResolver && GRABRESOLVER_SHORT_NAME.equals(name))
                    || (grabResolverAliases.contains(name))) {
                grabResolverAnnotations.add(annotation);
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
            if ((GRABEXCLUDE_CLASS_NAME.equals(name))
                    || (allowShortGrabExcludes && GRABEXCLUDE_SHORT_NAME.equals(name))
                    || (grabExcludeAliases.contains(name))) {
                grabExcludeAnnotations.add(an);
            }
            if ((GRABCONFIG_CLASS_NAME.equals(name))
                    || (allowShortGrabConfig && GRABCONFIG_SHORT_NAME.equals(name))
                    || (grabConfigAliases.contains(name))) {
                grabConfigAnnotations.add(an);
            }
            if ((GRAPES_CLASS_NAME.equals(name))
                    || (allowShortGrapes && GRAPES_SHORT_NAME.equals(name))
                    || (grapesAliases.contains(name))) {
                grapesAnnotations.add(an);
            }
            if ((GRABRESOLVER_CLASS_NAME.equals(name))
                    || (allowShortGrabResolver && GRABRESOLVER_SHORT_NAME.equals(name))
                    || (grabResolverAliases.contains(name))) {
                grabResolverAnnotations.add(an);
            }
        }
    }

}
