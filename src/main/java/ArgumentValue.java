public sealed interface ArgumentValue {
  public static record StringValue(String value) implements ArgumentValue {
  }

  public static record CommandValue(Command value) implements ArgumentValue {
  }
}