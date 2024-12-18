import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class ObjectParser {
  private final byte[] object;
  private final ByteBuffer objectBuffer;
  private int typeStartIndex;
  private int typeLength;
  private ObjectType type;
  private int contentSize;
  private int contentStartIndex;

  public ObjectParser(byte[] object) {
    this.object = object;
    this.objectBuffer = ByteBuffer.wrap(this.object).asReadOnlyBuffer();
    this.process();
  }

  private void parseObjectType() {
    this.typeStartIndex = 0;
    int typeEndIndex = this.typeStartIndex;
    while (this.object[typeEndIndex] != ASCII.BLANK_SPACE) {
      typeEndIndex++;
    }
    this.typeLength = typeEndIndex - this.typeStartIndex;
    String typeLabel = new String(this.object, typeStartIndex, typeLength, StandardCharsets.UTF_8);
    this.type = ObjectType.fromLabel(typeLabel);
  }

  private void parseObjectSize() {
    int sizeStartIndex = this.typeLength + 1; // After the blank space
    int sizeEndIndex = sizeStartIndex;
    while (this.object[sizeEndIndex] != ASCII.NULL) {
      sizeEndIndex++;
    }
    int sizeLength = sizeEndIndex - sizeStartIndex;
    this.contentStartIndex = sizeStartIndex + sizeLength + 1; // After the null terminator
    this.contentSize = Integer.parseInt(new String(this.object, sizeStartIndex, sizeLength, StandardCharsets.UTF_8),
        10);
  }

  private void process() {
    this.parseObjectType();
    this.parseObjectSize();
  }

  public ByteBuffer getContentBuffer() {
    return this.objectBuffer.position(this.contentStartIndex)
        .limit(this.contentStartIndex + this.contentSize).slice();
  }

  public ObjectType getType() {
    return this.type;
  }

  public String getContentString() {
    return StandardCharsets.UTF_8.decode(this.getContentBuffer()).toString();
  }
}
