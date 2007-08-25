/*
 * Copyright 2003-2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.tools;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final char WILDCARD = '*';
    private static final String WILD_CARD_REGEX = "[^/]+?";

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
            // assume properties contain paths
			if(propertyValue == null) {
				throw new IllegalArgumentException("Variable $"+propertyKey+" in groovy-starter.conf references a non-existent System property! Try passing the property to the VM using -D"+propertyKey+"=myValue");
			}
            propertyValue = getSlashyPath(propertyValue);
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
        int starIndex = filter.indexOf(WILDCARD);
        if (starIndex==-1) {
            addFile(new File(filter));
            return;
        }

        String startDir = filter.substring(0, starIndex-1);
        File root = new File(startDir);

        filter = filter.replaceAll("\\.","\\\\.");
        filter = filter.replaceAll("\\" + WILDCARD, WILD_CARD_REGEX);
        Pattern pattern = Pattern.compile(filter);

        final File[] files = root.listFiles();
        if(files!=null) {
            findMatchingFiles(files, pattern);
        }
    }

    private void findMatchingFiles(File[] files, Pattern pattern) {
        for (int i = 0; i < files.length; i++) {
            File file = files[i];
            Matcher m = pattern.matcher(getSlashyPath(file.getAbsolutePath()));
            if(m.matches() && file.isFile()) {
                addFile(file);
            }
            if(file.isDirectory()) {
                final File[] dirFiles = file.listFiles();
                if(dirFiles!=null) {                    
                    findMatchingFiles(dirFiles, pattern);
                }
            }
        }
    }

    // change path representation to something more system independent.
    // This solution is based on an absolute path
    private String getSlashyPath(final String path) {
        String changedPath = path;
        if (File.separatorChar != '/')
            changedPath = changedPath.replace(File.separatorChar, '/');

        return changedPath;
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
                throw new AssertionError("converting an existing file to an url should have never thrown an exception!");
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
