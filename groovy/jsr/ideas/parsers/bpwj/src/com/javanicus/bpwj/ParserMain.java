package com.javanicus.bpwj;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;

public class ParserMain {
    public static void main(String[] args) {
        // -- main bit
        List errorList = new ArrayList();
        if (args.length > 0) {
            try {
                File srcFile = new File(args[0]);
                BufferedReader src = new BufferedReader(new FileReader(srcFile));
                char[] srcChars = new char[(int)srcFile.length()];
                src.read(srcChars);
                String srcText = new String(srcChars);
                ParserFacade parser = new ParserFacade(new JavaParser().start());

                parser.parseWithoutSwallowingExceptions(srcText);

            } catch (IOException e) {
                errorList.add("error: cannot read: " + args[0]);
            } catch (TrackSequenceException e) {
                List anError = new ArrayList();
                anError.add(args[0] + ": " + e.getExpected() + " expected"); // todo - line numbers
                anError.add("after: " + e.getAfter());
                anError.add(e.getFound()); // todo - better context of error
                anError.add("^");
                errorList.add(anError);
            }

        }

        // latent error reporting...
        if (errorList.size() > 0) {
            Iterator errors = errorList.iterator();
            while (errors.hasNext()) {
                Object err = errors.next();
                if (err instanceof List) {
                    List errList = (List)err;
                    Iterator subErrors = errList.iterator();
                    while (subErrors.hasNext()) {
                        System.err.println(subErrors.next());
                    }
                    System.err.println(" ");
                } else {
                    System.err.println(err);
                }
            }
            int numberOfErrors = errorList.size();
            if (numberOfErrors == 1) {
                System.err.println(errorList.size() + " error");
            } else if (numberOfErrors > 1) {
                System.err.println(errorList.size() + " errors");
            }
        }
    }
}
