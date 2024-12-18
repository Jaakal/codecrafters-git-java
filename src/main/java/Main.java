import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.zip.Inflater;

public class Main {
  public static void main(String[] args) {
    final String command = args[0];

    switch (command) {
      case "init" -> {
        final File root = new File(".git");
        new File(root, "objects").mkdirs();
        new File(root, "refs").mkdirs();
        final File head = new File(root, "HEAD");

        try {
          head.createNewFile();
          Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
          System.out.println("Initialized git directory");
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
      case "cat-file" -> {
        String directory = args[2].substring(0, 2);
        String fileName = args[2].substring(2);

        try {
          InputStream inputStream = new FileInputStream(".git/objects/" + directory + "/" + fileName);
          byte[] compressedData = inputStream.readAllBytes();

          Inflater inflater = new Inflater();
          inflater.setInput(compressedData);
          byte[] contents = new byte[inflater.getRemaining()];
          inflater.inflate(contents);

          ObjectParser parsedObject = new ObjectParser(contents);
          System.out.print(parsedObject.getContentString());

          inflater.end();
          inputStream.close();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      case "hash-object" -> {
        String fileName = args[2];

        try {
          String content = Files.readString(Paths.get(fileName));
          String rawObject = "blob " + content.length() + "\0" + content;
          String shaHash = Util.generateSHAHash(rawObject, "SHA-1");
          System.out.println(shaHash);

          String objectDirectory = ".git/objects/" + shaHash.substring(0, 2);
          String objectFileName = shaHash.substring(2);
          new File(objectDirectory).mkdirs();
          final File objectFile = new File(objectDirectory, objectFileName);
          objectFile.createNewFile();
          Util.writeCompressedDataToFile(rawObject, objectFile);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }

      }
      case "ls-tree" -> {
        String directory = args[2].substring(0, 2);
        String fileName = args[2].substring(2);

        try {
          InputStream inputStream = new FileInputStream(".git/objects/" + directory + "/" + fileName);
          byte[] compressedData = inputStream.readAllBytes();

          Inflater inflater = new Inflater();
          inflater.setInput(compressedData);
          byte[] contents = new byte[inflater.getRemaining()];
          inflater.inflate(contents);

          ObjectParser parsedObject = new ObjectParser(contents);
          TreeParser parsedTree = new TreeParser(parsedObject);
          parsedTree.printNames();

          inflater.end();
          inputStream.close();
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
      default -> System.out.println("Unknown command: " + command);
    }
  }
}
