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
package com.recipes;

import groovy.lang.IntRange;
import org.codehaus.groovy.grails.orm.hibernate.validation.ConstrainedPersistentProperty;
import org.codehaus.groovy.grails.validation.ConstrainedProperty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Transient;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Graeme Rocher
 * @since 20-Feb-2006
 */
@Entity
public class Recipe {
    private Long id;
    private String title;
    private String description;
    private Date date;

    @Id
    @GeneratedValue
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Transient
    public Map getConstraints() {
        ConstrainedProperty titleConstraint = new ConstrainedPersistentProperty(Recipe.class, "title", String.class );
        titleConstraint.setOrder(1);
        titleConstraint.setLength(new IntRange(5,15));
        ConstrainedProperty descConstraint = new ConstrainedPersistentProperty(Recipe.class, "title", String.class );
        descConstraint.setOrder(2);
        descConstraint.setWidget("textarea");


        Map constraints = new HashMap();
        constraints.put("title", titleConstraint);
        constraints.put("description", descConstraint);

        return constraints;
    }
}
