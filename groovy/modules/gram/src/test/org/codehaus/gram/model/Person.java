package org.codehaus.gram.model;

import org.codehaus.gram.base.IdentifiableSupport;

import java.util.Set;

/**
 * @hibernate.class table="Person"
 *
 * @version $Revision$
 */
public class Person extends IdentifiableSupport {
    private String name;
    private Set children;
    private Object location;

    /**
     * @return Returns the children.
     * @hibernate.collection-key column = "parentId"
     * @hibernate.collection-many-to-many class="org.codehaus.gram.JamTest" column="id"
     * @hibernate.set inverse="false" cascade="save-update" table="JamTable" lazy="false"
     */
    public Set getChildren() {
        return children;
    }

    public void setChildren(Set children) {
        this.children = children;
    }

    /**
     * @return Returns the name.
     * @hibernate.property column="name" type="java.lang.String" length="255"
     * not-null="true"
     */
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

     /**
     * @return Returns the Location.
     * @hibernate.many-to-one class="Location"
     * constrained="true" foreign-key="FK_Location"
     * outer-join="auto" column="location"
     * not-null="true"
     */
    public Object getLocation() {
        return location;
    }

    public void setLocation(Object location) {
        this.location = location;
    }
}
