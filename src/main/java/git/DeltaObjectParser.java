package git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;

public class DeltaObjectParser {
  public static void parseDeltaObject(final byte[] deltaObjectData, final String baseObjectSha1Hash)
      throws IOException, NoSuchAlgorithmException {
    final ByteBuffer deltaObjectDataBuffer = ByteBuffer.wrap(deltaObjectData);

    final GitObjectParser parsedObject = new GitObjectParser(baseObjectSha1Hash);
    final ByteBuffer baseObjectContentBuffer = parsedObject.getObjectContentBuffer();
    final byte[] baseObjectContentData = new byte[baseObjectContentBuffer.remaining()];
    baseObjectContentBuffer.get(baseObjectContentData);

    final byte[] modifiedBaseObjectData = DeltaObjectParser.applyDelta(baseObjectContentData,
        deltaObjectDataBuffer);

    final ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
    contentStream.write(modifiedBaseObjectData);
    GitObjectParser.createObjectFile(GitObjectType.BLOB, contentStream.toByteArray());
  }

  private static final long readVariableLengthInteger(final ByteBuffer deltaObjectDataBuffer) {
    long value = 0;
    int shift = 0;
    while (true) {
      int currentByte = deltaObjectDataBuffer.get() & 0xFF;
      value |= (long) (currentByte & 0x7F) << shift;
      if ((currentByte & 0x80) == 0)
        break;
      shift += 7;
    }
    return value;
  }

  private static final byte[] applyDelta(byte[] baseObjectContentData, ByteBuffer deltaObjectDataBuffer)
      throws IOException {
    DeltaObjectParser.readVariableLengthInteger(deltaObjectDataBuffer); // skip source size
    final long targetSize = DeltaObjectParser.readVariableLengthInteger(deltaObjectDataBuffer);

    final ByteArrayOutputStream modifiedBaseObjectDataStream = new ByteArrayOutputStream((int) targetSize);

    while (deltaObjectDataBuffer.hasRemaining()) {
      final int instruction = deltaObjectDataBuffer.get() & 0xFF;

      if ((instruction & 0x80) != 0) { // Copy from base
        int offset = 0;
        int length = 0;

        if ((instruction & 0x01) != 0)
          offset |= deltaObjectDataBuffer.get() & 0xFF;
        if ((instruction & 0x02) != 0)
          offset |= (deltaObjectDataBuffer.get() & 0xFF) << 8;
        if ((instruction & 0x04) != 0)
          offset |= (deltaObjectDataBuffer.get() & 0xFF) << 16;
        if ((instruction & 0x08) != 0)
          offset |= (deltaObjectDataBuffer.get() & 0xFF) << 24;

        if ((instruction & 0x10) != 0)
          length |= deltaObjectDataBuffer.get() & 0xFF;
        if ((instruction & 0x20) != 0)
          length |= (deltaObjectDataBuffer.get() & 0xFF) << 8;
        if ((instruction & 0x40) != 0)
          length |= (deltaObjectDataBuffer.get() & 0xFF) << 16;

        modifiedBaseObjectDataStream.write(baseObjectContentData, offset, length);
      } else { // Insert literal
        modifiedBaseObjectDataStream.write(deltaObjectDataBuffer.array(), deltaObjectDataBuffer.position(),
            instruction);
        deltaObjectDataBuffer.position(deltaObjectDataBuffer.position() + instruction);
      }

    }

    return modifiedBaseObjectDataStream.toByteArray();
  }
}
