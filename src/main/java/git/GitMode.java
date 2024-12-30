package git;

public enum GitMode {
  BLOB("100644"),
  TREE("40000");

  private final String value;

  GitMode(String value) {
    this.value = value;
  }

  public static GitMode fromValue(String value) {
    for (GitMode gitMode : values()) {
      if (gitMode.value.equals(value)) {
        return gitMode;
      }
    }

    throw new IllegalArgumentException("Unknown Git mode: " + value);
  }

  public String getValue() {
    return this.value;
  }

  @Override
  public String toString() {
    return this.value;
  }
}