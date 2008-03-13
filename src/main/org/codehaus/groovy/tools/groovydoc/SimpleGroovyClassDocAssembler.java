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
		currentClassDoc = new SimpleGroovyClassDoc(className, links);
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
			
			GroovyConstructorDoc[] constructors = classDoc.constructors();
			if (constructors != null && constructors.length == 0) { // add default constructor to doc
	    		// name of class for the constructor
	    		GroovyConstructorDoc constructorDoc = new SimpleGroovyConstructorDoc(classDoc.name());
	        	// don't forget to tell the class about this default constructor.
	        	classDoc.add(constructorDoc);				
			}
		}
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
        		currentClassDoc = new SimpleGroovyClassDoc(className);
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
        	// now... get relevant values from the AST

    		// name of class for the constructor
    		currentConstructorDoc = new SimpleGroovyConstructorDoc(currentClassDoc.name());

    		// comments
    		String commentText = getJavaDocCommentsBeforeNode(t);
    		currentConstructorDoc.setRawCommentText(commentText);
    		
    		addParametersTo(currentConstructorDoc, t, visit);
    		
        	// don't forget to tell the class about this constructor.
        	currentClassDoc.add(currentConstructorDoc);
    	}		
	}


	public void visitMethodDef(GroovySourceAST t, int visit) {
    	if (visit == OPENING_VISIT) {
        	// init

        	// now... get relevant values from the AST

    		// method name
    		String methodName = t.childOfType(GroovyTokenTypes.IDENT).getText();
    		currentMethodDoc = new SimpleGroovyMethodDoc(methodName, links);

    		// comments
    		String commentText = getJavaDocCommentsBeforeNode(t);
    		currentMethodDoc.setRawCommentText(commentText);

            // modifiers
            GroovySourceAST modifiers = t.childOfType(GroovyTokenTypes.MODIFIERS);
            if (modifiers != null) {
                AST currentModifier = modifiers.getFirstChild();
                while (currentModifier != null) {
                    if (currentModifier.getType() == GroovyTokenTypes.LITERAL_static) {currentMethodDoc.setStatic(true);}
                    currentModifier = currentModifier.getNextSibling();
                }
            }

            // return type
    		String returnTypeName = getTypeNodeAsText(t.childOfType(GroovyTokenTypes.TYPE),"def");
        	SimpleGroovyType returnType = new SimpleGroovyType(returnTypeName); // todo !!!
        	currentMethodDoc.setReturnType(returnType);

    		addParametersTo(currentMethodDoc, t, visit);
    		
        	// don't forget to tell the class about this method so carefully constructed.
        	currentClassDoc.add(currentMethodDoc);
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
}
