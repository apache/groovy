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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.groovydoc.GroovyAnnotationRef;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyConstructorDoc;
import org.codehaus.groovy.groovydoc.GroovyFieldDoc;
import org.codehaus.groovy.groovydoc.GroovyMethodDoc;
import org.codehaus.groovy.groovydoc.GroovyPackageDoc;
import org.codehaus.groovy.groovydoc.GroovyParameter;
import org.codehaus.groovy.groovydoc.GroovyRootDoc;
import org.codehaus.groovy.groovydoc.GroovyType;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleGroovyClassDoc extends SimpleGroovyAbstractableElementDoc implements GroovyClassDoc {

    public static final Pattern TAG_REGEX = Pattern.compile("(?sm)\\s*@([a-zA-Z.]+)\\s+(.*?)(?=\\s+@)");
    public static final String DOCROOT_PATTERN2    = "(?m)[{]@docRoot}/";
    public static final String DOCROOT_PATTERN    = "(?m)[{]@docRoot}";

    // group 1: tag name, group 2: tag body
    public static final Pattern LINK_REGEX    = Pattern.compile("(?m)[{]@(link)\\s+([^}]*)}");
    public static final Pattern LITERAL_REGEX = Pattern.compile("(?m)[{]@(literal)\\s+([^}]*)}");
    public static final Pattern CODE_REGEX    = Pattern.compile("(?m)[{]@(code)\\s+([^}]*)}");

    public static final Pattern REF_LABEL_REGEX = Pattern.compile("([\\w.#\\$]*(\\(.*\\))?)(\\s(.*))?");
    public static final Pattern NAME_ARGS_REGEX = Pattern.compile("([^(]+)\\(([^)]*)\\)");
    public static final Pattern SPLIT_ARGS_REGEX = Pattern.compile(",\\s*");
    private static final List<String> PRIMITIVES = Arrays.asList("void", "boolean", "byte", "short", "char", "int", "long", "float", "double");
    private static final Map<String, String> TAG_TEXT = new LinkedHashMap<String, String>();
    private static final GroovyConstructorDoc[] EMPTY_GROOVYCONSTRUCTORDOC_ARRAY = new GroovyConstructorDoc[0];
    private static final GroovyClassDoc[] EMPTY_GROOVYCLASSDOC_ARRAY = new GroovyClassDoc[0];
    private static final GroovyFieldDoc[] EMPTY_GROOVYFIELDDOC_ARRAY = new GroovyFieldDoc[0];
    private static final GroovyMethodDoc[] EMPTY_GROOVYMETHODDOC_ARRAY = new GroovyMethodDoc[0];

    static {
        TAG_TEXT.put("see", "See Also");
        TAG_TEXT.put("param", "Parameters");
        TAG_TEXT.put("throw", "Throws");
        TAG_TEXT.put("exception", "Throws");
        TAG_TEXT.put("return", "Returns");
        TAG_TEXT.put("since", "Since");
        TAG_TEXT.put("author", "Authors");
        TAG_TEXT.put("version", "Version");
        TAG_TEXT.put("default", "Default");
        // typeparam is used internally as a specialization of param to separate type params from regular params.
        TAG_TEXT.put("typeparam", "Type Parameters");
    }
    private final List<GroovyConstructorDoc> constructors;
    private final List<GroovyFieldDoc> fields;
    private final List<GroovyFieldDoc> properties;
    private final List<GroovyFieldDoc> enumConstants;
    private final List<GroovyMethodDoc> methods;
    private final List<String> importedClassesAndPackages;
    private final Map<String, String> aliases;
    private final List<String> interfaceNames;
    private final List<GroovyClassDoc> interfaceClasses;
    private final List<GroovyClassDoc> nested;
    private final List<LinkArgument> links;
    private final Map<String, Class<?>> resolvedExternalClassesCache;
    private GroovyClassDoc superClass;
    private GroovyClassDoc outer;
    private String superClassName;
    private String fullPathName;
    private boolean isgroovy;
    private GroovyRootDoc savedRootDoc = null;
    private String nameWithTypeArgs;

    public SimpleGroovyClassDoc(List<String> importedClassesAndPackages, Map<String, String> aliases, String name, List<LinkArgument> links) {
        super(name);
        this.importedClassesAndPackages = importedClassesAndPackages;
        this.aliases = aliases;
        this.links = links;
        constructors = new ArrayList<GroovyConstructorDoc>();
        fields = new ArrayList<GroovyFieldDoc>();
        properties = new ArrayList<GroovyFieldDoc>();
        enumConstants = new ArrayList<GroovyFieldDoc>();
        methods = new ArrayList<GroovyMethodDoc>();
        interfaceNames = new ArrayList<String>();
        interfaceClasses = new ArrayList<GroovyClassDoc>();
        nested = new ArrayList<GroovyClassDoc>();
        resolvedExternalClassesCache = new HashMap<String, Class<?>>();
    }

    public SimpleGroovyClassDoc(List<String> importedClassesAndPackages, Map<String, String> aliases, String name) {
        this(importedClassesAndPackages, aliases, name, new ArrayList<LinkArgument>());
    }

    public SimpleGroovyClassDoc(List<String> importedClassesAndPackages, String name) {
        this(importedClassesAndPackages, new LinkedHashMap<String, String>(), name, new ArrayList<LinkArgument>());
    }

    /**
     * returns a sorted array of constructors
     */
    @Override
    public GroovyConstructorDoc[] constructors() {
        Collections.sort(constructors);
        return constructors.toArray(EMPTY_GROOVYCONSTRUCTORDOC_ARRAY);
    }

    public boolean add(GroovyConstructorDoc constructor) {
        return constructors.add(constructor);
    }

    // TODO remove?
    public GroovyClassDoc getOuter() {
        return outer;
    }

    public void setOuter(GroovyClassDoc outer) {
        this.outer = outer;
    }

    public boolean isGroovy() {
        return isgroovy;
    }

    public void setGroovy(boolean isgroovy) {
        this.isgroovy = isgroovy;
    }

    /**
     * returns a sorted array of nested classes and interfaces
     */
    @Override
    public GroovyClassDoc[] innerClasses() {
        Collections.sort(nested);
        return nested.toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    public boolean addNested(GroovyClassDoc nestedClass) {
        return nested.add(nestedClass);
    }

    /**
     * returns a sorted array of fields
     */
    @Override
    public GroovyFieldDoc[] fields() {
        Collections.sort(fields);
        return fields.toArray(EMPTY_GROOVYFIELDDOC_ARRAY);
    }

    public boolean add(GroovyFieldDoc field) {
        return fields.add(field);
    }

    /**
     * returns a sorted array of properties
     */
    @Override
    public GroovyFieldDoc[] properties() {
        Collections.sort(properties);
        return properties.toArray(EMPTY_GROOVYFIELDDOC_ARRAY);
    }

    public boolean addProperty(GroovyFieldDoc property) {
        return properties.add(property);
    }

    /**
     * returns a sorted array of enum constants
     */
    @Override
    public GroovyFieldDoc[] enumConstants() {
        Collections.sort(enumConstants);
        return enumConstants.toArray(EMPTY_GROOVYFIELDDOC_ARRAY);
    }

    public boolean addEnumConstant(GroovyFieldDoc field) {
        return enumConstants.add(field);
    }

    /**
     * returns a sorted array of methods
     */
    @Override
    public GroovyMethodDoc[] methods() {
        Collections.sort(methods);
        return methods.toArray(EMPTY_GROOVYMETHODDOC_ARRAY);
    }

    public boolean add(GroovyMethodDoc method) {
        return methods.add(method);
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public void setSuperClassName(String className) {
        superClassName = className;
    }

    @Override
    public GroovyClassDoc superclass() {
        return superClass;
    }

    public void setSuperClass(GroovyClassDoc doc) {
        superClass = doc;
    }

    @Override
    public String getFullPathName() {
        return fullPathName;
    }

    public void setFullPathName(String fullPathName) {
        this.fullPathName = fullPathName;
    }

    @Override
    public String getRelativeRootPath() {
        StringTokenizer tokenizer = new StringTokenizer(fullPathName, "/"); // todo windows??
        StringBuilder sb = new StringBuilder();
        if (tokenizer.hasMoreTokens()) {
            tokenizer.nextToken(); // ignore the first token, as we want n-1 parent dirs
        }
        while (tokenizer.hasMoreTokens()) {
            tokenizer.nextToken();
            sb.append("../");
        }
        return sb.toString();
    }

    // TODO move logic here into resolve
    public List<GroovyClassDoc> getParentClasses() {
        List<GroovyClassDoc> result = new LinkedList<GroovyClassDoc>();
        if (isInterface()) return result;
        result.add(0, this);
        GroovyClassDoc next = this;
        while (next.superclass() != null && !"java.lang.Object".equals(next.qualifiedTypeName())) {
            next = next.superclass();
            result.add(0, next);
        }
        GroovyClassDoc prev = next;
        Class nextClass = getClassOf(next.qualifiedTypeName());
        while (nextClass != null && nextClass.getSuperclass() != null && !Object.class.equals(nextClass)) {
            nextClass = nextClass.getSuperclass();
            GroovyClassDoc nextDoc = new ExternalGroovyClassDoc(nextClass);
            if (prev instanceof SimpleGroovyClassDoc) {
                SimpleGroovyClassDoc parent = (SimpleGroovyClassDoc) prev;
                parent.setSuperClass(nextDoc);
            }
            result.add(0, nextDoc);
            prev = nextDoc;
        }
        if (!result.get(0).qualifiedTypeName().equals("java.lang.Object")) {
            result.add(0, new ExternalGroovyClassDoc(Object.class));
        }
        return result;
    }

    public Set<GroovyClassDoc> getParentInterfaces() {
        Set<GroovyClassDoc> result = new LinkedHashSet<GroovyClassDoc>();
        result.add(this);
        Set<GroovyClassDoc> next = new LinkedHashSet<GroovyClassDoc>(Arrays.asList(this.interfaces()));
        while (!next.isEmpty()) {
            Set<GroovyClassDoc> temp = next;
            next = new LinkedHashSet<GroovyClassDoc>();
            for (GroovyClassDoc t : temp) {
                if (t instanceof SimpleGroovyClassDoc) {
                    next.addAll(((SimpleGroovyClassDoc)t).getParentInterfaces());
                } else if (t instanceof ExternalGroovyClassDoc) {
                    ExternalGroovyClassDoc d = (ExternalGroovyClassDoc) t;
                    next.addAll(getJavaInterfaces(d));
                }
            }
            next = DefaultGroovyMethods.minus(next, result);
            result.addAll(next);
        }
        return result;
    }

    private Set<GroovyClassDoc> getJavaInterfaces(ExternalGroovyClassDoc d) {
        Set<GroovyClassDoc> result = new LinkedHashSet<GroovyClassDoc>();
        Class[] interfaces = d.externalClass().getInterfaces();
        if (interfaces != null) {
            for (Class i : interfaces) {
                ExternalGroovyClassDoc doc = new ExternalGroovyClassDoc(i);
                result.add(doc);
                result.addAll(getJavaInterfaces(doc));
            }
        }
        return result;
    }

    private Class getClassOf(String next) {
        try {
            return Class.forName(next.replace("/", "."), false, getClass().getClassLoader());
        } catch (Throwable t) {
            return null;
        }
    }

    private void processAnnotationRefs(GroovyRootDoc rootDoc, GroovyAnnotationRef[] annotations) {
        for (GroovyAnnotationRef annotation : annotations) {
            SimpleGroovyAnnotationRef ref = (SimpleGroovyAnnotationRef) annotation;
            ref.setType(resolveClass(rootDoc, ref.name()));
        }
    }

    void resolve(GroovyRootDoc rootDoc) {
        this.savedRootDoc = rootDoc;
        Map visibleClasses = rootDoc.getVisibleClasses(importedClassesAndPackages);

        // resolve constructor parameter types
        for (GroovyConstructorDoc constructor : constructors) {

            // parameters
            for (GroovyParameter groovyParameter : constructor.parameters()) {
                SimpleGroovyParameter param = (SimpleGroovyParameter) groovyParameter;
                String paramTypeName = param.typeName();
                if (visibleClasses.containsKey(paramTypeName)) {
                    param.setType((GroovyType) visibleClasses.get(paramTypeName));
                } else {
                    GroovyClassDoc doc = resolveClass(rootDoc, paramTypeName);
                    if (doc != null) param.setType(doc);
                }
                processAnnotationRefs(rootDoc, param.annotations());
            }
            processAnnotationRefs(rootDoc, constructor.annotations());
        }

        for (GroovyFieldDoc field : fields) {
            SimpleGroovyFieldDoc mutableField = (SimpleGroovyFieldDoc) field;
            GroovyType fieldType = field.type();
            String typeName = fieldType.typeName();
            if (visibleClasses.containsKey(typeName)) {
                mutableField.setType((GroovyType) visibleClasses.get(typeName));
            } else {
                GroovyClassDoc doc = resolveClass(rootDoc, typeName);
                if (doc != null) mutableField.setType(doc);
            }
            processAnnotationRefs(rootDoc, field.annotations());
        }

        // resolve method return types and parameter types
        for (GroovyMethodDoc method : methods) {

            // return types
            GroovyType returnType = method.returnType();
            String typeName = returnType.typeName();
            if (visibleClasses.containsKey(typeName)) {
                method.setReturnType((GroovyType) visibleClasses.get(typeName));
            } else {
                GroovyClassDoc doc = resolveClass(rootDoc, typeName);
                if (doc != null) method.setReturnType(doc);
            }

            // parameters
            for (GroovyParameter groovyParameter : method.parameters()) {
                SimpleGroovyParameter param = (SimpleGroovyParameter) groovyParameter;
                String paramTypeName = param.typeName();
                if (visibleClasses.containsKey(paramTypeName)) {
                    param.setType((GroovyType) visibleClasses.get(paramTypeName));
                } else {
                    GroovyClassDoc doc = resolveClass(rootDoc, paramTypeName);
                    if (doc != null) param.setType(doc);
                }
                processAnnotationRefs(rootDoc, param.annotations());
            }
            processAnnotationRefs(rootDoc, method.annotations());
        }

        // resolve property types
        for (GroovyFieldDoc property : properties)  {
            if (property instanceof SimpleGroovyFieldDoc)  {
                SimpleGroovyFieldDoc simpleGroovyFieldDoc = (SimpleGroovyFieldDoc) property;
                if (simpleGroovyFieldDoc.type() instanceof SimpleGroovyType)  {
                    SimpleGroovyType simpleGroovyType = (SimpleGroovyType) simpleGroovyFieldDoc.type();
                    GroovyClassDoc propertyTypeClassDoc = resolveClass(rootDoc, simpleGroovyType.qualifiedTypeName());
                    if (propertyTypeClassDoc != null)  {
                        simpleGroovyFieldDoc.setType(propertyTypeClassDoc);
                    }
                }
            }
            processAnnotationRefs(rootDoc, property.annotations());
        }

        if (superClassName != null && superClass == null) {
            superClass = resolveClass(rootDoc, superClassName);
        }

        for (String name : interfaceNames) {
            interfaceClasses.add(resolveClass(rootDoc, name));
        }

        processAnnotationRefs(rootDoc, annotations());
    }

    public String getDocUrl(String type) {
        return getDocUrl(type, false);
    }

    public String getDocUrl(String type, boolean full) {
        return getDocUrl(type, full, links, getRelativeRootPath(), savedRootDoc, this);
    }

    private static String resolveMethodArgs(GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc, String type) {
        if (!type.contains("(")) return type;
            Matcher m = NAME_ARGS_REGEX.matcher(type);
        if (m.matches()) {
            String name = m.group(1);
            String args = m.group(2);
            StringBuilder sb = new StringBuilder();
            sb.append(name);
            sb.append("(");
            String[] argParts = SPLIT_ARGS_REGEX.split(args);
            boolean first = true;
            for (String argPart : argParts) {
                if (first) first = false;
                else sb.append(", ");
                GroovyClassDoc doc = classDoc.resolveClass(rootDoc, argPart);
                sb.append(doc == null ? argPart : doc.qualifiedTypeName());
            }
            sb.append(")");
            return sb.toString();
        }
        return type;
    }

    public static String getDocUrl(String type, boolean full, List<LinkArgument> links, String relativePath, GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc) {
        if (type == null)
            return type;
        type = type.trim();
        if (isPrimitiveType(type) || type.length() == 1) return type;
        if (type.equals("def")) type = "java.lang.Object def";
        // cater for explicit href in e.g. @see, TODO: push this earlier?
        if (type.startsWith("<a href=")) return type;
        if (type.startsWith("? extends ")) return "? extends " + getDocUrl(type.substring(10), full, links, relativePath, rootDoc, classDoc);
        if (type.startsWith("? super ")) return "? super " + getDocUrl(type.substring(8), full, links, relativePath, rootDoc, classDoc);

        String label = null;
        int lt = type.indexOf('<');
        if (lt != -1) {
            String outerType = type.substring(0, lt);
            int gt = type.lastIndexOf('>');
            if (gt != -1) {
                if (gt > lt) {
                    String allTypeArgs = type.substring(lt + 1, gt);
                    List<String> typeArgs = new ArrayList<String>();
                    int nested = 0;
                    StringBuilder sb = new StringBuilder();
                    for (char ch : allTypeArgs.toCharArray()) {
                        if (ch == '<') nested++;
                        else if (ch == '>') nested--;
                        else if (ch == ',' && nested == 0) {
                            typeArgs.add(sb.toString().trim());
                            sb = new StringBuilder();
                            continue;
                        }
                        sb.append(ch);
                    }
                    if (sb.length() > 0) {
                        typeArgs.add(sb.toString().trim());
                    }
                    List<String> typeUrls = new ArrayList<String>();
                    for (String typeArg : typeArgs) {
                        typeUrls.add(getDocUrl(typeArg, full, links, relativePath, rootDoc, classDoc));
                    }
                    sb = new StringBuilder(getDocUrl(outerType, full, links, relativePath, rootDoc, classDoc));
                    sb.append("&lt;");
                    sb.append(DefaultGroovyMethods.join((Iterable) typeUrls, ", "));
                    sb.append("&gt;");
                    return sb.toString();
                }
                return type.replace("<", "&lt;").replace(">", "&gt;");
            }
        }
        Matcher matcher = REF_LABEL_REGEX.matcher(type);
        if (matcher.find()) {
            type = matcher.group(1);
            label = matcher.group(4);
        }

        if (type.startsWith("#"))
            return "<a href='" + resolveMethodArgs(rootDoc, classDoc, type) + "'>" + (label == null ? type.substring(1) : label) + "</a>";

        if (type.endsWith("[]")) {
            String componentType = type.substring(0, type.length() - 2);
            if (label != null)
                return getDocUrl(componentType + " " + label, full, links, relativePath, rootDoc, classDoc);
            return getDocUrl(componentType, full, links, relativePath, rootDoc, classDoc) + "[]";
        }

        if (!type.contains(".") && classDoc != null) {
            String[] pieces = type.split("#");
            String candidate = pieces[0];
            Class c = classDoc.resolveExternalClassFromImport(candidate);
            if (c != null) type = c.getName();
            if (pieces.length > 1) type += "#" + pieces[1];
            type = resolveMethodArgs(rootDoc, classDoc, type);
        }

        final String[] target = type.split("#");
        String shortClassName = target[0].replaceAll(".*\\.", "");
        shortClassName += (target.length > 1 ? "#" + target[1].split("\\(")[0] : "");
        String name = (full ? target[0] : shortClassName).replace('#', '.').replace('$', '.');

        // last chance lookup for classes within the current codebase
        if (rootDoc != null) {
            String slashedName = target[0].replace('.', '/');
            GroovyClassDoc doc = rootDoc.classNamed(classDoc, slashedName);
            if (doc != null) {
                target[0] = doc.getFullPathName(); // if we added a package
                return buildUrl(relativePath, target, label == null ? name : label);
            }
        }
        if (type.indexOf('.') == -1)
            return type;

        if (links != null) {
            for (LinkArgument link : links) {
                final StringTokenizer tokenizer = new StringTokenizer(link.getPackages(), ", ");
                while (tokenizer.hasMoreTokens()) {
                    final String token = tokenizer.nextToken();
                    if (type.startsWith(token)) {
                        return buildUrl(link.getHref(), target, label == null ? name : label);
                    }
                }
            }
        }
        return type;
    }

    private static String buildUrl(String relativeRoot, String[] target, String shortClassName) {
        if (relativeRoot.length() > 0 && !relativeRoot.endsWith("/")) {
            relativeRoot += "/";
        }
        String url = relativeRoot + target[0].replace('.', '/').replace('$', '.') + ".html" + (target.length > 1 ? "#" + target[1] : "");
        return "<a href='" + url + "' title='" + shortClassName + "'>" + shortClassName + "</a>";
    }

    private GroovyClassDoc resolveClass(GroovyRootDoc rootDoc, String name) {
        if (isPrimitiveType(name)) return null;
        GroovyClassDoc groovyClassDoc;
        Map<String, GroovyClassDoc> resolvedClasses = null;
        if (rootDoc != null) {
            resolvedClasses = rootDoc.getResolvedClasses();
            groovyClassDoc = resolvedClasses.get(name);
            if (groovyClassDoc != null) {
                return groovyClassDoc;
            }
        }
        groovyClassDoc = doResolveClass(rootDoc, name);
        if (resolvedClasses != null) {
            resolvedClasses.put(name, groovyClassDoc);
        }
        return groovyClassDoc;
    }

    private GroovyClassDoc doResolveClass(final GroovyRootDoc rootDoc, final String name) {
        if (name.endsWith("[]")) {
            GroovyClassDoc componentClass = resolveClass(rootDoc, name.substring(0, name.length() - 2));
            if (componentClass != null) return new ArrayClassDocWrapper(componentClass);
            return null;
        }
//        if (name.equals("T") || name.equals("U") || name.equals("K") || name.equals("V") || name.equals("G")) {
//            name = "java/lang/Object";
//        }
        int slashIndex = name.lastIndexOf('/');
        if (rootDoc != null) {
            GroovyClassDoc doc = ((SimpleGroovyRootDoc)rootDoc).classNamedExact(name);
            if (doc != null) return doc;
            if (slashIndex < 1) {
                doc = resolveInternalClassDocFromImport(rootDoc, name);
                if (doc != null) return doc;
                doc = resolveInternalClassDocFromSamePackage(rootDoc, name);
                if (doc != null) return doc;
                for (GroovyClassDoc nestedDoc : nested) {
                    if (nestedDoc.name().endsWith("." + name))
                        return nestedDoc;
                }
                doc = rootDoc.classNamed(this, name);
                if (doc != null) return doc;
            }
        }

        // The class is not in the tree being documented
        String shortname = name;
        Class c;
        if (slashIndex > 0) {
            shortname = name.substring(slashIndex + 1);
            c = resolveExternalFullyQualifiedClass(name);
        } else {
            c = resolveExternalClassFromImport(name);
        }
        if (c == null) {
            c = resolveFromJavaLang(name);
        }
        if (c != null) {
            return new ExternalGroovyClassDoc(c);
        }

        if (name.contains("/")) {
            // search for nested class
            if (slashIndex > 0) {
                String outerName = name.substring(0, slashIndex);
                GroovyClassDoc gcd = resolveClass(rootDoc, outerName);
                if (gcd instanceof ExternalGroovyClassDoc) {
                    ExternalGroovyClassDoc egcd = (ExternalGroovyClassDoc) gcd;
                    String innerName = name.substring(slashIndex+1);
                    Class outerClass = egcd.externalClass();
                    for (Class inner : outerClass.getDeclaredClasses()) {
                        if (inner.getName().equals(outerClass.getName() + "$" + innerName)) {
                            return new ExternalGroovyClassDoc(inner);
                        }
                    }
                }

                if (gcd instanceof SimpleGroovyClassDoc) {
                    String innerClassName = name.substring(slashIndex + 1);
                    SimpleGroovyClassDoc innerClass = new SimpleGroovyClassDoc(importedClassesAndPackages, aliases, innerClassName);
                    innerClass.setFullPathName(gcd.getFullPathName() + "." + innerClassName);
                    return innerClass;
                }
            }
        }

        // check if the name is actually an aliased type name
        if (hasAlias(name))  {
            String fullyQualifiedTypeName = getFullyQualifiedTypeNameForAlias(name);
            GroovyClassDoc gcd = resolveClass(rootDoc, fullyQualifiedTypeName);
            if (gcd != null) return gcd;
        }

        // and we can't find it
        SimpleGroovyClassDoc placeholder = new SimpleGroovyClassDoc(null, shortname);
        placeholder.setFullPathName(name);
        return placeholder;
    }

    private Class resolveFromJavaLang(String name) {
        try {
            return Class.forName("java.lang." + name, false, getClass().getClassLoader());
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    private static boolean isPrimitiveType(String name) {
        String type = name;
        if (name.endsWith("[]")) type = name.substring(0, name.length() - 2);
        return PRIMITIVES.contains(type);
    }

    private GroovyClassDoc resolveInternalClassDocFromImport(GroovyRootDoc rootDoc, String baseName) {
        if (isPrimitiveType(baseName)) return null;
        for (String importName : importedClassesAndPackages) {
            if (importName.endsWith("/" + baseName)) {
                GroovyClassDoc doc = ((SimpleGroovyRootDoc)rootDoc).classNamedExact(importName);
                if (doc != null) return doc;
            } else if (importName.endsWith("/*")) {
                GroovyClassDoc doc = ((SimpleGroovyRootDoc)rootDoc).classNamedExact(importName.substring(0, importName.length() - 2) + baseName);
                if (doc != null) return doc;
            }
        }
        return null;
    }

    private GroovyClassDoc resolveInternalClassDocFromSamePackage(GroovyRootDoc rootDoc, String baseName) {
        if (isPrimitiveType(baseName)) return null;
        if (baseName.contains(".")) return null;
        int lastSlash = fullPathName.lastIndexOf('/');
        if (lastSlash < 0) return null;
        String pkg = fullPathName.substring(0, lastSlash + 1);
        return ((SimpleGroovyRootDoc)rootDoc).classNamedExact(pkg + baseName);
    }

    private Class resolveExternalClassFromImport(String name) {
        if (isPrimitiveType(name)) return null;
        Class<?> clazz = resolvedExternalClassesCache.get(name);
        if (clazz == null) {
            if (resolvedExternalClassesCache.containsKey(name)) {
                return null;
            }
            clazz = doResolveExternalClassFromImport(name);
            resolvedExternalClassesCache.put(name, clazz);
        }
        return clazz;
    }

    private Class doResolveExternalClassFromImport(final String name) {
        for (String importName : importedClassesAndPackages) {
            String candidate = null;
            if (importName.endsWith("/" + name)) {
                candidate = importName.replace('/', '.');
            } else if (importName.endsWith("/*")) {
                candidate = importName.substring(0, importName.length() - 2).replace('/', '.') + "." + name;
            }
            if (candidate != null) {
                try {
                    // TODO cache these??
                    return Class.forName(candidate, false, getClass().getClassLoader());
                } catch (NoClassDefFoundError | ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        return null;
    }

    private Class resolveExternalFullyQualifiedClass(String name) {
        String candidate = name.replace('/', '.');
        try {
            // TODO cache these??
            return Class.forName(candidate, false, getClass().getClassLoader());
        } catch (NoClassDefFoundError | ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    private boolean hasAlias(String alias)  {
        return aliases.containsKey(alias);
    }

    private String getFullyQualifiedTypeNameForAlias(String alias)  {
        if (!hasAlias(alias)) return "";
        return aliases.get(alias);
    }

    // methods from GroovyClassDoc

    @Override
    public GroovyConstructorDoc[] constructors(boolean filter) {/*todo*/
        return null;
    }

    @Override
    public boolean definesSerializableFields() {/*todo*/
        return false;
    }

    @Override
    public GroovyFieldDoc[] fields(boolean filter) {/*todo*/
        return null;
    }

    @Override
    public GroovyClassDoc findClass(String className) {/*todo*/
        return null;
    }

    @Override
    public GroovyClassDoc[] importedClasses() {/*todo*/
        return null;
    }

    @Override
    public GroovyPackageDoc[] importedPackages() {/*todo*/
        return null;
    }

    @Override
    public GroovyClassDoc[] innerClasses(boolean filter) {/*todo*/
        return null;
    }

    @Override
    public GroovyClassDoc[] interfaces() {
        Collections.sort(interfaceClasses);
        return interfaceClasses.toArray(EMPTY_GROOVYCLASSDOC_ARRAY);
    }

    @Override
    public GroovyType[] interfaceTypes() {/*todo*/
        return null;
    }

    @Override
    public boolean isExternalizable() {/*todo*/
        return false;
    }

    @Override
    public boolean isSerializable() {/*todo*/
        return false;
    }

    @Override
    public GroovyMethodDoc[] methods(boolean filter) {/*todo*/
        return null;
    }

    @Override
    public GroovyFieldDoc[] serializableFields() {/*todo*/
        return null;
    }

    @Override
    public GroovyMethodDoc[] serializationMethods() {/*todo*/
        return null;
    }

    @Override
    public boolean subclassOf(GroovyClassDoc gcd) {/*todo*/
        return false;
    }

    @Override
    public GroovyType superclassType() {/*todo*/
        return null;
    }
//    public GroovyTypeVariable[] typeParameters() {/*todo*/return null;} // not supported in groovy
//    public GroovyParamTag[] typeParamTags() {/*todo*/return null;} // not supported in groovy


    // methods from GroovyType (todo: remove this horrible copy of SimpleGroovyType.java)
//    public GroovyAnnotationTypeDoc asAnnotationTypeDoc() {/*todo*/return null;}
//    public GroovyClassDoc asClassDoc() {/*todo*/ return null; }
//    public GroovyParameterizedType asParameterizedType() {/*todo*/return null;}
//    public GroovyTypeVariable asTypeVariable() {/*todo*/return null;}
//    public GroovyWildcardType asWildcardType() {/*todo*/return null;}
//    public String dimension() {/*todo*/ return null; }

    @Override
    public boolean isPrimitive() {/*todo*/
        return false;
    }

    @Override
    public String qualifiedTypeName() {
        String qtnWithSlashes = fullPathName.startsWith("DefaultPackage/") ? fullPathName.substring("DefaultPackage/".length()) : fullPathName;
        return qtnWithSlashes.replace('/', '.');
    }

    // TODO remove dupe with SimpleGroovyType
    @Override
    public String simpleTypeName() {
        String typeName = qualifiedTypeName();
        int lastDot = typeName.lastIndexOf('.');
        if (lastDot < 0) return typeName;
        return typeName.substring(lastDot + 1);
    }

    @Override
    public String typeName() {
        return qualifiedTypeName();
    }

    public void addInterfaceName(String className) {
        interfaceNames.add(className);
    }

    @Override
    public String firstSentenceCommentText() {
        if (super.firstSentenceCommentText() == null)
            setFirstSentenceCommentText(replaceTags(calculateFirstSentence(getRawCommentText())));
        return super.firstSentenceCommentText();
    }

    @Override
    public String commentText() {
        if (super.commentText() == null)
            setCommentText(replaceTags(getRawCommentText()));
        return super.commentText();
    }

    public String replaceTags(String comment) {
        String result = comment.replaceAll("(?m)^\\s*\\*", ""); // todo precompile regex

        String relativeRootPath = getRelativeRootPath();
        if (!relativeRootPath.endsWith("/")) {
            relativeRootPath += "/";
        }
        result = result.replaceAll(DOCROOT_PATTERN2, relativeRootPath);
        result = result.replaceAll(DOCROOT_PATTERN, relativeRootPath);

        // {@link processing hack}
        result = replaceAllTags(result, "", "", LINK_REGEX);

        // {@literal tag}
        result = encodeAngleBracketsInTagBody(result, LITERAL_REGEX);
        result = replaceAllTags(result, "", "", LITERAL_REGEX);

        // {@code tag}
        result = encodeAngleBracketsInTagBody(result, CODE_REGEX);
        result = replaceAllTags(result, "<CODE>", "</CODE>", CODE_REGEX);

        // hack to reformat other groovydoc block tags (@see, @return, @param, @throws, @author, @since) into html
        result = replaceAllTagsCollated(result, "<DL><DT><B>", ":</B></DT><DD>", "</DD><DD>", "</DD></DL>", TAG_REGEX);

        return decodeSpecialSymbols(result);
    }

    public String replaceAllTags(String self, String s1, String s2, Pattern regex) {
        return replaceAllTags(self, s1, s2, regex, links, getRelativeRootPath(), savedRootDoc, this);
    }

    // TODO: this should go away once we have proper tags
    public static String replaceAllTags(String self, String s1, String s2, Pattern regex, List<LinkArgument> links, String relPath, GroovyRootDoc rootDoc, SimpleGroovyClassDoc classDoc) {
        Matcher matcher = regex.matcher(self);
        if (matcher.find()) {
            matcher.reset();
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String tagname = matcher.group(1);
                if (!"interface".equals(tagname)) {
                    String content = encodeSpecialSymbols(matcher.group(2));
                    if ("link".equals(tagname) || "see".equals(tagname)) {
                        content = getDocUrl(content, false, links, relPath, rootDoc, classDoc);
                    }
                    matcher.appendReplacement(sb, s1 + content + s2);
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
    }

    // TODO: is there a better way to do this?
    public String replaceAllTagsCollated(String self, String preKey, String postKey,
                                         String valueSeparator, String postValues, Pattern regex) {
        Matcher matcher = regex.matcher(self + " @endMarker");
        if (matcher.find()) {
            matcher.reset();
            Map<String, List<String>> savedTags = new LinkedHashMap<String, List<String>>();
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String tagname = matcher.group(1);
                if (!"interface".equals(tagname)) {
                    String content = encodeSpecialSymbols(matcher.group(2));
                    if ("see".equals(tagname) || "link".equals(tagname)) {
                        content = getDocUrl(content);
                    } else if ("param".equals(tagname)) {
                        int index = content.indexOf(' ');
                        if (index >= 0) {
                            String paramName = content.substring(0, index);
                            String paramDesc = content.substring(index);
                            if (paramName.startsWith("<") && paramName.endsWith(">")) {
                                paramName = paramName.substring(1, paramName.length() - 1);
                                tagname = "typeparam";
                            }
                            content = "<code>" + paramName + "</code> - " + paramDesc;
                        }
                    }
                    if (TAG_TEXT.containsKey(tagname)) {
                        String text = TAG_TEXT.get(tagname);
                        List<String> contents = savedTags.computeIfAbsent(text, k -> new ArrayList<String>());
                        contents.add(content);
                        matcher.appendReplacement(sb, "");
                    } else {
                        matcher.appendReplacement(sb, preKey + tagname + postKey + content + postValues);
                    }
                }
            }
            matcher.appendTail(sb);
            // remove @endMarker
            sb = new StringBuffer(sb.substring(0, sb.length() - 10));
            for (Map.Entry<String, List<String>> e : savedTags.entrySet()) {
                sb.append(preKey);
                sb.append(e.getKey());
                sb.append(postKey);
                sb.append(DefaultGroovyMethods.join((Iterable)e.getValue(), valueSeparator));
                sb.append(postValues);
            }
            return sb.toString();
        } else {
            return self;
        }
    }

    /**
     * Replaces angle brackets inside a tag.
     *
     * @param text GroovyDoc text to process
     * @param regex has to capture tag name in group 1 and tag body in group 2
     */
    public static String encodeAngleBracketsInTagBody(String text, Pattern regex) {
        Matcher matcher = regex.matcher(text);
        if (matcher.find()) {
            matcher.reset();
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String tagName = matcher.group(1);
                String tagBody = matcher.group(2);
                String encodedBody = Matcher.quoteReplacement(encodeAngleBrackets(tagBody));
                String replacement = "{@" + tagName + " " + encodedBody + "}";
                matcher.appendReplacement(sb, replacement);
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return text;
        }
    }

    public static String encodeAngleBrackets(String text) {
        return text == null ? null : text.replace("<", "&lt;").replace(">", "&gt;");
    }

    public static String encodeSpecialSymbols(String text) {
        return Matcher.quoteReplacement(text.replace("@", "&at;"));
    }

    public static String decodeSpecialSymbols(String text) {
        return text.replace("&at;", "@");
    }

    public void setNameWithTypeArgs(String nameWithTypeArgs) {
        this.nameWithTypeArgs = nameWithTypeArgs;
    }

    public String getNameWithTypeArgs() {
        return nameWithTypeArgs;
    }
}
