import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

public class DeltaObjectParser {
  public static void parseDeltaObject(String baseObjectSha1, byte[] objectData, Path objectsDirectory)
      throws IOException, NoSuchAlgorithmException {
    ByteBuffer byteBuffer = ByteBuffer.wrap(objectData);
    Path baseObjectPath = objectsDirectory.resolve(baseObjectSha1.substring(0, 2)).resolve(baseObjectSha1.substring(2));

    InputStream inputStream = new FileInputStream(baseObjectPath.toString());
    byte[] contents = ObjectInflater.inflateObject(inputStream);
    inputStream.close();

    ObjectParser parsedObject = new ObjectParser(contents);

    ByteBuffer contentBuffer = parsedObject.getContentBuffer();
    byte[] baseObject = new byte[contentBuffer.remaining()];
    contentBuffer.get(baseObject);

    byte[] modifiedObjectData = DeltaObjectParser.applyDelta(baseObject,
        byteBuffer);

    ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
    contentStream.write(modifiedObjectData);
    String hash = Util.writeBlobObjectFile(contentStream, objectsDirectory);
    // Console.printBlue(hash);
  }

  // private static String parseSHA1(ByteBuffer byteBuffer) {
  // byte[] sha1 = new byte[20];
  // byteBuffer.get(sha1);
  // StringBuilder hexString = new StringBuilder();
  // for (byte b : sha1) {
  // hexString.append(String.format("%02x", b));
  // }
  // return hexString.toString();
  // }

  private static long readVariableLengthInteger(ByteBuffer buffer) {
    long value = 0;
    int shift = 0;
    while (true) {
      int currentByte = buffer.get() & 0xFF;
      value |= (long) (currentByte & 0x7F) << shift;
      if ((currentByte & 0x80) == 0)
        break;
      shift += 7;
    }
    return value;
  }

  private static byte[] applyDelta(byte[] baseObject, ByteBuffer deltaBuffer) throws IOException {
    // ByteBuffer deltaBuffer = ByteBuffer.wrap(deltaData);

    // Read source and target sizes
    long sourceSize = DeltaObjectParser.readVariableLengthInteger(deltaBuffer);
    long targetSize = DeltaObjectParser.readVariableLengthInteger(deltaBuffer);

    // Console.printGreen("sourceSize: " + baseObject.length + " - " + sourceSize);

    ByteArrayOutputStream reconstructed = new ByteArrayOutputStream((int) targetSize);

    while (deltaBuffer.hasRemaining()) {
      int instruction = deltaBuffer.get() & 0xFF;

      if ((instruction & 0x80) != 0) { // Copy from base
        int offset = 0;
        int length = 0;

        if ((instruction & 0x01) != 0)
          offset |= deltaBuffer.get() & 0xFF;
        if ((instruction & 0x02) != 0)
          offset |= (deltaBuffer.get() & 0xFF) << 8;
        if ((instruction & 0x04) != 0)
          offset |= (deltaBuffer.get() & 0xFF) << 16;
        if ((instruction & 0x08) != 0)
          offset |= (deltaBuffer.get() & 0xFF) << 24;

        if ((instruction & 0x10) != 0)
          length |= deltaBuffer.get() & 0xFF;
        if ((instruction & 0x20) != 0)
          length |= (deltaBuffer.get() & 0xFF) << 8;
        if ((instruction & 0x40) != 0)
          length |= (deltaBuffer.get() & 0xFF) << 16;

        if (length == 0)
          length = 0x10000; // Default length if not specified

        reconstructed.write(baseObject, offset, length);
      } else { // Insert literal
        reconstructed.write(deltaBuffer.array(), deltaBuffer.position(), instruction);
        deltaBuffer.position(deltaBuffer.position() + instruction);
      }

    }
    byte[] result = reconstructed.toByteArray();
    // Console.printGreen("targetSize: " + result.length + " - " + targetSize);
    return result;
  }
}
