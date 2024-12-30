package git;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;

import constants.ASCII;

public class GitRepositoryMaker {
  private GitRepositoryMaker() {
  }

  public static void setupRepository(final String headRefSha1Hash)
      throws IOException, DataFormatException {
    GitObjectParser commitObject = new GitObjectParser(headRefSha1Hash);
    ByteBuffer commitBuffer = commitObject.getObjectContentBuffer();

    int spaceCharacterIndex = 0;
    while (commitBuffer.get(spaceCharacterIndex) != ASCII.BLANK_SPACE) {
      spaceCharacterIndex++;

    }

    int lineFeedCharacterIndex = spaceCharacterIndex;
    while (commitBuffer.get(lineFeedCharacterIndex) != ASCII.LINE_FEED) {
      lineFeedCharacterIndex++;
    }
    final byte[] treeEntryLine = new byte[lineFeedCharacterIndex - spaceCharacterIndex - 1]; // skip line feed character
    commitBuffer.get(spaceCharacterIndex + 1, treeEntryLine); // start after space character
    final String treeSha1Hash = new String(treeEntryLine);

    TreeTraverser.traverseTree(GitArguments.getRootDirectoryPath(), treeSha1Hash);
  }
}
