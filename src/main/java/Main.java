import git.GitArguments;
import git.GitCliController;
import git.GitCommand;

public class Main {
  public static void main(String[] arguments) {
    GitArguments.parseCliArguments(arguments);

    try {
      switch (GitArguments.getCommand()) {
        case GitCommand.INIT -> {
          GitCliController.initializeRepository();
          break;
        }
        case GitCommand.CAT_FILE -> {
          GitCliController.displayObjectContent();
          break;
        }
        case GitCommand.HASH_OBJECT -> {
          GitCliController.hashObject();
          break;
        }
        case GitCommand.LS_TREE -> {
          GitCliController.displayTreeObjectContent();
          break;
        }
        case GitCommand.WRITE_TREE -> {
          GitCliController.createTreeObject(GitArguments.getRootDirectoryPath());
          break;
        }
        case GitCommand.COMMIT_TREE -> {
          GitCliController.createCommitObjectBasedOnTree();
          break;
        }
        case GitCommand.CLONE -> {
          GitCliController.cloneRepository();
          break;
        }
      }
    } catch (Exception error) {
      throw new RuntimeException(error);
    }
  }
}
