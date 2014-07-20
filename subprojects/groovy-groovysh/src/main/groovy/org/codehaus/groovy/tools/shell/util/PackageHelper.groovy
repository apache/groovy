package org.codehaus.groovy.tools.shell.util

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.prefs.PreferenceChangeEvent
import java.util.prefs.PreferenceChangeListener
import java.util.regex.Matcher
import java.util.regex.Pattern
import java.util.zip.ZipException

/**
 * Helper class that crawls all items of the classpath for packages.
 * Retrieves from those sources the list of subpackages and classes on demand.
 */
class PackageHelper implements PreferenceChangeListener {

    public static final String IMPORT_COMPLETION_PREFERENCE_KEY = "disable-import-completion"
    // Pattern for regular Classnames
    public static final Pattern NAME_PATTERN = java.util.regex.Pattern.compile("^[A-Z][^.\$_]+\$")

    private static final String CLASS_SUFFIX = ".class"

    Map<String, CachedPackage> rootPackages = null
    ClassLoader groovyClassLoader
    protected static final Logger log = Logger.create(PackageHelper)

    PackageHelper(ClassLoader groovyClassLoader) {
        this.groovyClassLoader = groovyClassLoader
        if (! Boolean.valueOf(Preferences.get(IMPORT_COMPLETION_PREFERENCE_KEY))) {
            rootPackages = initializePackages(groovyClassLoader)
        }
        Preferences.addChangeListener(this)
    }

    @Override
    void preferenceChange(PreferenceChangeEvent evt) {
        if (evt.getKey() == IMPORT_COMPLETION_PREFERENCE_KEY) {
            if (Boolean.valueOf(evt.getNewValue())) {
                rootPackages = null
            } else if (rootPackages == null) {
                rootPackages = initializePackages(groovyClassLoader)
            }
        }
    }

    static Map<String, CachedPackage> initializePackages(ClassLoader groovyClassLoader) throws IOException {
        Map<String, CachedPackage> rootPackages = new HashMap()
        Set<URL> urls = new HashSet<URL>()

        // classes in CLASSPATH
        for (ClassLoader loader = groovyClassLoader; loader != null; loader = loader.parent) {
            if (!(loader instanceof URLClassLoader)) {
                log.debug('Ignoring classloader for completion: ' + loader)
                continue
            }

            urls.addAll(((URLClassLoader)loader).URLs)
        }

        // System classes
        Class[] systemClasses = [String, javax.swing.JFrame, GroovyObject] as Class[]
        systemClasses.each { Class systemClass ->
            // normal slash even in Windows
            String classfileName = systemClass.name.replace('.', '/') + ".class"
            URL classURL = systemClass.getResource(classfileName)
            if (classURL == null) {
                // this seems to work on Windows better than the earlier approach
                classURL = Thread.currentThread().getContextClassLoader().getResource(classfileName);
            }
            if (classURL != null) {
                URLConnection uc = classURL.openConnection()
                if (uc instanceof JarURLConnection) {
                    urls.add(((JarURLConnection) uc).getJarFileURL())
                } else {
                    String filepath = classURL.toExternalForm()
                    String rootFolder = filepath.substring(0, filepath.length() - classfileName.length() - 1)
                    urls.add(new URL(rootFolder))
                }
            }
        }

        for (url in urls) {
            Collection<String> packageNames = getPackageNames(url)
            if (packageNames) {
                mergeNewPackages(packageNames, url, rootPackages)
            }
        }
        return rootPackages
    }

    static mergeNewPackages(Collection<String> packageNames, URL url, Map<String, CachedPackage> rootPackages) {
        StringTokenizer tokenizer
        packageNames.each { String packname ->
            tokenizer = new StringTokenizer(packname, '.')
            if (!tokenizer.hasMoreTokens()) {
                return
            }
            String rootname = tokenizer.nextToken()
            CachedPackage cp
            CachedPackage childp
            cp = rootPackages.get(rootname, null) as CachedPackage
            if (cp == null) {
                cp = new CachedPackage(rootname, [url] as Set)
                rootPackages.put(rootname, cp)
            }

            while(tokenizer.hasMoreTokens()) {
                String packbasename = tokenizer.nextToken()
                if (cp.childPackages == null) {
                    // small initial size, to save memory
                    cp.childPackages = new HashMap<String, CachedPackage>(1)
                }
                childp = cp.childPackages.get(packbasename, null) as CachedPackage
                if (childp == null) {
                    // start with small arraylist, to save memory
                    Set<URL> urllist = new HashSet<URL>(1)
                    urllist.add(url)
                    childp = new CachedPackage(packbasename, urllist)
                    cp.childPackages.put(packbasename, childp)
                } else {
                    childp.sources.add(url)
                }
                cp = childp
            }
        }
    }

    /**
     * Returns all packagenames found at URL, accepts jar files and folders
     * @param url
     * @return
     */
    static Collection<String> getPackageNames(URL url) {
        //log.debug(url)
        String path = URLDecoder.decode(url.getFile(), "UTF-8")
        File urlfile = new File(path)
        if (urlfile.isDirectory()) {
            Set<String> packnames = new HashSet<String>()
            collectPackageNamesFromFolderRecursive(urlfile, "", packnames)
            return packnames
        } else {
            if(urlfile.path.endsWith('.jar')) {
                try {
                    JarFile jf = new JarFile(urlfile)
                    return getPackageNamesFromJar(jf)
                } catch(ZipException ze) {
                    if (log.debugEnabled) {
                        ze.printStackTrace();
                    }
                    log.debug("Error opening zipfile : '${url.getFile()}',  ${ze.toString()}");
                } catch (FileNotFoundException fnfe) {
                    log.debug("Error opening file : '${url.getFile()}',  ${fnfe.toString()}");
                }
            }
            return null;
        }
    }

    /**
     * Crawls a folder, iterates over subfolders, looking for class files.
     * @param directory
     * @param prefix
     * @param packnames
     * @return
     */
    static Collection<String> collectPackageNamesFromFolderRecursive(File directory, String prefix, Set<String> packnames) {
        //log.debug(directory)
        File[] files = directory.listFiles();
        boolean packageAdded = false;

        for (int i = 0; (files != null) && (i < files.length); i++) {
            if (files[i].isDirectory()) {
                if (files[i].name.startsWith('.')) {
                    return
                }
                String optionalDot = prefix ? '.' : ''
                collectPackageNamesFromFolderRecursive(files[i], prefix + optionalDot + files[i].getName(), packnames);
            } else if (! packageAdded) {
                if (files[i].getName().endsWith(CLASS_SUFFIX)) {
                    packageAdded = true
                    if (prefix) {
                        packnames.add(prefix);
                    }
                }
            }
        }
    }


    static Collection<String> getPackageNamesFromJar(JarFile jf) {
        Set<String> packnames = new HashSet<String>()
        for (Enumeration e = jf.entries(); e.hasMoreElements();) {
            JarEntry entry = (JarEntry) e.nextElement()

            if (entry == null) {
                continue;
            }

            String name = entry.getName()

            if (!name.endsWith(CLASS_SUFFIX)) {
                // only use class files
                continue;
            }
            // normal slashes also on Windows
            String fullname = name.replace('/', '.').substring(0, name.length() - CLASS_SUFFIX.length())
            // Discard classes in the default package
            if (fullname.lastIndexOf('.') > -1) {
                packnames.add(fullname.substring(0, fullname.lastIndexOf('.')))
            }
        }
        return packnames
    }

    // following block does not work, because URLClassLoader.packages only ever returns SystemPackages
    /*static Collection<String> getPackageNames(URL url) {
        URLClassLoader urlLoader = new URLClassLoader([url] as URL[])
        //log.debug(urlLoader.packages.getClass())

        urlLoader.getPackages().collect {Package pack ->
            pack.name
        }
    }*/

    /**
     * returns the names of Classes and direct subpackages contained in a package
     * @param packagename
     * @return
     */
    Set<String> getContents(String packagename) {
        if (! rootPackages) {
            return null
        }
        if (! packagename) {
            return rootPackages.collect { String key, CachedPackage v -> key } as Set
        }
        if (packagename.endsWith(".*")) {
            packagename = packagename[0..-3]
        }

        StringTokenizer tokenizer = new StringTokenizer(packagename, '.')
        CachedPackage cp = rootPackages.get(tokenizer.nextToken())
        while (cp != null && tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken()
            if (cp.childPackages == null) {
                // no match for taken,no subpackages known
                return null
            }
            cp = cp.childPackages.get(token) as CachedPackage
        }
        if (cp == null) {
            return null
        }
        // TreeSet for ordering
        Set<String> children = new TreeSet()
        if (cp.childPackages) {
            children.addAll(cp.childPackages.collect { String key, CachedPackage v -> key })
        }
        if (cp.checked && !cp.containsClasses) {
            return children
        }

        Set<String> classnames = getClassnames(cp.sources, packagename)

        cp.checked = true
        if (classnames) {
            cp.containsClasses = true
            children.addAll(classnames)
        }
        return children
    }

    /**
     * Copied from JLine 1.0 ClassNameCompletor
     * @param urls
     * @param packagename
     * @return
     */
    static Set<String> getClassnames(Set<URL> urls, String packagename) {
        List<String> classes = new LinkedList<String>()
        // normal slash even in Windows
        String pathname = packagename.replace('.', '/')
        for (Iterator it = urls.iterator(); it.hasNext();) {
            URL url = (URL) it.next();
            File file = new File(URLDecoder.decode(url.getFile(), "UTF-8"));
            if (file == null) {
                continue
            }
            if (file.isDirectory()) {
                File packFolder = new File(file, pathname)
                if (! packFolder.isDirectory()) {
                    continue
                }
                File[] files = packFolder.listFiles();
                for (int i = 0; (files != null) && (i < files.length); i++) {
                    if (files[i].isFile()) {
                        String filename = files[i].getName()
                        if (filename.endsWith(CLASS_SUFFIX)) {
                            String name = filename.substring(0, filename.length() - CLASS_SUFFIX.length())
                            Matcher matcher = NAME_PATTERN.matcher(name)
                            if (!matcher.find()) {
                                continue
                            }
                            classes.add(name);
                        }
                    }
                }
                continue
            }

            if (!file.toString().endsWith (".jar")) {
                continue
            }

            JarFile jf = new JarFile(file);

            for (Enumeration e = jf.entries(); e.hasMoreElements();) {
                JarEntry entry = (JarEntry) e.nextElement();

                if (entry == null) {
                    continue
                }

                String name = entry.getName();

                // only use class files
                if (!name.endsWith(CLASS_SUFFIX))
                {
                    continue
                }
                // normal slash inside jars even on windows
                int lastslash = name.lastIndexOf('/')
                if (lastslash  == -1 || name.substring(0, lastslash) != pathname) {
                    continue
                }
                name = name.substring(lastslash + 1, name.length() - CLASS_SUFFIX.length())
                Matcher matcher = NAME_PATTERN.matcher(name)
                if (!matcher.find()) {
                    continue
                }
                classes.add(name)
            }
        }

        // now filter classes by changing "/" to "." and trimming the
        // trailing ".class"
        Set classNames = new TreeSet();

        for (Iterator i = classes.iterator(); i.hasNext();) {
            String name = (String) i.next()
            classNames.add(name)
        }

        return classNames
    }
}


class CachedPackage {
    String name
    boolean containsClasses
    boolean checked
    Map<String, CachedPackage> childPackages
    Set<URL> sources

    CachedPackage(String name, Set<URL> sources) {
        this.sources = sources
        this.name = name
    }
}
