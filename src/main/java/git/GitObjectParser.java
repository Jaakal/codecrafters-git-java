package git;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.security.NoSuchAlgorithmException;

import constants.ASCII;
import utils.DataUtils;
import utils.InputStreamInflater;

public class GitObjectParser {
  private byte[] object;
  private ByteBuffer objectBuffer;
  private GitObjectType objectType;
  private int objectTypeLength;
  private int objectContentStartIndex;
  private int objectContentLength;

  public GitObjectParser() throws IOException {
    this.process(this.generateUrl());
  }

  public GitObjectParser(final String objectSha1Hash) throws IOException {
    this.process(this.generateUrl(objectSha1Hash));
  }

  // Statics
  public static final byte[] buildObjectFile(final GitObjectType objectType, final byte[] content) throws IOException {
    final ByteArrayOutputStream objectStream = new ByteArrayOutputStream();
    objectStream.write((objectType.getValueLabel() + " " + content.length + "\0").getBytes(StandardCharsets.UTF_8));
    objectStream.write(content);
    return objectStream.toByteArray();
  }

  public static final String createObjectFile(final GitObjectType objectType, final byte[] content)
      throws IOException, NoSuchAlgorithmException {
    final byte[] objectData = GitObjectParser.buildObjectFile(objectType, content);
    final byte[] hash = DataUtils.generateSha1Hash(objectData);
    final String hashString = DataUtils.bytesToHex(hash);
    GitObjectStorage.writeObjectFile(objectData, hashString);
    return hashString;
  }

  // Getters
  public final GitObjectType getObjectType() {
    return this.objectType;
  }

  public final ByteBuffer getObjectContentBuffer() {
    return this.objectBuffer.position(this.objectContentStartIndex)
        .limit(this.objectContentStartIndex + this.objectContentLength).slice();
  }

  public final String getObjectContentString() {
    return StandardCharsets.UTF_8.decode(this.getObjectContentBuffer()).toString();
  }

  // Internals
  private void process(final Path objectFileUrl) throws IOException {
    this.findObject(objectFileUrl);
    this.parseObjectType();
    this.parseObjectSize();
  }

  private final Path generateUrl() {
    return GitArguments.getObjectsDirectoryPath()
        .resolve(GitArguments.getCliArgument(GitCliArguments.DIRECTORY))
        .resolve(GitArguments.getCliArgument(GitCliArguments.FILE_NAME));
  }

  private final Path generateUrl(final String objectSha1Hash) {
    final String directoryName = objectSha1Hash.substring(0, 2);
    final String fileName = objectSha1Hash.substring(2);
    return GitArguments.getObjectsDirectoryPath().resolve(directoryName).resolve(fileName);
  }

  private void findObject(final Path objectFileUrlPath) throws IOException {
    final InputStream inputStream = new FileInputStream(objectFileUrlPath.toString());
    this.object = InputStreamInflater.inflate(inputStream);
    this.objectBuffer = ByteBuffer.wrap(this.object).asReadOnlyBuffer();
  }

  private void parseObjectType() {
    final int objectTypeStartIndex = 0;
    int objectTypeEndIndex = objectTypeStartIndex;

    while (this.object[objectTypeEndIndex] != ASCII.BLANK_SPACE) {
      objectTypeEndIndex++;
    }
    this.objectTypeLength = objectTypeEndIndex - objectTypeStartIndex;
    final String typeLabel = new String(this.object, objectTypeStartIndex, this.objectTypeLength,
        StandardCharsets.UTF_8);
    this.objectType = GitObjectType.fromValue(typeLabel);
  }

  private void parseObjectSize() {
    final int objectSizeStartIndex = this.objectTypeLength + 1; // After the blank space
    int objectSizeEndIndex = objectSizeStartIndex;
    while (this.object[objectSizeEndIndex] != ASCII.NULL) {
      objectSizeEndIndex++;
    }
    final int objectSizeLength = objectSizeEndIndex - objectSizeStartIndex;
    this.objectContentStartIndex = objectSizeStartIndex + objectSizeLength + 1; // After the null terminator
    this.objectContentLength = Integer.parseInt(
        new String(this.object, objectSizeStartIndex, objectSizeLength, StandardCharsets.UTF_8),
        10);
  }
}
