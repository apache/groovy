package org.codehaus.groovy.tools;

import org.codehaus.groovy.syntax.lexer.StringCharStream;

public class StringCompiler
{
    private Compiler compiler;

    public StringCompiler()
    {
        this.compiler = new Compiler();
    }

    protected Compiler getCompiler()
    {
        return this.compiler;
    }

    public void setClasspath(String classpath)
        throws Exception
    {
        getCompiler().setClasspath( classpath );
    }

    public GroovyClass[] compile(String source)
        throws Exception
    {
        StringCharStream charStream = new StringCharStream( source );

        return getCompiler().compile( charStream );
    }
}
