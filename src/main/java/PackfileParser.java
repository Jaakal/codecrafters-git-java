import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

public class PackfileParser {
  private static int parseHeader(ByteBuffer byteBuffer) throws IOException {
    while (byteBuffer.get() != ASCII.LINE_FEED) {
      // skip NAC
    }
    byteBuffer.getLong();
    return byteBuffer.getInt();
  }

  private static ObjectInfo parseObjectHeader(ByteBuffer byteBuffer) throws IOException {
    // Read first byte
    int firstByte = byteBuffer.get();

    // Extract type and initial size bits
    int type = (firstByte >> 4) & 0x07; // Get bits 4-6 for type
    long size = firstByte & 0x0F; // Get bits 0-3 for initial size

    // Process additional size bytes if MSB is set
    int shift = 4; // Start shifting after our initial 4 bits
    while ((firstByte & 0x80) != 0) { // While MSB (bit 7) is set
      firstByte = byteBuffer.get(); // Read next byte
      size |= (long) (firstByte & 0x7F) << shift; // Add 7 new bits to size
      shift += 7; // Next byte will shift 7 more positions
    }

    return new ObjectInfo(type, size);
  }

  private static long parseOffset(ByteBuffer byteBuffer) {
    long offset = 0;
    int shift = 0;
    while (true) {
      int currentByte = byteBuffer.get() & 0xFF;
      offset |= (long) (currentByte & 0x7F) << shift;
      if ((currentByte & 0x80) == 0)
        break; // MSB not set means end of offset
      shift += 7;
    }
    return offset;
  }

  private static String parseSHA1(ByteBuffer byteBuffer) {
    byte[] sha1 = new byte[20];
    byteBuffer.get(sha1);
    return Util.bytesToHexString(sha1);
  }

  private static void parseObject(ByteBuffer byteBuffer, Path objectsDirectory)
      throws IOException, NoSuchAlgorithmException {
    ObjectInfo objectInfo = PackfileParser.parseObjectHeader(byteBuffer);

    // switch (objectInfo.type) {
    // case 1:
    // ASCII.COMMIT_COUNTER++;
    // break;
    // case 2:
    // ASCII.TREE_COUNTER++;
    // break;
    // case 3:
    // ASCII.BLOB_COUNTER++;
    // break;
    // case 4:
    // ASCII.TAG_COUNTER++;
    // break;
    // case 6:
    // ASCII.OFS_DELTA_COUNTER++;
    // break;
    // case 7:
    // ASCII.REF_DELTA_COUNTER++;
    // break;
    // }

    String baseObjectSha1 = "";
    if (objectInfo.type == 7) {
      baseObjectSha1 = PackfileParser.parseSHA1(byteBuffer);
      // Console.printGreen(baseObjectSha1);
    }

    Inflater inflater = new Inflater();
    inflater.setInput(byteBuffer);

    byte[] objectData = new byte[((int) objectInfo.size)];

    try {
      inflater.inflate(objectData);
    } catch (DataFormatException e) {
      e.printStackTrace();
    }

    // Step 3.3: Handle object based on its type
    switch (objectInfo.type) {
      case 1: { // Commit
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        contentStream.write(objectData);
        Util.writeCommitObjectFile(contentStream, objectsDirectory);
        break;

      }
      case 2: { // Tree
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        contentStream.write(objectData);
        Util.writeTreeObjectFile(contentStream, objectsDirectory);
        break;
      }
      case 3: { // Blob
        ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
        contentStream.write(objectData);
        Util.writeBlobObjectFile(contentStream, objectsDirectory);
        break;
      }
      case 7: { // Reference Delta
        DeltaObjectParser.parseDeltaObject(baseObjectSha1, objectData,
            objectsDirectory);
        break;
      }
      default:
        throw new RuntimeException("Unsupported object type: " + objectInfo.type);
    }
  }

  public static void unpack(byte[] packfileData, Path rootDirectory) throws IOException, NoSuchAlgorithmException {
    Path objectsDirectoryPath = rootDirectory.resolve(".git").resolve("objects");
    Path objectsDirectory = Files.createDirectories(objectsDirectoryPath);

    ByteBuffer byteBuffer = ByteBuffer.wrap(packfileData).asReadOnlyBuffer();
    int objectCount = PackfileParser.parseHeader(byteBuffer);

    // System.out.println("objectCount: " + objectCount);

    // Step 3: Parse each object
    for (int i = 0; i < objectCount; i++) {
      PackfileParser.parseObject(byteBuffer, objectsDirectory);
    }

    // Console.printBlue("Commit: " + ASCII.COMMIT_COUNTER);
    // Console.printBlue("Tree: " + ASCII.TREE_COUNTER);
    // Console.printBlue("Blob: " + ASCII.BLOB_COUNTER);
    // Console.printBlue("Tag: " + ASCII.TAG_COUNTER);
    // Console.printBlue("Ofs: " + ASCII.OFS_DELTA_COUNTER);
    // Console.printBlue("Ref: " + ASCII.REF_DELTA_COUNTER);

    // System.out.println("Packfile parsing completed.");
  }
}
