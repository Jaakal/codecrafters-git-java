package git;

import java.util.ArrayList;

public class GitRefs {
  private final ArrayList<GitRef> refs;
  private final ArrayList<String> repositoryCapabilities;

  public GitRefs(final ArrayList<GitRef> gitRefs, final ArrayList<String> repositoryCapabilities) {
    this.refs = gitRefs;
    this.repositoryCapabilities = repositoryCapabilities;
  }

  public final ArrayList<GitRef> getRefs() {
    return this.refs;
  }

  public final ArrayList<String> getCapabilities() {
    return this.repositoryCapabilities;
  }

  public final String getHeadRefHash() {
    for (GitRef ref : this.refs) {
      if (ref.getName().equals("HEAD")) {
        return ref.getSha1Hash();
      }
    }

    return null;
  }
}
