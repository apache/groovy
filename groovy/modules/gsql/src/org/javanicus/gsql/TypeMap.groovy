package org.javanicus.gsql

import java.sql.Types

public class TypeMap {
    property nameToCode
    property codeToName
    
    public TypeMap() {
        nameToCode = [:]
        codeToName = [:]
                  
        map("bit",Types.BIT)
        map("tinyint",Types.TINYINT)
        map("smallint",Types.SMALLINT)
        map("integer",Types.INTEGER)
        map("bigint",Types.BIGINT)
        map("float",Types.FLOAT)
        map("real",Types.REAL)
        map("double",Types.DOUBLE)
        map("numeric",Types.NUMERIC)
        map("decimal",Types.DECIMAL)
        map("char",Types.CHAR)
        map("varchar",Types.VARCHAR)
        map("longvarchar",Types.LONGVARCHAR)
        map("date",Types.DATE)
        map("time",Types.TIME)
        map("timestamp",Types.TIMESTAMP)
        map("binary",Types.BINARY)
        map("varbinary",Types.VARBINARY)
        map("longvarbinary",Types.LONGVARBINARY)
        map("null",Types.NULL)
        map("other",Types.OTHER)
        map("java_object",Types.JAVA_OBJECT)
        map("distinct",Types.DISTINCT)
        map("struct",Types.STRUCT)
        map("array",Types.ARRAY)
        map("blob",Types.BLOB)
        map("clob",Types.CLOB)
        map("ref",Types.REF)
        map("datalink",Types.DATALINK)
        map("boolean",Types.BOOLEAN)
    }
    
    private map(name,code) {
        nameToCode.put(name,code)
        codeToName.put(code,name)
    }
    
    public int getJdbcTypeCode(String name) {
        return nameToCode.get(name,Types.OTHER)
    }

    public String getJdbcTypeName(int code) {
        return codeToName.get(code,"unknown")
    }
}
