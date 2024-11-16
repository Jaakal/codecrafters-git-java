import java.util.Arrays;

public class ParseObject {
  private final byte[] object;
  private String content;
  private String type;
  private int size;

  public ParseObject(byte[] object) {
    this.object = object;
    this.process();
  }

  private void readObjectType() {
    int index = 0;
    while (this.object[index] != 32) {
      index++;
    }

    this.type = new String(Arrays.copyOfRange(this.object, 0, ++index));
  }

  private void readObjectContent() {
    int index = this.type.length();
    while (this.object[index] != 0) {
      index++;
    }

    byte[] sizeByteArray = Arrays.copyOfRange(this.object, this.type.length(), index);
    this.size = Integer.parseInt(new String(sizeByteArray));
    this.content = new String(Arrays.copyOfRange(this.object, ++index, index + this.size));
  }

  private void process() {
    this.readObjectType();
    this.readObjectContent();
  }

  public String getContent() {
    return this.content;
  }
}
