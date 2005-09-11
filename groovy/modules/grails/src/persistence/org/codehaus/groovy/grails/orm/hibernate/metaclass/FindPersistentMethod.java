package org.codehaus.groovy.grails.orm.hibernate.metaclass;

import groovy.lang.MissingMethodException;

import java.sql.SQLException;
import java.util.regex.Pattern;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Example;
import org.springframework.orm.hibernate3.HibernateCallback;

public class FindPersistentMethod extends AbstractStaticPersistentMethod {

	private static final String METHOD_PATTERN = "^find$";

	public FindPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader) {
		super(sessionFactory, classLoader, Pattern.compile(METHOD_PATTERN));
	}

	protected Object doInvokeInternal(final Class clazz, String methodName,
			Object[] arguments) {
		
		if(arguments.length == 0)
			throw new MissingMethodException(methodName,clazz,arguments);
		
		final Object arg = arguments[0];
		// if the arg is an instance of the class find by example
		if(clazz.isInstance( arg.getClass() )) {			
			return super.getHibernateTemplate().executeFind( new HibernateCallback() {

				public Object doInHibernate(Session session) throws HibernateException, SQLException {
					
					Example example = Example.create(arg)
							.ignoreCase();
					
					Criteria crit = session.createCriteria(clazz);
					return crit
						.add(example)
						.list();
				}
				
			});			
		}
		throw new MissingMethodException(methodName,clazz,arguments);
	}

}
