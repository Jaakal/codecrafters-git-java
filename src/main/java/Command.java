public enum Command {
  INIT("init"),
  CAT_FILE("cat-file"),
  HASH_OBJECT("hash-object"),
  LS_TREE("ls-tree"),
  WRITE_TREE("write-tree");

  private final String label;

  Command(String label) {
    this.label = label;
  }

  public static Command fromLabel(String label) {
    for (Command command : values()) {
      if (command.label.equals(label)) {
        return command;
      }
    }

    throw new IllegalArgumentException("Unknown command: " + label);
  }
}
