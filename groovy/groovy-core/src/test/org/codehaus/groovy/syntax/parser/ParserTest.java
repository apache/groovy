package org.codehaus.groovy.syntax.parser;

import org.codehaus.groovy.GroovyTestCase;

import org.codehaus.groovy.syntax.lexer.CharStream;
import org.codehaus.groovy.syntax.lexer.StringCharStream;
import org.codehaus.groovy.syntax.lexer.Lexer;
import org.codehaus.groovy.syntax.lexer.LexerTokenStream;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.TokenStream;

public class ParserTest
    extends GroovyTestCase
{ 

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     package
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testPackageDeclaration_NoDots()
        throws Exception
    {
        Parser parser = newParser( "package cheese" );

        CSTNode root = parser.packageDeclaration();

        assertNode( root,
                    "package",
                    Token.KEYWORD_PACKAGE,
                    1 );

        {
            assertNode( root.getChild( 0 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );
        }
    }

    public void testPackageDeclaration_OneDot()
        throws Exception
    {
        Parser parser = newParser( "package cheese.toast" );

        CSTNode root = parser.packageDeclaration();

        assertNode( root,
                    "package",
                    Token.KEYWORD_PACKAGE,
                    1 );

        {
            assertNode( root.getChild( 0 ),
                        ".",
                        Token.DOT,
                        2 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "cheese",
                            Token.IDENTIFIER,
                            0 );

                assertNode( root.getChild( 0 ).getChild( 1 ),
                            "toast",
                            Token.IDENTIFIER,
                            0 );
            }
        }
    }

    public void testPackageDeclaration_MultipleDots()
        throws Exception
    {
        Parser parser = newParser( "package cheddar.cheese.toast" );

        CSTNode root = parser.packageDeclaration();

        assertNode( root,
                    "package",
                    Token.KEYWORD_PACKAGE,
                    1 );

        {
            assertNode( root.getChild( 0 ),
                        ".",
                        Token.DOT,
                        2 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            ".",
                            Token.DOT,
                            2 );
                {
                    assertNode( root.getChild( 0 ).getChild( 0 ).getChild( 0 ),
                                "cheddar",
                                Token.IDENTIFIER,
                                0 );

                    assertNode( root.getChild( 0 ).getChild( 0 ).getChild( 1 ),
                                "cheese",
                                Token.IDENTIFIER,
                                0 );
                }

                assertNode( root.getChild( 0 ).getChild( 1 ),
                            "toast",
                            Token.IDENTIFIER,
                            0 );
            }
        }
    }

    public void testPackageDeclaration_UnexpectedToken_NoInitialIdentifier()
        throws Exception
    {
        Parser parser = newParser( "package ." );

        try
        {
            parser.packageDeclaration();
            fail( "should have thrown UnexpectedTokenException" );
        }
        catch (UnexpectedTokenException e)
        {
            // expected and correct
            assertToken( e.getToken(),
                         ".",
                         Token.DOT );
            
            assertLength( 1,
                          e.getExpectedTypes() );

            assertContains( Token.IDENTIFIER,
                            e.getExpectedTypes() );
        }
    }

    public void testPackageDeclaration_UnexpectedToken_NoIdentifierFollowingDot()
        throws Exception
    {
        Parser parser = newParser( "package cheese." );

        try
        {
            parser.packageDeclaration();
            fail( "should have thrown UnexpectedTokenException" );
        }
        catch (UnexpectedTokenException e)
        {
            // expected and correct
            assertNull( e.getToken() );
            
            assertLength( 1,
                          e.getExpectedTypes() );

            assertContains( Token.IDENTIFIER,
                            e.getExpectedTypes() );
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     import
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testImportStatement_NoDots()
        throws Exception
    {
        Parser parser = newParser( "import Cheese" );

        CSTNode root = parser.importStatement();

        assertNode( root,
                    "import",
                    Token.KEYWORD_IMPORT,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        "Cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 1 ),
                            0 );
        }
    }

    public void testImportStatement_As_NoDots()
        throws Exception
    {
        Parser parser = newParser( "import Cheese as Toast" );

        CSTNode root = parser.importStatement();

        assertNode( root,
                    "import",
                    Token.KEYWORD_IMPORT,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        "Cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNode( root.getChild( 1 ),
                        "as",
                        Token.KEYWORD_AS,
                        1 );
            {
                assertNode( root.getChild( 1 ).getChild( 0 ),
                            "Toast",
                            Token.IDENTIFIER,
                            0 );
            }
        }
    }

    public void testImportStatement_OneDot()
        throws Exception
    {
        Parser parser = newParser( "import cheese.Toast" );

        CSTNode root = parser.importStatement();

        assertNode( root,
                    "import",
                    Token.KEYWORD_IMPORT,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        ".",
                        Token.DOT,
                        2 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "cheese",
                            Token.IDENTIFIER,
                            0 );

                assertNode( root.getChild( 0 ).getChild( 1 ),
                            "Toast",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNullNode( root.getChild( 1 ),
                            0 );
        }
    }

    public void testImportStatement_As_OneDot()
        throws Exception
    {
        Parser parser = newParser( "import cheese.Toast as Bread" );

        CSTNode root = parser.importStatement();

        assertNode( root,
                    "import",
                    Token.KEYWORD_IMPORT,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        ".",
                        Token.DOT,
                        2 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "cheese",
                            Token.IDENTIFIER,
                            0 );

                assertNode( root.getChild( 0 ).getChild( 1 ),
                            "Toast",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNode( root.getChild( 1 ),
                        "as",
                        Token.KEYWORD_AS,
                        1 );
            {
                assertNode( root.getChild( 1 ).getChild( 0 ),
                            "Bread",
                            Token.IDENTIFIER,
                            0 );
            }
        }
    }

    public void testImportStatement_MultipleDots()
        throws Exception
    {
        Parser parser = newParser( "import cheddar.cheese.Toast" );

        CSTNode root = parser.importStatement();

        assertNode( root,
                    "import",
                    Token.KEYWORD_IMPORT,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        ".",
                        Token.DOT,
                        2 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            ".",
                            Token.DOT,
                            2 );
                {
                    assertNode( root.getChild( 0 ).getChild( 0 ).getChild( 0 ),
                                "cheddar",
                                Token.IDENTIFIER,
                                0 );

                    assertNode( root.getChild( 0 ).getChild( 0 ).getChild( 1 ),
                                "cheese",
                                Token.IDENTIFIER,
                                0 );
                }
                
                assertNode( root.getChild( 0 ).getChild( 1 ),
                            "Toast",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNullNode( root.getChild( 1 ),
                            0 );
        }
    }

    public void testImportStatement_As_MultipleDots()
        throws Exception
    {
        Parser parser = newParser( "import cheddar.cheese.Toast as Bread" );

        CSTNode root = parser.importStatement();

        assertNode( root,
                    "import",
                    Token.KEYWORD_IMPORT,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        ".",
                        Token.DOT,
                        2 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            ".",
                            Token.DOT,
                            2 );
                {
                    assertNode( root.getChild( 0 ).getChild( 0 ).getChild( 0 ),
                                "cheddar",
                                Token.IDENTIFIER,
                                0 );

                    assertNode( root.getChild( 0 ).getChild( 0 ).getChild( 1 ),
                                "cheese",
                                Token.IDENTIFIER,
                                0 );
                }

                assertNode( root.getChild( 0 ).getChild( 1 ),
                            "Toast",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNode( root.getChild( 1 ),
                        "as",
                        Token.KEYWORD_AS,
                        1 );
            {
                assertNode( root.getChild( 1 ).getChild( 0 ),
                            "Bread",
                            Token.IDENTIFIER,
                            0 );
            }
        }
    }

    public void testImportStatement_UnexpectedToken_NoInitialIdentifier()
        throws Exception
    {
        Parser parser = newParser( "import ." );

        try
        {
            parser.importStatement();
            fail( "should have thrown UnexpectedTokenException" );
        }
        catch (UnexpectedTokenException e)
        {
            // expected and correct
            assertToken( e.getToken(),
                         ".",
                         Token.DOT );
            
            assertLength( 1,
                          e.getExpectedTypes() );

            assertContains( Token.IDENTIFIER,
                            e.getExpectedTypes() );
        }
    }

    public void testImportStatement_UnexpectedToken_NoIdentifierFollowingDot()
        throws Exception
    {
        Parser parser = newParser( "import cheese." );

        try
        {
            parser.importStatement();
            fail( "should have thrown UnexpectedTokenException" );
        }
        catch (UnexpectedTokenException e)
        {
            // expected and correct
            assertNull( e.getToken() );
            
            assertLength( 1,
                          e.getExpectedTypes() );

            assertContains( Token.IDENTIFIER,
                            e.getExpectedTypes() );
        }
    }
        
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     class
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testClassDeclaration_NoModifiers_NoBody_NoExtendsOrImplements()
        throws Exception
    {
        Parser parser = newParser( "class Cheese { }" );

        CSTNode modifiers = new CSTNode();

        CSTNode root = parser.classDeclaration( modifiers );

        assertNode( root,
                    "class",
                    Token.KEYWORD_CLASS,
                    5 );

        {
            assertSame( modifiers,
                        root.getChild( 0 ) );

            assertNode( root.getChild( 1 ),
                        "Cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );

            assertNullNode( root.getChild( 3 ),
                            0 );

            assertNullNode( root.getChild( 4 ),
                            0 );
        }
    }

    public void testClassDeclaration_NoIdentifier()
        throws Exception
    {
        Parser parser = newParser( "class {" );

        CSTNode modifiers = new CSTNode();

        try
        {
            parser.classDeclaration( modifiers );
        }
        catch (UnexpectedTokenException e)
        {
            // expected and correct
            assertToken( e.getToken(),
                         "{",
                         Token.LEFT_CURLY_BRACE );

            assertLength( 1,
                          e.getExpectedTypes() );

            assertContains( Token.IDENTIFIER,
                            e.getExpectedTypes() );
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     interface
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testInterfaceDeclaration_NoModifiers_NoBody_NoExtendsOrImplements()
        throws Exception
    {
        Parser parser = newParser( "interface Cheese { }" );

        CSTNode modifiers = new CSTNode();

        CSTNode root = parser.interfaceDeclaration( modifiers );

        assertNode( root,
                    "interface",
                    Token.KEYWORD_INTERFACE,
                    3 );

        {
            assertSame( modifiers,
                        root.getChild( 0 ) );

            assertNode( root.getChild( 1 ),
                        "Cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     <type declaration>
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testTypeDeclaration_Class_NoModifiers_NoBody_NoExtendsOrImplements()
        throws Exception
    {
        Parser parser = newParser( "class Cheese { }" );

        CSTNode root = parser.typeDeclaration();

        assertNode( root,
                    "class",
                    Token.KEYWORD_CLASS,
                    5 );

        {
            assertNullNode( root.getChild( 0 ),
                            0 );

            assertNode( root.getChild( 1 ),
                        "Cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );

            assertNullNode( root.getChild( 3 ),
                            0 );

            assertNullNode( root.getChild( 4 ),
                            0 );
        }
    }

    public void testTypeDeclaration_Class_WithModifiers_NoBody_NoExtendsOrImplements()
        throws Exception
    {
        Parser parser = newParser( "public class Cheese { }" );

        CSTNode root = parser.typeDeclaration();

        assertNode( root,
                    "class",
                    Token.KEYWORD_CLASS,
                    5 );

        {
            assertNullNode( root.getChild( 0 ),
                            1 );
            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "public",
                            Token.KEYWORD_PUBLIC,
                            0 );
            }

            assertNode( root.getChild( 1 ),
                        "Cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );

            assertNullNode( root.getChild( 3 ),
                            0 );

            assertNullNode( root.getChild( 4 ),
                            0 );
        }
    }

    public void testTypeDeclaration_Interface_NoModifiers_NoBody_NoExtendsOrImplements()
        throws Exception
    {
        Parser parser = newParser( "interface Cheese { }" );

        CSTNode root = parser.typeDeclaration();

        assertNode( root,
                    "interface",
                    Token.KEYWORD_INTERFACE,
                    3 );

        {
            assertNullNode( root.getChild( 0 ),
                            0 );

            assertNode( root.getChild( 1 ),
                        "Cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );
        }
    }

    public void testTypeDeclaration_Interface_WithModifiers_NoBody_NoExtendsOrImplements()
        throws Exception
    {
        Parser parser = newParser( "public interface Cheese { }" );

        CSTNode root = parser.typeDeclaration();

        assertNode( root,
                    "interface",
                    Token.KEYWORD_INTERFACE,
                    3 );

        {
            assertNullNode( root.getChild( 0 ),
                            1 );
            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "public",
                            Token.KEYWORD_PUBLIC,
                            0 );
            }

            assertNode( root.getChild( 1 ),
                        "Cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );
        }
    }

    public void testTypeDeclaration_UnexpectedToken()
        throws Exception
    {
        Parser parser = newParser( "cheese" );

        try
        {
            parser.typeDeclaration();
            fail( "should have thrown UnexpectedTokenException" );
        }
        catch (UnexpectedTokenException e)
        {
            assertToken( e.getToken(),
                         "cheese",
                         Token.IDENTIFIER );

            assertLength( 2,
                          e.getExpectedTypes() );

            assertContains( Token.KEYWORD_CLASS,
                            e.getExpectedTypes() );

            assertContains( Token.KEYWORD_INTERFACE,
                            e.getExpectedTypes() );
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     <compilation unit>
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testCompilationUnit_NoImports_OneClass()
        throws Exception
    {
        Parser parser = newParser( "package cheese; public class Cheese{}" );

        CSTNode root = parser.compilationUnit();

        assertNullNode( root,
                        3 );

        {
            assertNode( root.getChild( 0 ),
                        "package",
                        Token.KEYWORD_PACKAGE,
                        1 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "cheese",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNullNode( root.getChild( 1 ),
                            0 );

            assertNode( root.getChild( 2 ),
                        "class",
                        Token.KEYWORD_CLASS,
                        5 );
        }
    }

    public void testCompilationUnit_NoImports_OneInterface()
        throws Exception
    {
        Parser parser = newParser( "package cheese; public interface Cheese{}" );

        CSTNode root = parser.compilationUnit();

        assertNullNode( root,
                        3 );

        {
            assertNode( root.getChild( 0 ),
                        "package",
                        Token.KEYWORD_PACKAGE,
                        1 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "cheese",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNullNode( root.getChild( 1 ),
                            0 );

            assertNode( root.getChild( 2 ),
                        "interface",
                        Token.KEYWORD_INTERFACE,
                        3 );
        }
    }

    public void testCompilationUnit_WithImports_OneClass()
        throws Exception
    {
        Parser parser = newParser( "package cheese; import com.Toast; import com.Jelly; public class Cheese{}" );

        CSTNode root = parser.compilationUnit();

        assertNullNode( root,
                        3 );

        {
            assertNode( root.getChild( 0 ),
                        "package",
                        Token.KEYWORD_PACKAGE,
                        1 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "cheese",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNullNode( root.getChild( 1 ),
                            2 );

            {
                assertNode( root.getChild( 1 ).getChild( 0 ),
                            "import",
                            Token.KEYWORD_IMPORT,
                            2 );

                assertNode( root.getChild( 1 ).getChild( 1 ),
                            "import",
                            Token.KEYWORD_IMPORT,
                            2 );
            }

            assertNode( root.getChild( 2 ),
                        "class",
                        Token.KEYWORD_CLASS,
                        5 );
        }
    }

    public void testCompilationUnit_WithImports_TwoClasses()
        throws Exception
    {
        Parser parser = newParser( "package cheese; import com.Toast; import com.Jelly; public class Cheese{} public class Goober {}" );

        CSTNode root = parser.compilationUnit();

        assertNullNode( root,
                        4 );

        {
            assertNode( root.getChild( 0 ),
                        "package",
                        Token.KEYWORD_PACKAGE,
                        1 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "cheese",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNullNode( root.getChild( 1 ),
                            2 );

            {
                assertNode( root.getChild( 1 ).getChild( 0 ),
                            "import",
                            Token.KEYWORD_IMPORT,
                            2 );

                assertNode( root.getChild( 1 ).getChild( 1 ),
                            "import",
                            Token.KEYWORD_IMPORT,
                            2 );
            }
            
            assertNode( root.getChild( 2 ),
                        "class",
                        Token.KEYWORD_CLASS,
                        5 );
            {
                assertNode( root.getChild( 2 ).getChild( 1 ),
                            "Cheese",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNode( root.getChild( 3 ),
                        "class",
                        Token.KEYWORD_CLASS,
                        5 );
            {
                assertNode( root.getChild( 3 ).getChild( 1 ),
                            "Goober",
                            Token.IDENTIFIER,
                            0 );
            }
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     <body statement>
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testBodyStatement_PropertyDeclaration_NoModifiers_NoType()
        throws Exception
    {
        Parser parser = newParser( "property cheese;" );

        CSTNode root = parser.bodyStatement();

        assertNode( root,
                    "property",
                    Token.KEYWORD_PROPERTY,
                    3 );

        {
            assertNullNode( root.getChild( 0 ),
                            0 );

            assertNode( root.getChild( 1 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );
        }
    }

    public void testBodyStatement_PropertyDeclaration_OneModifier_NoType()
        throws Exception
    {
        Parser parser = newParser( "static property cheese;" );

        CSTNode root = parser.bodyStatement();

        assertNode( root,
                    "property",
                    Token.KEYWORD_PROPERTY,
                    3 );

        {
            assertNullNode( root.getChild( 0 ),
                            1 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "static",
                            Token.KEYWORD_STATIC,
                            0 );
            }

            assertNode( root.getChild( 1 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );
        }
    }

    public void testBodyStatement_PropertyDeclaration_TwoModifiers_NoType()
        throws Exception
    {
        Parser parser = newParser( "static synchronized property cheese;" );

        CSTNode root = parser.bodyStatement();

        assertNode( root,
                    "property",
                    Token.KEYWORD_PROPERTY,
                    3 );

        {
            assertNullNode( root.getChild( 0 ),
                            2 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "static",
                            Token.KEYWORD_STATIC,
                            0 );

                assertNode( root.getChild( 0 ).getChild( 1 ),
                            "synchronized",
                            Token.KEYWORD_SYNCHRONIZED,
                            0 );
            }
            
            assertNode( root.getChild( 1 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );
        }
    }

    public void testBodyStatement_MethodDeclaration_NoReturnType_NoParameters()
        throws Exception
    {
        Parser parser = newParser( "cheeseIt() { } " );

        CSTNode root = parser.bodyStatement();

        assertNode( root,
                    "<synthetic>",
                    Token.SYNTH_METHOD,
                    5 );

        {
            assertNullNode( root.getChild( 0 ),
                            0 );

            assertNode( root.getChild( 1 ),
                        "cheeseIt",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );

            assertNode( root.getChild( 3 ),
                        "(",
                        Token.LEFT_PARENTHESIS,
                        0 );
        }
    }

    public void testBodyStatement_MethodDeclaration_WithReturnType_NoParameters()
        throws Exception
    {
        Parser parser = newParser( "String cheeseIt() { }" );

        CSTNode root = parser.bodyStatement();

        assertNode( root,
                    "<synthetic>",
                    Token.SYNTH_METHOD,
                    5 );

        {
            assertNullNode( root.getChild( 0 ),
                            0 );

            assertNode( root.getChild( 1 ),
                        "cheeseIt",
                        Token.IDENTIFIER,
                        0 );

            assertNode( root.getChild( 2 ),
                        "String",
                        Token.IDENTIFIER,
                        0 );

            assertNode( root.getChild( 3 ),
                        "(",
                        Token.LEFT_PARENTHESIS,
                        0 );
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     <parameter>
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testParameterList()
        throws Exception
    {
    }

    public void testParameterDeclarationWithoutDatatype()
        throws Exception
    {
        Parser parser = newParser( "cheese" );

        CSTNode root = parser.parameterDeclarationWithoutDatatype();

        assertNode( root,
                    "<synthetic>",
                    Token.SYNTH_PARAMETER_DECLARATION,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 1 ),
                            0 );
        }
    }

    public void testParameterDeclarationWithDatatype_Simple()
        throws Exception
    {
        Parser parser = newParser( "String cheese" );

        CSTNode root = parser.parameterDeclarationWithDatatype();

        assertNode( root,
                    "<synthetic>",
                    Token.SYNTH_PARAMETER_DECLARATION,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        "String",
                        Token.IDENTIFIER,
                        0 );

            assertNode( root.getChild( 1 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );
        }
    }

    public void testParameterDeclarationWithDatatype_Qualified()
        throws Exception
    {
        Parser parser = newParser( "java.lang.String cheese" );

        CSTNode root = parser.parameterDeclarationWithDatatype();

        assertNode( root,
                    "<synthetic>",
                    Token.SYNTH_PARAMETER_DECLARATION,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        ".",
                        Token.DOT,
                        2 );
            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            ".",
                            Token.DOT,
                            2 );

                {
                    assertNode( root.getChild( 0 ).getChild( 0 ).getChild( 0 ),
                                "java",
                                Token.IDENTIFIER,
                                0 );

                    assertNode( root.getChild( 0 ).getChild( 0 ).getChild( 1 ),
                                "lang",
                                Token.IDENTIFIER,
                                0 );
                }

                assertNode( root.getChild( 0 ).getChild( 1 ),
                            "String",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNode( root.getChild( 1 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );
        }
    }

    public void testParameterDeclaration_General_WithoutDatatype()
        throws Exception
    {
        Parser parser = newParser( "cheese" );

        CSTNode root = parser.parameterDeclaration();

        assertNode( root,
                    "<synthetic>",
                    Token.SYNTH_PARAMETER_DECLARATION,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 1 ),
                            0 );
        }
    }

    public void testParameterDeclaration_General_WithDatatype()
        throws Exception
    {
        Parser parser = newParser( "String cheese" );

        CSTNode root = parser.parameterDeclaration();

        assertNode( root,
                    "<synthetic>",
                    Token.SYNTH_PARAMETER_DECLARATION,
                    2 );

        {
            assertNode( root.getChild( 0 ),
                        "String",
                        Token.IDENTIFIER,
                        0 );

            assertNode( root.getChild( 1 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     <parameter list>
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testParameterList_Empty()
        throws Exception
    {
        Parser parser = newParser( "" );

        CSTNode root = parser.parameterList();

        assertNullNode( root,
                        0 );
    }

    /*
    public void testParameterList_One_WithoutDatatype()
        throws Exception
    {
        Parser parser = newParser( "cheese" );

        CSTNode root = parser.parameterList();

        assertNullNode( root,
                        1 );

        {
            assertNode( root.getChild( 0 ),
                        "<synthetic>",
                        Token.SYNTH_PARAMETER_DECLARATION,
                        2 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "cheese",
                            Token.IDENTIFIER,
                            0 );

                assertNullNode( root.getChild( 0 ).getChild( 1 ),
                                0 );
            }
        }
    }

    public void testParameterList_One_WithDatatype()
        throws Exception
    {
        Parser parser = newParser( "String cheese" );

        CSTNode root = parser.parameterList( new CSTNode() );

        assertNullNode( root,
                        1 );

        {
            assertNode( root.getChild( 0 ),
                        "<synthetic>",
                        Token.SYNTH_PARAMETER_DECLARATION,
                        2 );

            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "String",
                            Token.IDENTIFIER,
                            0 );

                assertNode( root.getChild( 0 ).getChild( 1 ),
                            "cheese",
                            Token.IDENTIFIER,
                            0 );
            }
        }
    }

    public void testParameterList_Two_WithoutDatatype()
        throws Exception
    {
        Parser parser = newParser( "cheese, toast" );

        CSTNode root = parser.parameterList( new CSTNode() );

        assertNullNode( root,
                        2 );

        {
            assertNode( root.getChild( 0 ),
                        "<synthetic>",
                        Token.SYNTH_PARAMETER_DECLARATION,
                        2 );
            
            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "cheese",
                            Token.IDENTIFIER,
                            0 );
                
                assertNullNode( root.getChild( 0 ).getChild( 1 ),
                                0 );
            }
            
            assertNode( root.getChild( 1 ),
                        "<synthetic>",
                        Token.SYNTH_PARAMETER_DECLARATION,
                        2 );
            
            {
                assertNode( root.getChild( 1 ).getChild( 0 ),
                            "toast",
                            Token.IDENTIFIER,
                            0 );
                
                assertNullNode( root.getChild( 1 ).getChild( 1 ),
                                0 );
            }
        }
    }
    */
    
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     method
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testMethod_NoModifiers_NoReturnType_NoParameters()
        throws Exception
    {
        Parser parser = newParser( "cheeseIt() { }" );

        CSTNode modifiers = new CSTNode();

        CSTNode root = parser.methodDeclaration( modifiers );

        assertNode( root,
                    "<synthetic>",
                    Token.SYNTH_METHOD,
                    5 );

        {
            assertNullNode( root.getChild( 0 ),
                            0 );

            assertNode( root.getChild( 1 ),
                        "cheeseIt",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );

            assertNode( root.getChild( 3 ),
                        "(",
                        Token.LEFT_PARENTHESIS,
                        0 );
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     property
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testProperty_NoModifiers_NoType()
        throws Exception
    {
        Parser parser = newParser( "property cheese" );

        CSTNode modifiers = new CSTNode();

        CSTNode root = parser.propertyDeclaration( modifiers );

        assertNode( root,
                    "property",
                    Token.KEYWORD_PROPERTY,
                    3 );

        {
            assertSame( modifiers,
                        root.getChild( 0 ) );

            assertNode( root.getChild( 1 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNullNode( root.getChild( 2 ),
                            0 );
        }
    }

    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
    //     ((misc))
    // - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 

    public void testIsModifier()
        throws Exception
    {
        assertTrue( Parser.isModifier( Token.KEYWORD_PUBLIC ) );
        assertTrue( Parser.isModifier( Token.KEYWORD_PROTECTED ) );
        assertTrue( Parser.isModifier( Token.KEYWORD_PRIVATE ) );
        assertTrue( Parser.isModifier( Token.KEYWORD_STATIC ) );
        assertTrue( Parser.isModifier( Token.KEYWORD_FINAL ) );
        assertTrue( Parser.isModifier( Token.KEYWORD_SYNCHRONIZED ) );
        assertFalse( Parser.isModifier( Token.IDENTIFIER ) );
    }

    public void testConsumeUntil_Found()
        throws Exception
    {
        Parser parser = newParser( "cheese toast is; bread" );

        assertToken( parser.la(),
                     "cheese",
                     Token.IDENTIFIER );

        parser.consumeUntil( Token.SEMICOLON );

        assertToken( parser.la(),
                     "bread",
                     Token.IDENTIFIER );
    }
    
    public void testConsumeUntil_NotFound()
        throws Exception
    {
        Parser parser = newParser( "cheese toast" );

        assertToken( parser.la(),
                     "cheese",
                     Token.IDENTIFIER );

        parser.consumeUntil( Token.SEMICOLON );

        assertNull( parser.la() );
    }

    public void testDatatype_NoDots()
        throws Exception
    {
        Parser parser = newParser( "Cheese" );

        CSTNode root = parser.datatype();

        assertNode( root,
                    "Cheese",
                    Token.IDENTIFIER,
                    0 );
    }

    public void testDatatype_OneDot()
        throws Exception
    {
        Parser parser = newParser( "cheese.Toast" );

        CSTNode root = parser.datatype();

        assertNode( root,
                    ".",
                    Token.DOT,
                    2 );
        {
            assertNode( root.getChild( 0 ),
                        "cheese",
                        Token.IDENTIFIER,
                        0 );

            assertNode( root.getChild( 1 ),
                        "Toast",
                        Token.IDENTIFIER,
                        0 );
        }
    }

    public void testDatatype_TwoDots()
        throws Exception
    {
        Parser parser = newParser( "toast.is.Bread" );

        CSTNode root = parser.datatype();

        assertNode( root,
                    ".",
                    Token.DOT,
                    2 );
        {
            assertNode( root.getChild( 0 ),
                        ".",
                        Token.DOT,
                        2 );
            {
                assertNode( root.getChild( 0 ).getChild( 0 ),
                            "toast",
                            Token.IDENTIFIER,
                            0 );

                assertNode( root.getChild( 0 ).getChild( 1 ),
                            "is",
                            Token.IDENTIFIER,
                            0 );
            }

            assertNode( root.getChild( 1 ),
                        "Bread",
                        Token.IDENTIFIER,
                        0 );
        }
    }

    // ----------------------------------------------------------------------
    // ----------------------------------------------------------------------

    protected void assertNullNode(CSTNode node,
                                  int numChildren)
    {
        assertNotNull( node );
        assertNull( node.getToken() );
        assertLength( numChildren,
                      node.getChildren() );
    }

    protected void assertNode(CSTNode node,
                              String text,
                              int type)
    {
        assertNotNull( node );
        assertNotNull( node.getToken() );
        assertEquals( text,
                      node.getToken().getText() );
        assertEquals( type,
                      node.getToken().getType() );
    }

    protected void assertNode(CSTNode node,
                              String text,
                              int type,
                              int numChildren)
    {
        assertNotNull( node );
        assertNotNull( node.getToken() );
        assertEquals( text,
                      node.getToken().getText() );
        assertEquals( type,
                      node.getToken().getType() );
        assertLength( numChildren,
                      node.getChildren() );
    }

    protected void assertToken(Token token,
                               String text,
                               int type)
    {
        assertNotNull( token );
        assertEquals( text,
                      token.getText() );
        assertEquals( type,
                      token.getType() );
    }

    protected Parser newParser(String text)
    {
        CharStream  chars  = new StringCharStream( text );
        Lexer       lexer  = new Lexer( chars );
        TokenStream tokens = new LexerTokenStream( lexer );

        return new Parser( tokens );
    }
}
