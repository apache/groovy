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

  public int getLine() {
    return (line);
  }

  public int getColumn() {
    return (col);
  }
}
