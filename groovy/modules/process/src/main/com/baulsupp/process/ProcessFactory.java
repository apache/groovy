package com.baulsupp.process;

import java.io.IOException;

public class ProcessFactory {
  public static AppProcess buildProcessPipeline(String commandLine) {
    throw new UnsupportedOperationException();
  }
  
  public static AppProcess buildProcess(String command, String[] args) throws IOException {
    return JavaProcess.createProcess(command, args);
  }
}
