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
import org.codehaus.groovy.antlr.parser.GroovyTokenTypes;

/**
 * An antlr AST visitor that prints a format suitable for viewing in http://freemind.sourceforge.net
 *
 * @author <a href="mailto:groovy@ross-rayner.com">Jeremy Rayner</a>
 * @version $Revision$
 */

public class MindMapPrinter extends VisitorAdapter {
    private String[] tokenNames;
    private PrintStream out;
    private int depth;

    /**
     * A visitor that prints a format suitable for viewing in http://freemind.sourceforge.net
     * @param out where to print the mindmap file contents to
     * @param tokenNames an array of token names from antlr
     */

    public MindMapPrinter(PrintStream out,String[] tokenNames) {
        this.tokenNames = tokenNames;
        this.out = out;
    }

    public void setUp() {
        depth = 0;
        out.println("<map version='0.7.1'><node TEXT='AST'>");
    }

    public void visitDefault(GroovySourceAST t,int visit) {
        if (visit == OPENING_VISIT) {
            depth++;
            String name = getName(t);
            String colour = getColour(t);
            String folded = getFolded(t);
            out.print("<node TEXT='" + name + "' POSITION='right'" + colour + folded + ">");
        } else {
            out.println("</node>");
            depth--;
        }
    }

    public void tearDown() {
        out.println("</node></map>");
    }

    private String getFolded(GroovySourceAST t) {
        if (depth > 2 && t.getNumberOfChildren() > 0) {
            switch (t.getType()) {
                case GroovyTokenTypes.EXPR :
                case GroovyTokenTypes.METHOD_DEF :
                case GroovyTokenTypes.VARIABLE_DEF :
                    return " FOLDED='true'";
            }
        }
        if (t.getType() == GroovyTokenTypes.IMPORT) {
            return " FOLDED='true'";
        }
        return "";
    }

    private String getColour(GroovySourceAST t) {
        String colour = "";
        if (t.getNumberOfChildren() == 0) {
            colour = " COLOR=\"#006699\"";
        }
        return colour;
    }

    private String getName(GroovySourceAST t) {
        String name = tokenNames[t.getType()] + " <" + t.getType() + ">";
        if (!(escape(tokenNames[t.getType()]).equals(escape(t.getText())))) {
            name = name + " : " + t.getText();
        }
        switch (t.getType()) {
            case GroovyTokenTypes.METHOD_DEF :
            case GroovyTokenTypes.VARIABLE_DEF :
                GroovySourceAST identNode = t.childOfType(GroovyTokenTypes.IDENT);
                if (identNode != null) {
                    name = name + " : " + identNode.getText() + "";
                }
        }
        name = escape(name);
        return name;
    }

    private String escape(String name) {
        name = name.replace('"',' ');
        name = name.replace('\'',' ');
        name = name.replaceAll("&","&amp;");
        name = name.trim();
        return name;
    }

}
