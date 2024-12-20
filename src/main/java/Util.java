import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.zip.DataFormatException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;

public class Util {
  private Util() {
  }

  // public static int toInt(byte[] byteArray) {
  // int result = 0;

  // for (byte b : byteArray) {
  // // Shift existing bits and add current byte
  // result = (result << 8) | (b & 0xFF);
  // }

  // return result;
  // }

  public static String bytesToHexString(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();

    for (byte b : bytes) {
      hexString.append(String.format("%02x", b));
    }

    return hexString.toString();
  }

  public static byte[] generateSHAHash(byte[] input) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance("SHA-1");
    return digest.digest(input);
  }

  public static void writeCompressedDataToFile(byte[] data, File file) throws IOException {
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(fileOutputStream);
    deflaterOutputStream.write(data);
    deflaterOutputStream.close();
    fileOutputStream.close();
  }

  public static byte[] blobBuilder(Path fileName) throws IOException, NoSuchAlgorithmException {
    byte[] content = Files.readAllBytes(fileName);
    String header = "blob " + content.length + "\0";
    ByteArrayOutputStream blobStream = new ByteArrayOutputStream();
    blobStream.write(header.getBytes(StandardCharsets.UTF_8));
    blobStream.write(content);
    return blobStream.toByteArray();
  }

  public static byte[] treeGenerator(File currentDirectory) throws NoSuchAlgorithmException, IOException {
    boolean isTopLevel = currentDirectory == null;

    if (isTopLevel) {
      currentDirectory = new File("./");
    }

    File[] files = currentDirectory.listFiles();
    ArrayList<TreeEntry> entries = new ArrayList<>();

    for (File file : files) {
      if (file.isFile()) {
        byte[] hash = Util.generateSHAHash(Util.blobBuilder(file.toPath()));
        entries.add(new TreeEntry(Constants.BLOB_MODE, file.getName(), hash));
      }

      if (file.isDirectory() && !file.getName().equals(".git")) {
        byte[] hash = Util.treeGenerator(file);
        entries.add(new TreeEntry(Constants.TREE_MODE, file.getName(), hash));
      }
    }

    entries.sort(Comparator.comparing(TreeEntry::getName));

    int contentSize = 0;
    for (TreeEntry entry : entries) {
      contentSize += entry.serialize().length;
    }

    String treeFileHeader = "tree " + contentSize + "\0";
    ByteArrayOutputStream treeStream = new ByteArrayOutputStream();
    treeStream.write(treeFileHeader.getBytes(StandardCharsets.UTF_8));

    for (TreeEntry entry : entries) {
      treeStream.write(entry.serialize());
    }

    byte[] treeObjectData = treeStream.toByteArray();
    byte[] hash = Util.generateSHAHash(treeObjectData);

    if (isTopLevel) {
      String hashString = Util.bytesToHexString(hash);
      Util.writeObjectFile(treeObjectData, hashString);
      System.out.println(hashString);
    }

    return hash;
  }

  public static void displayObjectContent(HashMap<Args, ArgumentValue> args) throws IOException, DataFormatException {
    ObjectParser parsedObject = Util.parseObject(args);
    System.out.print(parsedObject.getContentString());
  }

  public static void treeReader(HashMap<Args, ArgumentValue> args) throws IOException, DataFormatException {
    ObjectParser parsedObject = Util.parseObject(args);
    TreeParser parsedTree = new TreeParser(parsedObject);
    parsedTree.printNames();
  }

  private static void writeObjectFile(byte[] objectData, String hash) throws IOException {
    String objectFileDirectory = ".git/objects/" + hash.substring(0, 2);
    String objectFileName = hash.substring(2);
    new File(objectFileDirectory).mkdirs();
    final File objectFile = new File(objectFileDirectory, objectFileName);
    objectFile.createNewFile();
    Util.writeCompressedDataToFile(objectData, objectFile);
  }

  public static HashMap<Args, ArgumentValue> parseArgs(String[] args) {
    HashMap<Args, ArgumentValue> parsedArgs = new HashMap<>();

    parsedArgs.put(Args.COMMAND, new ArgumentValue.CommandValue(Command.fromLabel(args[0])));
    switch (((ArgumentValue.CommandValue) parsedArgs.get(Args.COMMAND)).value()) {
      case Command.HASH_OBJECT -> {
        String fileName = args[2];
        parsedArgs.put(Args.FILE_NAME, new ArgumentValue.StringValue(fileName));
        break;
      }
      case Command.LS_TREE,
          Command.CAT_FILE -> {
        String directory = args[2].substring(0, 2);
        String fileName = args[2].substring(2);
        parsedArgs.put(Args.DIRECTORY, new ArgumentValue.StringValue(directory));
        parsedArgs.put(Args.FILE_NAME, new ArgumentValue.StringValue(fileName));
        break;
      }
      case Command.COMMIT_TREE -> {
        String treeSHA = args[1];
        String parentCommit = args[3];
        String commitMessage = args[5];
        parsedArgs.put(Args.TREE_SHA, new ArgumentValue.StringValue(treeSHA));
        parsedArgs.put(Args.COMMIT_SHA, new ArgumentValue.StringValue(parentCommit));
        parsedArgs.put(Args.COMMIT_MESSAGE, new ArgumentValue.StringValue(commitMessage));
        break;
      }
      default -> {
      }
    }

    return parsedArgs;
  }

  public static ObjectParser parseObject(HashMap<Args, ArgumentValue> args) throws IOException, DataFormatException {
    InputStream inputStream = new FileInputStream(
        ".git/objects/" + ((ArgumentValue.StringValue) args.get(Args.DIRECTORY)).value() + "/"
            + ((ArgumentValue.StringValue) args.get(Args.FILE_NAME)).value());
    byte[] compressedData = inputStream.readAllBytes();

    Inflater inflater = new Inflater();
    inflater.setInput(compressedData);
    byte[] contents = new byte[inflater.getRemaining()];
    inflater.inflate(contents);

    ObjectParser parsedObject = new ObjectParser(contents);

    inputStream.close();
    inflater.end();

    return parsedObject;
  }

  public static void initializeRepository() throws IOException {
    final File root = new File(".git");
    new File(root, "objects").mkdirs();
    new File(root, "refs").mkdirs();
    final File head = new File(root, "HEAD");
    head.createNewFile();
    Files.write(head.toPath(), "ref: refs/heads/main\n".getBytes());
    System.out.println("Initialized git directory");
  }

  public static void hashObject(HashMap<Args, ArgumentValue> parsedArgs) throws NoSuchAlgorithmException, IOException {
    byte[] blob = Util
        .blobBuilder(Paths.get(((ArgumentValue.StringValue) parsedArgs.get(Args.FILE_NAME)).value()));
    byte[] hash = Util.generateSHAHash(blob);
    String shaHash = Util.bytesToHexString(hash);

    System.out.println(shaHash);

    String objectDirectory = ".git/objects/" + shaHash.substring(0, 2);
    String objectFileName = shaHash.substring(2);
    new File(objectDirectory).mkdirs();
    final File objectFile = new File(objectDirectory, objectFileName);
    objectFile.createNewFile();
    Util.writeCompressedDataToFile(blob, objectFile);
  }

  private static String generateTimestamp() {
    long unixTimestamp = Instant.now().getEpochSecond();
    ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Tallinn"));
    String timeZoneOffset = DateTimeFormatter.ofPattern("XXX").format(now);
    return unixTimestamp + " " + timeZoneOffset;
  }

  public static void createCommit(HashMap<Args, ArgumentValue> parsedArgs)
      throws IOException, NoSuchAlgorithmException {
    ByteArrayOutputStream contentStream = new ByteArrayOutputStream();

    contentStream.write("tree ".getBytes(StandardCharsets.UTF_8));
    contentStream
        .write(((ArgumentValue.StringValue) parsedArgs.get(Args.TREE_SHA)).value().getBytes(StandardCharsets.UTF_8));
    contentStream.write("\n".getBytes());

    contentStream.write("parent ".getBytes(StandardCharsets.UTF_8));
    contentStream.write(
        ((ArgumentValue.StringValue) parsedArgs.get(Args.COMMIT_SHA)).value().getBytes(StandardCharsets.UTF_8));
    contentStream.write("\n".getBytes());

    byte[] timestamp = Util.generateTimestamp().getBytes(StandardCharsets.UTF_8);

    contentStream.write("author Jaakal <jaak.kivinukk@gmail.com> ".getBytes(StandardCharsets.UTF_8));
    contentStream.write(timestamp);
    contentStream.write("\n".getBytes());

    contentStream.write("commiter Jaakal <jaak.kivinukk@gmail.com> ".getBytes(StandardCharsets.UTF_8));
    contentStream.write(timestamp);
    contentStream.write("\n\n".getBytes());

    contentStream.write(
        ((ArgumentValue.StringValue) parsedArgs.get(Args.COMMIT_MESSAGE)).value().getBytes(StandardCharsets.UTF_8));
    contentStream.write("\n".getBytes());

    ByteArrayOutputStream commitStream = new ByteArrayOutputStream();

    commitStream.write(("commit " + contentStream.size() + "\0").getBytes(StandardCharsets.UTF_8));
    commitStream.write(contentStream.toByteArray());

    byte[] commitObjectData = commitStream.toByteArray();
    byte[] hash = Util.generateSHAHash(commitObjectData);

    String hashString = Util.bytesToHexString(hash);
    Util.writeObjectFile(commitObjectData, hashString);
    System.out.println(hashString);
  }
}
