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
package org.codehaus.groovy.ast.expr;

import java.io.OutputStream;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Type;
import org.codehaus.groovy.classgen.AsmClassGenerator2;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import groovy.lang.GString;

/**
 * Represents two expressions and an operation
 * 
 * @author <a href="mailto:james@coredevelopers.net">James Strachan</a>
 * @version $Revision$
 */
public class BinaryExpression extends Expression {
    
    private Expression leftExpression;
    private Expression rightExpression;
    private Token operation;
    
    public BinaryExpression(Expression leftExpression,
                            Token operation,
                            Expression rightExpression) {
        this.leftExpression = leftExpression;
        this.operation = operation;
        this.rightExpression = rightExpression;

    }

    public Class getTypeClass() {
        typeClass = resolveThisType(operation);
        return typeClass;
    }

    public boolean isDynamic() {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    private Class resolveThisType(Token operation) {
        switch (operation.getType()) {
            case Types.EQUAL : // = assignment
                if (!leftExpression.isDynamic())
                    return leftExpression.getTypeClass();
                else
                    return rightExpression.getTypeClass();
            case Types.COMPARE_IDENTICAL :
            case Types.COMPARE_EQUAL :
            case Types.COMPARE_NOT_EQUAL :
            case Types.COMPARE_GREATER_THAN :
            case Types.COMPARE_GREATER_THAN_EQUAL :
            case Types.COMPARE_LESS_THAN :
            case Types.COMPARE_LESS_THAN_EQUAL :
            case Types.KEYWORD_INSTANCEOF :
            case Types.MATCH_REGEX :
                return boolean.class;
            case Types.LOGICAL_AND :
            case Types.LOGICAL_OR :
                return Boolean.class;
            case Types.COMPARE_TO :
                return Integer.class;
            case Types.PLUS :
            case Types.PLUS_EQUAL :{
                if (leftExpression.getTypeClass() == String.class && rightExpression.getTypeClass() == String.class) {
                    return String.class;
                }
                else if (leftExpression.getTypeClass() == GString.class &&
                        (rightExpression.getTypeClass() == GString.class || rightExpression.getTypeClass() == String.class)) {
                    return GString.class;
                }
                else if (isNumber(leftExpression.getType()) && isNumber(rightExpression.getType())) {
                    return chooseWiderNumberType(leftExpression.getType(), rightExpression.getType());
                }
                else if (leftExpression.getTypeClass() == Date.class && Number.class.isAssignableFrom(rightExpression.getTypeClass()) ) {
                    return Date.class;
                }
                else if (leftExpression.getTypeClass() != null && Collection.class.isAssignableFrom(leftExpression.getTypeClass() )) {
                    return List.class;
                }
                else {
                    return null;
                }
            }
            case Types.MINUS :
            case Types.MINUS_EQUAL :{
                if (leftExpression.getTypeClass() == String.class) {
                    return String.class;
                } else if (leftExpression instanceof GStringExpression && isNumber(rightExpression.getType())) {
                    return String.class;
                } else if (isNumber(leftExpression.getType()) && isNumber(rightExpression.getType())) {
                    return chooseWiderNumberType(leftExpression.getType(), rightExpression.getType());
                }
                else if (leftExpression.getTypeClass() != null && List.class.isAssignableFrom(leftExpression.getTypeClass() )) {
                    return List.class;
                }
                else if (leftExpression.getTypeClass() == Date.class && Number.class.isAssignableFrom(rightExpression.getTypeClass()) ) {
                    return Date.class;
                }
                else {
                    return null;
                }
            }
            case Types.MULTIPLY :
            case Types.MULTIPLY_EQUAL : {
                if (leftExpression.getTypeClass() == String.class && isNumber(rightExpression.getType())) {
                    return String.class;
                } else if (leftExpression instanceof GStringExpression && isNumber(rightExpression.getType())) {
                    return String.class;
                } else if (isNumber(leftExpression.getType()) && isNumber(rightExpression.getType())) {
                    return chooseWiderNumberType(leftExpression.getType(), rightExpression.getType());
                }
                else if (leftExpression.getTypeClass() != null && Collection.class.isAssignableFrom(leftExpression.getTypeClass() )) {
                    return List.class;
                }
                else {
                    return null;
                }
            }

            case Types.DIVIDE :
            case Types.DIVIDE_EQUAL :
            case Types.MOD :
            case Types.MOD_EQUAL :
                if (isNumber(leftExpression.getType()) && isNumber(rightExpression.getType())) {
                    return chooseWiderNumberType(leftExpression.getType(), rightExpression.getType());
                }
                return null;
            case Types.LEFT_SHIFT :
                if (isNumber(leftExpression.getType()) && isNumber(rightExpression.getType())) {
                    return leftExpression.getTypeClass();
                }
                else if (leftExpression.getTypeClass() != null && Collection.class.isAssignableFrom(leftExpression.getTypeClass() )) {
                    return Collection.class;
                }
                else if (leftExpression.getTypeClass() != null && OutputStream.class.isAssignableFrom(leftExpression.getTypeClass())) {
                    return Writer.class;
                }
                else if (leftExpression.getTypeClass() != null && StringBuffer.class.isAssignableFrom(leftExpression.getTypeClass())) {
                    return Writer.class;
                }
                return null;
            case Types.RIGHT_SHIFT :
            case Types.RIGHT_SHIFT_UNSIGNED :
                if (isNumber(leftExpression.getType()) && isNumber(rightExpression.getType())) {
                    return leftExpression.getTypeClass();
                }
                return null;
            case Types.FIND_REGEX :
                return Matcher.class;
            case Types.LEFT_SQUARE_BRACKET :
                Class cls = leftExpression.getTypeClass();
                if (cls != null) {
                    if (cls.isArray()) {
                        Class elemType = cls.getComponentType();
                        //setTypeClass(elemType);
                        return elemType;
                    }
                    else if (leftExpression instanceof ListExpression) {
                        Class elemType = ((ListExpression)leftExpression).getComponentTypeClass();
                        //setTypeClass(elemType);
                        return elemType;
                    }
                    else if (leftExpression instanceof MapExpression) {
                        return Object.class;
                    }
                    else if (List.class.isAssignableFrom(cls)) {
                        return (Object.class);
                    }
                    else if (Map.class.isAssignableFrom(cls)) {
                        return (Object.class);
                    }
                }
                break;
        }
        return null;
    }

    private static boolean isNumber(String type) {
        if (type!= null) {
            if (    type.equals("int") ||
                    type.equals("short") ||
                    type.equals("byte") ||
                    type.equals("char") ||
                    type.equals("float") ||
                    type.equals("long") ||
                    type.equals("double") ||
                    type.equals("java.lang.Short") ||
                    type.equals("java.lang.Byte") ||
                    type.equals("java.lang.Character") ||
                    type.equals("java.lang.Integer") ||
                    type.equals("java.lang.Float") ||
                    type.equals("java.lang.Long") ||
                    type.equals("java.lang.Double") ||
                    type.equals("java.math.BigInteger") ||
                    type.equals("java.math.BigDecimal"))
            {
                return true;
            }
        }
        return false;
    }

    private static Class getObjectClassForNumber(String type) {
        if (type.equals("boolean") || type.equals("java.lang.Boolean")) {
            return Boolean.class;
        }
        else if (type.equals("short") || type.equals("java.lang.Short")) {
            return Short.class;
        }
        else if (type.equals("int") || type.equals("java.lang.Integer")) {
                    return Integer.class;
        }
        else if (type.equals("char") || type.equals("java.lang.Character")) {
                    return Integer.class;
        }
        else if (type.equals("long") || type.equals("java.lang.Long")) {
            return Long.class;
        }
        else if (type.equals("float") || type.equals("java.lang.Float")) {
            return Float.class;
        }
        else if (type.equals("double") || type.equals("java.lang.Double")) {
            return Double.class;
        }
        else if (type.equals("java.math.BigInteger")) {
            return BigInteger.class;
        }
        else if (type.equals("java.math.BigDecimal")) {
            return BigDecimal.class;
        }
        else {
            return null;
        }
    }

    private static boolean isFloatingPoint(Class cls) {
		return cls == Double.class || cls == Float.class;
	}

	private static boolean isInteger(Class cls) {
		return cls == Integer.class || cls == Byte.class || cls == Short.class || cls == Character.class;
	}

	private static boolean isLong(Class cls) {
		return cls == Long.class;
	}

	private static boolean isBigDecimal(Class cls) {
		return cls == BigDecimal.class;
	}

	private static boolean isBigInteger(Class cls) {
		return cls == BigInteger.class;
	}

    private static Class chooseWiderNumberType(String lefts, String rights) {
        Class left = getObjectClassForNumber(lefts);
        Class right = getObjectClassForNumber(rights);
        if (isFloatingPoint(left) || isFloatingPoint(right)) {
            return Double.class;
        }
        else if (isBigDecimal(left) || isBigDecimal(right)) {
            return BigDecimal.class;
        }
        else if (isBigInteger(left) || isBigInteger(right)) {
            return BigInteger.class;
        }
        else if (isLong(left) || isLong(right)){
            return Long.class;
        }
        return Integer.class;

        // see NumberMath for full Groovy math promotion
    }
    public String toString() {
        return super.toString() +"[" + leftExpression + operation + rightExpression + "]";
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitBinaryExpression(this);
    }

    public Expression transformExpression(ExpressionTransformer transformer) {
        return new BinaryExpression(transformer.transform(leftExpression), operation, transformer.transform(rightExpression));
    }

    public Expression getLeftExpression() {
        return leftExpression;
    }

    public void setLeftExpression(Expression leftExpression) {
        this.leftExpression = leftExpression;
    }

    public void setRightExpression(Expression rightExpression) {
        this.rightExpression = rightExpression;
    }

    public Token getOperation() {
        return operation;
    }

    public Expression getRightExpression() {
        return rightExpression;
    }

    public String getText() {
        if (operation.getType() == Types.LEFT_SQUARE_BRACKET) {
            return leftExpression.getText() + "[" + rightExpression.getText() + "]";
        }
        return "(" + leftExpression.getText() + " " + operation.getText() + " " + rightExpression.getText() + ")";
    }
    
    
   /**
    *  Creates an assignment expression in which the specified expression
    *  is written into the specified variable name.   
    */
    
    public static BinaryExpression newAssignmentExpression( String variable, Expression rhs ) {
    	VariableExpression lhs = new VariableExpression( variable );
    	Token         operator = Token.newPlaceholder( Types.ASSIGN );
    
    	return new BinaryExpression( lhs, operator, rhs );
    }


    /**
     *  Creates variable initialization expression in which the specified expression
     *  is written into the specified variable name.   
     */
     
     public static BinaryExpression newInitializationExpression( String variable, Type type, Expression rhs ) {
     	VariableExpression lhs = new VariableExpression( variable );
     
     	if( type != null ) {
     	    lhs.setType( type.getName() );
     	}
     
     	Token operator = Token.newPlaceholder( Types.ASSIGN );
     
        return new BinaryExpression( lhs, operator, rhs );
     }

    protected  void resolveType(AsmClassGenerator2 resolver) {
        leftExpression.resolve(resolver);
        rightExpression.resolve(resolver);
        Class cls = resolveThisType(operation);
        if (cls != null) {
            setTypeClass(cls);
        }
        else {
             setResolveFailed(true);
            setFailure("unknown. the right expression may have not been resolved");
        }
    }
}
