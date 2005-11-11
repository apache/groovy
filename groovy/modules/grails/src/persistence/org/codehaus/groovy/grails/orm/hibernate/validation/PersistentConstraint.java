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
package org.codehaus.groovy.grails.orm.hibernate.validation;

import org.codehaus.groovy.grails.validation.Constraint;
import org.springframework.orm.hibernate3.HibernateTemplate;
/**
 * Interface that defines a persistent constraint that evaluates the database.
 * 
 * @author Graeme Rocher
 * @since 10-Nov-2005
 */
public interface PersistentConstraint extends Constraint {

	/**
	 * Sets the hibernate template to use for the persistent constraint
	 * @param template
	 */
	void setHibernateTemplate(HibernateTemplate template);
}
