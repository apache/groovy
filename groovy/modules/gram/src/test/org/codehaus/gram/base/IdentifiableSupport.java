package org.codehaus.gram.base;

/**
 * @version $Revision$
 */
public class IdentifiableSupport implements Identifiable {
    private Integer id;

    /**
     * @return Returns the id.
     * @hibernate.id generator-class="native" type="java.lang.Integer" column="id"
     */
    public Integer getId() {
        return this.id;
    }

    /**
     * @param id The id to set.
     */
    public void setId(Integer id) {
        this.id = id;
    }
   
}
