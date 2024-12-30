package git;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public final class GitArguments {
  private static final HashMap<GitCliArguments, String> cliArguments = new HashMap<>();
  private static Path rootDirectoryPath = Paths.get("");
  private static Path gitDirectoryPath = rootDirectoryPath.resolve(".git");
  private static Path objectsDirectoryPath = gitDirectoryPath.resolve("objects");
  private static Path refsDirectoryPath = gitDirectoryPath.resolve("refs");
  private static Path headFilePath = gitDirectoryPath.resolve("HEAD");
  private static String headFileContent = "ref: refs/heads/main\n";
  private static GitCommand command;

  private GitArguments() {
  }

  public static final void parseCliArguments(String[] arguments) {
    GitArguments.command = GitCommand.fromValue(arguments[0]);

    switch (GitArguments.command) {
      case GitCommand.HASH_OBJECT -> {
        String fileName = arguments[2];
        GitArguments.cliArguments.put(GitCliArguments.FILE_NAME, fileName);
        break;
      }
      case GitCommand.LS_TREE,
          GitCommand.CAT_FILE -> {
        String directoryName = arguments[2].substring(0, 2);
        String fileName = arguments[2].substring(2);
        GitArguments.cliArguments.put(GitCliArguments.DIRECTORY, directoryName);
        GitArguments.cliArguments.put(GitCliArguments.FILE_NAME, fileName);
        break;
      }
      case GitCommand.COMMIT_TREE -> {
        String treeSha1Hash = arguments[1];
        String parentCommitSha1Hash = arguments[3];
        String commitMessage = arguments[5];
        GitArguments.cliArguments.put(GitCliArguments.TREE_SHA, treeSha1Hash);
        GitArguments.cliArguments.put(GitCliArguments.COMMIT_SHA, parentCommitSha1Hash);
        GitArguments.cliArguments.put(GitCliArguments.COMMIT_MESSAGE, commitMessage);
        break;
      }
      case GitCommand.CLONE -> {
        String repositoryUrl = arguments[1];
        String directoryName = arguments[2];
        GitArguments.cliArguments.put(GitCliArguments.URL, repositoryUrl);
        GitArguments.cliArguments.put(GitCliArguments.DIRECTORY, directoryName);
        GitArguments.setRootDirectoryPathName(GitArguments.getCliArgument(GitCliArguments.DIRECTORY));
        break;
      }
      case GitCommand.INIT,
          GitCommand.WRITE_TREE -> {
        break;
      }
      default -> throw new IllegalArgumentException(
          "Arguments parsing missing for Git command: " + GitArguments.command);
    }
  }

  // Getters
  public static final GitCommand getCommand() {
    return GitArguments.command;
  }

  public static final String getCliArgument(GitCliArguments cliArgument) {
    return GitArguments.cliArguments.get(cliArgument);
  }

  public static final Path getRootDirectoryPath() {
    return GitArguments.rootDirectoryPath;
  }

  public static final Path getGitDirectoryPath() {
    return GitArguments.gitDirectoryPath;
  }

  public static final Path getObjectsDirectoryPath() {
    return GitArguments.objectsDirectoryPath;
  }

  public static final Path getRefsDirectoryPath() {
    return GitArguments.refsDirectoryPath;
  }

  public static final Path getHeadFilePath() {
    return GitArguments.headFilePath;
  }

  public static final String getHeadFileContent() {
    return GitArguments.headFileContent;
  }

  // Setters
  public static void setRootDirectoryPathName(String pathName) {
    GitArguments.rootDirectoryPath = Paths.get(pathName);
    GitArguments.gitDirectoryPath = GitArguments.rootDirectoryPath.resolve(".git");
    GitArguments.objectsDirectoryPath = GitArguments.gitDirectoryPath
        .resolve("objects");
    GitArguments.refsDirectoryPath = GitArguments.gitDirectoryPath
        .resolve("refs");
    GitArguments.headFilePath = GitArguments.gitDirectoryPath.resolve("HEAD");
  }

  public static void setHeadFileContent(String headFileContent) {
    GitArguments.headFileContent = headFileContent;
  }
}
