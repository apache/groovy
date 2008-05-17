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

package org.codehaus.groovy.antlr;

/**
 * Process to decorate antlr AST with ending line/col info, and if
 * possible the snipppet of source from the start/end line/col for each node.
 *
 * @author <a href="mailto:groovy@ross-rayner.com">Jeremy Rayner</a>
 * @version $Revision$
 */

import antlr.collections.AST;
import java.util.*;

public class AntlrASTProcessSnippets implements AntlrASTProcessor{

    public AntlrASTProcessSnippets() {
    }

    /**
     * decorate antlr AST with ending line/col info, and if
     * possible the snipppet of source from the start/end line/col for each node.
     * @param t the AST to decorate
     * @return the decorated AST
     */
    public AST process(AST t) {
        // first visit
        List l = new ArrayList();
        traverse((GroovySourceAST)t,l,null);

        //System.out.println("l:" + l);
        // second visit
        Iterator itr = l.iterator();
        if (itr.hasNext()) { itr.next(); /* discard first */ }
        traverse((GroovySourceAST)t,null,itr);
        return t;
    }

    /**
     * traverse an AST node
     * @param t the AST node to traverse
     * @param l A list to add line/col info to
     * @param itr An iterator over a list of line/col
     * @return A decorated AST node
     */
    private void traverse(GroovySourceAST t,List l,Iterator itr) {
         while (t != null) {
             // first visit of node
             if (l != null) {
                 l.add(new LineColumn(t.getLine(),t.getColumn()));
             }

             // second vist of node
             if (itr != null && itr.hasNext()) {
                 LineColumn lc = (LineColumn)itr.next();
                 if (t.getLineLast() == 0) {
                     int nextLine = lc.getLine();
                     int nextColumn = lc.getColumn();
                     if (nextLine < t.getLine() || (nextLine == t.getLine() && nextColumn < t.getColumn())) {
                         nextLine = t.getLine();
                         nextColumn = t.getColumn();
                     }
                     t.setLineLast(nextLine);
                     t.setColumnLast(nextColumn);
                     // This is a good point to call t.setSnippet(),
                     // but it bulks up the AST too much for production code.
                 }
             }

             GroovySourceAST child = (GroovySourceAST)t.getFirstChild();
             if (child != null) {
                 traverse(child,l,itr);
             }

             t = (GroovySourceAST)t.getNextSibling();
         }
    }
}
