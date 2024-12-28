import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.zip.DataFormatException;

public class GitRepositoryMaker {
  private GitRepositoryMaker() {
  }

  public static void setupRepository(String headRefSha1Hash, Path rootDirectory)
      throws IOException, DataFormatException {
    String directoryName = headRefSha1Hash.substring(0, 2);
    String fileName = headRefSha1Hash.substring(2);

    InputStream commitInputStream = new FileInputStream(
        rootDirectory.toString() + "/.git/objects/" + directoryName + "/"
            + fileName);
    byte[] commitContents = ObjectInflater.inflateObject(commitInputStream);
    commitInputStream.close();

    ObjectParser commitObject = new ObjectParser(commitContents);

    ByteBuffer commitBuffer = commitObject.getContentBuffer();

    int spaceCharacterIndex = 0;
    while (commitBuffer.get(spaceCharacterIndex) != ASCII.BLANK_SPACE) {
      spaceCharacterIndex++;

    }

    int lineFeedCharacterIndex = spaceCharacterIndex;
    while (commitBuffer.get(lineFeedCharacterIndex) != ASCII.LINE_FEED) {
      lineFeedCharacterIndex++;
    }
    byte[] treeEntryLine = new byte[lineFeedCharacterIndex - spaceCharacterIndex - 1]; // skip line feed character
    commitBuffer.get(spaceCharacterIndex + 1, treeEntryLine); // start after space character
    String treeSha1Hash = new String(treeEntryLine);

    TreeTraverser.traverseTree(rootDirectory, rootDirectory, treeSha1Hash);

    // directoryName = treeSha1Hash.substring(0, 2);
    // fileName = treeSha1Hash.substring(2);

    // InputStream treeInputStream = new FileInputStream(
    // "challenge/.git/objects/" + directoryName + "/"
    // + fileName);
    // byte[] treeContents = ObjectInflater.inflateObject(treeInputStream);
    // treeInputStream.close();

    // ObjectParser treeObject = new ObjectParser(treeContents);
    // TreeParser parsedTree = new TreeParser(treeObject);

    // System.out.println("=============================");
    // System.out.println(parsedTree.toString());
    // System.out.println(new String(treeContents));
    // System.out.println(treeObject.getContentString());
    // System.out.println("=============================");
  }
}
