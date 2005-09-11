/* Copyright 2004-2005 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 
package org.codehaus.groovy.grails.orm.hibernate.metaclass;

import groovy.lang.MissingMethodException;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Expression;

/**
 * @author Graeme Rocher
 * @since 31-Aug-2005
 *
 */
public abstract class AbstractClausedStaticPersistentMethod extends
		AbstractStaticPersistentMethod {

	/**
	 * 
	 * @author Graeme Rocher
	 *
	 */
	protected abstract static class GrailsMethodExpression {
		private static final String LESS_THAN = "LessThan";
		private static final String LESS_THAN_OR_EQUAL = "LessThanOrEqual";
		private static final String GREATER_THAN = "GreaterThan";
		private static final String GREATER_THAN_OR_EQUAL = "GreaterThanOrEqual";
		private static final String LIKE = "Like";
		private static final String BETWEEN = "Between";
		private static final String IS_NOT_NULL = "IsNotNull";
		private static final String IS_NULL = "IsNull";
		private static final String NOT = "Not";
		private static final String EQUAL = "Equal";
		private static final String NOT_EQUAL = "NotEqual";
		
		
		protected String propertyName;
		protected Object[] arguments;
		protected int argumentsRequired;
		protected boolean negation;
		protected String type;
		
		GrailsMethodExpression(String propertyName, String type,int argumentsRequired,boolean negation) {
			this.propertyName = propertyName;
			this.type = type;
			this.argumentsRequired = argumentsRequired;
			this.negation = negation;
		}
		void setArguments(Object[] arguments) {
			if(arguments.length != argumentsRequired)
				throw new IllegalArgumentException("Expression '"+this.type+"' requires " + argumentsRequired + " arguments");
			this.arguments = arguments;
		}
		abstract Criterion createCriterion();
		protected Criterion getCriterion() {
			if(arguments == null)
				throw new IllegalStateException("Parameters array must be set before retrieving Criterion");
			
			if(negation) {
				return Expression.not( createCriterion() );
			}
			else {
				return createCriterion();
			}
		}
		
		protected static GrailsMethodExpression create(String queryParameter) {
			if(queryParameter.endsWith( LESS_THAN_OR_EQUAL )) {
				return new GrailsMethodExpression( calcPropertyName(queryParameter, LESS_THAN_OR_EQUAL),LESS_THAN_OR_EQUAL, 1,isNegation(queryParameter, LESS_THAN_OR_EQUAL) ) {
					Criterion createCriterion() {
						return Expression.le( this.propertyName, arguments[0] );
					}
					
				};
			}			
			else if(queryParameter.endsWith( LESS_THAN )) {
				return new GrailsMethodExpression( calcPropertyName(queryParameter, LESS_THAN),LESS_THAN, 1,isNegation(queryParameter, LESS_THAN) ) {
					Criterion createCriterion() {
						return Expression.lt( this.propertyName, arguments[0] );
					}
					
				};
			}
			else if(queryParameter.endsWith( GREATER_THAN_OR_EQUAL )) {
				return new GrailsMethodExpression( calcPropertyName(queryParameter, GREATER_THAN_OR_EQUAL),GREATER_THAN_OR_EQUAL, 1,isNegation(queryParameter, GREATER_THAN_OR_EQUAL) ) {
					Criterion createCriterion() {
						return Expression.ge( this.propertyName, arguments[0] );
					}
					
				};
			}			
			else if(queryParameter.endsWith( GREATER_THAN )) {
				return new GrailsMethodExpression( calcPropertyName(queryParameter, GREATER_THAN),GREATER_THAN,1, isNegation(queryParameter, GREATER_THAN) ) {
					Criterion createCriterion() {
						return Expression.gt( this.propertyName, arguments[0] );
					}
					
				};
			}
			else if(queryParameter.endsWith( LIKE )) {
				return new GrailsMethodExpression( calcPropertyName(queryParameter, LIKE),LIKE,1, isNegation(queryParameter, LIKE) ) {
					Criterion createCriterion() {
						return Expression.like( this.propertyName, arguments[0] );
					}
					
				};
			}			
			else if(queryParameter.endsWith( IS_NOT_NULL )) {
				return new GrailsMethodExpression( calcPropertyName(queryParameter, IS_NOT_NULL),IS_NOT_NULL,0, isNegation(queryParameter, IS_NOT_NULL) ) {
					Criterion createCriterion() {
							return Expression.isNotNull( this.propertyName );
					}
					
				};
			}
			else if(queryParameter.endsWith( IS_NULL )) {
				return new GrailsMethodExpression( calcPropertyName(queryParameter, IS_NULL),IS_NULL,0, isNegation(queryParameter, IS_NULL) ) {
					Criterion createCriterion() {
						return Expression.isNull( this.propertyName );
					}
					
				};
			}
			else if(queryParameter.endsWith( BETWEEN )) {
					
				return new GrailsMethodExpression( calcPropertyName(queryParameter, BETWEEN),BETWEEN, 2,isNegation(queryParameter, BETWEEN) ) {
					Criterion createCriterion() {
						return Expression.between( this.propertyName,this.arguments[0], this.arguments[1] );
					}
					
				};
			}
			else if(queryParameter.endsWith( NOT_EQUAL )) {
				return new GrailsMethodExpression( calcPropertyName(queryParameter, NOT_EQUAL),NOT_EQUAL, 1,isNegation(queryParameter, NOT_EQUAL) ) {
					Criterion createCriterion() {
						return Expression.ne( this.propertyName,this.arguments[0]);
					}
					
				};				
			}
			else {
				
				return new GrailsMethodExpression( calcPropertyName(queryParameter, null),EQUAL, 1,isNegation(queryParameter, EQUAL) ) {
					Criterion createCriterion() {
						return Expression.eq( this.propertyName,this.arguments[0]);
					}
					
				};			
			}
		}
		private static boolean isNegation(String queryParameter, String clause) {
			String propName;
			if(clause != null && !clause.equals( EQUAL )) {
				int i = queryParameter.indexOf(clause);
				propName = queryParameter.substring(0,i);
			}
			else {
				propName = queryParameter;
			}
			if(propName.endsWith(NOT)) {
				return true;
			}
			return false;
		}
		private static String calcPropertyName(String queryParameter, String clause) {
			String propName;
			if(clause != null && !clause.equals( EQUAL )) {
				int i = queryParameter.indexOf(clause);
				propName = queryParameter.substring(0,i);
			}
			else {
				propName = queryParameter;
			}
			if(propName.endsWith(NOT)) {
				int i = propName.lastIndexOf(NOT);
				propName = propName.substring(0, i);
			}
			return propName.substring(0,1).toLowerCase()
				+ propName.substring(1);			
		}
	}
	
	private String[] operators;
	protected String operatorInUse;
	
	public AbstractClausedStaticPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader, Pattern pattern, String[] operators) {
		super(sessionFactory, classLoader, pattern);
		this.operators = operators;
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.orm.hibernate.metaclass.AbstractStaticPersistentMethod#doInvokeInternal(java.lang.Class, java.lang.String, java.lang.Object[])
	 */
	protected Object doInvokeInternal(final Class clazz, String methodName,
			Object[] arguments) {
		List expressions = new ArrayList();
		Matcher match = super.getPattern().matcher( methodName );
		// find match
		match.find();
		
		String[] queryParameters = null;
		int totalRequiredArguments = 0;
		// get the sequence clauses
		String querySequence = match.group(2);
		// if it contains operator and split
		boolean containsOperator = false;
		for (int i = 0; i < operators.length; i++) {
			if(querySequence.matches( "(\\w+)("+this.operators[i]+")(\\w+)" )) {
				containsOperator = true;
				operatorInUse = this.operators[i];
				
				// TODO: Bit error prone this, as properties could start 
				// with "and" or "or" which would cause a problem. Need
				// to refactor this to be a bit more intelligent
				queryParameters = querySequence.split(this.operators[i]);
				
				// loop through query parameters and create expressions
				// calculating the numer of arguments required for the expression
				int argumentCursor = 0;
				for (int j = 0; j < queryParameters.length; j++) {
					GrailsMethodExpression currentExpression = GrailsMethodExpression.create(queryParameters[j]);
					totalRequiredArguments += currentExpression.argumentsRequired;
					// populate the arguments into the GrailsExpression from the argument list
					Object[] currentArguments = new Object[currentExpression.argumentsRequired];
					if((argumentCursor + currentExpression.argumentsRequired) > arguments.length)
						throw new MissingMethodException(methodName,clazz,arguments);
					
					for (int k = 0; k < currentExpression.argumentsRequired; k++,argumentCursor++) {
						currentArguments[k] = arguments[argumentCursor];
					}
					currentExpression.setArguments(currentArguments);					
					// add to list of expressions
					expressions.add(currentExpression);
				}
				break;
			}			
		}
		
		// otherwise there is only one expression
		if(!containsOperator) {
			GrailsMethodExpression solo = GrailsMethodExpression.create( querySequence );
			
			if(solo.argumentsRequired != arguments.length)
				throw new MissingMethodException(methodName,clazz,arguments);
			
			totalRequiredArguments += solo.argumentsRequired;			
			Object[] soloArgs = new Object[solo.argumentsRequired];
			
			for (int i = 0; i < solo.argumentsRequired; i++) {
				soloArgs[i] = arguments[i];
			}			
			solo.setArguments(soloArgs);
			expressions.add(solo);
		}

		// if the total of all the arguments necessary does not equal the number of arguments
		// throw exception
		if(totalRequiredArguments != arguments.length)
			throw new MissingMethodException(methodName,clazz,arguments);
		
		
		return doInvokeInternalWithExpressions(clazz, methodName, arguments, expressions);
	}
	
	protected abstract Object doInvokeInternalWithExpressions(Class clazz, String methodName, Object[] arguments, List expressions);

}
