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
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.orm.hibernate3.HibernateTemplate;

import java.util.regex.Pattern;

/**
 * Method that allows you to count the number of instances in the database
 *
 * eg. Account.count() // returns how many in total
 *
 *
 * @author Graeme Rocher
 * @since 17-Feb-2006
 */
public class CountPersistentMethod  extends AbstractStaticPersistentMethod {
    private static final Pattern METHOD_PATTERN = Pattern.compile("^count$");
    private static final String METHOD_SIGNATURE = "count";

    public CountPersistentMethod(SessionFactory sessionFactory, ClassLoader classLoader) {
        super(sessionFactory, classLoader, METHOD_PATTERN);
    }

    protected Object doInvokeInternal(final Class clazz, String methodName, Object[] arguments) {
        HibernateTemplate t = getHibernateTemplate();
        final StringBuffer b = new StringBuffer("select count(persistentClass) from ");
        b.append(clazz.getName());
        b.append(" as persistentClass");
        if(arguments.length == 0) {
              return t.execute(new HibernateCallback() {
                  public Object doInHibernate(Session session) throws HibernateException {
                      Query q = session.createQuery(b.toString());
                      return q.uniqueResult();
                  }
              });
        }
        // TODO add support for counting with a query
        throw new MissingMethodException(METHOD_SIGNATURE, clazz,arguments);
    }
}
