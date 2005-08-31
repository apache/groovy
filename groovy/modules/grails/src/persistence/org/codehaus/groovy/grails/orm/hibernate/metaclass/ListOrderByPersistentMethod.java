/**
 * 
 */
package org.codehaus.groovy.grails.orm.hibernate.metaclass;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.springframework.orm.hibernate3.HibernateCallback;

/**
 * The "listOrderBy*" static persistent method. This method allows
 * ordered listing of instances based on their properties.
 * 
 * eg.
 * Account.listOrderByHolder();
 * Account.listOrderByHolder(max); // max results
 * 
 * @author Graeme
 *
 */
public class ListOrderByPersistentMethod extends AbstractStaticPersistentMethod {

	private static final String METHOD_PATTERN = "(listOrderBy)(\\+)";

	public ListOrderByPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader) {
		super(sessionFactory, classLoader, Pattern.compile( METHOD_PATTERN ));
	}

	/* (non-Javadoc)
	 * @see org.codehaus.groovy.grails.orm.hibernate.metaclass.AbstractStaticPersistentMethod#doInvokeInternal(java.lang.Class, java.lang.String, java.lang.Object[])
	 */
	protected Object doInvokeInternal(final Class clazz, String methodName,
			Object[] arguments) {

		Matcher match = getPattern().matcher( methodName );
		
		String nameInSignature = match.group(2);
		final String propertyName = nameInSignature.substring(0,0).toLowerCase() +
										nameInSignature.substring(1);
		
		int temp = -1;
		if(arguments.length > 0) {
			Object arg = arguments[0];
			if(arg instanceof Integer) {
				temp = ((Integer)arg).intValue();
			}
		}
		final int max = temp;
		
		return super.getHibernateTemplate().executeFind( new HibernateCallback() {

			public Object doInHibernate(Session session) throws HibernateException, SQLException {
				
				Criteria crit = session.createCriteria(clazz);
				crit.addOrder( Order.asc( propertyName ) );
				if(max > -1) {
					crit.setMaxResults(max);
				}
				return crit.list();
			}
			
		});
	}

}
