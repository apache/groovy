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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.LineColumn;
import org.codehaus.groovy.antlr.SourceBuffer;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.antlr.treewalker.VisitorAdapter;
import org.codehaus.groovy.groovydoc.GroovyConstructorDoc;
import org.codehaus.groovy.groovydoc.GroovyClassDoc;
import org.codehaus.groovy.ant.Groovydoc;
import antlr.collections.AST;

public class SimpleGroovyClassDocAssembler extends VisitorAdapter implements GroovyTokenTypes {
    private static final String FS = "/";
    private Stack<GroovySourceAST> stack;
    private Map<String, GroovyClassDoc> classDocs;
    private List<String> importedClassesAndPackages;
    private List<Groovydoc.LinkArgument> links;
    private SimpleGroovyClassDoc currentClassDoc; // todo - stack?
    private SimpleGroovyConstructorDoc currentConstructorDoc; // todo - stack?
    private SimpleGroovyMethodDoc currentMethodDoc; // todo - stack?
    private SourceBuffer sourceBuffer;
    private String packagePath;
    private Pattern previousJavaDocCommentPattern;
    private LineColumn lastLineCol;
    private boolean insideEnum;

    public SimpleGroovyClassDocAssembler(String packagePath, String file, SourceBuffer sourceBuffer, List<Groovydoc.LinkArgument> links) {
        this.sourceBuffer = sourceBuffer;
        this.packagePath = packagePath;
        this.links = links;

        stack = new Stack<GroovySourceAST>();
        classDocs = new HashMap<String, GroovyClassDoc>();
        String className = file;
        if (file != null) {
            // todo: replace this simple idea of default class name
            int idx = file.lastIndexOf(".");
            className = file.substring(0, idx);
        }

        importedClassesAndPackages = new ArrayList<String>();
        importedClassesAndPackages.add(packagePath + "/*");  // everything in this package is automatically imported
        importedClassesAndPackages.add("groovy/lang/*");     // default imports in Groovy, from org.codehaus.groovy.control.ResolveVisitor.DEFAULT_IMPORTS
        importedClassesAndPackages.add("groovy/util/*");     // todo - non Groovy source files shouldn't import these, but let us import them for now, it won't hurt...

        currentClassDoc = new SimpleGroovyClassDoc(importedClassesAndPackages, className, links);
        currentClassDoc.setFullPathName(packagePath + FS + className);
        classDocs.put(currentClassDoc.getFullPathName(), currentClassDoc);
        lastLineCol = new LineColumn(1, 1);
        previousJavaDocCommentPattern = Pattern.compile("(?s)/\\*\\*(.*?)\\*/");
    }

    public Map<String, GroovyClassDoc> getGroovyClassDocs() {
        postProcessClassDocs();
        return classDocs;
    }

    // Step through ClassDocs and tie up loose ends
    private void postProcessClassDocs() {
        for (GroovyClassDoc groovyClassDoc : classDocs.values()) {
            SimpleGroovyClassDoc classDoc = (SimpleGroovyClassDoc) groovyClassDoc;

            // potentially add default constructor to class docs (but not interfaces)
            if (classDoc.isClass()) {          // todo Enums, anything else?
                GroovyConstructorDoc[] constructors = classDoc.constructors();
                if (constructors != null && constructors.length == 0) { // add default constructor to doc
                    // name of class for the constructor
                    GroovyConstructorDoc constructorDoc = new SimpleGroovyConstructorDoc(classDoc.name());
                    // don't forget to tell the class about this default constructor.
                    classDoc.add(constructorDoc);
                }
            }
        }
    }

    @Override
    public void visitInterfaceDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            currentClassDoc.setTokenType(t.getType());
            visitClassDef(t, visit);
        }
    }

    @Override
    public void visitEnumDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            currentClassDoc.setTokenType(t.getType());
            visitClassDef(t, visit);
        } else {
            adjustForAutomaticEnumMethods();
        }
    }

    private void adjustForAutomaticEnumMethods() {
        SimpleGroovyMethodDoc valueOf = new SimpleGroovyMethodDoc("valueOf", links);
        valueOf.setRawCommentText("Returns the enum constant of this type with the specified name.");
        SimpleGroovyParameter parameter = new SimpleGroovyParameter("name");
        parameter.setTypeName("String");
        valueOf.add(parameter);
        valueOf.setReturnType(new SimpleGroovyType(currentClassDoc.name()));
        currentClassDoc.add(valueOf);

        SimpleGroovyMethodDoc values = new SimpleGroovyMethodDoc("values", links);
        values.setRawCommentText("Returns an array containing the constants of this enum type, in the order they are declared.");
        values.setReturnType(new SimpleGroovyType(currentClassDoc.name() + "[]"));
        currentClassDoc.add(values);
    }

    @Override
    public void visitAnnotationDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            currentClassDoc.setTokenType(t.getType());
            // TODO do different behavior for annotations to show e.g.
            // retention etc. and required and optional element summary
            visitClassDef(t, visit);
        }
    }

    @Override
    public void visitImport(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            GroovySourceAST child = t.childOfType(DOT);
            if (child == null) {
                child = t.childOfType(IDENT);
            }
            String importTextWithSlashesInsteadOfDots = recurseDownImportBranch(child);
            importedClassesAndPackages.add(importTextWithSlashesInsteadOfDots);
        }
    }

    public String recurseDownImportBranch(GroovySourceAST t) {
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

    @Override
    public void visitExtendsClause(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            GroovySourceAST superClassNode = t.childOfType(IDENT);
            if (superClassNode != null) {
                String superClassName = superClassNode.getText();
                currentClassDoc.setSuperClassName(superClassName); // un 'packaged' class name
            }
        }
    }

    @Override
    public void visitClassDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            // todo is this correct for java + groovy src?
            String className = t.childOfType(IDENT).getText();
            currentClassDoc = (SimpleGroovyClassDoc) classDocs.get(packagePath + FS + className);
            if (currentClassDoc == null) {
                currentClassDoc = new SimpleGroovyClassDoc(importedClassesAndPackages, className, links);
            }
            // comments
            String commentText = getJavaDocCommentsBeforeNode(t);
            currentClassDoc.setRawCommentText(commentText);

            currentClassDoc.setFullPathName(packagePath + FS + currentClassDoc.name());
            classDocs.put(currentClassDoc.getFullPathName(), currentClassDoc);
        }
    }

    @Override
    public void visitCtorIdent(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            if (!insideAnonymousInnerClass()) {
                // now... get relevant values from the AST

                // name of class for the constructor
                currentConstructorDoc = new SimpleGroovyConstructorDoc(currentClassDoc.name());

                // comments
                String commentText = getJavaDocCommentsBeforeNode(t);
                currentConstructorDoc.setRawCommentText(commentText);

                // modifiers
                processModifiers(t, currentConstructorDoc);

                addParametersTo(currentConstructorDoc, t, visit);

                // don't forget to tell the class about this constructor.
                currentClassDoc.add(currentConstructorDoc);
            }
        }
    }

    @Override
    public void visitMethodDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT && !insideEnum) {
            if (!insideAnonymousInnerClass()) {
                // init

                // now... get relevant values from the AST

                // method name
                String methodName = t.childOfType(IDENT).getText();
                currentMethodDoc = new SimpleGroovyMethodDoc(methodName, links);

                // comments
                String commentText = getJavaDocCommentsBeforeNode(t);
                currentMethodDoc.setRawCommentText(commentText);

                // modifiers
                processModifiers(t, currentMethodDoc);

                // return type
                String returnTypeName = getTypeNodeAsText(t.childOfType(TYPE), "def");
                SimpleGroovyType returnType = new SimpleGroovyType(returnTypeName); // todo !!!
                currentMethodDoc.setReturnType(returnType);

                addParametersTo(currentMethodDoc, t, visit);
                currentClassDoc.add(currentMethodDoc);
            }
        }
    }

    @Override
    public void visitAnnotationFieldDef(GroovySourceAST t, int visit) {
        visitVariableDef(t, visit);
    }

    @Override
    public void visitEnumConstantDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            insideEnum = true;
            String enumConstantName = t.childOfType(IDENT).getText();
            SimpleGroovyFieldDoc currentEnumConstantDoc = new SimpleGroovyFieldDoc(enumConstantName);

            // comments
            String commentText = getJavaDocCommentsBeforeNode(t);
            currentEnumConstantDoc.setRawCommentText(commentText);

            // modifiers
            processModifiers(t, currentEnumConstantDoc);

            String typeName = getTypeNodeAsText(t.childOfType(TYPE), currentClassDoc.getTypeDescription());
            SimpleGroovyType type = new SimpleGroovyType(typeName);
            currentEnumConstantDoc.setType(type);

            currentClassDoc.addEnumConstant(currentEnumConstantDoc);
        } else {
            insideEnum = false;
        }
    }

    @Override
    public void visitVariableDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            if (!insideAnonymousInnerClass()) {
                GroovySourceAST parentNode = getParentNode();

                // todo - what about fields in interfaces/enums etc
                if (parentNode != null && parentNode.getType() == OBJBLOCK) {  // this should restrict us to just field definitions, and not local variable definitions

                    // field name
                    String fieldName = t.childOfType(IDENT).getText();
                    SimpleGroovyFieldDoc currentFieldDoc = new SimpleGroovyFieldDoc(fieldName);

                    // comments
                    String commentText = getJavaDocCommentsBeforeNode(t);
                    currentFieldDoc.setRawCommentText(commentText);

                    // modifiers
                    processModifiers(t, currentFieldDoc);

                    // type
                    String typeName = getTypeNodeAsText(t.childOfType(TYPE), "def");
                    SimpleGroovyType type = new SimpleGroovyType(typeName); // todo !!!
                    currentFieldDoc.setType(type);

                    currentClassDoc.add(currentFieldDoc);
                }
            }
        }
    }

    private boolean insideAnonymousInnerClass() {
        GroovySourceAST grandParentNode = getGrandParentNode();
        return grandParentNode != null && grandParentNode.getType() == LITERAL_new;
    }

    private void processModifiers(GroovySourceAST t, SimpleGroovyProgramElementDoc programElementDoc) {
        GroovySourceAST modifiers = t.childOfType(MODIFIERS);
        if (modifiers != null) {
            AST currentModifier = modifiers.getFirstChild();
            boolean seenNonPublicVisibilityModifier = false;
            while (currentModifier != null) {
                int type = currentModifier.getType();
                switch (type) {
                    case LITERAL_protected:
                    case LITERAL_private:
                        seenNonPublicVisibilityModifier = true;
                        break;
                    case LITERAL_static:
                        programElementDoc.setStatic(true);
                        break;
                }
                currentModifier = currentModifier.getNextSibling();
            }
            if (!seenNonPublicVisibilityModifier) {
                // in groovy (and java asts turned into groovy by Groovifier), methods are assumed public, unless informed otherwise
                programElementDoc.setPublic(true);
            }
        }
    }

    // todo - If no comment before node, then get comment from same node on parent class - ouch!

    private String getJavaDocCommentsBeforeNode(GroovySourceAST t) {
        String result = "";
        LineColumn thisLineCol = new LineColumn(t.getLine(), t.getColumn());
        String text = sourceBuffer.getSnippet(lastLineCol, thisLineCol);
        if (text != null) {
            Matcher m = previousJavaDocCommentPattern.matcher(text);
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

    private String getTypeNodeAsText(GroovySourceAST typeNode, String defaultText) {
        String returnValue = defaultText;
        if (typeNode != null &&
                typeNode.getType() == TYPE &&
                typeNode.getNumberOfChildren() > 0) {
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

                // identifiers
                case IDENT:
                    returnValue = child.getText();
                    break;
            }
        }
        return returnValue;
    }

    private void addParametersTo(SimpleGroovyExecutableMemberDoc executableMemberDoc, GroovySourceAST t, int visit) {
        // parameters
        GroovySourceAST parametersNode = t.childOfType(PARAMETERS);
        if (parametersNode != null && parametersNode.getNumberOfChildren() > 0) {
            GroovySourceAST currentNode = (GroovySourceAST) parametersNode.getFirstChild();
            while (currentNode != null) {
                String parameterTypeName = getTypeNodeAsText(currentNode.childOfType(TYPE), "def");
                String parameterName = getText(currentNode.childOfType(IDENT));
                SimpleGroovyParameter parameter = new SimpleGroovyParameter(parameterName);
                parameter.setTypeName(parameterTypeName);
                executableMemberDoc.add(parameter);
                currentNode = (GroovySourceAST) currentNode.getNextSibling();
            }
        }
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

}
