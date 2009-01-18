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

import antlr.collections.AST;
import org.codehaus.groovy.ant.Groovydoc;
import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.antlr.treewalker.VisitorAdapter;
import org.codehaus.groovy.control.ResolveVisitor;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.groovydoc.GroovyConstructorDoc;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SimpleGroovyClassDocAssembler extends VisitorAdapter implements GroovyTokenTypes {
    private static final String FS = "/";
    private static final Pattern PREV_JAVADOC_COMMENT_PATTERN = Pattern.compile("(?s)/\\*\\*(.*?)\\*/");
    private final Stack<GroovySourceAST> stack;
    private Map<String, GroovyClassDoc> classDocs;
    private List<String> importedClassesAndPackages;
    private List<Groovydoc.LinkArgument> links;
    private Properties properties; // TODO use it or lose it
    private SimpleGroovyFieldDoc currentFieldDoc;
    private SourceBuffer sourceBuffer;
    private String packagePath;
    private LineColumn lastLineCol;
    private boolean insideEnum;
    private Map<String, SimpleGroovyClassDoc> foundClasses;

    public SimpleGroovyClassDocAssembler(String packagePath, String file, SourceBuffer sourceBuffer, List<Groovydoc.LinkArgument> links, Properties properties, boolean isGroovy) {
        this.sourceBuffer = sourceBuffer;
        this.packagePath = packagePath;
        this.links = links;
        this.properties = properties;

        stack = new Stack<GroovySourceAST>();
        classDocs = new HashMap<String, GroovyClassDoc>();
        String className = file;
        if (file != null) {
            // todo: replace this simple idea of default class name
            int idx = file.lastIndexOf(".");
            className = file.substring(0, idx);
        }

        importedClassesAndPackages = new ArrayList<String>();
        importedClassesAndPackages.add(packagePath + "/*");  // everything in this package
        if (isGroovy) {
            for (String pkg : ResolveVisitor.DEFAULT_IMPORTS) {
                importedClassesAndPackages.add(pkg.replace('.', '/') + "*");
            }
        } else {
            importedClassesAndPackages.add("java/lang/*");
        }
        SimpleGroovyClassDoc currentClassDoc = new SimpleGroovyClassDoc(importedClassesAndPackages, className, links);
        currentClassDoc.setFullPathName(packagePath + FS + className);
        classDocs.put(currentClassDoc.getFullPathName(), currentClassDoc);
        lastLineCol = new LineColumn(1, 1);
    }

    public Map<String, GroovyClassDoc> getGroovyClassDocs() {
        postProcessClassDocs();
        return classDocs;
    }

    @Override
    public void visitInterfaceDef(GroovySourceAST t, int visit) {
        visitClassDef(t, visit);
    }

    @Override
    public void visitEnumDef(GroovySourceAST t, int visit) {
        visitClassDef(t, visit);
        SimpleGroovyClassDoc currentClassDoc = getCurrentOrTopLevelClassDoc(t);
        if (visit == CLOSING_VISIT && currentClassDoc != null) {
            adjustForAutomaticEnumMethods(currentClassDoc);
        }
    }

    @Override
    public void visitAnnotationDef(GroovySourceAST t, int visit) {
        visitClassDef(t, visit);
    }

    @Override
    public void visitClassDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            SimpleGroovyClassDoc parent = getCurrentClassDoc();
            String shortName = getIdentFor(t);
            String className = shortName;
            if (parent != null && isNested() && !insideAnonymousInnerClass()) {
                className = parent.name() + "." + className;
            } else {
                foundClasses = new HashMap<String, SimpleGroovyClassDoc>();
            }
            SimpleGroovyClassDoc current = (SimpleGroovyClassDoc) classDocs.get(packagePath + FS + className);
            if (current == null) {
                current = new SimpleGroovyClassDoc(importedClassesAndPackages, className, links);
            }
            current.setRawCommentText(getJavaDocCommentsBeforeNode(t));
            current.setFullPathName(packagePath + FS + current.name());
            current.setTokenType(t.getType());
            processAnnotations(t, current);
            classDocs.put(current.getFullPathName(), current);
            foundClasses.put(shortName, current);
            if (parent != null) {
                parent.addNested(current);
                current.setOuter(parent);
            }
        }
    }

    @Override
    public void visitImport(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            String importTextWithSlashesInsteadOfDots = extractImportPath(t);
            importedClassesAndPackages.add(importTextWithSlashesInsteadOfDots);
        }
    }

// TODO is this needed so we can click through on default values?
//    @Override
//    public void visitStaticImport(GroovySourceAST t, int visit) {
//        if (visit == OPENING_VISIT) {
//            // TODO
//            String importTextWithSlashesInsteadOfDots = extractImportPath(t);
//            System.out.println(currentClassDoc.name() + " has static import: " + importTextWithSlashesInsteadOfDots);
//        }
//    }

    @Override
    public void visitExtendsClause(GroovySourceAST t, int visit) {
        SimpleGroovyClassDoc currentClassDoc = getCurrentClassDoc();
        if (visit == OPENING_VISIT) {
            GroovySourceAST superClassNode = t.childOfType(IDENT);
            if (superClassNode != null) {
                String superClassName = extractName(superClassNode);
                if (currentClassDoc.isInterface()) {
                    currentClassDoc.addInterfaceName(superClassName);
                } else {
                    currentClassDoc.setSuperClassName(superClassName);
                }
            }
        }
    }

    @Override
    public void visitImplementsClause(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            GroovySourceAST classNode = t.childOfType(IDENT);
            if (classNode != null) {
                getCurrentClassDoc().addInterfaceName(extractName(classNode));
            }
        }
    }

    @Override
    public void visitCtorIdent(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT && !insideEnum && !insideAnonymousInnerClass()) {
            SimpleGroovyClassDoc currentClassDoc = getCurrentClassDoc();
            SimpleGroovyConstructorDoc currentConstructorDoc = new SimpleGroovyConstructorDoc(currentClassDoc.name(), currentClassDoc);
            currentConstructorDoc.setRawCommentText(getJavaDocCommentsBeforeNode(t));
            processModifiers(t, currentConstructorDoc);
            addParametersTo(t, currentConstructorDoc);
            processAnnotations(t, currentConstructorDoc);
            currentClassDoc.add(currentConstructorDoc);
        }
    }

    @Override
    public void visitMethodDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT && !insideEnum && !insideAnonymousInnerClass()) {
            SimpleGroovyClassDoc currentClassDoc = getCurrentClassDoc();
            if (currentClassDoc != null) {
                String methodName = getIdentFor(t);
                SimpleGroovyMethodDoc currentMethodDoc = new SimpleGroovyMethodDoc(methodName, currentClassDoc);
                currentMethodDoc.setRawCommentText(getJavaDocCommentsBeforeNode(t));
                processModifiers(t, currentMethodDoc);
                currentMethodDoc.setReturnType(new SimpleGroovyType(getTypeOrDefault(t)));
                addParametersTo(t, currentMethodDoc);
                processAnnotations(t, currentMethodDoc);
                currentClassDoc.add(currentMethodDoc);
            }
        }
    }

    @Override
    public void visitAnnotationFieldDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            visitVariableDef(t, visit);
            String defaultText = getDefaultValue(t);
            if (defaultText != null) {
                currentFieldDoc.setConstantValueExpression(defaultText);
                String orig = currentFieldDoc.getRawCommentText();
                currentFieldDoc.setRawCommentText(orig + "\n* @default " + defaultText);
            }
        }
    }

    @Override
    public void visitEnumConstantDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            SimpleGroovyClassDoc currentClassDoc = getCurrentClassDoc();
            insideEnum = true;
            String enumConstantName = getIdentFor(t);
            SimpleGroovyFieldDoc currentEnumConstantDoc = new SimpleGroovyFieldDoc(enumConstantName, currentClassDoc);
            currentEnumConstantDoc.setRawCommentText(getJavaDocCommentsBeforeNode(t));
            processModifiers(t, currentEnumConstantDoc);
            String typeName = getTypeNodeAsText(t.childOfType(TYPE), currentClassDoc.getTypeDescription());
            currentEnumConstantDoc.setType(new SimpleGroovyType(typeName));
            currentClassDoc.addEnumConstant(currentEnumConstantDoc);
        } else if (visit == CLOSING_VISIT) {
            insideEnum = false;
        }
    }

    @Override
    public void visitVariableDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT && !insideAnonymousInnerClass() && isFieldDefinition()) {
            SimpleGroovyClassDoc currentClassDoc = getCurrentClassDoc();
            if (currentClassDoc != null) {
                String fieldName = getIdentFor(t);
                currentFieldDoc = new SimpleGroovyFieldDoc(fieldName, currentClassDoc);
                currentFieldDoc.setRawCommentText(getJavaDocCommentsBeforeNode(t));
                processModifiers(t, currentFieldDoc);
                currentFieldDoc.setType(new SimpleGroovyType(getTypeOrDefault(t)));
                processAnnotations(t, currentFieldDoc);
                currentClassDoc.add(currentFieldDoc);
            }
        }
    }

    // Step through ClassDocs and tie up loose ends
    private void postProcessClassDocs() {
        for (GroovyClassDoc groovyClassDoc : classDocs.values()) {
            SimpleGroovyClassDoc classDoc = (SimpleGroovyClassDoc) groovyClassDoc;

            // potentially add default constructor to class docs (but not interfaces)
            if (classDoc.isClass()) {
                GroovyConstructorDoc[] constructors = classDoc.constructors();
                if (constructors != null && constructors.length == 0) { // add default constructor to doc
                    // name of class for the constructor
                    GroovyConstructorDoc constructorDoc = new SimpleGroovyConstructorDoc(classDoc.name(), classDoc);
                    // don't forget to tell the class about this default constructor.
                    classDoc.add(constructorDoc);
                }
            }
        }
    }

    private boolean isNested() {
        GroovySourceAST gpn = getGrandParentNode();
        return getParentNode() != null && gpn != null && (gpn.getType() == CLASS_DEF || gpn.getType() == INTERFACE_DEF);
    }

    private boolean isTopLevelConstruct(GroovySourceAST node) {
        if (node == null) return false;
        int type = node.getType();
        return type == CLASS_DEF || type == INTERFACE_DEF || type == ANNOTATION_DEF || type == ENUM_DEF;
    }

    private void adjustForAutomaticEnumMethods(SimpleGroovyClassDoc currentClassDoc) {
        SimpleGroovyMethodDoc valueOf = new SimpleGroovyMethodDoc("valueOf", currentClassDoc);
        valueOf.setRawCommentText("Returns the enum constant of this type with the specified name.");
        SimpleGroovyParameter parameter = new SimpleGroovyParameter("name");
        parameter.setTypeName("String");
        valueOf.add(parameter);
        valueOf.setReturnType(new SimpleGroovyType(currentClassDoc.name()));
        currentClassDoc.add(valueOf);

        SimpleGroovyMethodDoc values = new SimpleGroovyMethodDoc("values", currentClassDoc);
        values.setRawCommentText("Returns an array containing the constants of this enum type, in the order they are declared.");
        values.setReturnType(new SimpleGroovyType(currentClassDoc.name() + "[]"));
        currentClassDoc.add(values);
    }

    private String extractImportPath(GroovySourceAST t) {
        GroovySourceAST child = t.childOfType(DOT);
        if (child == null) {
            child = t.childOfType(IDENT);
        }
        return recurseDownImportBranch(child);
    }

    private String recurseDownImportBranch(GroovySourceAST t) {
        if (t != null) {
            if (t.getType() == DOT) {
                GroovySourceAST firstChild = (GroovySourceAST) t.getFirstChild();
                GroovySourceAST secondChild = (GroovySourceAST) firstChild.getNextSibling();
                return (recurseDownImportBranch(firstChild) + "/" + recurseDownImportBranch(secondChild));
            }
            if (t.getType() == IDENT) {
                return t.getText();
            }
            if (t.getType() == STAR) {
                return t.getText();
            }
        }
        return "";
    }

    private void addAnnotationRef(SimpleGroovyProgramElementDoc node, GroovySourceAST t) {
        GroovySourceAST classNode = t.childOfType(IDENT);
        if (classNode != null) {
            node.addAnnotationRef(new SimpleGroovyAnnotationRef(extractName(classNode), getChildTextFromSource(t).trim()));
        }
    }

    private void addAnnotationRef(SimpleGroovyParameter node, GroovySourceAST t) {
        GroovySourceAST classNode = t.childOfType(IDENT);
        if (classNode != null) {
            node.addAnnotationRef(new SimpleGroovyAnnotationRef(extractName(classNode), getChildTextFromSource(t).trim()));
        }
    }

    private void addAnnotationRefs(SimpleGroovyProgramElementDoc node, List<GroovySourceAST> nodes) {
        for (GroovySourceAST t : nodes) {
            addAnnotationRef(node, t);
        }
    }

    private void processAnnotations(GroovySourceAST t, SimpleGroovyProgramElementDoc node) {
        GroovySourceAST modifiers = t.childOfType(MODIFIERS);
        if (modifiers != null) {
            addAnnotationRefs(node, modifiers.childrenOfType(ANNOTATION));
        }
    }

    // hack warning! fragile! TODO find a better way
    private String getDefaultValue(GroovySourceAST t) {
        GroovySourceAST child = (GroovySourceAST) t.getFirstChild();
        if (t.getNumberOfChildren() != 4) return null;
        for (int i = 1; i < t.getNumberOfChildren(); i++) {
            child = (GroovySourceAST) child.getNextSibling();
        }
        GroovySourceAST nodeToProcess = child;
        if (child.getNumberOfChildren() > 0) {
            nodeToProcess = (GroovySourceAST) child.getFirstChild();
        }
        return getChildTextFromSource(nodeToProcess, ";");
    }

    private String getChildTextFromSource(GroovySourceAST child) {
        return sourceBuffer.getSnippet(
                new LineColumn(child.getLine(), child.getColumn()),
                new LineColumn(child.getLineLast(), child.getColumnLast()));
    }

    private String getChildTextFromSource(GroovySourceAST child, String tokens) {
        String text = sourceBuffer.getSnippet(
                new LineColumn(child.getLine(), child.getColumn()),
                new LineColumn(child.getLine() + 1, 0));
        StringTokenizer st = new StringTokenizer(text, tokens);
        return st.nextToken();
    }

    private boolean isFieldDefinition() {
        GroovySourceAST parentNode = getParentNode();
        return parentNode != null && parentNode.getType() == OBJBLOCK;
    }

    private boolean insideAnonymousInnerClass() {
        GroovySourceAST grandParentNode = getGrandParentNode();
        return grandParentNode != null && grandParentNode.getType() == LITERAL_new;
    }

    private void processModifiers(GroovySourceAST t, SimpleGroovyMemberDoc memberDoc) {
        GroovySourceAST modifiers = t.childOfType(MODIFIERS);
        if (modifiers != null) {
            AST currentModifier = modifiers.getFirstChild();
            boolean seenNonPublicVisibilityModifier = false;
            while (currentModifier != null) {
                int type = currentModifier.getType();
                switch (type) {
                    case LITERAL_protected:
                        memberDoc.setProtected(true);
                        seenNonPublicVisibilityModifier = true;
                        break;
                    case LITERAL_private:
                        memberDoc.setPrivate(true);
                        seenNonPublicVisibilityModifier = true;
                        break;
                    case LITERAL_static:
                        memberDoc.setStatic(true);
                        break;
                    case FINAL:
                        memberDoc.setFinal(true);
                        break;
                    case ABSTRACT:
                        memberDoc.setAbstract(true);
                        break;
                }
                currentModifier = currentModifier.getNextSibling();
            }
            if (!seenNonPublicVisibilityModifier) {
                // in groovy (and java asts turned into groovy by Groovifier), methods are assumed public, unless informed otherwise
                memberDoc.setPublic(true);
            }
        }
    }

    // todo - If no comment before node, then get comment from same node on parent class - ouch!

    private String getJavaDocCommentsBeforeNode(GroovySourceAST t) {
        String result = "";
        LineColumn thisLineCol = new LineColumn(t.getLine(), t.getColumn());
        String text = sourceBuffer.getSnippet(lastLineCol, thisLineCol);
        if (text != null) {
            Matcher m = PREV_JAVADOC_COMMENT_PATTERN.matcher(text);
            if (m.find()) {
                result = m.group(1);
            }
        }
        if (isMajorType(t)) {
            lastLineCol = thisLineCol;
        }
        return result;
    }

    private boolean isMajorType(GroovySourceAST t) {
        if (t == null) return false;
        int tt = t.getType();
        return tt == CLASS_DEF || tt == INTERFACE_DEF || tt == METHOD_DEF || tt == ANNOTATION_DEF ||
                tt == VARIABLE_DEF || tt == ANNOTATION_FIELD_DEF || tt == ENUM_CONSTANT_DEF;
    }

    private String getText(GroovySourceAST node) {
        String returnValue = null;
        if (node != null) {
            returnValue = node.getText();
        }
        return returnValue;
    }

    // preempt resolve as info is available here - TODO is this always safe?
    private String extractName(GroovySourceAST typeNode) {
        String typeName = typeNode.getText();
        if (typeName.indexOf(".") == -1) {
            for (String name : importedClassesAndPackages) {
                if (name.endsWith(typeName)) {
                    typeName = name;
                }
            }
        }
        return typeName;
    }

    private String getTypeOrDefault(GroovySourceAST t) {
        GroovySourceAST typeNode = t.childOfType(TYPE);
        return getTypeNodeAsText(typeNode, "def");
    }

    private String getTypeNodeAsText(GroovySourceAST typeNode, String defaultText) {
        if (typeNode != null && typeNode.getType() == TYPE && typeNode.getNumberOfChildren() > 0) {
            return getAsText(typeNode, defaultText);
        }
        return defaultText;
    }

    private String getAsText(GroovySourceAST typeNode, String defaultText) {
        String returnValue = defaultText;
        GroovySourceAST child = (GroovySourceAST) typeNode.getFirstChild(); // assume type has only one child // todo type of "foo.bar.Wibble"
        switch (child.getType()) {
            // literals
            case LITERAL_boolean:
                returnValue = "boolean";
                break;
            case LITERAL_byte:
                returnValue = "byte";
                break;
            case LITERAL_char:
                returnValue = "char";
                break;
            // note: LITERAL_def never created
            case LITERAL_double:
                returnValue = "double";
                break;
            case LITERAL_float:
                returnValue = "float";
                break;
            case LITERAL_int:
                returnValue = "int";
                break;
            case LITERAL_long:
                returnValue = "long";
                break;
            case LITERAL_short:
                returnValue = "short";
                break;
            case LITERAL_void:
                returnValue = "void";
                break;
            case ARRAY_DECLARATOR:
                String componentType = getAsText(child, defaultText);
                if (!componentType.equals("def")) returnValue = componentType + "[]";
                break;

            // identifiers
            case IDENT:
                returnValue = child.getText();
                break;
        }
        return returnValue;
    }

    private void addParametersTo(GroovySourceAST t, SimpleGroovyExecutableMemberDoc executableMemberDoc) {
        // parameters
        GroovySourceAST parametersNode = t.childOfType(PARAMETERS);
        if (parametersNode != null && parametersNode.getNumberOfChildren() > 0) {
            GroovySourceAST currentNode = (GroovySourceAST) parametersNode.getFirstChild();
            while (currentNode != null) {
                String parameterTypeName = getTypeOrDefault(currentNode);
                String parameterName = getText(currentNode.childOfType(IDENT));
                SimpleGroovyParameter parameter = new SimpleGroovyParameter(parameterName);
                parameter.setTypeName(parameterTypeName);
                GroovySourceAST modifiers = currentNode.childOfType(MODIFIERS);
                if (modifiers != null) {
                    List<GroovySourceAST> annotations = modifiers.childrenOfType(ANNOTATION);
                    for (GroovySourceAST a : annotations) {
                        addAnnotationRef(parameter, a);
                    }
                }
                executableMemberDoc.add(parameter);
                if (currentNode.getNumberOfChildren() == 4) {
                    handleDefaultValue(currentNode, parameter);
                }
                currentNode = (GroovySourceAST) currentNode.getNextSibling();
            }
        }
    }

    private void handleDefaultValue(GroovySourceAST currentNode, SimpleGroovyParameter parameter) {
        GroovySourceAST paramPart = (GroovySourceAST) currentNode.getFirstChild();
        for (int i = 1; i < currentNode.getNumberOfChildren(); i++) {
            paramPart = (GroovySourceAST) paramPart.getNextSibling();
        }
        GroovySourceAST nodeToProcess = paramPart;
        if (paramPart.getNumberOfChildren() > 0) {
            nodeToProcess = (GroovySourceAST) paramPart.getFirstChild();
        }
        // hack warning!
        // TODO handle , and ) when they occur within Strings
        parameter.setDefaultValue(getChildTextFromSource(nodeToProcess, ",)"));
    }

    public void push(GroovySourceAST t) {
        stack.push(t);
    }

    public GroovySourceAST pop() {
        if (!stack.empty()) {
            return stack.pop();
        }
        return null;
    }

    private GroovySourceAST getParentNode() {
        GroovySourceAST parentNode = null;
        GroovySourceAST currentNode = stack.pop();
        if (!stack.empty()) {
            parentNode = stack.peek();
        }
        stack.push(currentNode);
        return parentNode;
    }

    private GroovySourceAST getGrandParentNode() {
        GroovySourceAST grandParentNode = null;
        GroovySourceAST parentNode;
        GroovySourceAST currentNode = stack.pop();
        if (!stack.empty()) {
            parentNode = stack.pop();
            if (!stack.empty()) {
                grandParentNode = stack.peek();
            }
            stack.push(parentNode);
        }
        stack.push(currentNode);
        return grandParentNode;
    }

    private SimpleGroovyClassDoc getCurrentOrTopLevelClassDoc(GroovySourceAST node) {
        SimpleGroovyClassDoc current = getCurrentClassDoc();
        if (current != null) return current;
        return foundClasses.get(getIdentFor(node));
    }

    private SimpleGroovyClassDoc getCurrentClassDoc() {
        GroovySourceAST pn = getParentNode();
        if (isTopLevelConstruct(pn)) return foundClasses.get(getIdentFor(pn));
        GroovySourceAST gpn = getGrandParentNode();
        if (isTopLevelConstruct(gpn)) return foundClasses.get(getIdentFor(gpn));
        return null;
    }

    private String getIdentFor(GroovySourceAST gpn) {
        return gpn.childOfType(IDENT).getText();
    }
}
