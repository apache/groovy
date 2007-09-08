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
package org.codehaus.groovy.syntax;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.SourceUnit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A common base class of AST helper methods which can be shared across the classic and new parsers
 *
 * @author Jochen Theodorou
 * @author James Strachan
 * @author Bob McWhirter
 * @author Sam Pullara
 * @author Chris Poirier
 * @version $Revision$
 */
public class ASTHelper {

    /** The SourceUnit controlling us */
    private SourceUnit controller;

    /** Our ClassLoader, which provides information on external types */
    private ClassLoader classLoader;

    /** Our imports, simple name => fully qualified name */
    protected Map imports;
    /** Our explicit static imports, simple name => fully qualified name */
    protected Map staticImports;
    /** Our implicit static imports */
    protected List staticDotImports;
    protected ModuleNode output;

    /** The package name in which the module sits */
    private String packageName;   //

    // TODO should this really be static???
    protected static Map resolutions = new HashMap();  // cleared on build(), to be safe

//    private static String NOT_RESOLVED = new String();

    /** temporarily store the class names that the current modulenode contains */
    private final List newClasses = new ArrayList();

    public ASTHelper(SourceUnit controller, ClassLoader classLoader) {
        this();
        this.controller = controller;
        this.classLoader = classLoader;
    }

    public ASTHelper() {
        imports = new HashMap();
        staticImports = new HashMap();
        staticDotImports = new ArrayList();
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
        if (packageName!=null && packageName.length()>0){
            packageName+='.';
        }
        output.setPackageName(packageName);
    }


    /**
     * Returns our class loader (as supplied on construction).
     */
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public SourceUnit getController() {
        return controller;
    }

    public void setController(SourceUnit controller) {
        this.controller = controller;
    }
    
    /**
     * Returns a fully qualified name for any given potential type
     * name.  Returns null if no qualified name could be determined.
     */
/*    protected String resolveName(String name, boolean safe) {
        //
        // Use our cache of resolutions, if possible

        String resolution = (String) resolutions.get(name);
        if (NOT_RESOLVED.equals(resolution)) {
            return (safe ? name : null);
        }
        else if (resolution != null) {
            return (String) resolution;
        }

        try {
            getClassLoader().loadClass(name);
            resolutions.put(name,name);
            return name;
        } catch (ClassNotFoundException cnfe){
            if (cnfe.getCause() instanceof MultipleCompilationErrorsException) {
                MultipleCompilationErrorsException mcee = (MultipleCompilationErrorsException) cnfe.getCause();
                controller.getErrorCollector().addCollectorContents(mcee.getErrorCollector());
                resolutions.put(name,name);
                return name;
            }
        } catch (NoClassDefFoundError ncdfe) {
            //fall through
        }

        do {
            //
            // If the type name contains a ".", it's probably fully
            // qualified, and we don't take it to verification here.

            if (name.indexOf(".") >= 0) {
                resolution = name;
                break;                                            // <<< FLOW CONTROL <<<<<<<<<
            }


            //
            // Otherwise, we'll need the scalar type for checking, and
            // the postfix for reassembly.

            String scalar = name, postfix = "";
            while (scalar.endsWith("[]")) {
                scalar = scalar.substring(0, scalar.length() - 2);
                postfix += "[]";
            }


            //
            // Primitive types are all valid...

            if (Types.ofType(Types.lookupKeyword(scalar), Types.PRIMITIVE_TYPE)) {
                resolution = name;
                break;                                            // <<< FLOW CONTROL <<<<<<<<<
            }


            //
            // Next, check our imports and return the qualified name,
            // if available.

            if (this.imports.containsKey(scalar)) {
                resolution = ((String) this.imports.get(scalar)) + postfix;
                break;                                            // <<< FLOW CONTROL <<<<<<<<<
            }


            //
            // Next, see if our class loader can resolve it in the current package.

            if (packageName != null && packageName.length() > 0) {
                try {
                    getClassLoader().loadClass(dot(packageName, scalar));
                    resolution = dot(packageName, name);

                    break;                                        // <<< FLOW CONTROL <<<<<<<<<
                } catch (ClassNotFoundException cnfe){
                    if (cnfe.getCause() instanceof CompilationFailedException) {
                        resolution = dot(packageName, name);
                        break;
                    }
                } catch (NoClassDefFoundError ncdfe) {
                    //fall through
                }
            }

            // search the package imports path
            List packageImports = output.getImportPackages();
            for (int i = 0; i < packageImports.size(); i++) {
                String pack = (String) packageImports.get(i);
                String clsName = pack + name;
                try {
                    getClassLoader().loadClass(clsName);
                    resolution = clsName;
                    break;
                } catch (ClassNotFoundException cnfe){
                    if (cnfe.getCause() instanceof CompilationFailedException) {
                        resolution = clsName;
                        break;
                    }
                } catch (NoClassDefFoundError ncdfe) {
                    //fall through
                }
            }
            if (resolution != null) {
                break;
            }

            //
            // Last chance, check the default imports.

            for (int i = 0; i < DEFAULT_IMPORTS.length; i++) {
                String qualified = DEFAULT_IMPORTS[i] + scalar;
                try {
                    getClassLoader().loadClass(qualified);

                    resolution = qualified + postfix;
                    break;                                        // <<< FLOW CONTROL <<<<<<<<<
                } catch (ClassNotFoundException cnfe){
                    if (cnfe.getCause() instanceof CompilationFailedException) {
                        resolution = qualified + postfix;
                        break;
                    }
                } catch (NoClassDefFoundError ncdfee) {
                    // fall through
                }
            }

        }
        while (false);


        //
        // Cache the solution and return it

        if (resolution == null) {
            resolutions.put(name, NOT_RESOLVED);
            return (safe ? name : null);
        }
        else {
            resolutions.put(name, resolution);
            return resolution;
        }
    }
*/
    
    /**
     * Returns two names joined by a dot.  If the base name is
     * empty, returns the name unchanged.
     */
    public static String dot(String base, String name) {
        if (base != null && base.length() > 0) {
            return base + "." + name;
        }

        return name;
    }

    protected void makeModule() {
        this.newClasses.clear();
        this.output = new ModuleNode(controller);
        resolutions.clear();
    }

    /**
     * A synonym for <code>dot( base, "" )</code>.
     */
    protected String dot(String base) {
        return dot(base, "");
    }

    /*protected String resolveNewClassOrName(String name, boolean safe) {
        if (this.newClasses.contains(name)) {
            return dot(packageName, name);
        }
        else {
            return resolveName(name, safe);
        }
    }*/

    protected void addNewClassName(String name) {
        this.newClasses.add(name);
    }

    protected void importClass(ClassNode type, String name, String as) {
        if (as == null) as=name;

        output.addImport(as, type); 
        imports.put(as, type);
    }

    protected void staticImportMethodOrField(ClassNode type, String name, String alias) {
        if (alias == null) alias = name;
        output.addStaticMethodOrField(type, name, alias);
    }

    protected void staticImportClassWithStar(ClassNode type, String importClass) {
        // keep track of the fact that it was a static import
        output.addStaticImportClass(importClass, type);
    }

    protected void importPackageWithStar(String importPackage) {
        String[] classes = output.addImportPackage( dot(importPackage) );
        for( int i = 0; i < classes.length; i++ ) {
            imports.put( classes[i], dot(importPackage, classes[i]) );
        }
    }
}
