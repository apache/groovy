package org.codehaus.groovy.tools;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.MalformedURLException;

public class CompilerClassLoader
    extends URLClassLoader
{
    private static final URL[] EMPTY_URL_ARRAY = new URL[0];

    public CompilerClassLoader()
    {
        super( EMPTY_URL_ARRAY );
    }

    public void addPath(String path)
        throws MalformedURLException
    {
        addURL( new File( path ).toURL() );
    }
}
