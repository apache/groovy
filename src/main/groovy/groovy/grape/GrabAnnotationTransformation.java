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
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.control.io.StringReaderSource;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.tools.GrapeUtil;
import org.codehaus.groovy.transform.ASTTransformation;
import org.codehaus.groovy.transform.ASTTransformationVisitor;
import org.codehaus.groovy.transform.AbstractASTTransformation;
import org.codehaus.groovy.transform.GroovyASTTransformation;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.codehaus.groovy.ast.tools.GeneralUtils.args;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.eqX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.transform.AbstractASTTransformation.getMemberStringValue;

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
    private static final ClassNode SYSTEM_CLASSNODE = ClassHelper.make(System.class);

    private static final List<String> GRABEXCLUDE_REQUIRED = Arrays.asList("group", "module");
    private static final List<String> GRABRESOLVER_REQUIRED = Arrays.asList("name", "root");
    private static final List<String> GRAB_REQUIRED = Arrays.asList("group", "module", "version");
    private static final List<String> GRAB_OPTIONAL = Arrays.asList("classifier", "transitive", "conf", "ext", "type", "changing", "force", "initClass");
    private static final List<String> GRAB_BOOLEAN = Arrays.asList("transitive", "changing", "force", "initClass");
    private static final Collection<String> GRAB_ALL = DefaultGroovyMethods.plus(GRAB_REQUIRED, GRAB_OPTIONAL);
    private static final Pattern IVY_PATTERN = Pattern.compile("([a-zA-Z0-9-/._+=]+)#([a-zA-Z0-9-/._+=]+)(;([a-zA-Z0-9-/.\\(\\)\\[\\]\\{\\}_+=,:@][a-zA-Z0-9-/.\\(\\)\\]\\{\\}_+=,:@]*))?(\\[([a-zA-Z0-9-/._+=,]*)\\])?");
    private static final Pattern ATTRIBUTES_PATTERN = Pattern.compile("(.*;|^)([a-zA-Z0-9]+)=([a-zA-Z0-9.*\\[\\]\\-\\(\\),]*)$");

    private static final String AUTO_DOWNLOAD_SETTING = Grape.AUTO_DOWNLOAD_SETTING;
    private static final String DISABLE_CHECKSUMS_SETTING = Grape.DISABLE_CHECKSUMS_SETTING;
    private static final String SYSTEM_PROPERTIES_SETTING = Grape.SYSTEM_PROPERTIES_SETTING;

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
    Boolean autoDownload;
    Boolean disableChecksums;
    Map<String, String> systemProperties;

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
        grabAliases = new HashSet<>();
        grabExcludeAliases = new HashSet<>();
        grabConfigAliases = new HashSet<>();
        grapesAliases = new HashSet<>();
        grabResolverAliases = new HashSet<>();
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

        List<Map<String,Object>> grabMaps = new ArrayList<>();
        List<Map<String,Object>> grabMapsInit = new ArrayList<>();
        List<Map<String,Object>> grabExcludeMaps = new ArrayList<>();

        for (ClassNode classNode : sourceUnit.getAST().getClasses()) {
            grabAnnotations = new ArrayList<>();
            grabExcludeAnnotations = new ArrayList<>();
            grabConfigAnnotations = new ArrayList<>();
            grapesAnnotations = new ArrayList<>();
            grabResolverAnnotations = new ArrayList<>();

            visitClass(classNode);

            ClassNode grapeClassNode = ClassHelper.make(Grape.class);

            List<Statement> grabResolverInitializers = new ArrayList<>();

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
                    Map<String, Object> grabResolverMap = new HashMap<>();
                    String sval = getMemberStringValue(node, "value");
                    if (sval != null && sval.length() > 0) {
                        for (String s : GRABRESOLVER_REQUIRED) {
                            String mval = getMemberStringValue(node, s);
                            if (mval != null && mval.isEmpty()) mval = null;
                            if (mval != null) {
                                addError("The attribute \"" + s + "\" conflicts with attribute 'value' in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                                continue grabResolverAnnotationLoop;
                            }
                        }
                        grabResolverMap.put("name", sval);
                        grabResolverMap.put("root", sval);
                    } else {
                        for (String s : GRABRESOLVER_REQUIRED) {
                            String mval = getMemberStringValue(node, s);
                            Expression member = node.getMember(s);
                            if (member == null || (mval != null && mval.isEmpty())) {
                                addError("The missing attribute \"" + s + "\" is required in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                                continue grabResolverAnnotationLoop;
                            } else if (mval == null) {
                                addError("Attribute \"" + s + "\" has value " + member.getText() + " but should be an inline constant String in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                                continue grabResolverAnnotationLoop;
                            }
                            grabResolverMap.put(s, mval);
                        }
                    }

                    // If no scheme is specified for the repository root,
                    // then turn it into a URI relative to that of the source file.
                    String root = (String) grabResolverMap.get("root");
                    if (root != null && !root.contains(":")) {
                        URI sourceURI = null;
                        // Since we use the data: scheme for StringReaderSources (which are fairly common)
                        // and those are not hierarchical we can't use them for making an absolute URI.
                        if (!(getSourceUnit().getSource() instanceof StringReaderSource)) {
                            // Otherwise let's trust the source to know where it is from.
                            // And actually InputStreamReaderSource doesn't know what to do and so returns null.
                            sourceURI = getSourceUnit().getSource().getURI();
                        }
                        // If source doesn't know how to get a reference to itself,
                        // then let's use the current working directory, since the repo can be relative to that.
                        if (sourceURI == null) {
                            sourceURI = new File(".").toURI();
                        }
                        try {
                            URI rootURI = sourceURI.resolve(new URI(root));
                            grabResolverMap.put("root", rootURI.toString());
                        } catch (URISyntaxException e) {
                            // We'll be silent here.
                            // If the URI scheme is unknown or not hierarchical, then we just can't help them and shouldn't cause any trouble either.
                            // addError("Attribute \"root\" has value '" + root + "' which can't be turned into a valid URI relative to it's source '" + getSourceUnit().getName() + "' @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
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
                    checkForAutoDownload(node);
                    checkForSystemProperties(node);
                    checkForDisableChecksums(node);
                }
                addInitContextClassLoaderIfNeeded(classNode);
            }

            if (!grabExcludeAnnotations.isEmpty()) {
                grabExcludeAnnotationLoop:
                for (AnnotationNode node : grabExcludeAnnotations) {
                    Map<String, Object> grabExcludeMap = new HashMap<>();
                    checkForConvenienceForm(node, true);
                    for (String s : GRABEXCLUDE_REQUIRED) {
                        Expression member = node.getMember(s);
                        if (member == null) {
                            addError("The missing attribute \"" + s + "\" is required in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                            continue grabExcludeAnnotationLoop;
                        } else if (!(member instanceof ConstantExpression)) {
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
                    Map<String, Object> grabMap = new HashMap<>();
                    checkForConvenienceForm(node, false);
                    for (String s : GRAB_ALL) {
                        Expression member = node.getMember(s);
                        String mval = getMemberStringValue(node, s);
                        if (mval != null && mval.isEmpty()) member = null;
                        if (member == null && !GRAB_OPTIONAL.contains(s)) {
                            addError("The missing attribute \"" + s + "\" is required in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                            continue grabAnnotationLoop;
                        } else if (member != null && !(member instanceof ConstantExpression)) {
                            addError("Attribute \"" + s + "\" has value " + member.getText() + " but should be an inline constant in @" + node.getClassNode().getNameWithoutPackage() + " annotations", node);
                            continue grabAnnotationLoop;
                        }
                        if (node.getMember(s) != null) {
                            grabMap.put(s, ((ConstantExpression)member).getValue());
                        }
                    }
                    grabMaps.add(grabMap);
                    if ((node.getMember("initClass") == null) || (node.getMember("initClass") == ConstantExpression.TRUE)) {
                        grabMapsInit.add(grabMap);
                    }
                }
                callGrabAsStaticInitIfNeeded(classNode, grapeClassNode, grabMapsInit, grabExcludeMaps);
            }

            if (!grabResolverInitializers.isEmpty()) {
                classNode.addStaticInitializerStatements(grabResolverInitializers, true);
            }
        }

        if (!grabMaps.isEmpty()) {
            Map<String, Object> basicArgs = new HashMap<>();
            basicArgs.put("classLoader", loader != null ? loader : sourceUnit.getClassLoader());
            if (!grabExcludeMaps.isEmpty()) basicArgs.put("excludes", grabExcludeMaps);
            if (autoDownload != null) basicArgs.put(AUTO_DOWNLOAD_SETTING, autoDownload);
            if (disableChecksums != null) basicArgs.put(DISABLE_CHECKSUMS_SETTING, disableChecksums);
            if (systemProperties != null) basicArgs.put(SYSTEM_PROPERTIES_SETTING, systemProperties);

            try {
                Grape.grab(basicArgs, grabMaps.toArray(new Map[0]));
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

    private void callGrabAsStaticInitIfNeeded(ClassNode classNode, ClassNode grapeClassNode, List<Map<String,Object>> grabMapsInit, List<Map<String, Object>> grabExcludeMaps) {
        List<Statement> grabInitializers = new ArrayList<>();
        MapExpression basicArgs = new MapExpression();
        if (autoDownload != null)  {
            basicArgs.addMapEntryExpression(constX(AUTO_DOWNLOAD_SETTING), constX(autoDownload));
        }

        if (disableChecksums != null)  {
            basicArgs.addMapEntryExpression(constX(DISABLE_CHECKSUMS_SETTING), constX(disableChecksums));
        }

        if (systemProperties != null && !systemProperties.isEmpty()) {
            BlockStatement block = new BlockStatement();
            for(Map.Entry e : systemProperties.entrySet()) {
                block.addStatement(stmt(callX(SYSTEM_CLASSNODE, "setProperty", args(constX(e.getKey()), constX(e.getValue())))));
            }
            StaticMethodCallExpression enabled = callX(SYSTEM_CLASSNODE, "getProperty", args(constX("groovy.grape.enable"), constX("true")));
            grabInitializers.add(ifS(eqX(enabled, constX("true")), block));
        }

        if (!grabExcludeMaps.isEmpty()) {
            ListExpression list = new ListExpression();
            for (Map<String, Object> map : grabExcludeMaps) {
                Set<Map.Entry<String, Object>> entries = map.entrySet();
                MapExpression inner = new MapExpression();
                for (Map.Entry<String, Object> entry : entries) {
                    inner.addMapEntryExpression(constX(entry.getKey()), constX(entry.getValue()));
                }
                list.addExpression(inner);
            }
            basicArgs.addMapEntryExpression(constX("excludes"), list);
        }

        List<Expression> argList = new ArrayList<>();
        argList.add(basicArgs);
        if (grabMapsInit.isEmpty()) return;
        for (Map<String, Object> grabMap : grabMapsInit) {
            // add Grape.grab(excludeArgs, [group:group, module:module, version:version, classifier:classifier])
            // or Grape.grab([group:group, module:module, version:version, classifier:classifier])
            MapExpression dependencyArg = new MapExpression();
            for (String s : GRAB_REQUIRED) {
                dependencyArg.addMapEntryExpression(constX(s), constX(grabMap.get(s)));
            }
            for (String s : GRAB_OPTIONAL) {
                if (grabMap.containsKey(s))
                    dependencyArg.addMapEntryExpression(constX(s), constX(grabMap.get(s)));
            }
            argList.add(dependencyArg);
        }
        grabInitializers.add(stmt(callX(grapeClassNode, "grab", args(argList))));

        // insert at beginning so we have the classloader set up before the class is called
        classNode.addStaticInitializerStatements(grabInitializers, true);
    }

    private static void addGrabResolverAsStaticInitIfNeeded(ClassNode grapeClassNode, AnnotationNode node,
                                                      List<Statement> grabResolverInitializers, Map<String, Object> grabResolverMap) {
        if ((node.getMember("initClass") == null)
            || (node.getMember("initClass") == ConstantExpression.TRUE))
        {
            MapExpression resolverArgs = new MapExpression();
            for (Map.Entry<String, Object> next : grabResolverMap.entrySet()) {
                resolverArgs.addMapEntryExpression(constX(next.getKey()), constX(next.getValue()));
            }
            grabResolverInitializers.add(stmt(callX(grapeClassNode, "addResolver", args(resolverArgs))));
        }
    }

    private void addInitContextClassLoaderIfNeeded(ClassNode classNode) {
        if (initContextClassLoader) {
            Statement initStatement = stmt(callX(
                            callX(THREAD_CLASSNODE, "currentThread"),
                            "setContextClassLoader",
                            callX(callThisX("getClass"), "getClassLoader")
                    )
            );
            classNode.addObjectInitializerStatements(initStatement);
        }
    }

    private void checkForClassLoader(AnnotationNode node) {
        Object val = node.getMember("systemClassLoader");
        if (!(val instanceof ConstantExpression)) return;
        Object systemClassLoaderObject = ((ConstantExpression)val).getValue();
        if (!(systemClassLoaderObject instanceof Boolean)) return;
        Boolean systemClassLoader = (Boolean) systemClassLoaderObject;
        if (systemClassLoader) loader = ClassLoader.getSystemClassLoader();
    }

    private void checkForInitContextClassLoader(AnnotationNode node) {
        Object val = node.getMember("initContextClassLoader");
        if (!(val instanceof ConstantExpression)) return;
        Object initContextClassLoaderObject = ((ConstantExpression)val).getValue();
        if (!(initContextClassLoaderObject instanceof Boolean)) return;
        initContextClassLoader = (Boolean) initContextClassLoaderObject;
    }

    private void checkForAutoDownload(AnnotationNode node) {
        Object val = node.getMember(AUTO_DOWNLOAD_SETTING);
        if (!(val instanceof ConstantExpression)) return;
        Object autoDownloadValue = ((ConstantExpression)val).getValue();
        if (!(autoDownloadValue instanceof Boolean)) return;
        autoDownload = (Boolean) autoDownloadValue;
    }

    private void checkForDisableChecksums(AnnotationNode node) {
        Object val = node.getMember(DISABLE_CHECKSUMS_SETTING);
        if (!(val instanceof ConstantExpression)) return;
        Object disableChecksumsValue = ((ConstantExpression)val).getValue();
        if (!(disableChecksumsValue instanceof Boolean)) return;
        disableChecksums = (Boolean) disableChecksumsValue;
    }

    private void checkForSystemProperties(AnnotationNode node) {
        systemProperties = new HashMap<>();
        List<String> nameValueList = AbstractASTTransformation.getMemberStringList(node, SYSTEM_PROPERTIES_SETTING);
        if (nameValueList != null) {
            for (String nameValue : nameValueList) {
                int equalsDelim = nameValue.indexOf('=');
                if (equalsDelim != -1) {
                    systemProperties.put(nameValue.substring(0, equalsDelim), nameValue.substring(equalsDelim + 1));
                }
            }
        }
    }

    private static void checkForConvenienceForm(AnnotationNode node, boolean exclude) {
        Object val = node.getMember("value");
        if (!(val instanceof ConstantExpression)) return;
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
                ConstantExpression value = constX(isBool ? Boolean.valueOf(attrValue) : attrValue);
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
            node.addMember("module", constX(m.group(2)));
            node.addMember("group", constX(m.group(1)));
            if (m.group(6) != null) node.addMember("conf", constX(m.group(6)));
            if (m.group(4) != null) node.addMember("version", constX(m.group(4)));
            else if (!exclude && node.getMember("version") == null) node.addMember("version", constX("*"));
            node.getMembers().remove("value");
        } else if (allstr.contains(":")) {
            // assume gradle syntax
            // see: http://www.gradle.org/latest/docs/userguide/dependency_management.html#sec:how_to_declare_your_dependencies
            Map<String, Object> parts = GrapeUtil.getIvyParts(allstr);
            for (Map.Entry<String, Object> entry : parts.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue().toString();
                if (!key.equals("version") || !value.equals("*") || !exclude) {
                    node.addMember(key, constX(value));
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
