package org.codehaus.groovy.syntax.parser;

import groovy.util.GroovyTestCase;

import org.codehaus.groovy.syntax.Token;

public class CSTNodeTest
    extends GroovyTestCase
{
    public void testConstruct_Default()
    {
        CSTNode node = new CSTNode();

        assertNull( node.getToken() );

        assertLength( 0,
                      node.getChildren() );
    }

    public void testConstruct_WithToken()
    {
        Token token = Token.identifier( 1,
                                        1,
                                        "cheese" );

        CSTNode node = new CSTNode( token );

        assertSame( token,
                    node.getToken() );

        assertLength( 0,
                      node.getChildren() );
    }

    public void testChildren()
    {
        CSTNode node = new CSTNode();

        CSTNode childOne = new CSTNode();
        CSTNode childTwo = new CSTNode();

        node.addChild( childOne );
        node.addChild( childTwo );

        assertLength( 2,
                      node.getChildren() );

        assertSame( childOne,
                    node.getChild( 0 ) );

        assertSame( childTwo,
                    node.getChild( 1 ) );

        assertSame( childOne,
                    node.getChildren()[0] );

        assertSame( childTwo,
                    node.getChildren()[1] );
    }
}
