/*
 $Id$

 Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

 Redistribution and use of this software and associated documentation
 ("Software"), with or without modification, are permitted provided
 that the following conditions are met:

 1. Redistributions of source code must retain copyright
    statements and notices.  Redistributions must also contain a
    copy of this document.

 2. Redistributions in binary form must reproduce the
    above copyright notice, this list of conditions and the
    following disclaimer in the documentation and/or other
    materials provided with the distribution.

 3. The name "groovy" must not be used to endorse or promote
    products derived from this Software without prior written
    permission of The Codehaus.  For written permission,
    please contact info@codehaus.org.

 4. Products derived from this Software may not be called "groovy"
    nor may "groovy" appear in their names without prior written
    permission of The Codehaus. "groovy" is a registered
    trademark of The Codehaus.

 5. Due credit should be given to The Codehaus -
    http://groovy.codehaus.org/

 THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
 ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
 NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
 THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 OF THE POSSIBILITY OF SUCH DAMAGE.

 */

package org.codehaus.groovy.syntax.parser;

import org.codehaus.groovy.syntax.Token;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;


/** 
 *  Node in the concrete syntax tree.
 *
 *  @see Parser
 *  @see Token
 *
 *  @author <a href="mailto:bob@werken.com">bob mcwhirter</a>
 *
 *  @version $Id$
 */
public class CSTNode
{
    public static final CSTNode[] EMPTY_ARRAY = new CSTNode[0];

    private Token token;     // an optional identifier for this node
    private List children;   // a list of child nodes


   /**
    *  Initializes the node with token set to <code>null</code>.
    */

    public CSTNode()
    {
        this.children = new ArrayList();
    }


   /**
    *  Initializes the node with the specified token.
    */

    public CSTNode(Token token)
    {
        this.children = new ArrayList();
        this.token = token;
    }


   /**
    *  Returns the token set on construction, or null.
    */
    
    public Token getToken()
    {
        return this.token;
    }


   /**
    *  Changes the token set on construction.  Not generally
    *  a good idea.  :-)
    */

    public void setToken( Token value )
    {
        token = value;
    }


   /**
    *  Returns true if the node is empty (no token, no children).
    */

    public boolean isEmpty()
    {
        return token == null && children.size() == 0;
    }


   /**
    *  Appends a child node to the child list.
    */

    public void addChild(CSTNode node)
    {
        this.children.add( node );
    }


   /**
    *  Returns the child at the specified index.
    */

    public CSTNode getChild(int index)
    {
        if( index < 0 ) {
            index = this.children.size() + index;
        }

        return (CSTNode) this.children.get( index );
    }


   /**
    *  Returns an array of all children.
    */

    public CSTNode[] getChildren()
    {
        return (CSTNode[]) this.children.toArray( CSTNode.EMPTY_ARRAY );
    }


   /**
    *  Returns the number of children.
    */

    public int children()
    {
        return this.children.size();
    }


   /**
    *  Returns an <code>Iterator</code> on the child list.
    */

    public Iterator childIterator()
    {
        return this.children.iterator();
    }
    
   
   /**
    *  Returns a pretty-printed representation of the node, 
    *  including all descendents.
    */

    public String toString()
    {
        return dump();
    }


   /**
    *  Generates the pretty-printed representation of the node
    *  returned by <code>toString</code>.
    */

    public String dump()
    {
        String indent = "";

        return dump( indent );
    }


   /**
    *  Does the actual work of generating the pretty-printing for
    *  this node.
    */

    protected String dump(String indent)
    {
        StringBuffer buf = new StringBuffer();

        buf.append( indent + "[CSTNode: token=" + token + "\n" );

        CSTNode[] children = getChildren();

        for ( int i = 0 ; i < children.length ; ++i )
        {
            buf.append( indent + "..." + children[ i ].dump( indent + "    " ) + "\n" );
        }
        
        return buf.toString();
    }
}
