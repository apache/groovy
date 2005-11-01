package org.codehaus.groovy.runtime;
import groovy.lang.MetaMethod;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import org.codehaus.groovy.runtimemetaclassactionssupport.DefaultGroovyInstanceMethodGenerator;
import org.codehaus.groovy.runtimemetaclassactionssupport.DefaultGroovyStaticMethodGenerator;
import org.codehaus.groovy.runtimemetaclassactionssupport.InstanceMethodGenerator;
import org.codehaus.groovy.runtimemetaclassactionssupport.MethodGenerator;
import org.codehaus.groovy.runtimemetaclassactionssupport.ReflectionMethodGenerator;
import org.codehaus.groovy.runtimemetaclassactionssupport.StaticMethodGenerator;

/*
 * Copyright 2005 John G. Wilson
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
 *
 */

/**
 * @author John Wilson
 *
 */

public class MetaClassActionsGenerator {
  private static final Map actionsInfoMap = new WeakHashMap();
  
  static {
    actionsInfoMap.put(Object.class, new ActionsInfo(Object.class));
  }

  public static MetaClassActions getActions(final Class claz) {
    return getActionsInfo(claz).getActions();
  }
  
  protected static ActionsInfo getActionsInfo(final Class claz) {
  ActionsInfo classInfo = (ActionsInfo)actionsInfoMap.get(claz);
  
    if (classInfo == null) {
    final Class superClass = claz.getSuperclass();
    
      if (superClass != null) {
        getActionsInfo(superClass); // ensure that the superclass information has been created
      }
      
      classInfo = new ActionsInfo(claz);
      actionsInfoMap.put(claz, classInfo);
    }
    
    return classInfo;
  }
}

class ActionsInfo {
  private static Map defaultGroovyMethodsMap = new HashMap();
  static {
    try {
      final Method[] defaultGroovyMethods = Class.forName("org.codehaus.groovy.runtime.DefaultGroovyMethods").getDeclaredMethods();
      
      for (int i = 0; i != defaultGroovyMethods.length; i++) {
        final Method method = defaultGroovyMethods[i];
        
        if ((method.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) == (Modifier.PUBLIC | Modifier.STATIC)) {
        final Class[] parameterTypes = method.getParameterTypes();
        
          if (parameterTypes.length != 0) {
          List methodList = (List)defaultGroovyMethodsMap.get(parameterTypes[0]);
          
            if (methodList == null) {
              methodList = new LinkedList();
              defaultGroovyMethodsMap.put(parameterTypes[0], methodList);
            }

            methodList.add(new DefaultGroovyInstanceMethodGenerator(method));
          }
        }
      }
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  private static Map defaultGroovyStaticMethodsMap = new HashMap();
  static {
    try {
      final Method[] defaultGroovyStaticMethods = Class.forName("org.codehaus.groovy.runtime.DefaultGroovyStaticMethods").getDeclaredMethods();
      
      for (int i = 0; i != defaultGroovyStaticMethods.length; i++) {
        final Method method = defaultGroovyStaticMethods[i];
        
        if ((method.getModifiers() & (Modifier.PUBLIC | Modifier.STATIC)) == (Modifier.PUBLIC | Modifier.STATIC)) {
        final Class[] parameterTypes = method.getParameterTypes();
        
          if (parameterTypes.length != 0) {
          List methodList = (List)defaultGroovyStaticMethodsMap.get(parameterTypes[0]);
          
            if (methodList == null) {
              methodList = new LinkedList();
              defaultGroovyStaticMethodsMap.put(parameterTypes[0], methodList);
            }

            methodList.add(new DefaultGroovyStaticMethodGenerator(method));
          }
        }
      }
    } catch (SecurityException e) {
      e.printStackTrace();
    } catch (ClassNotFoundException e) {
      e.printStackTrace();
    }
  }
  
  private MetaClassActions actions = null;
  private final List generators = new LinkedList();

  public ActionsInfo(final Class claz) {
  final Package pack = claz.getPackage();
  final String packageName = "groovy.runtime.metaclassactions." + ((pack == null) ? "" : pack.getName() + ".");
  
    setUpGenerators(claz);  // Need to do this even if the class is already generated
  
    try {
    final Class actionsClass = Class.forName(packageName + claz.getSimpleName() + "MetaClassActions");
    
      this.actions = (MetaClassActions)actionsClass.newInstance();
    } catch (final Exception e1) {
    final File generatedFile = new File("generated/" + packageName.replace('.', '/'));
      
      generatedFile.mkdirs();
      
      try {
      final Writer out = new FileWriter(new File(generatedFile,  claz.getSimpleName() + "MetaClassActions.java"));
      
        try {
          out.write(generateJavaFile(claz));
        } finally {    
          out.close();
        }
      } catch (final IOException e) {
         e.printStackTrace();
      }
    }  
  }
  
  public MetaClassActions getActions() {
    return this.actions;
  }
  
  private void setUpGenerators(final Class claz) {
  final Method[] methods = claz.getDeclaredMethods();
  final List defaultMethods = (List)defaultGroovyMethodsMap.get(claz);
  final List defaultStaticMethods = (List)defaultGroovyStaticMethodsMap.get(claz);
  
    if (defaultMethods != null) {
      this.generators.addAll(defaultMethods);
    }
    
    if (defaultStaticMethods != null) {
      this.generators.addAll(defaultStaticMethods);
    }
    
    for (int i = 0; i != methods.length; i++) {
    final Method method = methods[i];
    final int methodModifiers = method.getModifiers();
    
      if ((methodModifiers & Modifier.PUBLIC) != 0) {        
        if ((methodModifiers & Modifier.STATIC) != 0) {
          this.generators.add(new StaticMethodGenerator(method));
        } else {
          this.generators.add(new InstanceMethodGenerator(method));
        }
      } else if ((methodModifiers & Modifier.PROTECTED) != 0) {
        this.generators.add(new ReflectionMethodGenerator(method));
      }
    }
    
    if (claz != Object.class) {
    final Class superClass = claz.getSuperclass();
    
      if (superClass != null) {
        this.generators.addAll(MetaClassActionsGenerator.getActionsInfo(superClass).generators);
      }
      
      final Class[] interfaces = claz.getInterfaces();
      
      for (int i = 0; i != interfaces.length; i++) {
        this.generators.addAll(MetaClassActionsGenerator.getActionsInfo(interfaces[i]).generators);
      }
    }
    
    Collections.sort(this.generators, new Comparator() {
      public int compare(final Object lhs, final Object rhs) {
      final int lhsNumberOfParameters = ((MethodGenerator)lhs).getNumberOfParameters();
      final int rhsNumberOfParameters = ((MethodGenerator)rhs).getNumberOfParameters();
      
        if (lhsNumberOfParameters  == rhsNumberOfParameters) {
        final String lhsName = ((MethodGenerator)lhs).getName();
        final String rhsName = ((MethodGenerator)rhs).getName();
        
          return lhsName.compareTo(rhsName);
        } else if (lhsNumberOfParameters  < rhsNumberOfParameters) {
          return -1;
        } else {
          return 1;
        }
      }
    });
    
    if (this.generators.size() > 1) {
    MethodGenerator g1 = (MethodGenerator)this.generators.get(this.generators.size() - 1);
      
      for (int i = this.generators.size() - 2; i > -1 ; i--) {
      final MethodGenerator g2 = (MethodGenerator)this.generators.get(i);
      
        if (g1.isOverloaded(g2)) {
          this.generators.remove(i + 1);
          g1 = g1.processOverloaded(claz, g2);
          this.generators.set(i, g1);
        } else {
          g1 = g2;
        }
      }
    }
  }
  
  private String generateJavaFile(final Class claz) {
  final StringBuffer code = new StringBuffer();
    
    generateStartOfFile(claz, code);
    
    final Iterator iter = this.generators.iterator();
    int methodIndex = 0;
    while (iter.hasNext()) {
    final MethodGenerator gen = (MethodGenerator)iter.next();
    
      code.append(gen.generateDeclaration(methodIndex++));
    }
    
    generateInvokemethod(claz, code);
    
    generateEndOfFile(claz, code);
    
    return code.toString();
  }
  
  private void addMethod(final Set methodSet, final Map methodMap, final List methods) {
  final Iterator methodIterator = methods.iterator();
    
    while (methodIterator.hasNext()) {
      final MetaMethod method = (MetaMethod)methodIterator.next();
      final String name = method.getName();
      
      methodSet.add(name);
      
      List methodList = (List)methodMap.get(name);
      if (methodList == null) {
        methodList = new LinkedList();
        
        methodMap.put(name, methodList);
      }
      methodList.add(method);
    }
  }
  
  private void generateStartOfFile(final Class claz, final StringBuffer code) {
  final Package pack = claz.getPackage();
  final String packageName = (pack == null) ? "" : "." + pack.getName();
  
    code.append("package groovy.runtime.metaclassactions").append(packageName).append(";\n");
    code.append("import java.lang.reflect.Method;\n");
    code.append("import java.security.AccessController;\n");
    code.append("import java.security.PrivilegedAction;\n");
    code.append("public class ").append(claz.getSimpleName()).append("MetaClassActions extends org.codehaus.groovy.runtime.MetaClassActions {\n");
  }
  
  private void generateInvokemethod(final Class claz, final StringBuffer code) {
    
    code.append("public Object doInvokeMethod(final Object target, final String name, final Object[] args) ");
    code.append("throws Exception {\n");
    
    if (this.generators.size() != 0) {
    final List generatorSubset = new LinkedList();
    MethodGenerator firstGenerator = (MethodGenerator)this.generators.get(0);
    int currentArgsLength = firstGenerator.getNumberOfParameters();
    char firstChar = firstGenerator.getName().charAt(0);
    final Iterator iter = this.generators.iterator();
    
      while (iter.hasNext()) {
      final MethodGenerator generator = (MethodGenerator)iter.next();
      
        if (currentArgsLength == generator.getNumberOfParameters()) {
          generatorSubset.add(generator);
        } else {
          generateCalls(claz, code, currentArgsLength, generatorSubset);
          currentArgsLength = generator.getNumberOfParameters();
        }
      }
      
      generateCalls(claz, code, currentArgsLength, generatorSubset);
      
      code.setLength(code.length() - 6);  // chop the trailing " else "     
    }
    
    code.append("\nreturn groovy.lang.MetaClass.NO_METHOD_FOUND;");
    code.append("\n}\n");
  }
  
  private void generateCalls(final Class claz, final StringBuffer code, final int currentArgsLength, final List generatorSubset) {
    if (generatorSubset.size() != 0) {
      code.append("if (args.length == ").append(currentArgsLength).append(") {\n");
      code.append("switch(name.charAt(0)) {");
      
      boolean firstCase = true;
      final Iterator iter1 = generatorSubset.iterator();
      char fc = 0;
      
      while (iter1.hasNext()) {
      final MethodGenerator generator1 = (MethodGenerator)iter1.next();
      final String name = generator1.getName();
      
        if (fc != name.charAt(0)) {
          if (firstCase) {
            firstCase = false;
          } else {
            code.append(" else {\n");
            code.append("return groovy.lang.MetaClass.NO_METHOD_FOUND;\n");
            code.append("}");
          }
          fc = name.charAt(0);
          code.append("\ncase '").append(fc).append("' :\n");
          code.append("if (\"").append(generator1.getName()).append("\".equals(name)) {\n");
          code.append(generator1.generateCall(claz));
          code.append("}");
        } else {
          code.append(" else if (\"").append(generator1.getName()).append("\".equals(name)) {\n");
          code.append(generator1.generateCall(claz));
          code.append("}");
        }
      }
      
      code.append(" else {\n");
      code.append("return groovy.lang.MetaClass.NO_METHOD_FOUND;\n");
      code.append("}\n");
      code.append("default:\n");
      code.append("return groovy.lang.MetaClass.NO_METHOD_FOUND;\n");
      code.append("}\n");
      code.append("} else ");
      
      generatorSubset.clear();
    }
  }
  
  private void generateEndOfFile(final Class claz, final StringBuffer code) {
    code.append("}\n");
  }
}
