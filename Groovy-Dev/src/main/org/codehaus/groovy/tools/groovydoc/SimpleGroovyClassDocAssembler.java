/*
 * Copyright 2003-2007 the original author or authors.
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
import org.codehaus.groovy.groovydoc.GroovyFieldDoc;
import antlr.collections.AST;

public class SimpleGroovyClassDocAssembler extends VisitorAdapter {
    private Stack stack;
	private Map classDocs;
	private SimpleGroovyClassDoc currentClassDoc; // todo - stack?
	private SimpleGroovyConstructorDoc currentConstructorDoc; // todo - stack?
	private SimpleGroovyMethodDoc currentMethodDoc; // todo - stack?
	private SourceBuffer sourceBuffer;
	private String packagePath;
	private Pattern previousJavaDocCommentPattern;
	private static final String FS = "/";
    private List importedClassesAndPackages;
    private List links;

    public SimpleGroovyClassDocAssembler(String packagePath, String file, SourceBuffer sourceBuffer, List links) {
		this.sourceBuffer = sourceBuffer;
		this.packagePath = packagePath;		
		this.links = links;

		stack = new Stack();
        classDocs = new HashMap();
        String className = file;
        if (file != null) {
        	// todo: replace this simple idea of default class name
        	int idx = file.lastIndexOf(".");
        	className = file.substring(0,idx);
        }

        importedClassesAndPackages = new ArrayList();
        importedClassesAndPackages.add(packagePath + "/*");  // everything in this package is automatically imported

        importedClassesAndPackages.add("groovy/lang/*");     // default imports in Groovy, from org.codehaus.groovy.control.ResolveVisitor.DEFAULT_IMPORTS
        importedClassesAndPackages.add("groovy/util/*");     // todo - non Groovy source files shouldn't import these, but let us import them for now, it won't hurt...


        currentClassDoc = new SimpleGroovyClassDoc(importedClassesAndPackages, className, links);
		currentClassDoc.setFullPathName(packagePath + FS + className);
		classDocs.put(currentClassDoc.getFullPathName(),currentClassDoc);
		
		previousJavaDocCommentPattern = Pattern.compile("(?s)/\\*\\*(.*?)\\*/");

    }
	
	public Map getGroovyClassDocs() {
		postProcessClassDocs();
		return classDocs;
	}
	
	// Step through ClassDocs and tie up loose ends
	private void postProcessClassDocs() {
		Iterator classDocIterator = classDocs.values().iterator();
		while (classDocIterator.hasNext()) {
			SimpleGroovyClassDoc classDoc = (SimpleGroovyClassDoc) classDocIterator.next();

            // potentially add default constructor to class docs (but not interfaces)
            if (classDoc.isClass()) {               // todo Enums, @Interfaces etc 
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

    public void visitInterfaceDef(GroovySourceAST t, int visit) {
        currentClassDoc.setAsInterfaceDefinition();
    }

    public void visitImport(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            GroovySourceAST child = t.childOfType(GroovyTokenTypes.DOT);
            if (child == null) {
                child = t.childOfType(GroovyTokenTypes.IDENT);
            }
            String importTextWithSlashesInsteadOfDots = recurseDownImportBranch(child);
            importedClassesAndPackages.add(importTextWithSlashesInsteadOfDots);
        }
    }
    public String recurseDownImportBranch(GroovySourceAST t) {
        if (t != null) {
            if (t.getType() == GroovyTokenTypes.DOT) {
                GroovySourceAST firstChild = (GroovySourceAST) t.getFirstChild();
                GroovySourceAST secondChild = (GroovySourceAST) firstChild.getNextSibling();
                return (recurseDownImportBranch(firstChild) + "/" + recurseDownImportBranch(secondChild));
            }
            if (t.getType() == GroovyTokenTypes.IDENT) {
                return t.getText();
            }
            if (t.getType() == GroovyTokenTypes.STAR) {
                return t.getText();
            }
        }
        return "";
    }
    public void visitExtendsClause(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
        	GroovySourceAST superClassNode = t.childOfType(GroovyTokenTypes.IDENT);
        	if (superClassNode != null) {
        		String superClassName = superClassNode.getText();
        		currentClassDoc.setSuperClassName(superClassName); // un 'packaged' class name
        	}
        }
    }
	
	public void visitClassDef(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            // todo is this correct for java + groovy src?
        	String className = t.childOfType(GroovyTokenTypes.IDENT).getText();
        	currentClassDoc = (SimpleGroovyClassDoc) classDocs.get(packagePath + FS + className);
        	if (currentClassDoc == null) {
        		currentClassDoc = new SimpleGroovyClassDoc(importedClassesAndPackages, className, importedClassesAndPackages);
        	}
    		// comments
    		String commentText = getJavaDocCommentsBeforeNode(t);
    		currentClassDoc.setRawCommentText(commentText);

    		currentClassDoc.setFullPathName(packagePath + FS + currentClassDoc.name());
        	classDocs.put(currentClassDoc.getFullPathName(), currentClassDoc);
        }
    }

	public void visitCtorIdent(GroovySourceAST t,int visit) {
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


	public void visitMethodDef(GroovySourceAST t, int visit) {
    	if (visit == OPENING_VISIT) {
            if (!insideAnonymousInnerClass()) {
                // init

                // now... get relevant values from the AST

                // method name
                String methodName = t.childOfType(GroovyTokenTypes.IDENT).getText();
                currentMethodDoc = new SimpleGroovyMethodDoc(methodName, links);

                // comments
                String commentText = getJavaDocCommentsBeforeNode(t);
                currentMethodDoc.setRawCommentText(commentText);

                // modifiers
                processModifiers(t, currentMethodDoc);

                // return type
                String returnTypeName = getTypeNodeAsText(t.childOfType(GroovyTokenTypes.TYPE),"def");
                SimpleGroovyType returnType = new SimpleGroovyType(returnTypeName); // todo !!!
                currentMethodDoc.setReturnType(returnType);

                addParametersTo(currentMethodDoc, t, visit);

                // don't forget to tell the class about this method so carefully constructed.
                currentClassDoc.add(currentMethodDoc);
            }
        }
	}

    public void visitVariableDef(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            if (!insideAnonymousInnerClass()) {
                GroovySourceAST parentNode = getParentNode();

                // todo - what about fields in interfaces/enums etc
                if (parentNode != null && parentNode.getType() == GroovyTokenTypes.OBJBLOCK) {  // this should restrict us to just field definitions, and not local variable definitions

                    // field name
                    String fieldName = t.childOfType(GroovyTokenTypes.IDENT).getText();
                    SimpleGroovyFieldDoc currentFieldDoc = new SimpleGroovyFieldDoc(fieldName);

                    // comments - todo check this is doing the right thing for fields...
                    String commentText = getJavaDocCommentsBeforeNode(t);
                    currentFieldDoc.setRawCommentText(commentText);

                    // modifiers
                    processModifiers(t, currentFieldDoc);

                    // type
                    String typeName = getTypeNodeAsText(t.childOfType(GroovyTokenTypes.TYPE),"def");
                    SimpleGroovyType type = new SimpleGroovyType(typeName); // todo !!!
                    currentFieldDoc.setType(type);

                    // don't forget to tell the class about this field so carefully constructed.
                    currentClassDoc.add(currentFieldDoc);
                }
            }
        }
    }


    private boolean insideAnonymousInnerClass() {
        GroovySourceAST grandParentNode = getGrandParentNode();
        if (grandParentNode != null && grandParentNode.getType() == GroovyTokenTypes.LITERAL_new) {
            return true;
        }
        return false;
    }
    private void processModifiers(GroovySourceAST t,SimpleGroovyProgramElementDoc programElementDoc) {
        GroovySourceAST modifiers = t.childOfType(GroovyTokenTypes.MODIFIERS);
        if (modifiers != null) {
            AST currentModifier = modifiers.getFirstChild();
            boolean seenNonPublicVisibilityModifier = false;
            while (currentModifier != null) {
                int type = currentModifier.getType();
                switch (type) {
                    case GroovyTokenTypes.LITERAL_protected:
                    case GroovyTokenTypes.LITERAL_private:
                        seenNonPublicVisibilityModifier = true;
                        break;
                    case GroovyTokenTypes.LITERAL_static:
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
		String returnValue = "";
		
		String text = sourceBuffer.getSnippet(new LineColumn(1,1), new LineColumn(t.getLine(), t.getColumn()));
        if (text != null) {
            int openBlockIndex = text.lastIndexOf("{");
            int closingBlockIndex = text.lastIndexOf("}");
            int lastBlockIndex = Math.max(openBlockIndex, closingBlockIndex);
            if (lastBlockIndex > 0) {
                text = text.substring(lastBlockIndex);
            }

            Matcher m = previousJavaDocCommentPattern.matcher(text);
            if (m.find()) {
                returnValue = m.group(1);
            }
        }
		
		return returnValue;
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
				typeNode.getType() == GroovyTokenTypes.TYPE && 
				typeNode.getNumberOfChildren() > 0) {
			GroovySourceAST child = (GroovySourceAST) typeNode.getFirstChild(); // assume type has only one child // todo type of "foo.bar.Wibble"
			switch (child.getType()) {
				// literals
				case GroovyTokenTypes.LITERAL_boolean: returnValue = "boolean"; break;	
				case GroovyTokenTypes.LITERAL_byte: returnValue = "byte"; break;	
				case GroovyTokenTypes.LITERAL_char: returnValue = "char"; break;	
				// note: LITERAL_def never created
				case GroovyTokenTypes.LITERAL_double: returnValue = "double"; break;	
				case GroovyTokenTypes.LITERAL_float: returnValue = "float"; break;	
				case GroovyTokenTypes.LITERAL_int: returnValue = "int"; break;	
				case GroovyTokenTypes.LITERAL_long: returnValue = "long"; break;	
				case GroovyTokenTypes.LITERAL_short: returnValue = "short"; break;	
				case GroovyTokenTypes.LITERAL_void: returnValue = "void"; break;	
				
				// identifiers
				case GroovyTokenTypes.IDENT: returnValue = child.getText(); break;	
			}
		}
		return returnValue;
	}

	
	private void addParametersTo(SimpleGroovyExecutableMemberDoc executableMemberDoc, GroovySourceAST t,int visit) {
		// parameters
		GroovySourceAST parametersNode = t.childOfType(GroovyTokenTypes.PARAMETERS);
		if (parametersNode != null && parametersNode.getNumberOfChildren() > 0) {
			GroovySourceAST currentNode = (GroovySourceAST) parametersNode.getFirstChild();
    		while (currentNode != null) {
    			String parameterTypeName = getTypeNodeAsText(currentNode.childOfType(GroovyTokenTypes.TYPE),"def");
        		String parameterName = getText(currentNode.childOfType(GroovyTokenTypes.IDENT));
        		SimpleGroovyParameter parameter = new SimpleGroovyParameter(parameterName);
        		parameter.setTypeName(parameterTypeName);
        		
        		executableMemberDoc.add(parameter);
        		
        		currentNode = (GroovySourceAST)currentNode.getNextSibling();
    		}
		}
	}

	
	
	public void push(GroovySourceAST t) {
        stack.push(t);
    }
    public GroovySourceAST pop() {
        if (!stack.empty()) {
            return (GroovySourceAST) stack.pop();
        }
        return null;
    }

    private GroovySourceAST getParentNode() {
        Object parentNode = null;
        Object currentNode = stack.pop();
        if (!stack.empty()) {
            parentNode = stack.peek();
        }
        stack.push(currentNode);
        return (GroovySourceAST) parentNode;
    }

    private GroovySourceAST getGrandParentNode() {
        Object grandParentNode = null;
        Object parentNode;
        Object currentNode = stack.pop();
        if (!stack.empty()) {
            parentNode = stack.pop();
            if (!stack.empty()) {
                grandParentNode = stack.peek();
            }
            stack.push(parentNode);
        }
        stack.push(currentNode);
        return (GroovySourceAST) grandParentNode;
    }

}
