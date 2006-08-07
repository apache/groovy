/**
 *
 * Copyright 2005 Jeremy Rayner
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 **/
package org.codehaus.groovy.antlr.treewalker;

import java.io.PrintStream;
import java.util.Stack;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;

/**
 * An antlr AST visitor that prints groovy source code for each visited node
 * to the supplied PrintStream.
 *
 * @author <a href="mailto:groovy@ross-rayner.com">Jeremy Rayner</a>
 * @version $Revision$
 */

public class SourcePrinter extends VisitorAdapter {
    private String[] tokenNames;
    private int tabLevel;
    private int lastLinePrinted;
    private boolean newLines;
    protected PrintStream out;
    private String className;
    private Stack stack;
    private int stringConstructorCounter;

    /**
     * A visitor that prints groovy source code for each node visited.
     * @param out where to print the source code to
     * @param tokenNames an array of token names from antlr
     */
    public SourcePrinter(PrintStream out,String[] tokenNames) {
        this(out,tokenNames,true);
    }

    /**
     * A visitor that prints groovy source code for each node visited.
     * @param out where to print the source code to
     * @param tokenNames an array of token names from antlr
     * @param newLines output newline character
     */
    public SourcePrinter(PrintStream out,String[] tokenNames, boolean newLines) {
        this.tokenNames = tokenNames;
        tabLevel = 0;
        lastLinePrinted = 0;
        this.out = out;
        this.newLines = newLines;
        this.stack = new Stack();
    }
    

	public void visitAbstract(GroovySourceAST t, int visit) {
		print(t,visit,"abstract ",null,null);
	}

	public void visitAnnotation(GroovySourceAST t, int visit) {
		if (visit == OPENING_VISIT) {
			print(t,visit,"@");
		}
		if (visit == SECOND_VISIT) {
			print(t,visit,"(");
		}
		if (visit == SUBSEQUENT_VISIT) {
			print(t,visit,", ");
		}
		if (visit == CLOSING_VISIT) {
			if (t.getNumberOfChildren() > 1) {
				print(t,visit,") ");
			} else {
				print(t,visit," ");
			}
		}

    }

    public void visitAnnotations(GroovySourceAST t, int visit) {
    	// do nothing
    }

    public void visitAnnotationDef(GroovySourceAST t,int visit) {
        print(t,visit,"@interface ",null,null);
    }

	public void visitAnnotationFieldDef(GroovySourceAST t, int visit) {
    	print(t,visit,"() ","default ",null);
	}

	public void visitAnnotationMemberValuePair(GroovySourceAST t, int visit) {
		print(t,visit," = ",null,null);
	}

	public void visitArrayDeclarator(GroovySourceAST t, int visit) {
		//<ARRAY_DECLARATOR>int</ARRAY_DECLARATOR> primes = new int(<ARRAY_DECLARATOR>5</ARRAY_DECLARATOR>)
		if (getParentNode().getType() == GroovyTokenTypes.TYPE) {
			// type defintion, i.e.   int[] x;
			print(t,visit,null,null,"[]");
		} else {
			// usually in new, i.e.   def y = new int[5];
			print(t,visit,"[",null,"]");
		}
	}

	public void visitAssign(GroovySourceAST t,int visit) {
        print(t,visit," = ",null,null);
    }
	
    // visitAt() ...
    //   token type 'AT' should never be visited, as annotation definitions and usage, and
    //   direct field access should have all moved this token out of the way. No test needed.

	//   one of the BAND tokens is actually replaced by TYPE_UPPER_BOUNDS (e.g. class Foo<T extends C & I> {T t} )
    public void visitBand(GroovySourceAST t, int visit) {
        print(t,visit," & ",null,null);
    }

	public void visitBandAssign(GroovySourceAST t,int visit) {
        print(t,visit," &= ",null,null);
    }
	
    // visitBigSuffix() ...
	//   token type BIG_SUFFIX never created/visited, NUM_BIG_INT, NUM_BIG_DECIMAL instead...    
    
	// visitBlock() ...
	//   token type BLOCK never created/visited, see CLOSED_BLOCK etc...
	
	public void visitBnot(GroovySourceAST t, int visit) {
		print(t,visit,"~",null,null);
	}
	
	// Note: old closure syntax using BOR is deprecated, and also never creates/visits a BOR node
    public void visitBor(GroovySourceAST t, int visit) {
        print(t,visit," | ",null,null);
    }
	
	public void visitBorAssign(GroovySourceAST t,int visit) {
        print(t,visit," |= ",null,null);
    }
	
    public void visitBsr(GroovySourceAST t, int visit) {
        print(t,visit," >>> ",null,null);
    }
	
	public void visitBsrAssign(GroovySourceAST t,int visit) {
        print(t,visit," >>>= ",null,null);
    }
	
    public void visitBxor(GroovySourceAST t, int visit) {
        print(t,visit," ^ ",null,null);
    }
	
	public void visitBxorAssign(GroovySourceAST t,int visit) {
        print(t,visit," ^= ",null,null);
    }
	
    public void visitCaseGroup(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            tabLevel++;
        }
        if (visit == CLOSING_VISIT) {
            tabLevel--;
        }
    }

    public void visitClassDef(GroovySourceAST t,int visit) {
        print(t,visit,"class ",null,null);

        if (visit == OPENING_VISIT) {
            // store name of class away for use in constructor ident
            className = t.childOfType(GroovyTokenTypes.IDENT).getText();
        }
    }

    public void visitClosedBlock(GroovySourceAST t, int visit) {
        printUpdatingTabLevel(t,visit,"{","-> ","}");
    }
    
    // visitClosureOp ...
	//   token type CLOSURE_OP never created/visited, see CLOSED_BLOCK...
	

    // visitColon ...
    //   token type COLON never created/visited, see LABELED_STAT, FOR_IN_ITERABLE, 
    //   ASSERT, CASE, QUESTION, MAP_CONSTRUCTOR, LABELED_ARG, SPREAD_MAP_ARG

    // visitComma ...
    //   token type COMMA never created/visited,
    //   see TYPE_ARGUMENTS, ANNOTATION, many others ...
    
    public void visitCompareTo(GroovySourceAST t,int visit) {
        print(t,visit," <=> ",null,null);
    }

    public void visitCtorCall(GroovySourceAST t,int visit) {
        printUpdatingTabLevel(t,visit,"this("," ",")");
    }

    public void visitCtorIdent(GroovySourceAST t, int visit) {
        // use name of class for constructor from the class definition
        print(t,visit,className,null,null);
    }

    public void visitDec(GroovySourceAST t, int visit) {
    	print(t,visit,"--",null,null);
    }
    
    // visitDigit ...
    //    never created/visited
    
    public void visitDiv(GroovySourceAST t, int visit) {
        print(t,visit," / ",null,null);
    }

	public void visitDivAssign(GroovySourceAST t,int visit) {
        print(t,visit," /= ",null,null);
    }
	
    // visitDollar ...
    //   token type DOLLAR never created/visited, see SCOPE_ESCAPE instead
    
    public void visitDot(GroovySourceAST t,int visit) {
        print(t,visit,".",null,null);
    }
    
    public void visitDynamicMember(GroovySourceAST t, int visit) {
    	if (t.childOfType(GroovyTokenTypes.STRING_CONSTRUCTOR) == null) {
    		printUpdatingTabLevel(t,visit,"(",null,")");
    	}
    }
    
    public void visitElist(GroovySourceAST t,int visit) {
    	if (getParentNode().getType() == GroovyTokenTypes.ENUM_CONSTANT_DEF) {
    		print(t,visit,"(",", ",")");
    	} else {
    		print(t,visit,null,", ",null);
    	}
    }

    // visitEmptyStat ...
    //   token type EMPTY_STAT obsolete and should be removed, never visited/created
    
    public void visitEnumConstantDef(GroovySourceAST t,int visit) {
    	GroovySourceAST sibling = (GroovySourceAST)t.getNextSibling();
    	if (sibling != null && sibling.getType() == GroovyTokenTypes.ENUM_CONSTANT_DEF) {
    		print(t,visit,null,null,", ");
    	}
    }

    public void visitEnumDef(GroovySourceAST t,int visit) {
        print(t,visit,"enum ",null,null);
    }

    // visitEof ...
    //   token type EOF never visited/created

    public void visitEqual(GroovySourceAST t,int visit) {
        print(t,visit," == ",null,null);
    }

    // visitExponent ...
    //   token type EXPONENT only used by lexer, never visited/created
    
    public void visitExpr(GroovySourceAST t,int visit) {
    }

    public void visitExtendsClause(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            if (t.getNumberOfChildren() != 0) {
                print(t,visit," extends ");
            }
        }
    }

    public void visitForCondition(GroovySourceAST t, int visit) {
    	print(t,visit," ; ",null,null);
    }

    public void visitForInit(GroovySourceAST t, int visit) {
    	print(t,visit,"(",null,null);
    }
    
    public void visitForInIterable(GroovySourceAST t, int visit) {
        printUpdatingTabLevel(t,visit,"("," in ",") ");
    }

    public void visitForIterator(GroovySourceAST t, int visit) {
    	print(t,visit," ; ",null,")");
    }
    public void visitGt(GroovySourceAST t, int visit) {
        print(t,visit," > ",null,null);
    }

    public void visitIdent(GroovySourceAST t,int visit) {
        print(t,visit,t.getText(),null,null);
    }
    public void visitImplementsClause(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            if (t.getNumberOfChildren() != 0) {
                print(t,visit," implements ");
            }
        }
        if (visit == CLOSING_VISIT) {
            //space between classdef and objblock
            print(t,visit," ");
        }
    }

    public void visitImplicitParameters(GroovySourceAST t, int visit) {
    }

    public void visitImport(GroovySourceAST t,int visit) {
        print(t,visit,"import ",null,null);
    }

    public void visitIndexOp(GroovySourceAST t, int visit) {
        printUpdatingTabLevel(t,visit,"[",null,"]");
    }

    public void visitInterfaceDef(GroovySourceAST t,int visit) {
        print(t,visit,"interface ",null,null);
    }
    
    public void visitLabeledArg(GroovySourceAST t, int visit) {
        print(t,visit,":",null,null);
    }

    public void visitLand(GroovySourceAST t, int visit) {
        print(t,visit," && ",null,null);
    }

    public void visitListConstructor(GroovySourceAST t, int visit) {
        printUpdatingTabLevel(t,visit,"[",null,"]");
    }

    public void visitLiteralAssert(GroovySourceAST t,int visit) {
        print(t,visit,"assert ",null,null);
    }

    public void visitLiteralBoolean(GroovySourceAST t, int visit) {
        print(t,visit,"boolean",null,null);
    }

    public void visitLiteralBreak(GroovySourceAST t, int visit) {
        print(t,visit,"break",null,null);
    }

    public void visitLiteralCase(GroovySourceAST t, int visit) {
        print(t,visit,"case ",null,":");
    }

    public void visitLiteralCatch(GroovySourceAST t,int visit) {
        printUpdatingTabLevel(t,visit," catch (",null,") ");
    }
    public void visitLiteralDefault(GroovySourceAST t,int visit) {
        print(t,visit,"default",null,":");
    }
    public void visitLiteralFalse(GroovySourceAST t,int visit) {
        print(t,visit,"false",null,null);
    }

    public void visitLiteralFinally(GroovySourceAST t,int visit) {
        print(t,visit,"finally ",null,null);
    }
    public void visitLiteralFloat(GroovySourceAST t,int visit) {
        print(t,visit,"float",null,null);
    }

    public void visitLiteralFor(GroovySourceAST t,int visit) {
        print(t,visit,"for ",null,null);
    }

    public void visitLiteralIf(GroovySourceAST t,int visit) {
        // slightly strange as subsequent visit is done after closing visit
        printUpdatingTabLevel(t,visit,"if ("," else ",") ");
    }

    public void visitLiteralInstanceof(GroovySourceAST t, int visit) {
        print(t,visit," instanceof ",null,null);
    }

    public void visitLiteralInt(GroovySourceAST t,int visit) {
        print(t,visit,"int",null,null);
    }

    public void visitLiteralNew(GroovySourceAST t,int visit) {
    	if (t.childOfType(GroovyTokenTypes.ARRAY_DECLARATOR) == null) {
    		// only print parenthesis if is not of form def x = new int[5]
    		print(t,visit,"new ","(",")");
    	} else {
    		print(t,visit,"new ",null,null);
    	}
    }

    public void visitLiteralNull(GroovySourceAST t, int visit) {
        print(t,visit,"null",null,null);
    }

    public void visitLiteralPrivate(GroovySourceAST t,int visit) {
        print(t,visit,"private ",null,null);
    }

    public void visitLiteralProtected(GroovySourceAST t,int visit) {
        print(t,visit,"protected ",null,null);
    }

    public void visitLiteralPublic(GroovySourceAST t,int visit) {
        print(t,visit,"public ",null,null);
    }

    public void visitLiteralReturn(GroovySourceAST t, int visit) {
        print(t,visit,"return ",null,null);
    }

    public void visitLiteralStatic(GroovySourceAST t, int visit) {
        print(t,visit,"static ",null,null);
    }

    public void visitLiteralSwitch(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,"switch (");
            tabLevel++;
        }
        if (visit == SUBSEQUENT_VISIT) {
            print(t,visit,") {");
        }
        if (visit == CLOSING_VISIT) {
            tabLevel--;
            print(t,visit,"}");
        }
    }

    public void visitLiteralThis(GroovySourceAST t, int visit) {
        print(t,visit,"this",null,null);
    }

    public void visitLiteralThrow(GroovySourceAST t, int visit) {
        print(t,visit,"throw ",null,null);
    }

    public void visitLiteralThrows(GroovySourceAST t, int visit) {
        print(t,visit,"throws ",null,null);
    }

    public void visitLiteralTrue(GroovySourceAST t,int visit) {
        print(t,visit,"true",null,null);
    }
    public void visitLiteralTry(GroovySourceAST t,int visit) {
        print(t,visit,"try ",null,null);
    }
    public void visitLiteralVoid(GroovySourceAST t,int visit) {
        print(t,visit,"void",null,null);
    }
    public void visitLiteralWhile(GroovySourceAST t,int visit) {
        printUpdatingTabLevel(t,visit,"while (",null,") ");
    }

    public void visitLnot(GroovySourceAST t, int visit) {
        print(t,visit,"!",null,null);
    }

	// Note: old closure syntax using LOR is deprecated, and also never creates/visits a LOR node
    public void visitLor(GroovySourceAST t, int visit) {
        print(t,visit," || ",null,null);
    }

    public void visitLt(GroovySourceAST t, int visit) {
        print(t,visit," < ",null,null);
    }

    public void visitMapConstructor(GroovySourceAST t, int visit) {
        if (t.getNumberOfChildren() == 0) {
            print(t,visit,"[:]",null,null);
        } else {
            printUpdatingTabLevel(t,visit,"[",null,"]");
        }
    }

    public void visitMemberPointer(GroovySourceAST t, int visit) {
        print(t,visit,".&",null,null);
    }

    public void visitMethodCall(GroovySourceAST t,int visit) {
    	if ("<command>".equals(t.getText())) {
    		printUpdatingTabLevel(t,visit," "," ",null);
    	} else {
    		printUpdatingTabLevel(t,visit,"("," ",")");
    	}
    }
    public void visitMinus(GroovySourceAST t,int visit) {
        print(t,visit," - ",null,null);
    }
    public void visitMethodDef(GroovySourceAST t,int visit) {
        //do nothing
    }
    public void visitModifiers(GroovySourceAST t,int visit) {
        //do nothing
    }

    public void visitNotEqual(GroovySourceAST t, int visit) {
        print(t,visit," != ",null,null);
    }

    public void visitNumBigDecimal(GroovySourceAST t,int visit) {
        print(t,visit,t.getText(),null,null);
    }
    public void visitNumBigInt(GroovySourceAST t,int visit) {
        print(t,visit,t.getText(),null,null);
    }
    public void visitNumDouble(GroovySourceAST t,int visit) {
        print(t,visit,t.getText(),null,null);
    }
    public void visitNumInt(GroovySourceAST t,int visit) {
        print(t,visit,t.getText(),null,null);
    }
    public void visitNumFloat(GroovySourceAST t,int visit) {
        print(t,visit,t.getText(),null,null);
    }
    public void visitObjblock(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            tabLevel++;
            print(t,visit,"{");
        } else {
            tabLevel--;
            print(t,visit,"}");
        }
    }

    public void visitPackageDef(GroovySourceAST t, int visit) {
        print(t,visit,"package ",null,null);
    }

    public void visitParameterDef(GroovySourceAST t,int visit) {
        //do nothing
    }

    public void visitParameters(GroovySourceAST t,int visit) {
    	if (getParentNode().getType() == GroovyTokenTypes.CLOSED_BLOCK) {
    		printUpdatingTabLevel(t,visit,null,","," ");
    	} else {
    		printUpdatingTabLevel(t,visit,"(",", ",") ");
    	}
    }

    public void visitPlus(GroovySourceAST t, int visit) {
        print(t,visit," + ",null,null);
    }
    
    public void visitPostDec(GroovySourceAST t, int visit) {
    	print(t,visit,null,null,"--");
    }

    public void visitPostInc(GroovySourceAST t, int visit) {
    	print(t,visit,null,null,"++");
    }

    public void visitQuestion(GroovySourceAST t, int visit) {
        // ternary operator
        print(t,visit,"?",":",null);
    }

    public void visitRangeExclusive(GroovySourceAST t, int visit) {
        print(t,visit,"..<",null,null);
    }

    public void visitRangeInclusive(GroovySourceAST t, int visit) {
        print(t,visit,"..",null,null);
    }

    public void visitScopeEscape(GroovySourceAST t, int visit) {
    	print(t,visit,"$",null,null);
    }
    
    public void visitSlist(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            tabLevel++;
            print(t,visit,"{");
        } else {
            tabLevel--;
            print(t,visit,"}");
        }
    }

    public void visitStar(GroovySourceAST t,int visit) {
        print(t,visit,"*",null,null);
    }
    public void visitStringConstructor(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            stringConstructorCounter = 0;
            print(t,visit,"\"");
        }
        if (visit == SUBSEQUENT_VISIT) {
            // every other subsequent visit use an escaping $
            if (stringConstructorCounter % 2 == 0) {
               print(t,visit,"$");
            }
            stringConstructorCounter++;
        }
        if (visit == CLOSING_VISIT) {
            print(t,visit,"\"");
        }
    }

    public void visitStringLiteral(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            String theString = escape(t.getText());
        if (getParentNode().getType() != GroovyTokenTypes.LABELED_ARG &&
            getParentNode().getType() != GroovyTokenTypes.STRING_CONSTRUCTOR) {
                theString = "\"" + theString + "\"";
            }
            print(t,visit,theString);
        }
    }

    private String escape(String literal) {
        literal = literal.replaceAll("\n","\\\\<<REMOVE>>n"); // can't seem to do \n in one go with Java regex
        literal = literal.replaceAll("<<REMOVE>>","");
        return literal;
    }

    public void visitType(GroovySourceAST t,int visit) {
        GroovySourceAST parent = getParentNode();
        GroovySourceAST modifiers = parent.childOfType(GroovyTokenTypes.MODIFIERS);

        // No need to print 'def' if we already have some modifiers
        if (modifiers == null || modifiers.getNumberOfChildren() == 0) {

            if (visit == OPENING_VISIT) {
                if (t.getNumberOfChildren() == 0 && 
                		parent.getType() != GroovyTokenTypes.PARAMETER_DEF) { // no need for 'def' if in a parameter list
                    print(t,visit,"def");
                }
            }
        	if (visit == CLOSING_VISIT) {
        		print(t,visit," ");
            }
        } else {
        	if (visit == CLOSING_VISIT) {
        		if (t.getNumberOfChildren() != 0) {
        			print(t,visit," ");
        		}
        	}
        }
    }

    public void visitTypecast(GroovySourceAST t,int visit) {
        print(t,visit,"(",null,")");
    }

    public void visitVariableDef(GroovySourceAST t,int visit) {
        // do nothing
    }

    public void visitDefault(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,"<" + tokenNames[t.getType()] + ">");
            //out.print("<" + t.getType() + ">");
        } else {
            print(t,visit,"</" + tokenNames[t.getType()] + ">");
            //out.print("</" + t.getType() + ">");
        }
    }

    protected void printUpdatingTabLevel(GroovySourceAST t,int visit,String opening, String subsequent, String closing) {
        if (visit == OPENING_VISIT && opening != null) {
            print(t,visit,opening);
            tabLevel++;
        }
        if (visit == SUBSEQUENT_VISIT && subsequent != null) {
            print(t,visit,subsequent);
        }
        if (visit == CLOSING_VISIT && closing != null) {
            tabLevel--;
            print(t,visit,closing);
        }
    }

    protected void print(GroovySourceAST t,int visit,String opening, String subsequent, String closing) {
        if (visit == OPENING_VISIT && opening != null) {
            print(t,visit,opening);
        }
        if (visit == SUBSEQUENT_VISIT && subsequent != null) {
            print(t,visit,subsequent);
        }
        if (visit == CLOSING_VISIT && closing != null) {
            print(t,visit,closing);
        }
    }
    protected void print(GroovySourceAST t,int visit,String value) {
        if(visit == OPENING_VISIT) {
            printNewlineAndIndent(t, visit);
        }
        if (visit == CLOSING_VISIT) {
            printNewlineAndIndent(t, visit);
        }
        out.print(value);
    }

    protected void printNewlineAndIndent(GroovySourceAST t, int visit) {
        int currentLine = t.getLine();
        if (lastLinePrinted == 0) { lastLinePrinted = currentLine; }
        if (lastLinePrinted != currentLine) {
            if (newLines) {
                if (!(visit == OPENING_VISIT && t.getType() == GroovyTokenTypes.SLIST)) {
                    for (int i=lastLinePrinted;i<currentLine;i++) {
                        out.println();
                    }
                    if (lastLinePrinted > currentLine) {
                        out.println();
                    }
                    if (visit == OPENING_VISIT || (visit == CLOSING_VISIT && lastLinePrinted > currentLine)) {
                        for (int i=0;i<tabLevel;i++) {
                            out.print("    ");
                        }
                    }
                }
            }
            lastLinePrinted = Math.max(currentLine,lastLinePrinted);
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
        Object currentNode = stack.pop();
        Object parentNode = stack.peek();
        stack.push(currentNode);
        return (GroovySourceAST) parentNode;
    }

}
