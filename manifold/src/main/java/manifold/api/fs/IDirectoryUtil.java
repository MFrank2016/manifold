/*
 * Copyright 2014 Guidewire Software, Inc.
 */

package manifold.api.fs;

import java.util.List;
import manifold.api.fs.jar.IJarFileDirectory;
import manifold.internal.host.ManifoldHost;
import manifold.util.DynamicArray;

public class IDirectoryUtil {

  public static DynamicArray<? extends IFile> allContainedFilesExcludingIgnored( IDirectory dir) {
    DynamicArray<IFile> files = new DynamicArray<IFile>();
    allContainedFilesExcludingIgnored(dir, dir, files);
    return files;
  }

  private static void allContainedFilesExcludingIgnored(IDirectory root, IDirectory dir, DynamicArray<IFile> files) {
    files.addAll(dir.listFiles());
    for (IDirectory subDir : dir.listDirs()) {
      if (!ManifoldHost.isPathIgnored( root.relativePath( subDir))) {
        allContainedFilesExcludingIgnored(root, subDir, files);
      }
    }
  }

  public static DynamicArray<String> splitPath(String relativePath) {
    DynamicArray<String> results = new DynamicArray<String>();
    int left = 0;
    int cur = 0;
    int right = relativePath.length();
    boolean consumingPathSeparators = true;
    boolean first = true;
    while (left < right) {
      while (cur < right && isPathSeparator(relativePath.charAt(cur)) == consumingPathSeparators) {
        cur++;
      }
      if (!consumingPathSeparators) {
        if (cur > left) {
          String pathComponent = relativePath.substring(left, cur);
          if (pathComponent.equals(".") && !first) {
            // ignore
          } else if (pathComponent.equals("..") && !first) {
            if (results.isEmpty() || results.get(results.size() - 1).equals("..")) {
              results.add(pathComponent);
            } else {
              results.remove(results.size() - 1);
            }
          } else {
            results.add(pathComponent);
          }
        }
      }
      if (left != cur) {
        left = cur;
        first = false;
      }
      consumingPathSeparators = !consumingPathSeparators;
    }
    return results;
  }

  private static boolean isPathSeparator(char character) {
    return character == '/' || character == '\\';
  }

  public static String relativePath(IResource root, IResource resource) {
    return root.getPath().relativePath(resource.getPath(), "/");
  }

  public static IDirectory dir( IJarFileDirectory root, String relativePath) {
    List<String> pathComponents = IDirectoryUtil.splitPath(relativePath);
    if (pathComponents.size() == 0) {
      return root;
    } else if (pathComponents.size() == 1) {
      return root.getOrCreateDirectory(pathComponents.get(0));
    } else {
      return findParentDirectory(root, pathComponents);
    }
  }

  public static IFile file(IJarFileDirectory root, String path) {
    List<String> pathComponents = IDirectoryUtil.splitPath(path);
    if (pathComponents.size() == 0) {
      throw new IllegalArgumentException("Cannot call file() with an empty path");
    } else if (pathComponents.size() == 1) {
      return root.getOrCreateFile(pathComponents.get(0));
    } else {
      String fileName = pathComponents.remove(pathComponents.size() - 1);
      IDirectory parentDir = findParentDirectory(root, pathComponents);
      return parentDir.file(fileName);
    }
  }

  private static IDirectory findParentDirectory(IDirectory root, List<String> relativePath) {
    IDirectory parent = root;
    for (String pathComponent : relativePath) {
      if (pathComponent.equals(".")) {
        // Do nothing
      } else if (pathComponent.equals("..")) {
        parent = parent.getParent();
      } else {
        parent = parent.dir(pathComponent);
      }
    }
    return parent;
  }

}
