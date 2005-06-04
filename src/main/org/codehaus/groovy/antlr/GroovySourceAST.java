package org.codehaus.groovy.antlr;

import antlr.collections.AST;
import antlr.*;

/**
 * We have an AST subclass so we can track source information.
 * Very odd that ANTLR doesn't do this by default.
 *
 * @author Mike Spille
 * @author Jeremy Rayner <groovy@ross-rayner.com>
 */
public class GroovySourceAST extends CommonAST {
  private int line;
  private int col;
  private int lineLast;
  private int colLast;
  private String snippet;

  public GroovySourceAST() {
  }

  public GroovySourceAST(Token t) {
    super (t);
  }

  public void initialize(AST ast) {
    super.initialize(ast);
    line = ast.getLine();
    col = ast.getColumn();
  }

  public void initialize(Token t) {
    super.initialize(t);
    line = t.getLine();
    col = t.getColumn();
  }

    public void setLast(Token last) {
        lineLast = last.getLine();
        colLast = last.getColumn();
    }

    public int getLineLast() {
        return lineLast;
    }
    public void setLineLast(int lineLast) {
        this.lineLast = lineLast;
    }

    public int getColumnLast() {
        return colLast;
    }
    public void setColumnLast(int colLast) {
        this.colLast = colLast;
    }

    public int getLine() {
    return (line);
  }

  public int getColumn() {
    return (col);
  }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public String getSnippet() {
        return snippet;
    }

}
