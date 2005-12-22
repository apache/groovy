package org.codehaus.groovy.grails.orm.hibernate.metaclass;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.codehaus.groovy.grails.commons.GrailsApplication;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
/**
 * The "findBy*" static persistent method. This method allows querying for
 * instances of grails domain classes based on their properties. This method returns a list of all found results
 * 
 * 
 * eg.
 * Account.findAllByHolder("Joe Blogs"); // Where class "Account" has a property called "holder"
 * Account.findAllByHolderAndBranch("Joe Blogs", "London" ); // Where class "Account" has a properties called "holder" and "branch"
 * 
 * @author Graeme Rocher
 * @since 13-Dec-2005
 *
 */
public class FindAllByPersistentMethod extends
		AbstractClausedStaticPersistentMethod {

	private static final String OPERATOR_OR = "Or";
	private static final String OPERATOR_AND = "And";
	
	private static final String METHOD_PATTERN = "(findAllBy)(\\w+)";
	private static final String[] OPERATORS = new String[]{ OPERATOR_AND, OPERATOR_OR };

	public FindAllByPersistentMethod(GrailsApplication application, SessionFactory sessionFactory, ClassLoader classLoader) {
		super(application, sessionFactory, classLoader, Pattern.compile(METHOD_PATTERN), OPERATORS);
	}

	protected Object doInvokeInternalWithExpressions(final Class clazz,
			String methodName, final Object[] arguments, final List expressions) {
		return super.getHibernateTemplate().executeFind( new HibernateCallback() {

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				Criteria c = session.createCriteria(clazz);
				
				
				if(arguments.length > 0) {
					if(arguments[0] instanceof Map) {
						Map argMap = (Map)arguments[0];
						populateArgumentsForCriteria(c,argMap);										
					}
				}				
								
				for (Iterator i = expressions.iterator(); i.hasNext();) {
					GrailsMethodExpression current = (GrailsMethodExpression) i.next();
					c.add( current.getCriterion() );
					
				}

				return c.list();
			}
		});
	}



}
