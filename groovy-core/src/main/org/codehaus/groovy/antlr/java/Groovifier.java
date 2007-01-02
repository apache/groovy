package org.codehaus.groovy.antlr.java;

import org.codehaus.groovy.antlr.GroovySourceAST;
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;
import org.codehaus.groovy.antlr.treewalker.VisitorAdapter;

public class Groovifier extends VisitorAdapter implements GroovyTokenTypes {
    private String[] tokenNames;
    
	public Groovifier(String[] tokenNames) {
		this.tokenNames = tokenNames;
	}
	
    public void visitDefault(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            // only want to do this once per node...

        	// remove 'public' when implied already
        	if (t.getType() == LITERAL_public) {
        		t.setType(EXPR);
        	}
/*        	if (t.getType() == MODIFIERS) {
       			GroovySourceAST publicNode = t.childOfType(LITERAL_public);
       			if (t.getNumberOfChildren() > 1 && publicNode != null) {
       				// has more than one modifier, and one of them is public
       				
       				// delete 'public' node
       				publicNode.setType(EXPR); // near enough the same as delete for now...
       			}
        	}*/
        	// ----        	
        }
    }
}
