package git;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.Deflater;

public final class GitObjectStorage {
  private GitObjectStorage() {
  }

  public static void writeObjectFile(final byte[] objectData, final String hash) throws IOException {
    final String objectDirectoryName = hash.substring(0, 2);
    final Path objectDirectoryPath = GitArguments.getObjectsDirectoryPath().resolve(objectDirectoryName);
    final Path objectDirectory = Files.createDirectories(objectDirectoryPath);
    final String objectFileName = hash.substring(2);
    final Path objectFilePath = objectDirectory.resolve(objectFileName);
    GitObjectStorage.writeCompressedDataToFile(objectFilePath, objectData);
  }

  private static void writeCompressedDataToFile(final Path filePath, final byte[] data) throws IOException {
    Files.createDirectories(filePath.getParent());

    final Deflater deflater = new Deflater();
    deflater.setInput(data);
    deflater.finish();

    final byte[] outputBuffer = new byte[data.length + 50];
    final int compressedDataLength = deflater.deflate(outputBuffer);

    final byte[] compressedData = new byte[compressedDataLength];
    System.arraycopy(outputBuffer, 0, compressedData, 0, compressedDataLength);

    Files.write(filePath, compressedData);
  }
}
