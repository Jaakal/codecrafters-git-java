package git;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import utils.DataUtils;

public final class TreeTraverser {
  private TreeTraverser() {
  }

  public static void traverseTree(Path targetDirectory, String treeSha1Hash)
      throws IOException {
    Files.createDirectories(targetDirectory);

    final GitObjectParser treeObject = new GitObjectParser(treeSha1Hash);
    final TreeParser parsedTree = new TreeParser(treeObject);

    for (final TreeEntry treeEntry : parsedTree.getEntries()) {
      switch (treeEntry.getMode()) {
        case GitMode.BLOB:
          TreeTraverser.createFile(targetDirectory, treeEntry);
          break;
        case GitMode.TREE:
          final Path subTargetDirectory = targetDirectory.resolve(treeEntry.getName());
          TreeTraverser.traverseTree(subTargetDirectory,
              DataUtils.bytesToHex(treeEntry.getHash()));
          break;
        default:
          throw new RuntimeException("Unsupported object mode: " + treeEntry.getMode());
      }
    }
  }

  private static void createFile(Path targetDirectory, TreeEntry treeEntry) throws IOException {
    final GitObjectParser blobObject = new GitObjectParser(DataUtils.bytesToHex(treeEntry.getHash()));

    final File targetFile = targetDirectory.resolve(treeEntry.getName()).toFile();
    targetFile.createNewFile();
    final ByteBuffer fileContentBuffer = blobObject.getObjectContentBuffer();
    final byte[] fileContents = new byte[fileContentBuffer.remaining()];
    fileContentBuffer.get(fileContents);

    final FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
    fileOutputStream.write(fileContents);
    fileOutputStream.close();
  }
}
