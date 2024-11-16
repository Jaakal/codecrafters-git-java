import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
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

          ParseObject parsedObject = new ParseObject(contents);
          System.out.print(parsedObject.getContent());

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
