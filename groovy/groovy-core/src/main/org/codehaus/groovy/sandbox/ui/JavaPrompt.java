package org.codehaus.groovy.sandbox.ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Pure Java prompt using just System.in.
 */
public class JavaPrompt implements Prompt {
  private String prompt;
  private BufferedReader input;
  
  public JavaPrompt() {
    // make a buffered reader to support readLine
    this.input = new BufferedReader(new InputStreamReader(System.in));
  }
  
  public String readLine() throws IOException {
    System.out.print(prompt);
    System.out.flush();
    
    return input.readLine();
  }
  
  public String getPrompt() {
    return prompt;
  }
  
  public void setPrompt(String prompt) {
    this.prompt = prompt;
  }

  public void setCompleter(Completer completer) {
    // completer not supported
  }
  
  public void close() {
    try {
      input.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
