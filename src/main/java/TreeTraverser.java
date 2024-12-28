import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TreeTraverser {
  private TreeTraverser() {
  }

  public static void traverseTree(Path rootDirectory, Path targetDirectory, String treeSha1Hash)
      throws IOException {
    Files.createDirectories(targetDirectory);

    String directoryName = treeSha1Hash.substring(0, 2);
    String fileName = treeSha1Hash.substring(2);

    InputStream treeInputStream = new FileInputStream(
        rootDirectory.toString() + "/.git/objects/" + directoryName + "/"
            + fileName);
    byte[] treeContents = ObjectInflater.inflateObject(treeInputStream);
    treeInputStream.close();

    ObjectParser treeObject = new ObjectParser(treeContents);
    TreeParser parsedTree = new TreeParser(treeObject);

    for (TreeEntry treeEntry : parsedTree.getEntries()) {
      switch (treeEntry.getMode()) {
        case Constants.BLOB_MODE:
          TreeTraverser.createFile(rootDirectory, targetDirectory, treeEntry);
          break;
        case Constants.TREE_MODE:
          Path subTargetDirectory = targetDirectory.resolve(treeEntry.getName());
          TreeTraverser.traverseTree(rootDirectory, subTargetDirectory,
              Util.bytesToHexString(treeEntry.getHash()));
          break;
        default:
          throw new RuntimeException("Unsupported object mode: " + treeEntry.getMode());
      }
    }
  }

  private static void createFile(Path rootDirectory, Path targetDirectory, TreeEntry treeEntry) throws IOException {
    String blobSha1Hash = Util.bytesToHexString(treeEntry.getHash());
    String blobDirectoryName = blobSha1Hash.substring(0, 2);
    String blobFileName = blobSha1Hash.substring(2);
    InputStream blobInputStream = new FileInputStream(
        rootDirectory.toString() + "/.git/objects/" + blobDirectoryName + "/"
            + blobFileName);
    byte[] blobFileContents = ObjectInflater.inflateObject(blobInputStream);
    blobInputStream.close();

    ObjectParser blobObject = new ObjectParser(blobFileContents);

    final File targetFile = targetDirectory.resolve(treeEntry.getName()).toFile();
    targetFile.createNewFile();
    ByteBuffer fileContentBuffer = blobObject.getContentBuffer();
    byte[] fileContents = new byte[fileContentBuffer.remaining()];
    fileContentBuffer.get(fileContents);

    FileOutputStream fileOutputStream = new FileOutputStream(targetFile);
    fileOutputStream.write(fileContents);
    fileOutputStream.close();
  }
}
