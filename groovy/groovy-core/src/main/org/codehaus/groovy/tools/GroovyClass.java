package org.codehaus.groovy.tools;

public class GroovyClass
{
    public static final GroovyClass[] EMPTY_ARRAY = new GroovyClass[ 0 ];

    private String name;
    private byte[] bytes;

    public GroovyClass(String name,
                       byte[] bytes)
    {
        this.name  = name;
        this.bytes = bytes;
    }

    public String getName()
    {
        return this.name;
    }

    public byte[] getBytes()
    {
        return this.bytes;
    }
}

