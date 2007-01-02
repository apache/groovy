package org.codehaus.groovy.sandbox.ui;

import java.io.IOException;

public interface Prompt {
  String readLine() throws IOException;
  
  void setCompleter(Completer completer);
  
  void setPrompt(String prompt);
  
  void close();
}
