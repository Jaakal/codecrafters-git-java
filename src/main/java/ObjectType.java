public enum ObjectType {
  BLOB("blob"),
  TREE("tree"),
  COMMIT("commit");

  private final String label;

  ObjectType(String label) {
    this.label = label;
  }

  public static ObjectType fromLabel(String label) {
    for (ObjectType type : values()) {
      if (type.label.equals(label)) {
        return type;
      }
    }

    throw new IllegalArgumentException("No object type with label: " + label);
  }
}
