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
package org.codehaus.groovy.tools.groovydoc;

import org.codehaus.groovy.ant.Groovydoc;
import org.codehaus.groovy.groovydoc.*;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleGroovyClassDoc extends SimpleGroovyProgramElementDoc implements GroovyClassDoc {
    private static final Pattern TAG_REGEX = Pattern.compile("(?m)@([a-z]+)\\s+(.*$[^@]*)");
    private static final Pattern LINK_REGEX = Pattern.compile("(?m)[{]@(link)\\s+([^}]*)}");
    private static final Pattern CODE_REGEX = Pattern.compile("(?m)[{]@(code)\\s+([^}]*)}");

    private final List<GroovyConstructorDoc> constructors;
    private final List<GroovyFieldDoc> fields;
    private final List<GroovyFieldDoc> enumConstants;
    private final List<GroovyMethodDoc> methods;
    private final List<String> importedClassesAndPackages;
    private final List<String> interfaceNames;
    private final List<GroovyClassDoc> interfaceClasses;
    private final List<GroovyClassDoc> nested;
    private final List<Groovydoc.LinkArgument> links;
    private GroovyClassDoc superClass;
    private GroovyClassDoc outer;
    private String superClassName;
    private String fullPathName;

    public SimpleGroovyClassDoc(List<String> importedClassesAndPackages, String name, List<Groovydoc.LinkArgument> links) {
        super(name);
        this.importedClassesAndPackages = importedClassesAndPackages;
        this.links = links;
        constructors = new ArrayList<GroovyConstructorDoc>();
        fields = new ArrayList<GroovyFieldDoc>();
        enumConstants = new ArrayList<GroovyFieldDoc>();
        methods = new ArrayList<GroovyMethodDoc>();
        interfaceNames = new ArrayList<String>();
        interfaceClasses = new ArrayList<GroovyClassDoc>();
        nested = new ArrayList<GroovyClassDoc>();
    }

    public SimpleGroovyClassDoc(List<String> importedClassesAndPackages, String name) {
        this(importedClassesAndPackages, name, new ArrayList<Groovydoc.LinkArgument>());
    }

    /**
     * returns a sorted array of constructors
     */
    public GroovyConstructorDoc[] constructors() {
        Collections.sort(constructors);
        return constructors.toArray(new GroovyConstructorDoc[constructors.size()]);
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

    /**
     * returns a sorted array of nested classes and interfaces
     */
    public GroovyClassDoc[] innerClasses() {
        Collections.sort(nested);
        return nested.toArray(new GroovyClassDoc[nested.size()]);
    }

    public boolean addNested(GroovyClassDoc nestedClass) {
        return nested.add(nestedClass);
    }

    /**
     * returns a sorted array of fields
     */
    public GroovyFieldDoc[] fields() {
        Collections.sort(fields);
        return fields.toArray(new GroovyFieldDoc[fields.size()]);
    }

    public boolean add(GroovyFieldDoc field) {
        return fields.add(field);
    }

    /**
     * returns a sorted array of enum constants
     */
    public GroovyFieldDoc[] enumConstants() {
        Collections.sort(enumConstants);
        return enumConstants.toArray(new GroovyFieldDoc[enumConstants.size()]);
    }

    public boolean addEnumConstant(GroovyFieldDoc field) {
        return enumConstants.add(field);
    }

    /**
     * returns a sorted array of methods
     */
    public GroovyMethodDoc[] methods() {
        Collections.sort(methods);
        return methods.toArray(new GroovyMethodDoc[methods.size()]);
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

    public GroovyClassDoc superclass() {
        return superClass;
    }

    public void setSuperClass(GroovyClassDoc doc) {
        superClass = doc;
    }

    public String getFullPathName() {
        return fullPathName;
    }

    public void setFullPathName(String fullPathName) {
        this.fullPathName = fullPathName;
    }

    public String getRelativeRootPath() {
        StringTokenizer tokenizer = new StringTokenizer(fullPathName, "/"); // todo windows??
        StringBuffer sb = new StringBuffer();
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
        Set<GroovyClassDoc> result = new HashSet<GroovyClassDoc>();
        result.add(this);
        Set<GroovyClassDoc> next = new HashSet<GroovyClassDoc>();
        next.addAll(Arrays.asList(this.interfaces()));
        while (next.size() > 0) {
            Set<GroovyClassDoc> temp = next;
            next = new HashSet<GroovyClassDoc>();
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
        Set<GroovyClassDoc> result = new HashSet<GroovyClassDoc>();
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
            return Class.forName(next.replace("/", "."));
        } catch (Throwable t) {
            return null;
        }
    }

    void resolve(GroovyRootDoc rootDoc) {
        Map visibleClasses = rootDoc.getVisibleClasses(importedClassesAndPackages);

        // resolve constructor parameter types
        for (GroovyConstructorDoc constructor : constructors) {

            // parameters
            for (GroovyParameter groovyParameter : constructor.parameters()) {
                SimpleGroovyParameter param = (SimpleGroovyParameter) groovyParameter;
                String paramTypeName = param.typeName();
                if (visibleClasses.containsKey(paramTypeName)) {
                    param.setType((GroovyType) visibleClasses.get(paramTypeName));
                }
            }
        }

        for (GroovyFieldDoc field : fields) {
            SimpleGroovyFieldDoc mutableField = (SimpleGroovyFieldDoc) field;
            GroovyType fieldType = field.type();
            String typeName = fieldType.typeName();
            if (visibleClasses.containsKey(typeName)) {
                mutableField.setType((GroovyType) visibleClasses.get(typeName));
            }
        }

        // resolve method return types and parameter types
        for (GroovyMethodDoc method : methods) {

            // return types
            GroovyType returnType = method.returnType();
            String typeName = returnType.typeName();
            if (visibleClasses.containsKey(typeName)) {
                method.setReturnType((GroovyType) visibleClasses.get(typeName));
            }

            // parameters
            for (GroovyParameter groovyParameter : method.parameters()) {
                SimpleGroovyParameter param = (SimpleGroovyParameter) groovyParameter;
                String paramTypeName = param.typeName();
                if (visibleClasses.containsKey(paramTypeName)) {
                    param.setType((GroovyType) visibleClasses.get(paramTypeName));
                }
            }
        }

        if (superClassName != null && superClass == null) {
            superClass = resolveClass(rootDoc, superClassName);
        }

        for (String name : interfaceNames) {
            interfaceClasses.add(resolveClass(rootDoc, name));
        }

        for (GroovyAnnotationRef annotation : annotations()) {
            SimpleGroovyAnnotationRef ref = (SimpleGroovyAnnotationRef) annotation;
            ref.setType(resolveClass(rootDoc, ref.name()));
        }
    }

    public String getDocUrl(String type) {
        return getDocUrl(type, false);
    }

    public String getDocUrl(String type, boolean full) {
        if (type == null)
            return type;
        type = type.trim();
        if (type.startsWith("#"))
            return "<a href='" + type + "'>" + type + "</a>";
        if (type.endsWith("[]")) {
            return getDocUrl(type.substring(0, type.length() - 2), full) + "[]";
        }
        // TODO move next 4 lines to resolve?
        if (type.indexOf('.') == -1) {
            Class c = resolveExternalClassFromImport(type);
            if (c != null) type = c.getName();
        }
        if (type.indexOf('.') == -1)
            return type;

        final String[] target = type.split("#");
        String shortClassName = target[0].replaceAll(".*\\.", "");
        shortClassName += (target.length > 1 ? "#" + target[1].split("\\(")[0] : "");
        String name = full ? target[0] : shortClassName;
        for (Groovydoc.LinkArgument link : links) {
            final StringTokenizer tokenizer = new StringTokenizer(link.getPackages(), ", ");
            while (tokenizer.hasMoreTokens()) {
                final String token = tokenizer.nextToken();
                if (type.startsWith(token)) {
                    return buildUrl(link.getHref(), target, name);
                }
            }
        }
        return type;
    }

    private String buildUrl(String relativeRoot, String[] target, String shortClassName) {
        if (!relativeRoot.endsWith("/")) {
            relativeRoot += "/";
        }
        String url = relativeRoot + target[0].replace('.', '/') + ".html" + (target.length > 1 ? "#" + target[1] : "");
        return "<a href='" + url + "' title='" + shortClassName + "'>" + shortClassName + "</a>";
    }

    private GroovyClassDoc resolveClass(GroovyRootDoc rootDoc, String name) {
        GroovyClassDoc doc = rootDoc.classNamed(name);
        if (doc != null) return doc;

        // The class is not in the tree being documented
        String shortname = name;
        int slashIndex = name.lastIndexOf("/");
        Class c = null;
        if (slashIndex > 0) {
            shortname = name.substring(slashIndex + 1);
            c = resolveExternalClass(name);
        } else {
            c = resolveExternalClassFromImport(name);
        }
        if (c != null) {
            return new ExternalGroovyClassDoc(c);
        }

        // and we can't find it
        SimpleGroovyClassDoc placeholder = new SimpleGroovyClassDoc(null, shortname);
        placeholder.setFullPathName(name);
        return placeholder;
    }

    private Class resolveExternalClassFromImport(String name) {
        for (String importName : importedClassesAndPackages) {
            if (importName.endsWith("/*")) {
                String candidate = importName.substring(0, importName.length() - 2).replace('/', '.') + "." + name;
                try {
                    // TODO cache these??
                    return Class.forName(candidate);
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        return null;
    }

    private Class resolveExternalClass(String name) {
        String candidate = name.replace('/', '.');
        try {
            // TODO cache these??
            return Class.forName(candidate);
        } catch (ClassNotFoundException e) {
            // ignore
        }
        return null;
    }

    // methods from GroovyClassDoc

    public GroovyConstructorDoc[] constructors(boolean filter) {/*todo*/
        return null;
    }

    public boolean definesSerializableFields() {/*todo*/
        return false;
    }

    public GroovyFieldDoc[] fields(boolean filter) {/*todo*/
        return null;
    }

    public GroovyClassDoc findClass(String className) {/*todo*/
        return null;
    }

    public GroovyClassDoc[] importedClasses() {/*todo*/
        return null;
    }

    public GroovyPackageDoc[] importedPackages() {/*todo*/
        return null;
    }

    public GroovyClassDoc[] innerClasses(boolean filter) {/*todo*/
        return null;
    }

    public GroovyClassDoc[] interfaces() {
        Collections.sort(interfaceClasses);
        return interfaceClasses.toArray(new GroovyClassDoc[interfaceClasses.size()]);
    }

    public GroovyType[] interfaceTypes() {/*todo*/
        return null;
    }

    public boolean isAbstract() {/*todo*/
        return false;
    }

    public boolean isExternalizable() {/*todo*/
        return false;
    }

    public boolean isSerializable() {/*todo*/
        return false;
    }

    public GroovyMethodDoc[] methods(boolean filter) {/*todo*/
        return null;
    }

    public GroovyFieldDoc[] serializableFields() {/*todo*/
        return null;
    }

    public GroovyMethodDoc[] serializationMethods() {/*todo*/
        return null;
    }

    public boolean subclassOf(GroovyClassDoc gcd) {/*todo*/
        return false;
    }

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

    public boolean isPrimitive() {/*todo*/
        return false;
    }

    public String qualifiedTypeName() {
        return fullPathName == null ? "null" : fullPathName.replace('/', '.');
    }

    public String simpleTypeName() {/*todo*/
        return null;
    }

    public String typeName() {/*todo*/
        return null;
    }

    public void addInterfaceName(String className) {
        interfaceNames.add(className);
    }

    public void setRawCommentText(String rawCommentText) {
        super.setRawCommentText(rawCommentText);
        setCommentText(replaceTags(rawCommentText));
     }

    public String replaceTags(String comment) {
        String result = comment.replaceAll("(?m)^\\s*\\*", ""); // todo precompile regex

        // {@link processing hack}
        result = replaceAllTags(result, "", "", LINK_REGEX);

        // {@code processing hack}
        result = replaceAllTags(result, "<TT>", "</TT>", CODE_REGEX);

        // hack to reformat other groovydoc tags (@see, @return, @link, @param, @throws, @author, @since) into html
        // todo: replace with proper tag support
        result = replaceAllTags(result, "<DL><DT><B>$1:</B></DT><DD>", "</DD></DL>", TAG_REGEX);

        return decodeSpecialSymbols(result);
    }

    // TODO: this should go away once we have proper tags
    public String replaceAllTags(String self, String s1, String s2, Pattern regex) {
        Matcher matcher = regex.matcher(self);
        if (matcher.find()) {
            matcher.reset();
            StringBuffer sb = new StringBuffer();
            while (matcher.find()) {
                String tagname = matcher.group(1);
                if (tagname.equals("see") || tagname.equals("link")) {
                    matcher.appendReplacement(sb, s1 + getDocUrl(encodeSpecialSymbols(matcher.group(2))) + s2);
                } else if (!tagname.equals("interface")) {
                    matcher.appendReplacement(sb, s1 + encodeSpecialSymbols(matcher.group(2)) + s2);
                }
            }
            matcher.appendTail(sb);
            return sb.toString();
        } else {
            return self;
        }
    }

    private String encodeSpecialSymbols(String text) {
        return Matcher.quoteReplacement(text.replaceAll("@", "&at;"));
    }

    private String decodeSpecialSymbols(String text) {
        return text.replaceAll("&at;", "@");
    }

}
