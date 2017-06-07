package manifold.api.sourceprod;

import javax.tools.JavaFileObject;
import manifold.internal.javac.IssueReporter;
import manifold.internal.javac.TypeProcessor;

/**
 */
public interface ITypeProcessor
{
  void process( String fqn, TypeProcessor typeProcessor, IssueReporter<JavaFileObject> issueReporter );
}
