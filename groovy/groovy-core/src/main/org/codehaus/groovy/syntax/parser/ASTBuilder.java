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

import java.util.HashMap;
import java.util.Map;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.PropertyNode;
import org.codehaus.groovy.ast.Type;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.CastExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.GStringExpression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.NegationExpression;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.ast.expr.PostfixExpression;
import org.codehaus.groovy.ast.expr.PrefixExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.RangeExpression;
import org.codehaus.groovy.ast.expr.RegexExpression;
import org.codehaus.groovy.ast.expr.TernaryExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.AssertStatement;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.BreakStatement;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.CatchStatement;
import org.codehaus.groovy.ast.stmt.ContinueStatement;
import org.codehaus.groovy.ast.stmt.DoWhileStatement;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.ForStatement;
import org.codehaus.groovy.ast.stmt.IfStatement;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;
import org.codehaus.groovy.ast.stmt.SynchronizedStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.ast.stmt.TryCatchStatement;
import org.codehaus.groovy.ast.stmt.WhileStatement;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.syntax.CSTNode;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.syntax.Numbers;
import org.codehaus.groovy.GroovyBugError;
import org.objectweb.asm.Constants;



/**
 *  Builds an Abstract Syntax Tree from the Concrete Syntax Tree produced
 *  by the Parser.  The resulting AST is very preliminary, and must still
 *  be validated and massaged before it is ready to be used.
 *  <code>build()</code> is the primary entry point.
 *
 *  @author James Strachan
 *  @author Bob McWhirter
 *  @author Sam Pullara
 *  @author Chris Poirier
 */

public class ASTBuilder
{

    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private static final String[] DEFAULT_IMPORTS    = { "java.lang.", "groovy.lang.", "groovy.util." };



  //---------------------------------------------------------------------------
  // INITIALIZATION AND MEMBER ACCESS


    private SourceUnit  controller;    // The SourceUnit controlling us
    private ClassLoader classLoader;   // Our ClassLoader, which provides information on external types
    private Map         imports;       // Our imports, simple name => fully qualified name
    private String      packageName;   // The package name in which the module sits


   /**
    *  Initializes the <code>ASTBuilder</code>.
    */

    public ASTBuilder( SourceUnit sourceUnit, ClassLoader classLoader )
    {
        this.controller  = sourceUnit;
        this.classLoader = classLoader;
        this.imports     = new HashMap();
        this.packageName = null;
    }



   /**
    *  Returns our class loader (as supplied on construction).
    */

    public ClassLoader getClassLoader()
    {
        return this.classLoader;
    }




  //---------------------------------------------------------------------------
  // ENTRY POINT


   /**
    *  Builds an AST ModuleNode from a Parser.module() Reduction.
    */

    public ModuleNode build( CSTNode input ) throws ParserException
    {
        ModuleNode output = new ModuleNode( controller );
        resolutions.clear();

        //
        // input structure:
        //    1: package
        //    2: imports
        //   3+: statements

        packageName = packageDeclaration( input.get(1) );
        output.setPackageName( packageName );

        importStatements( output, input.get(2) );

        for( int i = 3; i < input.size(); ++i )
        {
            topLevelStatement( output, input.get(i) );
        }

        if( output.isEmpty() )
        {
            output.addStatement( new BlockStatement() );
        }

        return output;
    }




  //---------------------------------------------------------------------------
  // DECLARATIONS


   /**
    *  Processes the Reduction produced by Parser.packageDeclaration().
    */

    protected String packageDeclaration( CSTNode reduction )
    {
        if( reduction.hasChildren() )
        {
            return makeName( reduction.get(1) );
        }

        return null;

    }



   /**
    *  Processes the imports Reduction produced by Parser.module().
    */

    protected void importStatements( ModuleNode module, CSTNode container )
    {
        for( int i = 1; i < container.size(); ++i)
        {
            importStatement( module, container.get(i) );
        }
    }



   /**
    *  Processes the Reduction produced by Parser.importStatement().
    */

    protected void importStatement( ModuleNode module, CSTNode reduction )
    {
        //
        // First, get the package name (if supplied).

        String importPackage = makeName( reduction.get(1), null );



        //
        // If the first clause is Types.STAR, it's a package import.

        if( reduction.get(2).isA(Types.STAR) )
        {
            String[] classes = module.addImportPackage( dot(importPackage) );
            for( int i = 0; i < classes.length; i++ )
            {
                imports.put( classes[i], dot(importPackage, classes[i]) );
            }
        }


        //
        // Otherwise, it's a series of specific imports.

        else
        {
            for( int i = 2; i < reduction.size(); i++ )
            {
                CSTNode clause = reduction.get(i);
                String  name   = identifier( clause );
                String  as     = (clause.hasChildren() ? identifier(clause.get(1)) : name);

                //
                // There appears to be a bug in the previous code for
                // single imports, in that the old code passed unqualified
                // class names to module.addImport().  This hasn't been a
                // problem apparently because those names are resolved here.
                // Passing module.addImport() a fully qualified name does
                // currently causes problems with classgen, possibly because
                // of name collisions.  So, for now, we use the old method...

                module.addImport( as, name );  // unqualified

                name = dot( importPackage, name );

                // module.addImport( as, name );  // qualified
                imports.put( as, name );
            }
        }
    }



   /**
    *  Processes the Reduction produced by Parser.topLevelStatement().
    */

    protected void topLevelStatement( ModuleNode module, CSTNode reduction ) throws ParserException
    {
        int type = reduction.getMeaning();
        switch( type )
        {
            case Types.SYNTH_CLASS:
                module.addClass( classDeclaration(null, reduction) );
                break;

            case Types.SYNTH_INTERFACE:
                module.addClass( interfaceDeclaration(null, reduction) );
                break;

            case Types.SYNTH_METHOD:
                module.addMethod( methodDeclaration(null, reduction) );
                break;

            default:
                module.addStatement( statement(reduction) );
                break;
        }

    }



   /**
    *  Processes the Reduction produced by Parser.classDeclaration().
    */

    protected ClassNode classDeclaration( ClassNode context, CSTNode reduction ) throws ParserException
    {
        //
        // Calculate the easy stuff

        String   name = identifier( reduction );
        int modifiers = modifiers( reduction.get(1) );
        String parent = resolveName( reduction.get(2).get(1) );


        //
        // Then process the interface list.

        CSTNode  interfaceReduction = reduction.get(3);
        String[] interfaces = new String[interfaceReduction.children()];
        for( int i = 1; i < interfaceReduction.size(); i++ )
        {
            interfaces[i-1] = resolveName( interfaceReduction.get(i) );
        }


        //
        // Create the class.

        ClassNode classNode = (
            context == null
                ? new ClassNode(               dot(packageName, name), modifiers, parent, interfaces, MixinNode.EMPTY_ARRAY )
                : new InnerClassNode( context, dot(packageName, name), modifiers, parent, interfaces, MixinNode.EMPTY_ARRAY )
        );

        classNode.setCSTNode( reduction.get(0) );
        typeBody( classNode, reduction.get(4), 0, 0 );
        return classNode;
    }



   /**
    *  Processes a type body for classDeclaration() and others.
    */

    protected void typeBody( ClassNode classNode, CSTNode body, int propertyModifiers, int methodModifiers ) throws ParserException
    {
        for( int i = 1; i < body.size(); i++ )
        {
            CSTNode statement = body.get(i);
            switch( statement.getMeaning() )
            {
                case Types.SYNTH_PROPERTY:
                    addPropertyDeclaration( classNode, statement, propertyModifiers );
                    break;

                case Types.SYNTH_METHOD:
                    methodDeclaration( classNode, statement, methodModifiers );
                    break;

                case Types.SYNTH_CLASS:
                    classDeclaration( classNode, statement );
                    break;

                case Types.SYNTH_INTERFACE:
                    interfaceDeclaration( classNode, statement );
                    break;

                default:
                    throw new GroovyBugError( "unrecognized type body statement [" + statement.toString() + "]" );
            }
        }
    }



   /**
    *  Processes the Reduction produced by Parser.propertyDeclaration().
    *  Adds the property to the supplied class.
    */

    protected void addPropertyDeclaration( ClassNode classNode, CSTNode reduction, int extraModifiers ) throws ParserException
    {
        String   name = identifier( reduction );
        int modifiers = modifiers( reduction.get(1) ) | extraModifiers;
        String   type = resolveName( reduction.get(2) );

        Expression value = reduction.size() > 3 ? expression(reduction.get(3)) : null;

        PropertyNode propertyNode = classNode.addProperty( name, modifiers, type, value, null, null );
        propertyNode.setCSTNode( reduction.get(0) );

    }



   /**
    *  A synonym for <code>addPropertyDeclaration( classNode, reduction, 0 )</code>.
    */

    protected void addPropertyDeclaration( ClassNode classNode, CSTNode reduction ) throws ParserException
    {
        addPropertyDeclaration( classNode, reduction, 0 );
    }



   /**
    *  Processes the Reduction produced by Parser.methodDeclaration().
    *  Adds the method to the supplied class.
    */

    protected MethodNode methodDeclaration( ClassNode classNode, CSTNode reduction, int extraModifiers ) throws ParserException
    {
        String className = null;
        if( classNode != null  )
        {
            className = classNode.getNameWithoutPackage();
        }


        //
        // Get the basic method data

        String   name = identifier( reduction );
        int modifiers = modifiers( reduction.get(1) ) | extraModifiers;
        String   type = resolveName( reduction.get(2) );

        Parameter[] parameters = parameterDeclarations( reduction.get(3) );
        BlockStatement    body = statementBody( reduction.get(5) );


        //
        // Process the throws clause

        CSTNode  clause     = reduction.get(4);
        String[] throwTypes = new String[clause.children()];
        for( int i = 1; i < clause.size(); i++ )
        {
            throwTypes[i-1] = resolveName( clause.get(i) );
        }

        if( clause.hasChildren() ) { throw new GroovyBugError( "NOT YET IMPLEMENTED: throws clause" ); }


        //
        // An unnamed method is a static initializer

        if( name.length() == 0 )
        {
            throw new GroovyBugError( "NOT YET IMPLEMENTED: static initializers" );

            /*

            ConstructorNode node = new ConstructorNode( modifiers | Constants.ACC_STATIC, parameters, body );
            node.setCSTNode( reduction.get(0) );

            classNode.addConstructor( node );
            return null;

            */
        }


        //
        // A method with the class name is a constructor

        else if( className != null && name.equals(className) )
        {
            ConstructorNode node = new ConstructorNode( modifiers, parameters, body );
            node.setCSTNode( reduction.get(0) );

            classNode.addConstructor( node );
            return null;
        }


        //
        // Anything else is a plain old method

        else
        {
            MethodNode method = new MethodNode( name, modifiers, type, parameters, body );
            method.setCSTNode( reduction.get(0) );

            if( classNode != null )
            {
                classNode.addMethod( method );
            }

            return method;
        }

    }



   /**
    *  A synonym for <code>methodDeclaration( classNode, reduction, 0 )</code>.
    */

    protected MethodNode methodDeclaration( ClassNode classNode, CSTNode reduction ) throws ParserException
    {
        return methodDeclaration( classNode, reduction, 0 );
    }



   /**
    *  Processes the Reduction produced by Parser.parameterDeclarationList().
    */

    protected Parameter[] parameterDeclarations( CSTNode reduction ) throws ParserException
    {
        Parameter[] parameters = new Parameter[ reduction.children() ];

        for( int i = 1; i < reduction.size(); i++ )
        {
            CSTNode node = reduction.get(i);

            String identifier = identifier( node );
            String type       = resolveName( node.get(1) );

            if( node.size() > 2 )
            {
                parameters[i-1] = new Parameter( type, identifier, expression(node.get(2)) );
            }
            else
            {
                parameters[i-1] = new Parameter( type, identifier );
            }
        }

        return parameters;

    }



   /**
    * Processes the Reduction produced by Parser.interfaceDeclaration().
    */

    protected ClassNode interfaceDeclaration( ClassNode context, CSTNode reduction ) throws ParserException
    {
        throw new GroovyBugError( "NOT YET IMPLEMENTED: interfaces" );

        /*

        //
        // Calculate the easy stuff

        String   name = identifier( reduction );
        int modifiers = modifiers( reduction.get(1) ) | Constants.ACC_ABSTRACT | Constants.ACC_STATIC;
        String parent = null;


        //
        // Then process the interface list.

        CSTNode  interfaceReduction = reduction.get(3);
        String[] interfaces = new String[interfaceReduction.children()];
        for( int i = 1; i < interfaceReduction.size(); i++ )
        {
            interfaces[i-1] = resolveName( interfaceReduction.get(i) );
        }


        //
        // Create the interface.

        ClassNode classNode = (
            context == null
                ? new ClassNode(               dot(packageName, name), modifiers, parent, interfaces, MixinNode.EMPTY_ARRAY )
                : new InnerClassNode( context, dot(packageName, name), modifiers, parent, interfaces, MixinNode.EMPTY_ARRAY )
        );

		  classNode.setCSTNode( reduction.get(0) );

        int propertyModifiers = Constants.ACC_STATIC | Constants.ACC_FINAL | Constants.ACC_PUBLIC;
        int methodModifiers   = Constants.ACC_ABSTRACT;

        typeBody( classNode, reduction.get(4), propertyModifiers, methodModifiers );


        return classNode;

        */
    }




  //---------------------------------------------------------------------------
  // STATEMENTS


   /**
    *  Processes the Reduction that results from Parser.statementBody().
    */

    protected BlockStatement statementBody( CSTNode reduction ) throws ParserException
    {
        if( reduction.isEmpty() )
        {
            return new BlockStatement();
        }
        else if( reduction.getMeaning() == Types.LEFT_CURLY_BRACE )
        {
            return statementBlock( reduction );
        }
        else
        {
            Statement statement = statement( reduction );
            statement.setCSTNode( reduction );

            BlockStatement block = null;
            if( statement instanceof BlockStatement )
            {
                block = (BlockStatement)statement;
            }
            else
            {
                block = new BlockStatement();
                block.addStatement( statement );
            }

            return block;
        }
    }



   /**
    *  Processes any series of statements, starting at the specified offset
    *  and running to the end of the CSTNode.
    */

    protected BlockStatement statements( CSTNode reduction, int first ) throws ParserException
    {
        BlockStatement block = new BlockStatement();

        for( int i = first; i < reduction.size(); i++ )
        {
            CSTNode statementReduction = reduction.get(i);

            Statement statement = statement( statementReduction );
            statement.setCSTNode( statementReduction );

            block.addStatement( statement );
        }

        return block;
    }



   /**
    *  Processes any statement block.
    */

    protected BlockStatement statementBlock( CSTNode reduction ) throws ParserException
    {
        return statements( reduction, 1 );
    }



   /**
    *  Processes the Reduction produced by Parser.statement().
    */

    protected Statement statement( CSTNode reduction ) throws ParserException
    {
        Statement statement = null;

        //
        // Convert the statement

        switch( reduction.getMeaning() )
        {
            case Types.KEYWORD_ASSERT:
            {
                statement = assertStatement( reduction );
                break;
            }

            case Types.KEYWORD_BREAK:
            {
                statement = breakStatement( reduction );
                break;
            }

            case Types.KEYWORD_CONTINUE:
            {
                statement = continueStatement( reduction );
                break;
            }

            case Types.KEYWORD_IF:
            {
                statement = ifStatement( reduction );
                break;
            }

            case Types.KEYWORD_RETURN:
            {
                statement = returnStatement( reduction );
                break;
            }

            case Types.KEYWORD_SWITCH:
            {
                statement = switchStatement( reduction );
                break;
            }

            case Types.KEYWORD_SYNCHRONIZED:
            {
                statement = synchronizedStatement( reduction );
                break;
            }

            case Types.KEYWORD_THROW:
            {
                statement = throwStatement( reduction );
                break;
            }

            case Types.KEYWORD_TRY:
            {
                statement = tryStatement( reduction );
                break;
            }

            case Types.KEYWORD_FOR:
            {
                statement = forStatement( reduction );
                break;
            }

            case Types.KEYWORD_WHILE:
            {
                statement = whileStatement( reduction );
                break;
            }

            case Types.KEYWORD_DO:
            {
                statement = doWhileStatement( reduction );
                break;
            }

            case Types.SYNTH_BLOCK:
            case Types.LEFT_CURLY_BRACE:
            {
                statement = statementBlock( reduction );
                break;
            }

            case Types.SYNTH_LABEL:
            {
                statement = statement( reduction.get(1) );
                statement.setStatementLabel( identifier(reduction) );
                break;
            }

            case Types.SYNTH_CLOSURE:
            default:
            {
                statement = expressionStatement( reduction );
                break;
            }

        }


        statement.setCSTNode( reduction );
        return statement;
    }



   /**
    *  Processes the Reduction produced by Parser.assertStatement().
    */

    protected AssertStatement assertStatement( CSTNode reduction ) throws ParserException
    {
        BooleanExpression expression = new BooleanExpression( expression(reduction.get(1)) );

        if( reduction.children() > 1 )
        {
            return new AssertStatement( expression, expression(reduction.get(2)) );
        }

        return new AssertStatement( expression, ConstantExpression.NULL );
    }



   /**
    *  Processes the Reduction produced by Parser.breakStatement().
    */

    protected BreakStatement breakStatement( CSTNode reduction ) throws ParserException
    {
        if( reduction.hasChildren() )
        {
            return new BreakStatement( reduction.get(1).getRootText() );
        }

        return new BreakStatement();
    }



   /**
    *  Processes the Reduction produced by Parser.continueStatement().
    */

    protected ContinueStatement continueStatement( CSTNode reduction ) throws ParserException
    {

        if( reduction.hasChildren() )
        {
            return new ContinueStatement( reduction.get(1).getRootText() );
        }

        return new ContinueStatement();
    }



   /**
    *  Processes the Reduction produced by Parser.ifStatement().
    */

    protected IfStatement ifStatement( CSTNode reduction ) throws ParserException
    {
        Expression condition = expression( reduction.get(1) );
        BlockStatement  body = statementBody( reduction.get(2) );
        Statement  elseBlock = EmptyStatement.INSTANCE;

        if( reduction.size() > 3 )
        {
            CSTNode elseReduction = reduction.get(3);
            if( elseReduction.getMeaning() == Types.KEYWORD_IF )
            {
                elseBlock = ifStatement( elseReduction );
            }
            else
            {
                elseBlock = statementBody( elseReduction.get(1) );
            }

        }

        return new IfStatement( new BooleanExpression(condition), body, elseBlock );
    }



   /**
    *  Processes the Reduction produced by Parser.returnStatement().
    */

    protected ReturnStatement returnStatement( CSTNode reduction ) throws ParserException
    {
        if( reduction.hasChildren() )
        {
            return new ReturnStatement( expression(reduction.get(1)) );
        }

        return ReturnStatement.RETURN_VOID;
    }



   /**
    *  Processes the Reduction produced by Parser.switchStatement().
    */

    protected SwitchStatement switchStatement( CSTNode reduction ) throws ParserException
    {
        SwitchStatement statement = new SwitchStatement( expression(reduction.get(1)) );

        for( int i = 2; i < reduction.size(); i++ )
        {
            CSTNode child = reduction.get(i);

            switch( child.getMeaning() )
            {

                case Types.KEYWORD_CASE:
                    statement.addCase( caseStatement(child) );
                    break;

                case Types.KEYWORD_DEFAULT:
                    statement.setDefaultStatement( statementBlock(child) );
                    break;

                default:
                    throw new GroovyBugError( "invalid something in switch [" + child + "]" );
            }
        }

        return statement;
    }



   /**
    *  Processes the Reduction produced by Parser.switchStatement() for cases.
    */

    protected CaseStatement caseStatement( CSTNode reduction ) throws ParserException
    {
        return new CaseStatement( expression(reduction.get(1)), statements(reduction, 2) );
    }



   /**
    *  Processes the Reduction produced by Parser.synchronizedStatement().
    */

    protected SynchronizedStatement synchronizedStatement( CSTNode reduction ) throws ParserException
    {
        return new SynchronizedStatement( expression(reduction.get(1)), statementBody(reduction.get(2)) );
    }



   /**
    *  Processes the Reduction produced by Parser.throwStatement().
    */

    protected ThrowStatement throwStatement( CSTNode reduction ) throws ParserException
    {
        return new ThrowStatement( expression(reduction.get(1)) );
    }



   /**
    *  Processes the Reduction produced by Parser.tryStatement().
    */

    protected TryCatchStatement tryStatement( CSTNode reduction ) throws ParserException
    {
        BlockStatement         body = statementBody( reduction.get(1) );
        BlockStatement finallyBlock = statementBody( reduction.get(3) );

        TryCatchStatement statement = new TryCatchStatement( body, finallyBlock );

        CSTNode catches = reduction.get(2);
        for( int i = 1; i < catches.size(); i++ )
        {
            CSTNode   element = catches.get(i);
            String       type = resolveName( element.get(1) );
            String identifier = identifier( element.get(2) );

            statement.addCatch( new CatchStatement(type, identifier, statementBody(element.get(3))) );
        }

        return statement;
    }



   /**
    *  Processes the Reduction produced by Parser.forStatement().
    */

    protected ForStatement forStatement( CSTNode reduction ) throws ParserException
    {
        CSTNode header = reduction.get(1);
        Statement body = statementBody( reduction.get(2) );


        //
        // If the header has type Types.UNKNOWN, it's a standard for loop.

        if( header.getMeaning() == Types.UNKNOWN )
        {
            Expression[] init = expressions( header.get(1) );
            Expression   test = expression(  header.get(2) );
            Expression[] incr = expressions( header.get(3) );

            throw new GroovyBugError( "NOT YET IMPLEMENTED: standard for loop" );
        }


        //
        // Otherwise, it's a for each loop.

        else
        {

            Type         type = typeExpression( header.get(1) );
            String identifier = identifier(  header.get(2) );
            Expression source = expression(  header.get(3) );

            return new ForStatement( identifier, type, source, body );
        }
    }



   /**
    *  Processes the Reduction produced by Parser.doWhileStatement().
    */

    protected DoWhileStatement doWhileStatement( CSTNode reduction ) throws ParserException
    {
        Expression condition = expression( reduction.get(2) );
        BlockStatement  body = statementBody( reduction.get(1) );

        return new DoWhileStatement( new BooleanExpression(condition), body );
    }



   /**
    *  Processes the Reduction produced by Parser.whileStatement().
    */

    protected WhileStatement whileStatement( CSTNode reduction ) throws ParserException
    {
        Expression condition = expression( reduction.get(1) );
        BlockStatement  body = statementBody( reduction.get(2) );

        return new WhileStatement( new BooleanExpression(condition), body );

    }




  //---------------------------------------------------------------------------
  // EXPRESSIONS


   /**
    *  Processes any expression that forms a complete statement.
    */

    protected Statement expressionStatement( CSTNode node ) throws ParserException
    {
        return new ExpressionStatement( expression(node) );
    }



   /**
    *  Processes a series of expression to an Expression[].
    */

    protected Expression[] expressions( CSTNode reduction ) throws ParserException
    {
        Expression[] expressions = new Expression[ reduction.children() ];

        for( int i = 1; i < reduction.size(); i++ )
        {
            expressions[i-1] = expression( reduction.get(i) );
        }

        return expressions;
    }



   /**
    *  Processes the CSTNode produced by Parser.expression().
    */

    protected Expression expression( CSTNode reduction ) throws ParserException
    {
        Expression expression = null;

        int type = reduction.getMeaningAs( EXPRESSION_HANDLERS );
        switch( type )
        {
            case Types.SYNTHETIC:
            {
                expression = syntheticExpression( reduction );
                break;
            }

            case Types.RANGE_OPERATOR:
            {
                Expression from = expression( reduction.get(1) );
                Expression   to = expression( reduction.get(2) );

                expression = new RangeExpression( from, to, reduction.getMeaning() == Types.DOT_DOT );
                break;
            }


            case Types.LEFT_SQUARE_BRACKET:
            case Types.INFIX_OPERATOR:
            {
                expression = infixExpression( reduction );
                break;
            }


            case Types.REGEX_PATTERN:
            {
                expression = new RegexExpression( expression(reduction.get(1)) );
                break;
            }


            case Types.PREFIX_OPERATOR:
            {
                expression = prefixExpression( reduction );
                break;
            }


            case Types.POSTFIX_OPERATOR:
            {
                Expression body = expression( reduction.get(1) );
                expression = new PostfixExpression( body, reduction.getRoot() );
                break;
            }


            case Types.SIMPLE_EXPRESSION:
            {
                expression = simpleExpression( reduction );
                break;
            }


            case Types.KEYWORD_NEW:
            {
                expression = newExpression( reduction );
                break;
            }

            default:
                throw new GroovyBugError( "unhandled CST: [" + reduction.toString() + "]" );

        }

        if( expression == null )
        {
            throw new GroovyBugError( "expression produced null: [" + reduction.toString() + "]" );
        }

        expression.setCSTNode( reduction );
        return expression;
    }


    public static final int[] EXPRESSION_HANDLERS = {
          Types.SYNTHETIC
        , Types.RANGE_OPERATOR
        , Types.LEFT_SQUARE_BRACKET
        , Types.INFIX_OPERATOR
        , Types.REGEX_PATTERN
        , Types.PREFIX_OPERATOR
        , Types.POSTFIX_OPERATOR
        , Types.SIMPLE_EXPRESSION
        , Types.KEYWORD_NEW
    };




   /**
    *  Processes most infix operators.
    */

    public Expression infixExpression( CSTNode reduction ) throws ParserException
    {
        Expression expression;

        int type = reduction.getMeaning();
        switch( type )
        {
            case Types.DOT:
            case Types.NAVIGATE:
            {
                String name = reduction.get(2).getRootText();

                Expression context = null;
                if( name.equals("class") )
                {
                    CSTNode node = reduction.get(1);
                    if( node.isA(Types.LEFT_SQUARE_BRACKET) && node.children() == 1 )
                    {
                        throw new GroovyBugError( "NOT YET IMPLEMENTED: .class for array types" );
                        // context = classExpression( reduction.get(1) );
                    }
                }

                if( context == null )
                {
                    context = expression( reduction.get(1) );
                }

                expression = new PropertyExpression( context, name, type == Types.NAVIGATE );
                break;
            }


            case Types.KEYWORD_INSTANCEOF:
            {
                Expression   lhs = expression(  reduction.get(1) );
                Expression   rhs = classExpression( reduction.get(2) );
                expression = new BinaryExpression( lhs, reduction.getRoot(), rhs );
                break;
            }


            default:
            {
                Expression lhs = expression( reduction.get(1) );
                Expression rhs = expression( reduction.get(2) );
                expression = new BinaryExpression( lhs, reduction.getRoot(), rhs );
                break;
            }
        }

        return expression;
    }



   /**
    *  Processes most prefix operators.
    */

    public Expression prefixExpression( CSTNode reduction ) throws ParserException
    {
        Expression expression = null;
        CSTNode    body       = reduction.get(1);

        int type = reduction.getMeaning();
        switch( type )
        {
            case Types.PREFIX_MINUS:
                if( body.size() == 1 && body.isA(Types.NUMBER) )
                {
                    expression = numericExpression( body, true );
                }
                else
                {
                    expression = new NegationExpression( expression(body) );
                }
                break;

            case Types.PREFIX_PLUS:
                expression = expression(body);
                break;

            case Types.NOT:
                expression = new NotExpression( expression(body) );
                break;

            default:
                expression = new PrefixExpression( reduction.getRoot(), expression(body) );
                break;
        }

        return expression;
    }



   /**
    *  Processes most simple expressions.
    */

    public Expression simpleExpression( CSTNode reduction ) throws ParserException
    {
        Expression expression = null;

        int type = reduction.getMeaning();
        switch( type )
        {
            case Types.KEYWORD_NULL:
                expression = ConstantExpression.NULL;
                break;

            case Types.KEYWORD_TRUE:
                expression = ConstantExpression.TRUE;
                break;

            case Types.KEYWORD_FALSE:
                expression = ConstantExpression.FALSE;
                break;

            case Types.STRING:
                expression = new ConstantExpression( reduction.getRootText() );
                break;

            case Types.INTEGER_NUMBER:
            case Types.DECIMAL_NUMBER:
                expression = numericExpression( reduction, false );
                break;

            case Types.KEYWORD_SUPER:
            case Types.KEYWORD_THIS:
                expression = variableExpression( reduction );
                break;

            case Types.IDENTIFIER:
                expression = variableOrClassExpression( reduction );
                break;

        }

        return expression;
    }



   /**
    *  Processes numeric literals.
    */

    public Expression numericExpression( CSTNode reduction, boolean negate ) throws ParserException
    {
        Token  token  = reduction.getRoot();
        String text   = reduction.getRootText();
        String signed = negate ? "-" + text : text;

        boolean isInteger = (token.getMeaning() == Types.INTEGER_NUMBER);

        try
        {
            Number number = isInteger ? Numbers.parseInteger(signed) : Numbers.parseDecimal(signed);

            return new ConstantExpression( number );
        }
        catch( NumberFormatException e )
        {
            error( "numeric literal [" + signed + "] invalid or out of range for its type", token );
        }

        throw new GroovyBugError( "this should never happen" );
    }



   /**
    *  Processes most synthetic expressions.
    */

    public Expression syntheticExpression( CSTNode reduction ) throws ParserException
    {
        Expression expression = null;

        int type = reduction.getMeaning();
        switch( type )
        {
            case Types.SYNTH_TERNARY:
            {
                BooleanExpression condition   = new BooleanExpression( expression(reduction.get(1)) );
                Expression        trueBranch  = expression( reduction.get(2) );
                Expression        falseBranch = expression( reduction.get(3) );

                expression = new TernaryExpression( condition, trueBranch, falseBranch );
                break;
            }


            case Types.SYNTH_CAST:
            {
                String className = resolveName( reduction.get(1) );
                Expression  body = expression(  reduction.get(2) );

                expression = new CastExpression( className, body );
                break;
            }


            case Types.SYNTH_VARIABLE_DECLARATION:
            {
                expression = variableDeclarationExpression( reduction );
                break;
            }


            case Types.SYNTH_METHOD_CALL:
            {
                expression = methodCallExpression( reduction );
                break;
            }


            case Types.SYNTH_CLOSURE:
            {
                expression = closureExpression( reduction );
                break;
            }


            case Types.SYNTH_GSTRING:
            {
                expression = gstringExpression( reduction );
                break;
            }


            case Types.SYNTH_LIST:
            {
                expression = listExpression( reduction );
                break;
            }


            case Types.SYNTH_MAP:
            {
                expression = mapExpression( reduction );
                break;
            }
        }

        return expression;
    }




   /**
    *  Converts a (typically IDENTIFIER) CSTNode to a ClassExpression, if valid,
    *  or a VariableExpression otherwise.
    */

    protected Expression variableOrClassExpression( CSTNode reduction ) throws ParserException
    {
        String className = resolveName( reduction, false );

        if( className == null )
        {
            return variableExpression( reduction );
        }
        else
        {
            return new ClassExpression( className );
        }
    }



   /**
    *  Converts a CSTNode into a ClassExpression.
    */

    protected ClassExpression classExpression( CSTNode reduction ) throws ParserException
    {
        String name = resolveName( reduction, true );
        return new ClassExpression( name );
    }



   /**
    *  Converts a (typically IDENTIFIER) CSTNode to a VariableExpression, if
    *  valid.
    */

    protected VariableExpression variableExpression( CSTNode reduction )
    {
        return new VariableExpression( reduction.getRootText() );
    }



   /**
    *  Converts an (possibly optional) type expression to a Type.
    */

    protected Type typeExpression( CSTNode reduction )
    {
        String name = makeName( reduction, null );
        if( name == null )
        {
            return Type.DYNAMIC_TYPE;
        }
        else
        {
            return new Type( resolveName(name, true) );
        }
    }



   /**
    *  Processes the Reduction produced by parsing a typed variable
    *  declaration.
    */

    protected Expression variableDeclarationExpression( CSTNode reduction ) throws ParserException
    {
        String type = resolveName( reduction.get(1) );


        //
        // TEMPORARY UNTIL GENERAL SUPPORT IN PLACE

        if( reduction.size() == 3 )
        {
            CSTNode node = reduction.get(2);

            VariableExpression name = variableExpression( node );
            name.setType( type );

            Token symbol = Token.newSymbol( Types.EQUAL, -1, -1 );

            return new BinaryExpression( name, symbol, expression(node.get(1)) );
        }


        throw new GroovyBugError( "NOT YET IMPLEMENTED: generalized variable declarations" );

        /*

        VariableDeclarationExpression expression = new VariableDeclarationExpression( type );

        for( i = 2; i < reduction.size(); i++ )
        {
            CSTNode node = reduction.get(i);
            declaration.add( node.get(0), expression(node.get(1)) );
        }

        return expression;

        */
    }



   /**
    *  Processes a SYNTH_METHOD_CALL Reduction produced by Parser.expression().
    */

    protected MethodCallExpression methodCallExpression( CSTNode reduction ) throws ParserException
    {
        MethodCallExpression call = null;

        //
        // Figure out the name and context of the method call.

        CSTNode descriptor = reduction.get(1);
        Expression context = null;
        boolean   implicit = false;
        String      method = "call";
        boolean       safe = false;

        int type = descriptor.getMeaning();
        switch( type )
        {
            case Types.KEYWORD_SUPER:
            {
                context  = variableExpression( descriptor );
                method   = identifier( descriptor );
                break;
            }

            case Types.KEYWORD_THIS:
            {
                context  = VariableExpression.THIS_EXPRESSION;
                method   = identifier( descriptor );
                break;
            }

            case Types.IDENTIFIER:
            {
                context  = VariableExpression.THIS_EXPRESSION;
                method   = identifier( descriptor );
                implicit = true;
                break;
            }

            case Types.DOT:
            case Types.NAVIGATE:
            {
                context = expression( descriptor.get(1) );
                method  = identifier( descriptor.get(2) );
                safe    = type == Types.NAVIGATE;
                break;
            }

            default:
            {
                context = expression( descriptor );
                break;
            }
        }


        //
        // And build the expression

        Expression parameters = parameterList( reduction.get(2) );

        // System.out.println( "method call expression: " + context + ", " + method + ", " + parameters + ", " + implicit );

        call = new MethodCallExpression( context, method, parameters );
        call.setImplicitThis( implicit );
        call.setSafe( safe );

        return call;
    }



   /**
    *  Processes the Reduction produced by Parser.closureExpression().
    */

    protected ClosureExpression closureExpression( CSTNode reduction ) throws ParserException
    {
        ClosureExpression expression = null;

        Parameter[] parameters = parameterDeclarations( reduction.get(1) );
        expression = new ClosureExpression( parameters, statementBlock(reduction.get(2)) );

        return expression;
    }



   /**
    *  Processes the Reduction produced by Parser.parameterList().
    */

    protected Expression parameterList( CSTNode reduction ) throws ParserException
    {
        TupleExpression list = new TupleExpression();

        for( int i = 1; i < reduction.size(); i++ )
        {
            CSTNode node = reduction.get(i);
            list.addExpression( expression(node) );
        }

        return list;
    }



   /**
    *  Processes the Reduction produced by Parser.newExpression().
    */

    protected Expression newExpression( CSTNode reduction ) throws ParserException
    {
        Expression expression = null;
        CSTNode      typeNode = reduction.get(1);
        String           type = resolveName( typeNode );


        //
        // Array types have dimension and initialization data to handle.

        if( typeNode.getMeaning() == Types.LEFT_SQUARE_BRACKET )
        {
            CSTNode dimensions = reduction.get(2);

            //
            // BUG: at present, ArrayExpression expects a scalar type and
            // does not support multi-dimensional arrays.  In future, the
            // the latter will need to change, and that may require the
            // former to change, as well.  For now, we calculate the scalar
            // type and error for multiple dimensions.

            if( typeNode.get(1).getMeaning() == Types.LEFT_SQUARE_BRACKET )
            {
                throw new GroovyBugError( "NOT YET IMPLEMENTED: multidimensional arrays" );
            }
            else
            {
                type = resolveName( typeNode.get(1) );
            }


            //
            // If there are no dimensions, process a tuple initializer

            if( dimensions.isEmpty() )
            {
                CSTNode data = reduction.get(3);

                if( data.get(1, true).getMeaning() == Types.SYNTH_TUPLE )
                {
                    throw new GroovyBugError( "NOT YET IMPLEMENTED: multidimensional arrays" );
                }

                expression = new ArrayExpression( type, tupleExpression(data).getExpressions() );
            }


            //
            // Otherwise, process the dimensions

            else
            {
                if( dimensions.size() > 2 )
                {
                    throw new GroovyBugError( "NOT YET IMPLEMENTED: multidimensional arrays" );

                    /*

                    expression = new ArrayExpression( type, tupleExpression(dimensions) );

                    */
                }
                else
                {
                    expression = new ArrayExpression( type, expression(dimensions.get(1)) );
                }
            }
        }


        //
        // Scalar types have a constructor parameter list and possibly a type body

        else
        {
            Expression parameters = parameterList( reduction.get(2) );

            if( reduction.size() > 3 )
            {
                throw new GroovyBugError( "NOT YET IMPLEMENTED: anonymous classes" );
            }

            expression = new ConstructorCallExpression( type, parameters );
        }

        return expression;
    }



   /**
    *  Processes the Reduction produced by Parser.newArrayInitializer().
    */

    protected TupleExpression tupleExpression( CSTNode reduction ) throws ParserException
    {
        TupleExpression tuple = new TupleExpression();

        for( int i = 1; i < reduction.size(); i++ )
        {
            CSTNode element = reduction.get(i);

            if( element.getMeaning() == Types.SYNTH_TUPLE )
            {
                tuple.addExpression( tupleExpression(element) );
            }
            else
            {
                tuple.addExpression( expression(element) );
            }
        }

        return tuple;
    }



   /**
    *  Processes the Reduction produced by Parser.gstring().
    */

    protected Expression gstringExpression( CSTNode reduction ) throws ParserException
    {
        if( !reduction.hasChildren() )
        {
            return new ConstantExpression( "" );
        }

        if( reduction.children() == 1 && reduction.get(1).getMeaning() == Types.STRING )
        {
            return expression( reduction.get(1) );
        }


        GStringExpression expression = new GStringExpression( reduction.getRootText() );
        boolean lastWasExpression = false;

        for( int i = 1; i < reduction.size(); i++ )
        {
            CSTNode element = reduction.get(i);
            if( element.getMeaning() == Types.STRING )
            {
                ConstantExpression string = new ConstantExpression( element.getRootText() );
                string.setCSTNode( element );

                expression.addString( string );

                lastWasExpression = false;
            }
            else
            {
                if( lastWasExpression )
                {
                    expression.addString( new ConstantExpression("") );
                }

                lastWasExpression = true;
                expression.addValue( element.isEmpty() ? ConstantExpression.NULL : expression(element) );
            }
        }

        return expression;
    }



   /**
    *  Processes one of the Reductions produced by Parser.listOrMapExpression().
    */

    protected ListExpression listExpression( CSTNode reduction ) throws ParserException
    {
        ListExpression list = new ListExpression();

        for( int i = 1; i < reduction.size(); i++ )
        {
            list.addExpression( expression(reduction.get(i)) );
        }

        return list;
    }



   /**
    *  Processes the other Reduction produced by Parser.listOrMapExpression().
    */

    protected MapExpression mapExpression( CSTNode reduction ) throws ParserException
    {
        MapExpression map = new MapExpression();

        for( int i = 1; i < reduction.size(); i++ )
        {
            CSTNode  element = reduction.get(i);
            Expression   key = expression( element.get(1) );
            Expression value = expression( element.get(2) );

            map.addMapEntryExpression( key, value );
        }

        return map;
    }





  //---------------------------------------------------------------------------
  // NAMING

    private static HashMap resolutions = new HashMap();  // cleared on build(), to be safe
    private static String NOT_RESOLVED = new String();


   /**
    *  Converts a CSTNode representation of a type name back into
    *  a string.
    */

    protected String makeName( CSTNode root, String defaultName )
    {
        if( root == null )
        {
            return defaultName;
        }

        String name = "";
        switch( root.getMeaning() )
        {
            case Types.LEFT_SQUARE_BRACKET:
            {
                name = makeName( root.get(1) ) + "[]";
                break;
            }

            case Types.DOT:
            {
                CSTNode node = root;
                while( node.isA(Types.DOT) )
                {
                    name = "." + node.get(2).getRootText() + name;
                    node = node.get(1);
                }

                name = node.getRootText() + name;
                break;
            }

            case Types.UNKNOWN:
            {
                name = defaultName;
                break;
            }

            default:
            {
                name = root.getRootText();
                break;
            }

        }

        return name;
    }



   /**
    *  A synonym for <code>makeName( root, "java.lang.Object" )</code>.
    */

    protected String makeName( CSTNode root )
    {
        return makeName( root, "java.lang.Object" );
    }



   /**
    *  Returns the text of an identifier.
    */

    protected String identifier( CSTNode identifier )
    {
        return identifier.getRootText();
    }



   /**
    *  Returns a fully qualified name for any given potential type
    *  name.  Returns null if no qualified name could be determined.
    */

    protected String resolveName( String name, boolean safe )
    {
        //
        // Use our cache of resolutions, if possible

        String resolution = (String)resolutions.get( name );
        if( resolution == NOT_RESOLVED )
        {
            return (safe ? name : null);
        }
        else if( resolution != null )
        {
            return (String)resolution;
        }


        do
        {
            //
            // If the type name contains a ".", it's probably fully
            // qualified, and we don't take it to verification here.

            if( name.indexOf(".") >= 0 )
            {
                resolution = name;
                break;                                            // <<< FLOW CONTROL <<<<<<<<<
            }


            //
            // Otherwise, we'll need the scalar type for checking, and
            // the postfix for reassembly.

            String scalar = name, postfix = "";
            while( scalar.endsWith("[]") )
            {
                scalar = scalar.substring( 0, scalar.length() - 2 );
                postfix += "[]";
            }


            //
            // Primitive types are all valid...

            if( Types.ofType(Types.lookupKeyword(scalar), Types.PRIMITIVE_TYPE) )
            {
                resolution = name;
                break;                                            // <<< FLOW CONTROL <<<<<<<<<
            }


            //
            // Next, check our imports and return the qualified name,
            // if available.

            if( this.imports.containsKey(scalar) )
            {
                resolution = ((String)this.imports.get(scalar)) + postfix;
                break;                                            // <<< FLOW CONTROL <<<<<<<<<
            }


            //
            // Next, see if our class loader can resolve it in the current package.

            if( packageName != null && packageName.length() > 0 )
            {
                try
                {
                    getClassLoader().loadClass( dot(packageName, scalar) );
                    resolution = dot(packageName, name);

                    break;                                        // <<< FLOW CONTROL <<<<<<<<<
                }
                catch( Throwable e )
                {
                    /* ignore */
                }
            }


            //
            // Last chance, check the default imports.

            for( int i = 0; i < DEFAULT_IMPORTS.length; i++ )
            {
                try
                {
                    String qualified = DEFAULT_IMPORTS[i] + scalar;
                    getClassLoader().loadClass( qualified );

                    resolution = qualified + postfix;
                    break;                                        // <<< FLOW CONTROL <<<<<<<<<
                }
                catch( Throwable e )
                {
                    /* ignore */
                }
            }

        } while( false );


        //
        // Cache the solution and return it

        if( resolution == null )
        {
            resolutions.put( name, NOT_RESOLVED );
            return (safe ? name : null);
        }
        else
        {
            resolutions.put( name, resolution );
            return resolution;
        }
    }



   /**
    *  Builds a name from a CSTNode, then resolves it.  Returns the resolved name
    *  if available, or null, unless safe is set, in which case the built name
    *  is returned instead of null.
    *
    *  @todo we should actually remove all resolving code from the ASTBuilder and
    *        move it into the verifier / analyser
    */

    protected String resolveName( CSTNode root, boolean safe )
    {
        String name = makeName( root );
        return resolveName( name, safe );
    }



   /**
    *  A synonym for <code>resolveName( root, true )</code>.
    */

    protected String resolveName( CSTNode root )
    {
        return resolveName( root, true );
    }



   /**
    *  Returns true if the specified name is a known type name.
    */

    protected boolean isDatatype( String name )
    {
        return resolveName( name, false ) != null;
    }



   /**
    *  Returns two names joined by a dot.  If the base name is
    *  empty, returns the name unchanged.
    */

    protected String dot( String base, String name )
    {
        if( base != null && base.length() > 0 )
        {
            return base + "." + name;
        }

        return name;
    }



   /**
    *  A synonym for <code>dot( base, "" )</code>.
    */

    protected String dot( String base )
    {
        return dot( base, "" );
    }




  //---------------------------------------------------------------------------
  // ASM SUPPORT


   /**
    *  Returns the ASM Constant bits for the specified modifiers.
    */

    protected int modifiers( CSTNode list )
    {
        int modifiers = 0;

        for( int i = 1; i < list.size(); ++i )
        {
            SWITCH: switch( list.get(i).getMeaning() )
            {
                case Types.KEYWORD_PUBLIC:
                {
                    modifiers |= Constants.ACC_PUBLIC;
                    break SWITCH;
                }

                case Types.KEYWORD_PROTECTED:
                {
                    modifiers |= Constants.ACC_PROTECTED;
                    break SWITCH;
                }

                case Types.KEYWORD_PRIVATE:
                {
                    modifiers |= Constants.ACC_PRIVATE;
                    break SWITCH;
                }


                case Types.KEYWORD_ABSTRACT:
                {
                    modifiers |= Constants.ACC_ABSTRACT;
                    break SWITCH;
                }

                case Types.KEYWORD_FINAL:
                {
                    modifiers |= Constants.ACC_FINAL;
                    break SWITCH;
                }

                case Types.KEYWORD_NATIVE:
                {
                    modifiers |= Constants.ACC_NATIVE;
                    break SWITCH;
                }

                case Types.KEYWORD_TRANSIENT:
                {
                    modifiers |= Constants.ACC_TRANSIENT;
                    break SWITCH;
                }

                case Types.KEYWORD_VOLATILE:
                {
                    modifiers |= Constants.ACC_VOLATILE;
                    break SWITCH;
                }


                case Types.KEYWORD_SYNCHRONIZED:
                {
                    modifiers |= Constants.ACC_SYNCHRONIZED;
                    break SWITCH;
                }
                case Types.KEYWORD_STATIC:
                {
                    modifiers |= Constants.ACC_STATIC;
                    break SWITCH;
                }

            }
        }


        //
        // If not protected or private we default to public.

        if( (modifiers & (Constants.ACC_PROTECTED | Constants.ACC_PRIVATE)) == 0 )
        {
            modifiers |= Constants.ACC_PUBLIC;
        }

        return modifiers;
    }




  //---------------------------------------------------------------------------
  // ERROR HANDLING


   /**
    *  Throws a <code>ParserException</code>.
    */

    protected void error( String description, CSTNode node ) throws ParserException
    {
        throw new ParserException( description, node.getRoot() );
    }


}
