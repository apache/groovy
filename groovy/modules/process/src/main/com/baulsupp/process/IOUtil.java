package com.baulsupp.process;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import EDU.oswego.cs.dl.util.concurrent.Executor;
import EDU.oswego.cs.dl.util.concurrent.FutureResult;
import EDU.oswego.cs.dl.util.concurrent.ThreadedExecutor;

public class IOUtil {
  private static final int BUFFER_SIZE = 8192;
  public static final Executor executor = new ThreadedExecutor();

  public static int pump(InputStream is, OutputStream stream) throws IOException {
    int pumped = 0;
    byte[] buffy = new byte[BUFFER_SIZE];
    
    int read = 0;
    while ((read = is.read(buffy)) != -1) {
      stream.write(buffy, 0, read);
      pumped += read;  
    }
    
    return read;
  }

  public static FutureResult pumpAsync(final InputStream is, final OutputStream os) {
    final FutureResult result = new FutureResult();
    try {
      executor.execute(new Runnable() {
        public void run() {
          try {
            int read = IOUtil.pump(is, os);
            result.set(new Integer(read));
          } catch (IOException e) {
            // TODO remove debug once caller handle exception
            System.out.println("ASYNC EXCEPTION (IOU.pumpAsync): " + e);
            result.setException(e);
          } finally {
            try {
              os.close();
              is.close();
            } catch (IOException e) {
              // TODO remove debug once caller handle exception
              System.out.println("ASYNC EXCEPTION (IOU.pumpAsync): " + e);
              result.setException(e);
            }
          }
        }
      });
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    
    return result;
  }
}
