package org.codehaus.gram.base;

import java.io.Serializable;

/**
 * @version $Revision$
 */
public interface Identifiable { //extends Cloneable, Serializable {

    /**
     * @return Returns the id.
     * @hibernate.id generator-class="native" type="java.lang.Integer" column="id"
     */
    public Integer getId();

    public void setId(Integer id);

    //public Object clone();
}
