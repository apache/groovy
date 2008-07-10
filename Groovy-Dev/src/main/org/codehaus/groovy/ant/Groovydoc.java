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
package org.codehaus.groovy.ant;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.DirSet;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.PatternSet;
import org.codehaus.groovy.tools.groovydoc.ClasspathResourceManager;
import org.codehaus.groovy.tools.groovydoc.FileOutputTool;
import org.codehaus.groovy.tools.groovydoc.GroovyDocTool;

/**
 * Access to the GroovyDoc tool from Ant.
 *
 * @version $Id$
 */
public class Groovydoc extends Task
{
    private final LoggingHelper log = new LoggingHelper(this);
    
    private Path sourcePath;
    private File destDir;
    private List packageNames;
    private List excludePackageNames;
	private String windowTitle;
	private boolean privateScope;
    private boolean useDefaultExcludes;
    private boolean includeNoSourcePackages;
    private List packageSets;
	private List sourceFilesToDoc;
    // TODO: hook this in
    private List links = new ArrayList();


    public Groovydoc() {
    	packageNames = new ArrayList();
    	excludePackageNames = new ArrayList();
    	packageSets = new ArrayList();
    	sourceFilesToDoc = new ArrayList();
        privateScope = false;
        useDefaultExcludes = true;
        includeNoSourcePackages = false;
    }
    /**
     * Specify where to find source file
     *
     * @param src a Path instance containing the various source directories.
     */
    public void setSourcepath(Path src) {
        if (sourcePath == null) {
            sourcePath = src;
        } else {
            sourcePath.append(src);
        }
    }

    /**
     * Set the directory where the Javadoc output will be generated.
     *
     * @param dir the destination directory.
     */
    public void setDestdir(File dir) {
        destDir = dir;
        // todo: maybe tell groovydoc to use file output
    }
    
    /**
     * Set the package names to be processed.
     *
     * @param packages a comma separated list of packages specs
     *        (may be wildcarded).
     *
     */
    public void setPackagenames(String packages) {
        StringTokenizer tok = new StringTokenizer(packages, ",");
        while (tok.hasMoreTokens()) {
            String packageName = tok.nextToken();
            packageNames.add(packageName);
        }
    }

    public void setUse(boolean b) {
    	//ignore as 'use external file' irrelevant with groovydoc :-)
    }
    
    /**
     * Set the title to be placed in the HTML &lt;title&gt; tag of the
     * generated documentation.
     *
     * @param title the window title to use.
     */
    public void setWindowtitle(String title) {
    	windowTitle = title;
    }
    
    /**
     * Indicate whether all classes and
     * members are to be included in the scope processed
     *
     * @param b true if scope is to be private level.
     */
    public void setPrivate(boolean b) {
    	privateScope = b;
    }

    /**
     * Add the directories matched by the nested dirsets to the Vector
     * and the base directories of the dirsets to the Path.  It also
     * handles the packages and excludepackages attributes and
     * elements.
     *
     * @since 1.5
     */
    private void parsePackages(List resultantPackages, Path sp) {
        List addedPackages = new ArrayList();
        List dirSets = new ArrayList(packageSets);

        // for each sourcePath entry, add a directoryset with includes
        // taken from packagenames attribute and nested package
        // elements and excludes taken from excludepackages attribute
        // and nested excludepackage elements
        if (sourcePath != null) {
            PatternSet ps = new PatternSet();
            if (packageNames.size() > 0) {
                Iterator itr = packageNames.iterator();
                while (itr.hasNext()) {
                    String p = (String) itr.next();
                    String pkg = p.replace('.', '/');
                    if (pkg.endsWith("*")) {
                        pkg += "*";
                    }
                    ps.createInclude().setName(pkg);
                }
            } else {
                ps.createInclude().setName("**");
            }

            Iterator itr2 = excludePackageNames.iterator();
            while (itr2.hasNext()) {
                String p = (String) itr2.next();
                String pkg = p.replace('.', '/');
                if (pkg.endsWith("*")) {
                    pkg += "*";
                }
                ps.createExclude().setName(pkg);
            }


            String[] pathElements = sourcePath.list();
            for (int i = 0; i < pathElements.length; i++) {
                File dir = new File(pathElements[i]);
                if (dir.isDirectory()) {
                    DirSet ds = new DirSet();
                    ds.setDefaultexcludes(useDefaultExcludes);
                    ds.setDir(dir);
                    ds.createPatternSet().addConfiguredPatternset(ps);
                    dirSets.add(ds);
                } else {
                    log.warn("Skipping " + pathElements[i] + " since it is no directory.");
                }
            }
        }

        Iterator itr3 = dirSets.iterator();
        while (itr3.hasNext()) {
            DirSet ds = (DirSet) itr3.next();
            File baseDir = ds.getDir(getProject());
            log.debug("scanning " + baseDir + " for packages.");
            DirectoryScanner dsc = ds.getDirectoryScanner(getProject());
            String[] dirs = dsc.getIncludedDirectories();
            boolean containsPackages = false;
            for (int i = 0; i < dirs.length; i++) {
                // are there any groovy or java files in this directory?
                File pd = new File(baseDir, dirs[i]);
                String[] files = pd.list(new FilenameFilter () {
                        public boolean accept(File dir1, String name) {
                            return name.endsWith(".java")
                                || name.endsWith(".groovy")
                                || name.endsWith(".gv")
                                || name.endsWith(".gvy")
                                || name.endsWith(".gsh")
                                || (includeNoSourcePackages
                                    && name.equals("package.html"));
                        }
                    });

                Iterator itr4 = Arrays.asList(files).iterator();
                while (itr4.hasNext()) {
                	String filename = (String) itr4.next();
                	sourceFilesToDoc.add(dirs[i] + File.separator + filename);
                }
                
                if (files.length > 0) {
                    if ("".equals(dirs[i])) {
                        log.warn(baseDir
                            + " contains source files in the default package,"
                            + " you must specify them as source files"
                            + " not packages.");
                    } else {
                        containsPackages = true;
                        String packageName =
                            dirs[i].replace(File.separatorChar, '.');
                        if (!addedPackages.contains(packageName)) {
                            addedPackages.add(packageName);
                            resultantPackages.add(packageName);
                        }
                    }
                }
            }
            if (containsPackages) {
                // We don't need to care for duplicates here,
                // Path.list does it for us.
                sp.createPathElement().setLocation(baseDir);
            } else {
                log.verbose(baseDir + " doesn\'t contain any packages, dropping it.");
            }
        }
    }

    
    public void execute() throws BuildException {
    	// do it

        List packagesToDoc = new ArrayList();
        Path sourceDirs = new Path(getProject());

        if (sourcePath != null) {
            sourceDirs.addExisting(sourcePath);
        }
        parsePackages(packagesToDoc, sourceDirs);      
        
  		GroovyDocTool htmlTool = new GroovyDocTool(
				new ClasspathResourceManager(), // we're gonna get the default templates out of the dist jar file
				sourcePath.toString(), // sourcepath                     - TODO multiple paths need to be handled here
				new String[] { // top level templates
						"org/codehaus/groovy/tools/groovydoc/gstring-templates/top-level/index.html",
						"org/codehaus/groovy/tools/groovydoc/gstring-templates/top-level/overview-frame.html", // needs all package names
						"org/codehaus/groovy/tools/groovydoc/gstring-templates/top-level/allclasses-frame.html", // needs all packages / class names
						"org/codehaus/groovy/tools/groovydoc/gstring-templates/top-level/overview-summary.html", // needs all packages
						"org/codehaus/groovy/tools/groovydoc/gstring-templates/top-level/stylesheet.css",
				},
				new String[] { // package level templates
						"org/codehaus/groovy/tools/groovydoc/gstring-templates/package-level/package-frame.html", 
						"org/codehaus/groovy/tools/groovydoc/gstring-templates/package-level/package-summary.html"
				},
				new String[] { // class level templates
						"org/codehaus/groovy/tools/groovydoc/gstring-templates/class-level/classDocName.html"
				},
                  links
                );

		try {
			Iterator itr = sourceFilesToDoc.iterator();
			while (itr.hasNext()) {
				htmlTool.add((String) itr.next());
			}
			
			FileOutputTool output = new FileOutputTool();
			htmlTool.renderToOutput(output, destDir.getCanonicalPath()); // TODO push destDir through APIs?
		} catch (Exception e) {
			e.printStackTrace();
		}

    }

    /**
     * Create link to Javadoc/GroovyDoc output at the given URL.
     *
     * @return link argument to configure
     */
    public LinkArgument createLink() {
        LinkArgument la = new LinkArgument();
        links.add(la);
        return la;
    }

    /**
     * Represents a link pair (href, packages).
     */
    public static class LinkArgument {
        private String href;
        private String packages;

        /**
         * Get the packages attribute.
         *
         * @return the packages attribute.
         */
        public String getPackages() {
            return packages;
        }

        /**
         * Set the packages attribute.
         *
         * @param packages the comma separated package prefixs corresponding to this link
         */
        public void setPackages(String packages) {
            this.packages = packages;
        }

        /**
         * Get the href attribute.
         *
         * @return the href attribute.
         */
        public String getHref() {
            return href;
        }

        /**
         * Set the href attribute.
         *
         * @param hr a <code>String</code> value representing the URL to use for this link
         */
        public void setHref(String hr) {
            href = hr;
        }

    }

}
