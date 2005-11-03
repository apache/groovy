/**
 * ported from commons-sql
 * @author Jeremy Rayner
 * @author Dierk Koenig, adapted to jsr-04
 */
package org.javanicus.gsql

public class Column implements Cloneable {
    @Property name
    @Property groovyName
    @Property boolean primaryKey
    @Property required
    @Property autoIncrement
    @Property typeCode
    @Property type
    @Property size
    @Property defaultValue
    @Property scale
    @Property precisionRadix
    @Property ordinalPosition
    private typeMap

    public Column(typeMap) {
        this.typeMap = typeMap
                  
        primaryKey = false
        required = false
        autoIncrement = false
        size = null
        defaultValue = null
        scale = 0
        precisionRadix = 10
        ordinalPosition = 0
    }

    public Object clone() { //todo: throws CloneNotSupportedException {
        def result = new Column(typeMap)
        
        result.name            = name
        result.groovyName      = groovyName
        result.primaryKey      = primaryKey
        result.required        = required
        result.autoIncrement   = autoIncrement
        result.typeCode        = typeCode
        result.type            = type
        result.size            = size
        result.defaultValue    = defaultValue
        result.scale           = scale
        result.precisionRadix  = precisionRadix
        result.ordinalPosition = ordinalPosition
                  
        return result
    }

    public Column(TypeMap typeMap, name, groovyName, int typeCode, size, required, primaryKey, autoIncrement, defaultValue) {
        //bug?        this(typeMap)
        this.typeMap = typeMap
        this.setName(name)
        this.setGroovyName(groovyName)
        this.setTypeCode(typeCode)
        this.setSize(size)
        this.setRequired(required)
        this.setPrimaryKey(primaryKey)
        this.setAutoIncrement(autoIncrement)
        this.setDefaultValue(defaultValue)
    }
    
    public Column(typeMap, name, groovyName, String type, size, required, primaryKey, autoIncrement, defaultValue ) {
        this(typeMap, name, groovyName, typeMap.getJdbcTypeCode(type), size, required, primaryKey, autoIncrement, defaultValue)
    }

    public Column(typeMap, name, groovyName, typeCode, size, required, primaryKey, autoIncrement, defaultValue, scale) {
        //bug?        this(typeMap)
        this.typeMap = typeMap
        this.setName(name)
        this.setGroovyName(groovyName)
        this.setTypeCode(typeCode)
        this.setSize(size)
        this.setRequired(required)
        this.setPrimaryKey(primaryKey)
        this.setAutoIncrement(autoIncrement)
        this.setDefaultValue(defaultValue)
        this.setScale(scale)
    }

    public String toString() {
        return "Column[name=${name};type=${type}]"
    }

    public void setTypeCode(int typeCode) {
        this.typeCode = typeCode
        this.type = typeMap.getJdbcTypeName(typeCode)
    }

    public void setType(String type) {
        this.type = type
        this.typeCode = typeMap.getJdbcTypeCode(type)
    }

    public void setSize(String size) {
        int pos = size.indexOf(",")

        if (pos < 0) {
            this.size = size
        } else {
            this.size = size.substring(0, pos)
            scale     = Integer.parseInt(size.substring(pos + 1))
        }
    }
}
