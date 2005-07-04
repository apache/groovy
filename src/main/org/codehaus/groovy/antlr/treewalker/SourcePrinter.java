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
        if (visit == OPENING_VISIT) {
            print(t,visit,"@");
        }
    }

    public void visitAnnotations(GroovySourceAST t, int visit) {
        if (t.getNumberOfChildren() > 0) {
            //todo - default line below is just a placeholder
            visitDefault(t,visit);
        }
    }

    public void visitAssign(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit," = ");
        }
    }
    public void visitClassDef(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,"class ");
        }
    }

    public void visitClosedBlock(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit," {");
        }
        if (visit == SUBSEQUENT_VISIT) {
            print(t,visit," -> ");
        }
        if (visit == CLOSING_VISIT) {
            print(t,visit,"}");
        }
    }

    public void visitDot(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,".");
        }
    }
    public void visitElist(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            //print(t,visit,"(");
        }
        if (visit == SUBSEQUENT_VISIT) {
            print(t,visit,",");
        }
        if (visit == CLOSING_VISIT) {
            //print(t,visit,")");
        }
    }

    public void visitEqual(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit," == ");
        }
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
    public void visitGt(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit," > ");
        }
    }

    public void visitIdent(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
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
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText() + " ");
        }
    }

    public void visitIndexOp(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,"[");
        }
        if (visit == CLOSING_VISIT) {
            print(t,visit,"]");
        }
    }

    public void visitLabeledArg(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,":");
        }
    }

    public void visitListConstructor(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,"[");
        }
        if (visit == CLOSING_VISIT) {
            print(t,visit,"]");
        }
    }

    public void visitLiteralAssert(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText() + " ");
        }
    }

    public void visitLiteralBoolean(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
    }

    public void visitLiteralCatch(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText() + " (");
        } else {
            print(t,visit,")");
        }
    }
    public void visitLiteralFalse(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
    }

    public void visitLiteralFloat(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
    }

    public void visitLiteralIf(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText() + " (");
        } else if (visit == CLOSING_VISIT) {
            print(t,visit,")");
        } else if (visit == SUBSEQUENT_VISIT) {
            print(t,visit," else ");
        }
    }
    public void visitLiteralInt(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
    }

    public void visitLiteralNew(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText() + " ");
        }
        if (visit == SUBSEQUENT_VISIT) {
            print(t,visit,"(");
        }
        if (visit == CLOSING_VISIT) {
            print(t,visit,")");
        }
    }

    public void visitLiteralNull(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
    }

    public void visitLiteralPrivate(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText() + " ");
        }
    }

    public void visitLiteralReturn(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText() + " ");
        }
    }

    public void visitLiteralStatic(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText() + " ");
        }
    }

    public void visitLiteralThis(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,"this");
        }
    }

    public void visitLiteralTrue(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
    }
    public void visitLiteralTry(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
    }
    public void visitLiteralVoid(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
    }
    public void visitLiteralWhile(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText() + " (");
        } else {
            print(t,visit,")");
        }
    }

    public void visitLt(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit," < ");
        }
    }

    public void visitMemberPointer(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,".&");
        }
    }

    public void visitMethodCall(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,"(");
        }
        if (visit == CLOSING_VISIT) {
            print(t,visit,")");
        }
    }
    public void visitMinus(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit," - ");
        }
    }
    public void visitMethodDef(GroovySourceAST t,int visit) {
        //do nothing
    }
    public void visitModifiers(GroovySourceAST t,int visit) {
        //do nothing
    }

    public void visitNotEqual(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit," != ");
        }
    }

    public void visitNumInt(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
    }
    public void visitNumFloat(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,t.getText());
        }
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
        if (visit == OPENING_VISIT) {
            print(t,visit,"package ");
        }
    }

    public void visitParameterDef(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            //todoprint(t,visit,"[[[" + t.getText() + "]]]");
        }
    }

    public void visitParameters(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,"(");
        }
        if (visit == CLOSING_VISIT) {
            print(t,visit,")");
        }
    }

    public void visitPlus(GroovySourceAST t, int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit," + ");
        }
    }

    public void visitQuestion(GroovySourceAST t, int visit) {
        // ternary operator
        if (visit == OPENING_VISIT) {
            print(t,visit,"?");
        }
        if (visit == SUBSEQUENT_VISIT) {
            print(t,visit,":");
        }
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
        if (visit == OPENING_VISIT) {
            print(t,visit,"*");
        }
    }
    public void visitStringConstructor(GroovySourceAST t,int visit) {
        if (visit == SUBSEQUENT_VISIT) {
            print(t,visit," + "); // string concatenate, so ("abc$foo") becomes ("abc" + foo) for now (todo)
        }
    }

    public void visitStringLiteral(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            print(t,visit,"\"" + t.getText() + "\"");
        }
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
        if (visit == OPENING_VISIT) {
            print(t,visit,"(");
        } else {
            print(t,visit,")");
        }
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
