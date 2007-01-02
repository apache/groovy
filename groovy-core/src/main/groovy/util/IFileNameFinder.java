package groovy.util;

import java.util.List;

public interface IFileNameFinder {
    List getFileNames(String basedir, String pattern);
}
