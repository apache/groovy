/*
 * Created on Mar 7, 2004
 *
 */
package groovy.swt.scrapbook;

/**
 * @author <a href:ckl at dacelo.nl">Christiaan ten Klooster </a> $Id$
 */
public class NamedObject {
    private String name = "empty name";
    private String description = "empty desc";
    private String value = "empty value";

    /**
     * @return Returns the description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            The description to set.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value
     *            The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
