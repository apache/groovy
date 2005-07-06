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
import org.codehaus.groovy.antlr.GroovySourceAST;

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
    private PrintStream out;

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
    }

    public void visitAnnotation(GroovySourceAST t, int visit) {
        print(t,visit,"@",null,null);
    }

    public void visitAnnotations(GroovySourceAST t, int visit) {
        if (t.getNumberOfChildren() > 0) {
            //todo - default line below is just a placeholder
            visitDefault(t,visit);
        }
    }

    public void visitAssign(GroovySourceAST t,int visit) {
        print(t,visit," = ",null,null);
    }

    public void visitCaseGroup(GroovySourceAST t, int visit) {
        //do nothing
    }

    public void visitClassDef(GroovySourceAST t,int visit) {
        print(t,visit,"class ",null,null);
    }

    public void visitClosedBlock(GroovySourceAST t, int visit) {
        print(t,visit," {"," -> ","}");
    }

    public void visitDot(GroovySourceAST t,int visit) {
        print(t,visit,".",null,null);
    }
    public void visitElist(GroovySourceAST t,int visit) {
        print(t,visit,null,",",null);
    }

    public void visitEqual(GroovySourceAST t,int visit) {
        print(t,visit," == ",null,null);
    }

    public void visitExpr(GroovySourceAST t,int visit) {
    }

    public void visitExtendsClause(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            if (t.getNumberOfChildren() != 0) {
                print(t,visit," extends ");
            }
        }
    }

    public void visitForInIterable(GroovySourceAST t, int visit) {
        print(t,visit,"("," in ",")");
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
    }

    public void visitImplicitParameters(GroovySourceAST t, int visit) {
    }

    public void visitImport(GroovySourceAST t,int visit) {
        print(t,visit,"import ",null,null);
    }

    public void visitIndexOp(GroovySourceAST t, int visit) {
        print(t,visit,"[",null,"]");
    }

    public void visitLabeledArg(GroovySourceAST t, int visit) {
        print(t,visit,":",null,null);
    }

    public void visitLand(GroovySourceAST t, int visit) {
        print(t,visit," && ",null,null);
    }

    public void visitListConstructor(GroovySourceAST t, int visit) {
        print(t,visit,"[",null,"]");
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
        print(t,visit,"catch (",null,")");
    }
    public void visitLiteralFalse(GroovySourceAST t,int visit) {
        print(t,visit,"false",null,null);
    }

    public void visitLiteralFloat(GroovySourceAST t,int visit) {
        print(t,visit,"float",null,null);
    }

    public void visitLiteralFor(GroovySourceAST t,int visit) {
        print(t,visit,"for ",null,null);
    }

    public void visitLiteralIf(GroovySourceAST t,int visit) {
        // slightly strange as subsequent visit is done after closing visit
        print(t,visit,"if ("," else ",")");
    }
    public void visitLiteralInt(GroovySourceAST t,int visit) {
        print(t,visit,"int",null,null);
    }

    public void visitLiteralNew(GroovySourceAST t,int visit) {
        print(t,visit,"new ","(",")");
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
        }
        if (visit == SUBSEQUENT_VISIT) {
            tabLevel++;
            print(t,visit,") {");
        }
        if (visit == CLOSING_VISIT) {
            print(t,visit,"}");
            tabLevel--;
        }
    }

    public void visitLiteralThis(GroovySourceAST t, int visit) {
        print(t,visit,"this",null,null);
    }

    public void visitLiteralTrue(GroovySourceAST t,int visit) {
        print(t,visit,"true",null,null);
    }
    public void visitLiteralTry(GroovySourceAST t,int visit) {
        print(t,visit,"try",null,null);
    }
    public void visitLiteralVoid(GroovySourceAST t,int visit) {
        print(t,visit,"void",null,null);
    }
    public void visitLiteralWhile(GroovySourceAST t,int visit) {
        print(t,visit,"while (",null,")");
    }

    public void visitLnot(GroovySourceAST t, int visit) {
        print(t,visit,"!",null,null);
    }

    public void visitLt(GroovySourceAST t, int visit) {
        print(t,visit," < ",null,null);
    }

    public void visitMemberPointer(GroovySourceAST t, int visit) {
        print(t,visit,".&",null,null);
    }

    public void visitMethodCall(GroovySourceAST t,int visit) {
        print(t,visit,"(",null,")");
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

    public void visitNumInt(GroovySourceAST t,int visit) {
        print(t,visit,t.getText(),null,null);
    }
    public void visitNumFloat(GroovySourceAST t,int visit) {
        print(t,visit,t.getText(),null,null);
    }
    public void visitObjblock(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            tabLevel++;
            print(t,visit," {");
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
        print(t,visit,"(",",",")");
    }

    public void visitPlus(GroovySourceAST t, int visit) {
        print(t,visit," + ",null,null);
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

    public void visitSlist(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            tabLevel++;
            print(t,visit," {");
        } else {
            tabLevel--;
            printNewlineAndIndent(t,true);
            print(t,visit,"}",true);
        }
    }

    public void visitStar(GroovySourceAST t,int visit) {
        print(t,visit,"*",null,null);
    }
    public void visitStringConstructor(GroovySourceAST t,int visit) {
        print(t,visit,null," + ",null); // string concatenate, so ("abc$foo") becomes ("abc" + foo) for now (todo)
    }

    public void visitStringLiteral(GroovySourceAST t,int visit) {
        print(t,visit,"\"" + t.getText() + "\"",null,null);
    }

    public void visitType(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            if (t.getNumberOfChildren() == 0) {
                print(t,visit,"def");
            }
        }
        if (visit == CLOSING_VISIT) {
            print(t,visit," ");
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

    private void print(GroovySourceAST t,int visit,String opening, String subsequent, String closing) {
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
    private void print(GroovySourceAST t,int visit,String value) {
        print(t,visit,value,false);
    }
    private void print(GroovySourceAST t,int visit,String value,boolean suggestNewline) {
        if(visit == OPENING_VISIT) {
            printNewlineAndIndent(t, suggestNewline);
        }
        out.print(value);
    }

    private void printNewlineAndIndent(GroovySourceAST t, boolean suggestNewline) {
        int currentLine = t.getLine();
        if (lastLinePrinted == 0) { lastLinePrinted = currentLine; }
        if (lastLinePrinted != currentLine || suggestNewline) {
            if (newLines) {
                out.println();
                for (int i=0;i<tabLevel;i++) {
                    out.print("    ");
                }
            }
            lastLinePrinted = currentLine;
        }
    }
}
