package com.baulsupp.process;

import java.io.IOException;

// TODO how does the completion of the input/output i.e. to a file get monitored?
public interface AppProcess {
  Sink getInput();
  Source getOutput();
  Source getError();
  
  void start() throws IOException;
  int result();
  boolean hadError();
}
