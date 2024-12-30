package git;

public enum GitObjectType {
  BLOB("blob", 3),
  TREE("tree", 2),
  COMMIT("commit", 1),
  REF_DELTA("ref", 7);

  private final String valueLabel;
  private final int value;

  GitObjectType(final String valueLabel, final int value) {
    this.valueLabel = valueLabel;
    this.value = value;
  }

  public final String getValueLabel() {
    return this.valueLabel;
  }

  public final int getValue() {
    return this.value;
  }

  public static final GitObjectType fromValue(String value) {
    for (GitObjectType gitObjectType : values()) {
      if (gitObjectType.valueLabel.equals(value)) {
        return gitObjectType;
      }
    }

    throw new IllegalArgumentException("Unknown Git object type: " + value);
  }

  public static final GitObjectType fromValue(int value) {
    for (GitObjectType gitObjectType : values()) {
      if (gitObjectType.value == value) {
        return gitObjectType;
      }
    }

    throw new IllegalArgumentException("Unknown Git object type: " + value);
  }
}
