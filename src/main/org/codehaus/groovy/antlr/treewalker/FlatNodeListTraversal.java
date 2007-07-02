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
package org.codehaus.groovy.antlr.treewalker;

import java.util.Iterator;
import java.util.List;

import org.codehaus.groovy.antlr.AntlrASTProcessor;
import org.codehaus.groovy.antlr.GroovySourceAST;

import antlr.collections.AST;

/**
 * A simple iterator over an ordered (flat) List of the nodes of the AST.
 *
 * @author <a href="mailto:groovy@ross-rayner.com">Jeremy Rayner</a>
 * @version $Revision: 3626 $
 */
public class FlatNodeListTraversal extends TraversalHelper {
    
    public FlatNodeListTraversal(Visitor visitor) {
        super(visitor);
    }

    public AST process(AST t) {
        GroovySourceAST node = (GroovySourceAST) t;

        // fetch all the nodes in this AST into a List
        NodeCollector collector = new NodeCollector();
        AntlrASTProcessor internalTraversal = new PreOrderTraversal(collector);
        internalTraversal.process(t);
        List listOfAllNodesInThisAST = collector.getNodes();
        
        // process each node in turn
        setUp(node);        
        Iterator itr = listOfAllNodesInThisAST.iterator();
        while (itr.hasNext()) {
        	GroovySourceAST currentNode = (GroovySourceAST) itr.next();
        	accept(currentNode);
        }
        tearDown(node);
        return null;
    }

	protected void accept(GroovySourceAST currentNode) {
    	openingVisit(currentNode);
    	closingVisit(currentNode);
	}    
}
