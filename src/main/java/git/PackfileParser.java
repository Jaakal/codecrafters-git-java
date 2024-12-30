package git;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DataFormatException;

import constants.ASCII;
import utils.DataUtils;
import utils.InputStreamInflater;

public class PackfileParser {
  private static final int parseHeader(final ByteBuffer packfileResponseBuffer) throws IOException {
    while (packfileResponseBuffer.get() != ASCII.LINE_FEED) {
      // skip NAC
    }
    packfileResponseBuffer.getLong(); // skip magic bytes "PACK" and the packfile version
    return packfileResponseBuffer.getInt(); // object count
  }

  private static final GitObjectInfo parseObjectHeader(final ByteBuffer byteBuffer) throws IOException {
    int firstByte = byteBuffer.get();

    // Extract type and initial size bits
    final int type = (firstByte >> 4) & 0x07; // Get bits 4-6 for type
    long size = firstByte & 0x0F; // Get bits 0-3 for initial size

    // Process additional size bytes if MSB is set
    int shift = 4; // Start shifting after our initial 4 bits
    while ((firstByte & 0x80) != 0) { // While MSB (bit 7) is set
      firstByte = byteBuffer.get();
      size |= (long) (firstByte & 0x7F) << shift; // Add 7 new bits to size
      shift += 7; // Next byte will shift 7 more positions
    }

    return new GitObjectInfo(GitObjectType.fromValue(type), (int) size);
  }

  private static final String parseSha1Hash(final ByteBuffer packfileResponseBuffer) {
    final byte[] sha1Hash = new byte[20];
    packfileResponseBuffer.get(sha1Hash);
    return DataUtils.bytesToHex(sha1Hash);
  }

  private static void parseObject(ByteBuffer packfileResponseBuffer)
      throws IOException, NoSuchAlgorithmException, DataFormatException {
    final GitObjectInfo objectInfo = PackfileParser.parseObjectHeader(packfileResponseBuffer);

    switch (objectInfo.getType()) {
      case GitObjectType.COMMIT,
          GitObjectType.TREE,
          GitObjectType.BLOB -> {
        final byte[] objectData = InputStreamInflater.inflate(packfileResponseBuffer, objectInfo.getSize());
        GitObjectParser.createObjectFile(objectInfo.getType(), objectData);
        break;

      }
      case GitObjectType.REF_DELTA -> {
        final String baseObjectSha1Hash = PackfileParser.parseSha1Hash(packfileResponseBuffer);
        final byte[] objectData = InputStreamInflater.inflate(packfileResponseBuffer, objectInfo.getSize());
        DeltaObjectParser.parseDeltaObject(objectData, baseObjectSha1Hash);
        break;
      }
      default -> throw new RuntimeException("Unsupported object type: " + objectInfo.getType().getValueLabel());
    }
  }

  public static void unpack(final byte[] packfileResponse)
      throws IOException, NoSuchAlgorithmException, DataFormatException {
    final ByteBuffer packfileResponseBuffer = ByteBuffer.wrap(packfileResponse).asReadOnlyBuffer();
    final int objectCount = PackfileParser.parseHeader(packfileResponseBuffer);

    for (int index = 0; index < objectCount; index++) {
      PackfileParser.parseObject(packfileResponseBuffer);
    }
  }
}
