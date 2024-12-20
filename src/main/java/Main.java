import java.util.HashMap;

public class Main {
  public static void main(String[] args) {
    final HashMap<Args, ArgumentValue> parsedArgs = Util.parseArgs(args);

    try {
      switch (((ArgumentValue.CommandValue) parsedArgs.get(Args.COMMAND)).value()) {
        case Command.INIT -> {
          Util.initializeRepository();
          break;
        }
        case Command.CAT_FILE -> {
          Util.displayObjectContent(parsedArgs);
          break;
        }
        case Command.HASH_OBJECT -> {
          Util.hashObject(parsedArgs);
          break;
        }
        case Command.LS_TREE -> {
          Util.treeReader(parsedArgs);
          break;
        }
        case Command.WRITE_TREE -> {
          Util.treeGenerator(null);
          break;
        }
        case Command.COMMIT_TREE -> {
          Util.createCommit(parsedArgs);
          break;
        }
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
