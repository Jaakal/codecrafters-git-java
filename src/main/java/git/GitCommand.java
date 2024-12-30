package git;

public enum GitCommand {
  INIT("init"),
  CAT_FILE("cat-file"),
  HASH_OBJECT("hash-object"),
  LS_TREE("ls-tree"),
  WRITE_TREE("write-tree"),
  COMMIT_TREE("commit-tree"),
  CLONE("clone");

  private final String value;

  GitCommand(String value) {
    this.value = value;
  }

  public static GitCommand fromValue(String value) {
    for (GitCommand gitCommand : values()) {
      if (gitCommand.value.equals(value)) {
        return gitCommand;
      }
    }

    throw new IllegalArgumentException("Unknown Git command: " + value);
  }
}
