package com.baulsupp.process;

import java.io.IOException;

public abstract class Source {  
  public abstract void connect(Sink sink) throws IOException;
}
