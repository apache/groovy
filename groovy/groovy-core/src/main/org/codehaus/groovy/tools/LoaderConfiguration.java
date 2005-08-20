/*
$Id$

Copyright 2003 (C) James Strachan and Bob Mcwhirter. All Rights Reserved.

Redistribution and use of this software and associated documentation
("Software"), with or without modification, are permitted provided
that the following conditions are met:

1. Redistributions of source code must retain copyright
   statements and notices.  Redistributions must also contain a
   copy of this document.

2. Redistributions in binary form must reproduce the
   above copyright notice, this list of conditions and the
   following disclaimer in the documentation and/or other
   materials provided with the distribution.

3. The name "groovy" must not be used to endorse or promote
   products derived from this Software without prior written
   permission of The Codehaus.  For written permission,
   please contact info@codehaus.org.

4. Products derived from this Software may not be called "groovy"
   nor may "groovy" appear in their names without prior written
   permission of The Codehaus. "groovy" is a registered
   trademark of The Codehaus.

5. Due credit should be given to The Codehaus -
   http://groovy.codehaus.org/

THIS SOFTWARE IS PROVIDED BY THE CODEHAUS AND CONTRIBUTORS
``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT
NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL
THE CODEHAUS OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
OF THE POSSIBILITY OF SUCH DAMAGE.

*/
package org.codehaus.groovy.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * class used to configure a RootLoader from a stream or by using 
 * it's methods.
 * 
 * The stream can be for example a FileInputStream from a file with
 * the following format:
 * 
 * # comment
 * main is classname
 * load path
 * load file
 * load pathWith${property}
 * load path/*.jar
 *
 *<ul>
 * <li>All lines starting with "#" are ignored.</li> 
 * <li>The "main is" part may only be once in the file. The String
 * afterwards is the name of a class if a main method. </li>
 * <li>The "load" command will add the given file or path to the 
 * classpath in this configuration object.
 * </li>
 *</ul>
 * 
 * Defining the main class is optional if @see #setRequireMain(boolean) was 
 * called with false, before reading the configuration. 
 * You can use the wildcard "*" to filter the path, but only for files, not
 * directories. The  ${propertyname} is replaced by the value of the system's
 * propertyname. You can use user.home here for example. If the property does
 * not exist, an empty string will be used. If the path or file after the load
 * does not exist, the path will be ignored.
 *
 * @see RootLoader
 * @author Jochen Theodorou
 * @version $Revision$
 */
public class LoaderConfiguration {
    
    private final static String 
        MAIN_PREFIX = "main is", LOAD_PREFIX="load";
    private ArrayList classPath = new ArrayList();
    private String main;
    private boolean requireMain;
    
    /**
     * creates a new loader configuration
     */
    public LoaderConfiguration() {
        this.requireMain = true;
    }
    
    /**
     * configures this loader with a stream 
     * 
     * @param is           stream used to read the configuration
     * @throws IOException if reading or parsing the contents of the stream fails
     */
    public void configure(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader( new InputStreamReader(is));
        int lineNumber=0;
        
        while(true) {
            String line = reader.readLine();
            if (line==null) break;
            
            line = line.trim();
            lineNumber++;
            
            if (line.startsWith("#") || line.length()==0) continue;
            
            if (line.startsWith(LOAD_PREFIX)) {
                String loadPath = line.substring(LOAD_PREFIX.length()).trim();
                loadPath = assignProperties(loadPath);
                loadFilteredPath(loadPath);
            } else if (line.startsWith(MAIN_PREFIX)) {
                if (main!=null) throw new IOException("duplicate definition of main in line "+lineNumber+" : "+line);
                main = line.substring(MAIN_PREFIX.length()).trim();
            } else {
                throw new IOException("unexpected line in "+lineNumber+" : "+line);
            }
        }
        
        if (requireMain && main == null) throw new IOException("missing main class definition in config file");
    }
   
    /**
     * exapands the properties inside the given string to it's values
     */
    private String assignProperties(String str) {
        int propertyIndexStart=0,propertyIndexEnd=0;
        String result="";

        while (propertyIndexStart<str.length()) {
            propertyIndexStart=str.indexOf("${",propertyIndexStart);
            if (propertyIndexStart==-1) break;
            result += str.substring(propertyIndexEnd,propertyIndexStart);

            propertyIndexEnd=str.indexOf("}",propertyIndexStart);
            if (propertyIndexEnd==-1) break;
            
            String propertyKey = str.substring(propertyIndexStart+2,propertyIndexEnd);
            String propertyValue = System.getProperty(propertyKey);
            result+=propertyValue;
            
            propertyIndexEnd++;
            propertyIndexStart=propertyIndexEnd;
        }
        
        if (propertyIndexStart==-1 || propertyIndexStart>=str.length()) {
            result+=str.substring(propertyIndexEnd);
        } else if (propertyIndexEnd==-1) {
            result+=str.substring(propertyIndexStart);
        } 
        
        return result;
    }
    
    
    /**
     * load a possible filtered path. Filters are defined
     * by using the * wildcard like in any shell
     */
    private void loadFilteredPath(String filter) {
        int starIndex = filter.indexOf('*');
        if (starIndex==-1) {
            addFile(new File(filter));
            return;
        } 
        if (!parentPathDoesExist(filter)) return;        
        String filterPart = getParentPath(filter);
        int index = filterPart.indexOf('*');
        final String prefix = filterPart.substring(0,index);
        final String suffix = filterPart.substring(index+1);
        File dir = new File(filter.substring(0,filter.length()-filterPart.length()));
        FilenameFilter ff = new FilenameFilter() {
            public boolean accept(File dir, String name) {
                if (!name.startsWith(prefix)) return false;
                if (!name.endsWith(suffix)) return false;
                return true;
            }
        };
        File[] matches = dir.listFiles(ff);
        for (int i=0; i<matches.length; i++) addFile(matches[i]);
    }
    
    /**
     * return true if the parent of the path inside the given
     * string does exist
     */
    private boolean parentPathDoesExist(String path) {
        File dir = new File (path).getParentFile();
        return dir.exists();
    }
    
    /**
     * seperates the given path at the last '/'
     */
    private String getParentPath(String filter) {
        int index = filter.lastIndexOf('/');
        if (index==-1) return "";
        return filter.substring(index+1);
    }
    
    /**
     * adds a file to the classpath if it does exist
     */
    public void addFile(File f) {
        if (f!=null && f.exists()) {
            try {
                classPath.add(f.toURI().toURL());
            } catch (MalformedURLException e) {
                assert false:"converting an existing file to an url should have never thrown an exception!";
            }
        }        
    }
    
    /**
     * adds a file to the classpath if it does exist
     */
    public void addFile(String s) {
        if (s!=null) addFile(new File(s));
    }
    
    /**
     * adds a classpath to this configuration. It expects a string
     * with multiple paths, seperated by the system dependent 
     * @see java.io.File#pathSeparator
     */
    public void addClassPath(String path) {
        String[] paths = path.split(File.pathSeparator);
        for (int i=0; i<paths.length; i++) {
            addFile(new File(paths[i]));
        }
    }
    
    /**
     * gets a classpath as URL[] from this configuration. 
     * This can be used to construct a @see java.net.URLClassLoader
     */
    public URL[] getClassPathUrls() {
        return (URL[]) classPath.toArray(new URL[]{});
    }
    
    /**
     * returns the main class or null is no is defined
     */
    public String getMainClass() {
        return main;
    }
    
    /**
     * sets the main class. If there is already a main class
     * it is overwritten. Calling @see #configure(InputStream) 
     * after calling this method does not require a main class
     * definition inside the stream 
     */
    public void setMainClass(String clazz) {
        main = clazz;
        requireMain = false;
    }
    
    /**
     * if set to false no main class is required when calling
     * @see #configure(InputStream)
     */
    public void setRequireMain(boolean requireMain) {
        this.requireMain = requireMain;
    }
}
