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
package org.codehaus.groovy.antlr.treewalker;

import antlr.collections.AST;
import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;

import java.io.PrintStream;
import java.util.Stack;

/**
 * An antlr AST visitor that prints groovy source code for each visited node
 * to the supplied PrintStream.
 */
public class SourcePrinter extends VisitorAdapter {
    private final String[] tokenNames;
    private int tabLevel;
    private int lastLinePrinted;
    private final boolean newLines;
    protected final PrintStream out;
    private String className;
    private final Stack stack;
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
        if (getParentNode().getType() == GroovyTokenTypes.TYPE ||
                getParentNode().getType() == GroovyTokenTypes.TYPECAST) { // ugly hack
            // type definition, i.e.   int[] x;
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
    //   token type BLOCK never created/visited, see CLOSABLE_BLOCK etc...
    
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
    
    public void visitClosureList(GroovySourceAST t, int visit) {
        print(t,visit,"(","; ",")");
    }
    // visitClosureOp ...
    //   token type CLOSABLE_BLOCK_OP never created/visited, see CLOSABLE_BLOCK...
    

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
        // do nothing
    }

    public void visitExtendsClause(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            if (t.getNumberOfChildren() != 0) {
                print(t,visit," extends ");
            }
        }
    }
    
    public void visitFinal(GroovySourceAST t, int visit) {
        print(t,visit,"final ",null,null);
    }

    // visitFloatSuffix ... never visited/created see NUM_DOUBLE or NUM_FLOAT instead
    
    public void visitForCondition(GroovySourceAST t, int visit) {
        print(t,visit," ; ",null,null);
    }
    
    // visitForEachClause ... 
    //   FOR_EACH_CLAUSE obsolete and should be removed, never visited/created

    public void visitForInit(GroovySourceAST t, int visit) {
        print(t,visit,"(",null,null);
    }
    
    public void visitForInIterable(GroovySourceAST t, int visit) {
        printUpdatingTabLevel(t,visit,"("," in ",") ");
    }

    public void visitForIterator(GroovySourceAST t, int visit) {
        print(t,visit," ; ",null,")");
    }
    
    public void visitGe(GroovySourceAST t, int visit) {
        print(t,visit," >= ",null,null);
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
        // do nothing
    }

    public void visitImport(GroovySourceAST t,int visit) {
        print(t,visit,"import ",null,null);
    }

    public void visitInc(GroovySourceAST t, int visit) {
        print(t,visit,"++",null,null);
    }

    public void visitIndexOp(GroovySourceAST t, int visit) {
        printUpdatingTabLevel(t,visit,"[",null,"]");
    }

    public void visitInterfaceDef(GroovySourceAST t,int visit) {
        print(t,visit,"interface ",null,null);
    }

    public void visitInstanceInit(GroovySourceAST t, int visit) {
        // do nothing
    }

    public void visitLabeledArg(GroovySourceAST t, int visit) {
        print(t,visit,":",null,null);
    }

    public void visitLabeledStat(GroovySourceAST t, int visit) {
        print(t,visit,":",null,null);
    }

    public void visitLand(GroovySourceAST t, int visit) {
        print(t,visit," && ",null,null);
    }

    // visit lbrack()
    //   token type LBRACK only used inside parser, never visited/created

    // visit lcurly()
    //   token type LCURLY only used inside parser, never visited/created
    
    public void visitLe(GroovySourceAST t, int visit) {
        print(t,visit," <= ",null,null);
    }

    // visitLetter ...
    //   token type LETTER only used by lexer, never visited/created

    public void visitListConstructor(GroovySourceAST t, int visit) {
        printUpdatingTabLevel(t,visit,"[",null,"]");
    }

    public void visitLiteralAs(GroovySourceAST t,int visit) {
        print(t,visit," as ",null,null);
    }

    public void visitLiteralAssert(GroovySourceAST t,int visit) {
        if (t.getNumberOfChildren() > 1) {
            print(t,visit,"assert ",null," : ");
        } else {
            print(t,visit,"assert ",null,null);
        }
    }

    public void visitLiteralBoolean(GroovySourceAST t, int visit) {
        print(t,visit,"boolean",null,null);
    }

    public void visitLiteralBreak(GroovySourceAST t, int visit) {
        print(t,visit,"break ",null,null);
    }

    public void visitLiteralByte(GroovySourceAST t, int visit) {
        print(t,visit,"byte",null,null);
    }

    public void visitLiteralCase(GroovySourceAST t, int visit) {
        print(t,visit,"case ",null,":");
    }

    public void visitLiteralCatch(GroovySourceAST t,int visit) {
        printUpdatingTabLevel(t, visit, " catch (", null, ") ");
    }

    public void visitLiteralChar(GroovySourceAST t, int visit) {
        print(t,visit,"char",null,null);
    }

    // visitLiteralClass ...
    //   token type "class" only used by parser, never visited/created directly

    public void visitLiteralContinue(GroovySourceAST t, int visit) {
        print(t,visit,"continue ",null,null);
    }

    // visitLiteralDef ...
    //   token type "def" only used by parser, never visited/created directly

    public void visitLiteralDefault(GroovySourceAST t,int visit) {
        print(t,visit,"default",null,":");
    }

    public void visitLiteralDouble(GroovySourceAST t, int visit) {
        print(t,visit,"double",null,null);
    }

    // visitLiteralElse ...
    //   token type "else" only used by parser, never visited/created directly

    // visitLiteralEnum ...
    //   token type "enum" only used by parser, never visited/created directly

    // visitLiteralExtends
    //   token type "extends" only used by parser, never visited/created directly
    
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

    // visitLiteralImplements
    //   token type "implements" only used by parser, never visited/created directly

    // visitLiteralImport
    //   token type "import" only used by parser, never visited/created directly

    public void visitLiteralIn(GroovySourceAST t, int visit) {
        print(t,visit," in ",null,null);
    }

    public void visitLiteralInstanceof(GroovySourceAST t, int visit) {
        print(t,visit," instanceof ",null,null);
    }

    public void visitLiteralInt(GroovySourceAST t,int visit) {
        print(t,visit,"int",null,null);
    }

    // visitLiteralInterface
    //   token type "interface" only used by parser, never visited/created directly

    public void visitLiteralLong(GroovySourceAST t,int visit) {
        print(t,visit,"long",null,null);
    }

    public void visitLiteralNative(GroovySourceAST t,int visit) {
        print(t,visit,"native ",null,null);
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

    // visitLiteralPackage
    //   token type "package" only used by parser, never visited/created directly

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

    public void visitLiteralShort(GroovySourceAST t,int visit) {
        print(t,visit,"short",null,null);
    }

    public void visitLiteralStatic(GroovySourceAST t, int visit) {
        print(t,visit,"static ",null,null);
    }

    public void visitLiteralSuper(GroovySourceAST t, int visit) {
        // only visited when calling super() without parentheses, i.e. "super 99" is equivalent to "super(99)"
        print(t,visit,"super",null,null);
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

    public void visitLiteralSynchronized(GroovySourceAST t,int visit) {
        if (t.getNumberOfChildren() > 0) {
            print(t,visit,"synchronized (",null,") ");
        } else {
            print(t,visit,"synchronized ",null,null);            
        }
    }

    public void visitLiteralThis(GroovySourceAST t, int visit) {
        print(t,visit,"this",null,null);
    }

    public void visitLiteralThreadsafe(GroovySourceAST t,int visit) {
        print(t,visit,"threadsafe ",null,null);
    }

    public void visitLiteralThrow(GroovySourceAST t, int visit) {
        print(t,visit,"throw ",null,null);
    }

    public void visitLiteralThrows(GroovySourceAST t, int visit) {
        print(t,visit,"throws ",null,null);
    }

    public void visitLiteralTransient(GroovySourceAST t,int visit) {
        print(t,visit,"transient ",null,null);
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
    public void visitLiteralVolatile(GroovySourceAST t,int visit) {
        print(t,visit,"volatile ",null,null);
    }
    public void visitLiteralWhile(GroovySourceAST t,int visit) {
        printUpdatingTabLevel(t,visit,"while (",null,") ");
    }

//deprecated
//  public void visitLiteralWith(GroovySourceAST t,int visit) {
//        printUpdatingTabLevel(t,visit,"with (",null,") ");
//    }
    
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
    public void visitMethodDef(GroovySourceAST t,int visit) {
        //do nothing
    }
    public void visitMinus(GroovySourceAST t,int visit) {
        print(t,visit," - ",null,null);
    }
    public void visitMinusAssign(GroovySourceAST t, int visit) {
        print(t,visit," -= ",null,null);
    }

    // visitMlComment
    //   multi-line comments are not created on the AST currently.

    public void visitMod(GroovySourceAST t, int visit) {
        print(t,visit," % ",null,null);
    }

    public void visitModifiers(GroovySourceAST t,int visit) {
        //do nothing
    }
    public void visitModAssign(GroovySourceAST t, int visit) {
        print(t,visit," %= ",null,null);
    }

    @Override
    public void visitMultiCatch(final GroovySourceAST t, final int visit) {
        if (visit == CLOSING_VISIT) {
            final AST child = t.getFirstChild();
            if ("MULTICATCH_TYPES".equals(child.getText())) {
                print(t, visit, null, null, " "+child.getNextSibling().getText());
            } else {
                print(t, visit, null, null, " "+child.getFirstChild().getText());
            }
        }
    }

    @Override
    public void visitMultiCatchTypes(final GroovySourceAST t, final int visit) {
    }

    // visitNls
    //   new lines are used by parser, but are not created on the AST,
    //   they can be implied by the source code line/column information

    // visitNullTreeLookahead
    //   not used explicitly by parser.
    
    
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
    public void visitNumLong(GroovySourceAST t,int visit) {
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

    // visitOneNl
    //   new lines are used by parser, but are not created on the AST,
    //   they can be implied by the source code line/column information

    public void visitOptionalDot(GroovySourceAST t,int visit) {
        print(t,visit,"?.",null,null);
    }
    
    public void visitPackageDef(GroovySourceAST t, int visit) {
        print(t,visit,"package ",null,null);
    }

    public void visitParameterDef(GroovySourceAST t,int visit) {
        //do nothing
    }

    public void visitParameters(GroovySourceAST t,int visit) {
        if (getParentNode().getType() == GroovyTokenTypes.CLOSABLE_BLOCK) {
            printUpdatingTabLevel(t,visit,null,","," ");
        } else {
            printUpdatingTabLevel(t,visit,"(",", ",") ");
        }
    }

    public void visitPlus(GroovySourceAST t, int visit) {
        print(t,visit," + ",null,null);
    }
    
    public void visitPlusAssign(GroovySourceAST t, int visit) {
        print(t,visit," += ",null,null);
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

    // visit rbrack()
    //   token type RBRACK only used inside parser, never visited/created

    // visit rcurly()
    //   token type RCURLY only used inside parser, never visited/created

    // visit RegexpCtorEnd
    // visit RegexpLiteral
    // visit RegexpSymbol
    //    token types REGEXP_CTOR_END, REGEXP_LITERAL, REGEXP_SYMBOL only used inside lexer
    
    public void visitRegexFind(GroovySourceAST t, int visit) {
        print(t,visit," =~ ",null,null);
    }
    public void visitRegexMatch(GroovySourceAST t, int visit) {
        print(t,visit," ==~ ",null,null);
    }
    // visit rparen()
    //   token type RPAREN only used inside parser, never visited/created

    public void visitSelectSlot(GroovySourceAST t, int visit) {
        print(t,visit,"@",null,null);
    }
    
    // visit semi()
    //  SEMI only used inside parser, never visited/created (see visitForCondition(), visitForIterator())
    
    // visit ShComment()
    //  never visited/created by parser
    
    public void visitSl(GroovySourceAST t, int visit) {
        print(t,visit," << ",null,null);
    }
    public void visitSlAssign(GroovySourceAST t, int visit) {
        print(t,visit," <<= ",null,null);
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

    // visit SlComment()
    //   never visited/created by parser
    
    public void visitSpreadArg(GroovySourceAST t,int visit) {
        print(t,visit,"*",null,null);
    }

    public void visitSpreadDot(GroovySourceAST t,int visit) {
    print(t,visit,"*.",null,null);
    }

    public void visitSpreadMapArg(GroovySourceAST t,int visit) {
        print(t,visit,"*:",null,null);
    }
    
    public void visitSr(GroovySourceAST t, int visit) {
        print(t,visit," >> ",null,null);
    }
    public void visitSrAssign(GroovySourceAST t, int visit) {
        print(t,visit," >>= ",null,null);
    }

    public void visitStar(GroovySourceAST t,int visit) {
        print(t,visit,"*",null,null);
    }
    public void visitStarAssign(GroovySourceAST t, int visit) {
        print(t,visit," *= ",null,null);
    }
    public void visitStarStar(GroovySourceAST t,int visit) {
        print(t,visit,"**",null,null);
    }
    public void visitStarStarAssign(GroovySourceAST t, int visit) {
        print(t,visit," **= ",null,null);
    }
    
    public void visitStaticInit(GroovySourceAST t, int visit) {
        print(t,visit,"static ",null,null);
    }
    public void visitStaticImport(GroovySourceAST t,int visit) {
        print(t,visit,"import static ",null,null);
    }
    public void visitStrictfp(GroovySourceAST t,int visit) {
        print(t,visit,"strictfp ",null,null);
    }

    // visitStringch
    //   String characters only used by lexer, never visited/created directly


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

    private static String escape(String literal) {
        literal = literal.replaceAll("\n","\\\\<<REMOVE>>n"); // can't seem to do \n in one go with Java regex
        literal = literal.replaceAll("<<REMOVE>>","");
        return literal;
    }

    public void visitSuperCtorCall(GroovySourceAST t,int visit) {
        printUpdatingTabLevel(t,visit,"super("," ",")");
    }

    public void visitTraitDef(GroovySourceAST t,int visit) {
        print(t,visit,"trait ",null,null);

        if (visit == OPENING_VISIT) {
            // store name of class away for use in constructor ident
            className = t.childOfType(GroovyTokenTypes.IDENT).getText();
        }
    }

    // visit TripleDot, not used in the AST
    
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
                if (  parent.getType() == GroovyTokenTypes.VARIABLE_DEF         ||
                      parent.getType() == GroovyTokenTypes.METHOD_DEF           ||
                      parent.getType() == GroovyTokenTypes.ANNOTATION_FIELD_DEF ||
                     (parent.getType() == GroovyTokenTypes.PARAMETER_DEF && t.getNumberOfChildren()!=0))             
                {
                    print(t,visit," ");
                }
            }
            
            /*if (visit == CLOSING_VISIT) {
                print(t,visit," ");
            }*/
        } else {
            if (visit == CLOSING_VISIT) {
                if (t.getNumberOfChildren() != 0) {
                    print(t,visit," ");
                }
            }
        }
    }
    public void visitTypeArgument(GroovySourceAST t, int visit) {
        // print nothing
    }

    public void visitTypeArguments(GroovySourceAST t, int visit) {
        print(t,visit,"<",", ",">");
    }

    public void visitTypecast(GroovySourceAST t,int visit) {
        print(t,visit,"(",null,")");
    }
    public void visitTypeLowerBounds(GroovySourceAST t,int visit) {
        print(t,visit," super "," & ",null);
    }
    public void visitTypeParameter(GroovySourceAST t, int visit) {
        // print nothing
    }

    public void visitTypeParameters(GroovySourceAST t, int visit) {
        print(t,visit,"<",", ",">");
    }

    public void visitTypeUpperBounds(GroovySourceAST t,int visit) {
        print(t,visit," extends "," & ",null);
    }
    public void visitUnaryMinus(GroovySourceAST t, int visit) {
        print(t,visit,"-",null,null);
    }
    public void visitUnaryPlus(GroovySourceAST t, int visit) {
        print(t,visit,"+",null,null);
    }

    // visit Unused "const", "do", "goto" - unsurprisingly these are unused by the AST.
    
    public void visitVariableDef(GroovySourceAST t,int visit) {
        // do nothing
    }

    // a.k.a. "variable arity parameter" in the JLS
    public void visitVariableParameterDef(GroovySourceAST t,int visit) {
        print(t,visit,null,"... ",null);
    }
    
    // visit Vocab - only used by Lexer
    
    public void visitWildcardType(GroovySourceAST t, int visit) {
        print(t,visit,"?",null,null);
    }

    // visit WS - only used by lexer
    
    
    
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
                        lastLinePrinted = currentLine;
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
