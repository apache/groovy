package com.baulsupp.process;

import java.io.InputStream;
import java.io.OutputStream;

// TODO need an isFinished method
// in general process.waitForExit() knows when input has finished
// but not aware of output has reached destination neccesarily! 
public class Sink {  
  public boolean receivesStream() {
    return false;  
  }
  
  public boolean providesStream() {
    return false;  
  }
  
  public OutputStream getStream() {
    throw new UnsupportedOperationException();  
  }
  
  public void setStream(InputStream channel) {
    throw new UnsupportedOperationException();  
  }
}
