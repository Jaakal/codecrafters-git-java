package git;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import constants.ASCII;

public class TreeParser {
  private final GitObjectParser parsedObject;
  private final ArrayList<TreeEntry> entries;

  public TreeParser(final GitObjectParser parsedObject) {
    this.parsedObject = parsedObject;
    this.entries = new ArrayList<TreeEntry>();
    this.process();
  }

  private static final byte[] extractUntil(ByteBuffer byteBuffer, int delimiter) {
    final int startPosition = byteBuffer.position();
    while (byteBuffer.get() != delimiter) {
    }
    final int endPosition = byteBuffer.position() - 1; // Exclude the delimiter
    final byte[] data = new byte[endPosition - startPosition];
    byteBuffer.position(startPosition);
    byteBuffer.get(data);
    byteBuffer.position(byteBuffer.position() + 1);
    return data;
  }

  private void process() {
    final ByteBuffer treeContentBuffer = this.parsedObject.getObjectContentBuffer();

    while (treeContentBuffer.hasRemaining()) {
      final byte[] mode = TreeParser.extractUntil(treeContentBuffer, ASCII.BLANK_SPACE);
      final byte[] name = extractUntil(treeContentBuffer, ASCII.NULL);
      final byte[] hash = new byte[20];
      treeContentBuffer.get(hash);
      this.entries
          .add(new TreeEntry(GitMode.fromValue(new String(mode, StandardCharsets.UTF_8)),
              new String(name, StandardCharsets.UTF_8), hash));
    }
  }

  public final ArrayList<TreeEntry> getEntries() {
    return this.entries;
  }

  public void printNames() {
    for (final TreeEntry treeEntry : this.entries) {
      System.out.println(treeEntry.getName());
    }
  }
}
