package org.javanicus.gsql

import java.sql.Types

public class TypeMap {
    @Property nameToCode
    @Property codeToName
    @Property decimalTypes
    @Property textTypes
    @Property otherTypes
              
    public TypeMap() {
        nameToCode = [:]
        codeToName = [:]
                  
        decimalTypes = [
            "numeric" : Types.NUMERIC,
            "decimal" : Types.DECIMAL,
            "float" : Types.FLOAT,
            "real" : Types.REAL,
            "double" : Types.DOUBLE
        ]
        textTypes = [
            "char" : Types.CHAR,
            "varchar" : Types.VARCHAR,
            "longvarchar" : Types.LONGVARCHAR,
            "clob" : Types.CLOB,
            "date" : Types.DATE,
            "time" : Types.TIME,
            "timestamp" : Types.TIMESTAMP
        ]
        otherTypes = [
            "bit" : Types.BIT,
            "tinyint" : Types.TINYINT,
            "smallint" : Types.SMALLINT,
            "integer" : Types.INTEGER,
            "bigint" : Types.BIGINT,
            "binary" : Types.BINARY,
            "varbinary" : Types.VARBINARY,
            "longvarbinary" : Types.LONGVARBINARY,
            "null" : Types.NULL,
            "other" : Types.OTHER,
            "java_object" : Types.JAVA_OBJECT,
            "distinct" : Types.DISTINCT,
            "struct" : Types.STRUCT,
            "array" : Types.ARRAY,
            "blob" : Types.BLOB,
            "ref" : Types.REF,
            "datalink" : Types.DATALINK,
            "boolean" : Types.BOOLEAN
        ]
        for (entries in decimalTypes.entrySet()) {
            crossRef(entries.key,entries.value)          
        }          
        for (entries in textTypes.entrySet()) {
            crossRef(entries.key,entries.value)          
        }          
        for (entries in otherTypes.entrySet()) {
            crossRef(entries.key,entries.value)          
        }          
  
    }
    
    private crossRef(name,code) {
        nameToCode.put(name,code)
        codeToName.put(code,name)
    }
    
    public int getJdbcTypeCode(String name) {
        return nameToCode.get(name,Types.OTHER)
    }

    public String getJdbcTypeName(int code) {
        return codeToName.get(code,"unknown")
    }
    
    /**
      * Returns true if values for the type need have size and scale measurements
      *
      * @param type The type to check.
      */
    public boolean isDecimalType(int type) {
        return isDecimalType(getJdbcTypeName(type));
    }
    
    /**
      * Returns true if values for the type need have size and scale measurements
      *
      * @param type The type to check.
      */
    public boolean isDecimalType(String type) {
        return decimalTypes.keySet().any {
            type.equalsIgnoreCase(it)
        }
    }
    
}
