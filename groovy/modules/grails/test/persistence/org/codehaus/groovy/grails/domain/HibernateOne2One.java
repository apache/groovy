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
package org.codehaus.groovy.grails.domain;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratorType;
import javax.persistence.Id;
import javax.persistence.OneToOne;

/**
 * @author Graeme Rocher
 * 04-Aug-2005
 */
@Entity
public class HibernateOne2One {

	
	private Long id;
	private HibernateOne2One2 other;
	/**
	 * @return Returns the id.
	 */
	@Id(generate = GeneratorType.AUTO)
	public Long getId() {
		return id;
	}
	/**
	 * @param id The id to set.
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return Returns the other.
	 */
	@OneToOne(cascade = CascadeType.ALL)	
	public HibernateOne2One2 getOther() {
		return other;
	}
	/**
	 * @param other The other to set.
	 */
	public void setOther(HibernateOne2One2 other) {
		this.other = other;
	}
	
	
}
