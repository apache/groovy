package com.baulsupp.groovy.groosh;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;


import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.ThreadedExecutor;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;

public class Groosh extends GroovyObjectSupport {
  private Executor executor = new ThreadedExecutor();
  private static Map registeredProcesses = new HashMap();
  
  static {
    registerProcess("groovy", StreamClosureProcess.class);
    registerProcess("each_line", LineClosureProcess.class);
    registerProcess("grid", GridClosureProcess.class);
  }

  public static void registerProcess(String name, Class clazz) {
    registeredProcesses.put(name, clazz);
  }

  public Object invokeMethod(String name, Object args) { 
    GrooshProcess process;
    try {
      if (registeredProcesses.containsKey(name)) {
        process = createProcess((Class) registeredProcesses.get(name), args);  
      } else {
        process = new ShellProcess(name, args);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    
    return process;
  }

  private GrooshProcess createProcess(Class class1, Object arg) {
    GrooshProcess process = null;
    
    try {
      Constructor c = class1.getConstructor(new Class[] {Closure.class});
      process = (GrooshProcess) c.newInstance((Object[]) arg);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    
    return process;
  }

  public void execute(Runnable r) {
    try {
      executor.execute(r);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
  }
}
