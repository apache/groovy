/*
 * Copyright 2004-2005 the original author or authors.
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
package grails.orm;

import grails.util.ExtendProxy;
import groovy.lang.MissingMethodException;
import groovy.util.BuilderSupport;
import groovy.util.Proxy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.orm.hibernate3.SessionHolder;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * <p>Wraps the Hibernate Criteria API in a builder. The builder can be retrieved through the "createCriteria()" dynamic static 
 * method of Grails domain classes (Example in Groovy): 
 * 
 * <pre>
 * 		def c = Account.createCriteria()
 * 		def results = c {
 * 			like("holderFirstName", "Fred%")
 * 			and {
 * 				between("balance", 500, 1000)
 * 				eq("branch", "London")
 * 			}
 * 			maxResults(10)
 * 			order("holderLastName", "desc")
 * 		}
 * </pre>
 * 
 * <p>The builder can also be instantiated standalone with a SessionFactory and persistent Class instance:
 * 
 * <pre>
 * 	 new HibernateCritiriaBuilder(clazz, sessionFactory).list {
 * 		eq("firstName", "Fred")
 * 	 }
 * </pre>
 * 
 * @author Graeme Rocher
 * @since Oct 10, 2005
 */
public class HibernateCriteriaBuilder extends BuilderSupport {

	private static final String AND = "and"; // builder
	private static final String IS_NULL = "isNull"; // builder
	private static final String IS_NOT_NULL = "notNull"; // builder
	private static final String NOT = "not";// builder
	private static final String OR = "or"; // builder
	private static final String ID_EQUALS = "idEq"; // builder
	private static final String IS_EMPTY = "isEmpty"; //builder
	private static final String IS_NOT_EMPTY = "isNotEmpty"; //builder
	
	
	private static final String BETWEEN = "between";//method
	private static final String EQUALS = "eq";//method	
	private static final String EQUALS_PROPERTY = "eqProperty";//method
	private static final String GREATER_THAN = "gt";//method
	private static final String GREATER_THAN_PROPERTY = "gtProperty";//method
	private static final String GREATER_THAN_OR_EQUAL = "ge";//method
	private static final String GREATER_THAN_OR_EQUAL_PROPERTY = "geProperty";//method
	private static final String ILIKE = "ilike";//method
	private static final String IN = "in";//method
	private static final String LESS_THAN = "lt"; //method
	private static final String LESS_THAN_PROPERTY = "ltProperty";//method
	private static final String LESS_THAN_OR_EQUAL = "le";//method
	private static final String LESS_THAN_OR_EQUAL_PROPERTY = "leProperty";//method	
	private static final String LIKE = "like";//method
	private static final String NOT_EQUAL = "ne";//method
	private static final String NOT_EQUAL_PROPERTY = "neProperty";//method
	private static final String SIZE_EQUALS = "sizeEq"; //method
	private static final String ORDER_DESCENDING = "desc";
	private static final String ORDER_ASCENDING = "asc";


	private static final String ROOT_CALL = "doCall";
	private static final String LIST_CALL = "list";
	private static final String GET_CALL = "get";
	private static final String SETTER_PREFIX = "set";
	
	
	private SessionFactory sessionFactory;
	private Session session;
	private Class targetClass;
	private Criteria criteria;
	private boolean uniqueResult = false;
	private Proxy resultProxy = new ExtendProxy();
	private Proxy criteriaProxy;
	private Object parent;
	private List logicalExpressions = new ArrayList();
	private List logicalExpressionArgs = new ArrayList();
	private boolean participate;
	
	
	public HibernateCriteriaBuilder(Class targetClass, SessionFactory sessionFactory) {
		super();
		this.targetClass = targetClass;
		this.sessionFactory = sessionFactory;		
	}
	
	public HibernateCriteriaBuilder(Class targetClass, SessionFactory sessionFactory, boolean uniqueResult) {
		super();
		this.targetClass = targetClass;
		this.sessionFactory = sessionFactory;
		this.uniqueResult = uniqueResult;
	}	

	public void setUniqueResult(boolean uniqueResult) {
		this.uniqueResult = uniqueResult;
	}
	
	/**
	 * Creates a Criterion that compares to class properties for equality
	 * @param propertyName The first property name
	 * @param otherPropertyName The second property name
	 * @return A Criterion instance
	 */
	public Object eqProperty(String propertyName, String otherPropertyName) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [eqProperty] with propertyName ["+propertyName+"] and other property name ["+otherPropertyName+"] not allowed here.") );
		}		
		Criterion c = Restrictions.eqProperty( propertyName, otherPropertyName );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;		
	}


	/**
	 * Creates a Criterion that compares to class properties for !equality
	 * @param propertyName The first property name
	 * @param otherPropertyName The second property name
	 * @return A Criterion instance
	 */
	public Object neProperty(String propertyName, String otherPropertyName) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [neProperty] with propertyName ["+propertyName+"] and other property name ["+otherPropertyName+"] not allowed here."));
		}		
		Criterion c = Restrictions.neProperty( propertyName, otherPropertyName );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;		
	}	
	/**
	 * Creates a Criterion that tests if the first property is greater than the second property
	 * @param propertyName The first property name
	 * @param otherPropertyName The second property name
	 * @return A Criterion instance
	 */
	public Object gtProperty(String propertyName, String otherPropertyName) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [gtProperty] with propertyName ["+propertyName+"] and other property name ["+otherPropertyName+"] not allowed here."));
		}		
		Criterion c = Restrictions.gtProperty( propertyName, otherPropertyName );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;		
	}
	/**
	 * Creates a Criterion that tests if the first property is greater than or equal to the second property
	 * @param propertyName The first property name
	 * @param otherPropertyName The second property name
	 * @return A Criterion instance
	 */
	public Object geProperty(String propertyName, String otherPropertyName) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [geProperty] with propertyName ["+propertyName+"] and other property name ["+otherPropertyName+"] not allowed here."));
		}		
		Criterion c = Restrictions.geProperty( propertyName, otherPropertyName );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;		
	}	
	/**
	 * Creates a Criterion that tests if the first property is less than the second property
	 * @param propertyName The first property name
	 * @param otherPropertyName The second property name
	 * @return A Criterion instance
	 */
	public Object ltProperty(String propertyName, String otherPropertyName) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [ltProperty] with propertyName ["+propertyName+"] and other property name ["+otherPropertyName+"] not allowed here."));
		}		
		Criterion c = Restrictions.ltProperty( propertyName, otherPropertyName );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;		
	}	
	/**
	 * Creates a Criterion that tests if the first property is less than or equal to the second property
	 * @param propertyName The first property name
	 * @param otherPropertyName The second property name
	 * @return A Criterion instance
	 */
	public Object leProperty(String propertyName, String otherPropertyName) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [leProperty] with propertyName ["+propertyName+"] and other property name ["+otherPropertyName+"] not allowed here."));
		}		
		Criterion c = Restrictions.leProperty( propertyName, otherPropertyName );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;		
	}	
	/**
	 * Creates a "greater than" Criterion based on the specified property name and value
	 * @param propertyName The property name
	 * @param propertyValue The property value
	 * @return A Criterion instance
	 */
	public Object gt(String propertyName, Object propertyValue) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [gt] with propertyName ["+propertyName+"] and value ["+propertyValue+"] not allowed here."));
		}		
		Criterion c = Restrictions.gt( propertyName, propertyValue );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;		
	}
	/**
	 * Creates a "greater than or equal to" Criterion based on the specified property name and value
	 * @param propertyName The property name
	 * @param propertyValue The property value
	 * @return A Criterion instance
	 */
	public Object ge(String propertyName, Object propertyValue) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [ge] with propertyName ["+propertyName+"] and value ["+propertyValue+"] not allowed here."));
		}		
		Criterion c = Restrictions.ge( propertyName, propertyValue );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;		
	}	
	/**
	 * Creates a "less than" Criterion based on the specified property name and value
	 * @param propertyName The property name
	 * @param propertyValue The property value
	 * @return A Criterion instance
	 */
	public Object lt(String propertyName, Object propertyValue) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [lt] with propertyName ["+propertyName+"] and value ["+propertyValue+"] not allowed here."));
		}		
		Criterion c = Restrictions.lt( propertyName, propertyValue );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;		
	}	
	/**
	 * Creates a "less than or equal to" Criterion based on the specified property name and value
	 * @param propertyName The property name
	 * @param propertyValue The property value
	 * @return A Criterion instance
	 */
	public Object le(String propertyName, Object propertyValue) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [le] with propertyName ["+propertyName+"] and value ["+propertyValue+"] not allowed here."));
		}		
		Criterion c = Restrictions.le( propertyName, propertyValue );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;		
	}	
	/**
	 * Creates an "equals" Criterion based on the specified property name and value
	 * @param propertyName The property name
	 * @param propertyValue The property value
	 * 
	 * @return A Criterion instance
	 */
	public Object eq(String propertyName, Object propertyValue) {		
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [eq] with propertyName ["+propertyName+"] and value ["+propertyValue+"] not allowed here."));
		}		
		Criterion c = Restrictions.eq( propertyName, propertyValue );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;
	}
	/**
	 * Creates a Criterion with from the specified property name and "like" expression
	 * @param propertyName The property name
	 * @param value The like value
	 * 
	 * @return A Criterion instance
	 */
	public Object like(String propertyName, Object value) {		
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [like] with propertyName ["+propertyName+"] and value ["+value+"] not allowed here."));
		}		
		Criterion c = Restrictions.like( propertyName, value );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;
	}
	/**
	 * Creates a Criterion with from the specified property name and "ilike" (a case sensitive version of "like") expression
	 * @param propertyName The property name
	 * @param value The ilike value
	 * 
	 * @return A Criterion instance
	 */
	public Object ilike(String propertyName, Object value) {		
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [ilike] with propertyName ["+propertyName+"] and value ["+value+"] not allowed here."));
		}		
		Criterion c = Restrictions.ilike( propertyName, value );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;
	}		
	/**
	 * Applys a "in" contrain on the specified property 
	 * @param propertyName The property name
	 * @param values A collection of values
	 * 
	 * @return A Criterion instance
	 */
	public Object in(String propertyName, Collection values) {		
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [in] with propertyName ["+propertyName+"] and values ["+values+"] not allowed here."));
		}		
		Criterion c = Restrictions.in( propertyName, values );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;
	}
	/**
	 * Applys a "in" contrain on the specified property 
	 * @param propertyName The property name
	 * @param values A collection of values
	 * 
	 * @return A Criterion instance
	 */
	public Object in(String propertyName, Object[] values) {		
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [in] with propertyName ["+propertyName+"] and values ["+values+"] not allowed here."));
		}		
		Criterion c = Restrictions.in( propertyName, values );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;
	}
	
	/**
	 * Orders by the specified property name (defaults to ascending)
	 * 
	 * @param propertyName The property name to order by
	 * @return A Order instance
	 */
	public Object order(String propertyName) {
		if(this.criteria == null)
				throwRuntimeException( new IllegalArgumentException("Call to [order] with propertyName ["+propertyName+"]not allowed here."));
		
		Order o = Order.asc(propertyName);
		this.criteria.addOrder(o);
		
		return o;
	}

	/**
	 * Orders by the specified property name and direction
	 * 
	 * @param propertyName The property name to order by
	 * @param direction Either "asc" for ascending or "desc" for descending
	 * 
	 * @return A Order instance
	 */
	public Object order(String propertyName, String direction) {
		if(this.criteria == null)
				throwRuntimeException( new IllegalArgumentException("Call to [order] with propertyName ["+propertyName+"]not allowed here."));
		Order o;
		if(direction.equals( ORDER_DESCENDING )) {
			o = Order.desc(propertyName);
		}
		else {
			o = Order.asc(propertyName);
		}
		this.criteria.addOrder(o);
		
		return o;
	}	
	/**
	 * Creates a Criterion that contrains a collection property by size
	 * 
	 * @param propertyName The property name
	 * @param size The size to constrain by
	 * 
	 * @return A Criterion instance
	 */
	public Object sizeEq(String propertyName, int size) {		
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [sizeEq] with propertyName ["+propertyName+"] and size ["+size+"] not allowed here."));
		}		
		Criterion c = Restrictions.sizeEq( propertyName, size );
		
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}		
		return c;
	}	
	
	/**
	 * Creates a "not equal" Criterion based on the specified property name and value
	 * @param propertyName The property name
	 * @param propertyValue The property value
	 * @return
	 */
	public Object ne(String propertyName, Object propertyValue) {		
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [ne] with propertyName ["+propertyName+"] and value ["+propertyValue+"] not allowed here."));
		}		
		Criterion c = Restrictions.ne( propertyName, propertyValue );
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}
		return c;
	}	

	/**
	 * Creates a "between" Criterion based on the property name and specified lo and hi values
	 * @param propertyName The property name
	 * @param lo The low value
	 * @param hi The high value
	 * @return A Criterion instance
	 */
	public Object between(String propertyName, Object lo, Object hi) {
		if(!validateSimpleExpression()) {
			throwRuntimeException( new IllegalArgumentException("Call to [between] with propertyName ["+propertyName+"]  not allowed here."));
		}		
		Criterion c = Restrictions.between( propertyName, lo,hi);
		if(isInsideLogicalExpression()) {
			this.logicalExpressionArgs.add(c);
		}else {
			this.criteria.add(c);
		}
		return c;		
	}
	
	protected Object createNode(Object name) {
		return createNode( name, Collections.EMPTY_MAP );
	}

	private boolean validateSimpleExpression() {
		if(this.criteria == null)
			return false;
				
		if(!isInsideLogicalExpression() &&
		   !(this.parent instanceof Proxy) &&
		   this.parent != null)
				return false;
		
		return true;
	}
	
	private boolean isInsideLogicalExpression() {
		if(this.logicalExpressions.size() > 0) {
			String currentLogicalExpression = (String)this.logicalExpressions.get( this.logicalExpressions.size() - 1 );
			if(currentLogicalExpression.equals( AND ) ||
			   currentLogicalExpression.equals( OR ) ||
			   currentLogicalExpression.equals( NOT ))
					return true;
		}
		return false;
	}
	
	protected Object createNode(Object name, Map attributes) {		
		if(name.equals(ROOT_CALL) || name.equals(LIST_CALL) || name.equals(GET_CALL)) {
			
			if(this.criteria != null)
				throwRuntimeException( new IllegalArgumentException("call to [" + name + "] not supported here"));
				
			if(name.equals(GET_CALL))
				this.uniqueResult = true;
			
			if(TransactionSynchronizationManager.hasResource(sessionFactory)) {
				this.participate = true;
				this.session = ((SessionHolder)TransactionSynchronizationManager.getResource(sessionFactory)).getSession();
			}
			else {
				this.session = sessionFactory.openSession();
			}
			this.criteria = this.session.createCriteria(targetClass);
			this.criteriaProxy = new ExtendProxy();
			this.criteriaProxy.setAdaptee(this.criteria);
			this.parent = resultProxy;
						
			return resultProxy;
		} else if(name.equals( AND ) ||
				  name.equals( OR ) ||
				  name.equals( NOT )) {
			if(this.criteria == null)
				throwRuntimeException( new IllegalArgumentException("call to [" + name + "] not supported here"));
			
			this.logicalExpressions.add(name);
			return name;
		}
		closeSessionFollowingException();
		throw new MissingMethodException((String) name, getClass(), new Object[] {}) ;		
	}	
	

	protected void nodeCompleted(Object parent, Object node) {
		if(node instanceof Proxy) {
			if(!uniqueResult) {
				resultProxy.setAdaptee(
						this.criteria.list()
				);
			}
			else {
				resultProxy.setAdaptee(
						this.criteria.uniqueResult()
				);
			}
			this.criteria = null;
			if(!this.participate) {
				this.session.close();
			}
		}		
		else if(node.equals( AND ) ||
				node.equals( OR )) {
			
			if(this.logicalExpressionArgs.size() < 2)
				throwRuntimeException( new IllegalArgumentException("Logical expression [" + node +"] must contain at least 2 expressions"));
			
			Criterion lhs = (Criterion)this.logicalExpressionArgs.remove(this.logicalExpressionArgs.size() - 1);
			Criterion rhs = (Criterion)this.logicalExpressionArgs.remove(this.logicalExpressionArgs.size() - 1);
			
			if(parent instanceof Proxy) {
				if(node.equals( AND ))
					this.criteria.add( Restrictions.and( lhs, rhs ) );
				else
					this.criteria.add( Restrictions.or( lhs, rhs ) );
			}
			else if(parent.equals( AND ) ||
					parent.equals( OR )) {
				
				if(node.equals( AND ))
					this.logicalExpressionArgs.add( Restrictions.and( lhs, rhs ) );
				else
					this.logicalExpressionArgs.add( Restrictions.or( lhs, rhs ) );
				
				this.logicalExpressions.remove(this.logicalExpressions.size() - 1);
			}
		}
		else if(node.equals(NOT)) {
			if(this.logicalExpressionArgs.size() < 1)
				throwRuntimeException( new IllegalArgumentException("Logical expression [" + node +"] must contain at least 1 expression"));
			
			Criterion c = (Criterion)this.logicalExpressionArgs.remove(this.logicalExpressionArgs.size() - 1);
			
			if(parent instanceof Proxy) {
				this.criteria.add( Restrictions.not( c ) );
			}
			else if(parent.equals( AND ) ||
					parent.equals( OR ) ||
					parent.equals( NOT )) {
				this.logicalExpressionArgs.add( Restrictions.not( c ) );
				this.logicalExpressions.remove(this.logicalExpressions.size() - 1);
			}			
		}
		super.nodeCompleted(parent, node);
	}

	/**
	 * Throws a runtime exception where necessary to ensure the session gets closed
	 */
	private void throwRuntimeException(RuntimeException t) {
		closeSessionFollowingException();
		throw t;
	}
	
	private void closeSessionFollowingException() {
		if(this.session != null && this.session.isOpen() && !this.participate) {
			this.session.close();
		}
		if(this.criteria != null) {
			this.criteria = null;
		}				
	}
	
	protected void setParent(Object parent, Object child) {
		this.parent = parent;
	}

	protected Object createNode(Object name, Object value) {
		return createNode(name, Collections.EMPTY_MAP, value);
	}
	
	protected Object createNode(Object name, Map attributes, Object value) {
		if(this.criteria == null)
			throwRuntimeException( new IllegalArgumentException("call to [" + name + "] not supported here"));
		
		Criterion c = null;		
		if(name.equals(ID_EQUALS)) {
			c = Restrictions.idEq(value);
		}
		else {
			
			if(	name.equals( IS_NULL ) ||
				name.equals( IS_NOT_NULL ) ||	
				name.equals( IS_EMPTY ) ||
				name.equals( IS_NOT_EMPTY )) {
				if(!(value instanceof String))
					throwRuntimeException( new IllegalArgumentException("call to [" + name + "] with value ["+value+"] requires a String value."));
				
				if(name.equals( IS_NULL )) {				
					c = Restrictions.isNull( (String)value ) ;	
				}
				else if(name.equals( IS_NOT_NULL )) {
					c = Restrictions.isNotNull( (String)value );
	
				}
				else if(name.equals( IS_EMPTY )) {
					c = Restrictions.isEmpty( (String)value );
					this.criteria.add( c );			
				}
				else if(name.equals( IS_NOT_EMPTY )) {
					c = Restrictions.isNotEmpty( (String)value );
					this.criteria.add( c );			
				}		
			}
		}
		if(c != null) {
			if(isInsideLogicalExpression()) {
				this.logicalExpressionArgs.add(c);
			}
			else {
				this.criteria.add( c );
			}
			return c;
		}
		else {
			String nameString = (String)name;
			nameString = SETTER_PREFIX + nameString.substring(0,1).toUpperCase() + nameString.substring(1);
			if(parent instanceof Proxy) {
					try {
						criteriaProxy.invokeMethod(nameString, value);
						return this.criteria;
					}
					catch(MissingMethodException mme) {
						throwRuntimeException( new MissingMethodException(nameString, getClass(), new Object[] {value}) );
					}
			}
			throwRuntimeException( new MissingMethodException(nameString, getClass(), new Object[] {value}));
		}
		return c;
	}

}

