import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;

public class TreeParser {
  private ObjectParser parsedObject;
  private ArrayList<TreeEntry> entries;

  public TreeParser(ObjectParser parsedObject) {
    this.parsedObject = parsedObject;
    this.entries = new ArrayList<TreeEntry>();
    this.process();
  }

  // Helper method to extract a ByteBuffer slice until a specific delimiter
  private ByteBuffer extractUntil(ByteBuffer buffer, int delimiter) {
    int startPosition = buffer.position();
    while (buffer.get() != delimiter) {
      // Advance until the delimiter is found
    }
    int endPosition = buffer.position() - 1; // Exclude the delimiter
    return buffer.duplicate().position(startPosition).limit(endPosition).slice();
  }

  private void process() {
    ByteBuffer treeContentBuffer = this.parsedObject.getContentBuffer();

    treeContentBuffer.position(0);
    while (treeContentBuffer.hasRemaining()) {
      ByteBuffer modeBuffer = extractUntil(treeContentBuffer, ASCII.BLANK_SPACE);
      byte[] mode = new byte[modeBuffer.remaining()];
      modeBuffer.get(mode);

      ByteBuffer nameBuffer = extractUntil(treeContentBuffer, ASCII.NULL);
      byte[] name = new byte[nameBuffer.remaining()];
      nameBuffer.get(name);

      byte[] hash = new byte[20];
      treeContentBuffer.get(hash); // Directly read the next 20 bytes

      this.entries
          .add(new TreeEntry(new String(mode, StandardCharsets.UTF_8), new String(name, StandardCharsets.UTF_8), hash));
    }
  }

  public void printNames() {
    Iterator<TreeEntry> it = this.entries.iterator();
    while (it.hasNext()) {
      TreeEntry entry = it.next();
      System.out.println(entry.getName());
    }
  }
}
