package com.baulsupp.groovy.groosh;

import java.io.IOException;

import com.baulsupp.process.AppProcess;
import com.baulsupp.process.ProcessFactory;
import com.baulsupp.process.Sink;
import com.baulsupp.process.Source;

public class ShellProcess extends GrooshProcess {
  private AppProcess process = null;
  private String name;
  private String[] args;
  
  public ShellProcess(String name, Object arg1) throws IOException {    
    this.name = name;
    this.args = getArgs(arg1);
    
    process = ProcessFactory.buildProcess(name, args);
  }

  private String[] getArgs(Object arg1) {
    if (arg1 == null)
      return new String[0];
    else if (arg1 instanceof String[])
      return (String[]) arg1;
    else if (arg1 instanceof Object[]) {
      Object[] argsO = (Object[]) arg1;
      String[] argsS = new String[argsO.length];
      for (int i = 0; i < argsO.length; i++) {
        argsS[i] = String.valueOf(argsO[i]);
      }
      return argsS;
    } else if (arg1 instanceof String)
      return new String[] {(String) arg1};
    else 
      throw new IllegalStateException("no support for args of type " + arg1.getClass());
  }
  
  public void waitForExit() throws IOException { 
    process.result();
  }
  
  public void start() throws IOException {
    process.start();
  }

  public Sink getSink() {
    return process.getInput();
  }

  public Source getSource() {
    return process.getOutput();
  }
}
