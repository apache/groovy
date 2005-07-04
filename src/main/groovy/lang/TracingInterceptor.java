package groovy.lang;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;

public class TracingInterceptor implements Interceptor {

    protected Writer writer = new PrintWriter(System.out);
    private int indent = 0;

    public Writer getWriter() {
        return writer;
    }

    public void setWriter(Writer writer) {
        this.writer = writer;
    }

    public Object beforeInvoke(Object object, String methodName, Object[] arguments) {
        write(object, methodName, arguments, "before");
        indent++ ;
        return null;
    }

    public Object afterInvoke(Object object, String methodName, Object[] arguments, Object result) {
        indent--;
        write(object, methodName, arguments, "after ");
        return result;
    }

    public boolean doInvoke() {
        return true;
    }
    private String indent(){
        StringBuffer result = new StringBuffer();
        for (int i=0; i<indent;i++){
            result.append("  ");
        }
        return result.toString();
    }

    protected void write(Object object, String methodName, Object[] arguments, final String origin) {
        try {
            writer.write(indent());
            writer.write(origin);
            writer.write(" ");
            Class theClass = object instanceof Class ? (Class) object: object.getClass();
            writeInfo(theClass, methodName, arguments);
            writer.write("\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeInfo(final Class aClass, String methodName, Object[] arguments) throws IOException {
        writer.write(aClass.getName());
        writer.write(".");
        writer.write(methodName);
        writer.write("(");
        for (int i = 0; i < arguments.length; i++) {
            if (i > 0) writer.write(", ");
            Object argument = arguments[i];
            writer.write(argument.getClass().getName());
        }
        writer.write(")");
    }
}
