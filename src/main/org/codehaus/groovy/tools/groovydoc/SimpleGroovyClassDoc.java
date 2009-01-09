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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleGroovyClassDoc extends SimpleGroovyProgramElementDoc implements GroovyClassDoc {
    private static final Pattern TAG_REGEX = Pattern.compile("(?m)@([a-z]+)\\s+(.*$[^@]*)");
    private static final Pattern LINK_REGEX = Pattern.compile("(?m)[{]@(link)\\s+([^}]*)}");
    private static final Pattern CODE_REGEX = Pattern.compile("(?m)[{]@(code)\\s+([^}]*)}");
    private static SimpleGroovyClassDoc object;

    private final List<GroovyConstructorDoc> constructors;
    private final List<GroovyFieldDoc> fields;
    private final List<GroovyFieldDoc> enumConstants;
    private final List<GroovyMethodDoc> methods;
    private final List<String> importedClassesAndPackages;
    private final List<String> interfaceNames;
    private final List<GroovyClassDoc> interfaceClasses;
    private final List<String> annotationNames;
    private final List<GroovyClassDoc> annotationClasses;
    private final List<Groovydoc.LinkArgument> links;
    private final List<GroovyClassDoc> superClasses;
    private final List<String> superClassNames;
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
        annotationNames = new ArrayList<String>();
        annotationClasses = new ArrayList<GroovyClassDoc>();
        superClassNames = new ArrayList<String>();
        superClasses = new ArrayList<GroovyClassDoc>();
    }

    public SimpleGroovyClassDoc(List<String> importedClassesAndPackages, String name) {
        this(importedClassesAndPackages, name, new ArrayList<Groovydoc.LinkArgument>());
    }

    private SimpleGroovyClassDoc getDefaultObject() {
        if (object == null) {
            object = new SimpleGroovyClassDoc(null, "Object");
            object.setFullPathName("java/lang/Object");
        }
        return object;
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

    public List<String> getSuperClassNames() {
        return superClassNames;
    }

    public void addSuperClassName(String className) {
        superClassNames.add(className);
    }

    public GroovyClassDoc superclass() {
        if (superClasses.size() == 0) {
            if ("java.lang.Object".equals(fullDottedName())) return null;
            return getDefaultObject();
        }
        return superClasses.get(0);
    }

    public List<GroovyClassDoc> superclasses() {
        return superClasses;
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

    public List<String> getParentClasses() {
        List<String> result = new LinkedList<String>();
        result.add(0, getFullName(this));
        SimpleGroovyClassDoc next = this;
        while (next.superclass() != null) {
            next = (SimpleGroovyClassDoc) next.superclass();
            result.add(0, getFullName(next));
        }
        if ("java.lang.Object".equals(next.fullDottedName())) {
            return result;
        }
        Class nextClass = getClassOf(next.name());
        while (nextClass != null && nextClass.getSuperclass() != null && !Object.class.equals(nextClass)) {
            nextClass = nextClass.getSuperclass();
            result.add(0, nextClass.getName());
        }
        return result;
    }

    private String getFullName(SimpleGroovyClassDoc next) {
        return (next.getFullPathName() != null ? next.getFullPathName() : next.name()).replace("/", ".");
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

        for (String name : superClassNames) {
            superClasses.add(resolveClass(rootDoc, name));
        }

        for (String name : interfaceNames) {
            interfaceClasses.add(resolveClass(rootDoc, name));
        }

        for (String name : annotationNames) {
            annotationClasses.add(resolveClass(rootDoc, name));
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
        if (type.indexOf('.') == -1)
            type = resolveExternalClass(type);
        if (type.indexOf('.') == -1)
            return type;

        final String[] target = type.split("#");
        String shortClassName = target[0].replaceAll(".*\\.", "");
        shortClassName += (target.length > 1 ? "#" + target[1].split("\\(")[0] : "");
        String name = full ? target[0] : shortClassName;
        if (shortClassName.startsWith("groovy.") || shortClassName.startsWith("org.codehaus.groovy.")) {
            return buildUrl(getRelativeRootPath(), target, name);
        }
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
        SimpleGroovyClassDoc doc = (SimpleGroovyClassDoc) rootDoc.classNamed(name);
        if (doc == null) {
            // The class is not in the tree being documented
            String shortname = name;
            int slashIndex = name.lastIndexOf("/");
            if (slashIndex > 0) {
                shortname = name.substring(slashIndex + 1);
            } else {
                name = resolveExternalClass(name);
            }
            doc = new SimpleGroovyClassDoc(null, shortname); // dummy class with name, not to be put into main tree
            doc.setFullPathName(name);
        }
        return doc;
    }

    private String resolveExternalClass(String name) {
        for (String importName : importedClassesAndPackages) {
            if (importName.endsWith("/*")) {
                String candidate = importName.substring(0, importName.length() - 2).replace('/', '.') + "." + name;
                try {
                    // TODO cache these
                    Class.forName(candidate);
//                    SimpleGroovyClassDoc doc = new SimpleGroovyClassDoc(null, name);
//                    doc.setFullPathName(candidate);
                    return candidate;
                } catch (ClassNotFoundException e) {
                    // ignore
                }
            }
        }
        return name;
    }

    // methods from GroovyClassDoc

    public GroovyConstructorDoc[] constructors(boolean filter) {/*todo*/
        return null;
    }

    public GroovyClassDoc[] annotationClasses(boolean filter) {
        Collections.sort(annotationClasses);
        return annotationClasses.toArray(new GroovyClassDoc[interfaceClasses.size()]);
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

    public GroovyClassDoc[] innerClasses() {/*todo*/
        return null;
    } // not supported in groovy

    public GroovyClassDoc[] innerClasses(boolean filter) {/*todo*/
        return null;
    } // not supported in groovy

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
//	public GroovyTypeVariable[] typeParameters() {/*todo*/return null;} // not supported in groovy
//	public GroovyParamTag[] typeParamTags() {/*todo*/return null;} // not supported in groovy


    // methods from GroovyType (todo: remove this horrible copy of SimpleGroovyType.java)

    //	public GroovyAnnotationTypeDoc asAnnotationTypeDoc() {/*todo*/return null;}
    public GroovyClassDoc asClassDoc() {/*todo*/
        return null;
    }
//	public GroovyParameterizedType asParameterizedType() {/*todo*/return null;}
//	public GroovyTypeVariable asTypeVariable() {/*todo*/return null;}

    //	public GroovyWildcardType asWildcardType() {/*todo*/return null;}
    public String dimension() {/*todo*/
        return null;
    }

    public boolean isPrimitive() {/*todo*/
        return false;
    }

    public String qualifiedTypeName() {/*todo*/
        return null;
    }

    public String simpleTypeName() {/*todo*/
        return null;
    }

    public String typeName() {/*todo*/
        return null;
    }

    public String fullDottedName() {
        return fullPathName == null ? "null" : fullPathName.replace('/', '.');
    }

    public void addInterfaceName(String className) {
        interfaceNames.add(className);
    }

    public void addAnnotationName(String className) {
        annotationNames.add(className);
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
