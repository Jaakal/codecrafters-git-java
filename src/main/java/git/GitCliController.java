package git;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.DataFormatException;

import utils.DataUtils;

public final class GitCliController {
  private GitCliController() {
  }

  public static void initializeRepository() throws IOException {
    Files.createDirectories(GitArguments.getObjectsDirectoryPath());
    Files.createDirectories(GitArguments.getRefsDirectoryPath());
    Files.writeString(GitArguments.getHeadFilePath(),
        GitArguments.getHeadFileContent());
  }

  public static void displayObjectContent()
      throws IOException, DataFormatException {
    final GitObjectParser parsedObject = new GitObjectParser();
    System.out.print(parsedObject.getObjectContentString());
  }

  public static void hashObject()
      throws IOException, NoSuchAlgorithmException {
    final byte[] content = Files
        .readAllBytes(Paths.get(GitArguments.getCliArgument(GitCliArguments.FILE_NAME)));
    final byte[] blob = GitObjectParser.buildObjectFile(GitObjectType.BLOB, content);
    final byte[] sha1HashBytes = DataUtils.generateSha1Hash(blob);
    final String sha1Hash = DataUtils.bytesToHex(sha1HashBytes);
    System.out.println(sha1Hash);
    GitObjectStorage.writeObjectFile(blob, sha1Hash);
  }

  public static void displayTreeObjectContent() throws IOException {
    final GitObjectParser parsedObject = new GitObjectParser();
    final TreeParser parsedTree = new TreeParser(parsedObject);
    parsedTree.printNames();
  }

  public static final byte[] createTreeObject(final Path currentDirectoryPath)
      throws IOException, NoSuchAlgorithmException {
    final boolean isTopLevel = currentDirectoryPath.equals(GitArguments.getRootDirectoryPath());
    final List<TreeEntry> treeEntries = new ArrayList<>();

    try (final DirectoryStream<Path> directoryStream = Files.newDirectoryStream(currentDirectoryPath)) {
      for (final Path directoryEntryPath : directoryStream) {
        if (Files.isRegularFile(directoryEntryPath)) {
          final byte[] content = Files.readAllBytes(directoryEntryPath);
          final byte[] blob = GitObjectParser.buildObjectFile(GitObjectType.BLOB, content);
          final byte[] sha1Hash = DataUtils.generateSha1Hash(blob);
          treeEntries.add(new TreeEntry(GitMode.BLOB, directoryEntryPath.getFileName().toString(), sha1Hash));
        } else if (Files.isDirectory(directoryEntryPath)
            && !directoryEntryPath.getFileName().toString().equals(".git")) {
          final byte[] sha1Hash = createTreeObject(directoryEntryPath);
          treeEntries.add(new TreeEntry(GitMode.TREE, directoryEntryPath.getFileName().toString(), sha1Hash));
        }
      }
    }

    treeEntries.sort(Comparator.comparing(TreeEntry::getName));

    final ByteArrayOutputStream contentStream = new ByteArrayOutputStream();
    for (final TreeEntry entry : treeEntries) {
      contentStream.write(entry.serialize());
    }

    final byte[] tree = GitObjectParser.buildObjectFile(GitObjectType.TREE, contentStream.toByteArray());
    final byte[] sha1HashBytes = DataUtils.generateSha1Hash(tree);

    if (isTopLevel) {
      String sha1Hash = DataUtils.bytesToHex(sha1HashBytes);
      GitObjectStorage.writeObjectFile(tree, sha1Hash);
      System.out.println(sha1Hash);
    }

    return sha1HashBytes;
  }

  public static void createCommitObjectBasedOnTree()
      throws NoSuchAlgorithmException, IOException {
    final byte[] commitContent = GitCommit.createCommitContent(
        GitArguments.getCliArgument(GitCliArguments.TREE_SHA),
        GitArguments.getCliArgument(GitCliArguments.COMMIT_SHA), "Jaakal",
        "jaak.kivinukk@gmail.com", DataUtils.generateTimestamp(), "Jaakal", "jaak.kivinukk@gmail.com",
        DataUtils.generateTimestamp(),
        GitArguments.getCliArgument(GitCliArguments.COMMIT_MESSAGE));
    final String sha1hash = GitObjectParser.createObjectFile(GitObjectType.COMMIT, commitContent);
    System.out.println(sha1hash);
  }

  public static void cloneRepository()
      throws IOException, InterruptedException, NoSuchAlgorithmException, DataFormatException {
    final byte[] gitRefsResponse = GitHttpClient.fetchReferences();
    final GitRefs gitRefs = GitProtocolParser.parseRefs(gitRefsResponse);
    final byte[] packfileResponse = GitHttpClient.fetchPackfile(gitRefs);

    GitArguments.setHeadFileContent("ref: refs/heads/master\n");
    GitCliController.initializeRepository();

    PackfileParser.unpack(packfileResponse);

    GitRepositoryMaker.setupRepository(gitRefs.getHeadRefHash());
  }
}
