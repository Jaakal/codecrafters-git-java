import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.zip.DeflaterOutputStream;

public class Util {
  private Util() {
  }

  public static ArrayList<String> values = new ArrayList<>();

  public static int toInt(byte[] byteArray) {
    int result = 0;

    for (byte b : byteArray) {
      // Shift existing bits and add current byte
      result = (result << 8) | (b & 0xFF);
    }

    return result;
  }

  public static String bytesToHexString(byte[] bytes) {
    StringBuilder hexString = new StringBuilder();

    for (byte b : bytes) {
      hexString.append(String.format("%02x", b));
    }

    return hexString.toString();
  }

  public static byte[] generateRawSHAHash(byte[] input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-1");
      return digest.digest(input);
    } catch (Exception e) {
      // TODO: handle exception
    }

    return new byte[] {};
  }

  public static byte[] generateRawSHAHash(String input, String algorithm) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance(algorithm);
    return digest.digest(input.getBytes());
  }

  public static String generateSHAHash(String input, String algorithm) throws NoSuchAlgorithmException {
    byte[] hashBytes = Util.generateRawSHAHash(input, algorithm);
    return Util.bytesToHexString(hashBytes);
  }

  public static void writeCompressedDataToFile(String data, File file) throws IOException {
    FileOutputStream fileOutputStream = new FileOutputStream(file);
    DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(fileOutputStream);
    deflaterOutputStream.write(data.getBytes());
    deflaterOutputStream.close();
    fileOutputStream.close();
  }

  public static void writeCompressedDataToFile(byte[] data, File file) {
    try {
      FileOutputStream fileOutputStream = new FileOutputStream(file);
      DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(fileOutputStream);
      deflaterOutputStream.write(data);
      deflaterOutputStream.close();
      fileOutputStream.close();
    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  public static String[] blobBuilder(String fileName) throws IOException, NoSuchAlgorithmException {
    String content = Files.readString(Paths.get(fileName));
    String rawObject = "blob " + content.length() + "\0" + content;
    String shaHash = Util.generateSHAHash(rawObject, "SHA-1");
    return new String[] { shaHash, rawObject };
  }

  public static byte[] rawBlobHashBuilder(Path fileName) throws IOException, NoSuchAlgorithmException {
    byte[] content = Files.readAllBytes(fileName);
    String header = "blob " + content.length + "\0";
    ByteArrayOutputStream blobStream = new ByteArrayOutputStream();
    blobStream.write(header.getBytes(StandardCharsets.UTF_8));
    blobStream.write(content);
    byte[] blobData = blobStream.toByteArray();
    return generateRawSHAHash(blobData);
  }

  public static void enlist(File currentDirectory) {
    boolean isTopLevel = currentDirectory == null;
    if (isTopLevel) {
      currentDirectory = new File("./");
    }

    System.out.println("==== DIR ================================ " + currentDirectory.getName());

    File[] files = currentDirectory.listFiles();
    for (File file : files) {
      try {
        if (file.isFile()) {
          // String content = Files.readString(Paths.get(file.getName()));
          String content = Files.readString(file.toPath());
          System.out.println("---- FILE ------------- " + file.getName());
          byte[] shaHash = Util.rawBlobHashBuilder(file.toPath());
          System.out.println(Util.bytesToHexString(shaHash));
          System.out.println(content);
          System.out.println("---- FILE ------------- " + file.getName());
        }

        if (file.isDirectory() && !file.getName().equals(".git")) {
          Util.enlist(file);
        }
      } catch (Exception e) {
        // TODO: handle exception
      }
    }

    System.out.println("==== DIR ================================ " + currentDirectory.getName());
  }

  public static byte[] treeGenerator(File currentDirectory) {

    boolean isTopLevel = currentDirectory == null;
    if (isTopLevel) {
      currentDirectory = new File("./");
    }

    File[] files = currentDirectory.listFiles();
    ArrayList<TreeEntry> entries = new ArrayList<>();
    for (File file : files) {

      // System.out.println(file.getName());
      try {
        if (file.isFile()) {
          byte[] shaHash = Util.rawBlobHashBuilder(file.toPath());
          entries.add(new TreeEntry("100644", file.getName(), shaHash));
        }

        if (file.isDirectory() && !file.getName().equals(".git")) {
          byte[] shaHash = Util.treeGenerator(file);
          entries.add(new TreeEntry("40000", file.getName(), shaHash));
        }
      } catch (Exception e) {
        // TODO: handle exception
      }
    }

    entries.sort(Comparator.comparing(TreeEntry::getName));

    ByteArrayOutputStream contentStream = new ByteArrayOutputStream();

    for (TreeEntry entry : entries) {
      try {
        contentStream.write(entry.serialize());
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    String header = "tree " + contentStream.size() + "\0";
    ByteArrayOutputStream blobStream = new ByteArrayOutputStream();
    try {
      blobStream.write(header.getBytes(StandardCharsets.UTF_8));
      blobStream.write(contentStream.toByteArray());
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    byte[] blobData = blobStream.toByteArray();
    byte[] hash = generateRawSHAHash(blobData);

    // if (contentStream.size() == 0) {
    // System.out.println(Util.bytesToHexString(hash));
    // }

    if (isTopLevel) {
      String shaHash = Util.bytesToHexString(hash);
      String objectDirectory = ".git/objects/" + shaHash.substring(0, 2);
      String objectFileName = shaHash.substring(2);
      new File(objectDirectory).mkdirs();
      final File objectFile = new File(objectDirectory, objectFileName);
      try {
        objectFile.createNewFile();
      } catch (IOException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
      Util.writeCompressedDataToFile(blobData, objectFile);
      System.out.println(shaHash);
    }

    return hash;
  }

  public static byte[] rawTreeHashBuilder(ArrayList<TreeEntry> entries) throws NoSuchAlgorithmException {
    entries.sort(Comparator.comparing(TreeEntry::getName));
    StringBuilder content = new StringBuilder();

    for (TreeEntry entry : entries) {
      content.append(entry.serialize());
    }

    String rawObject = "tree " + content.length() + "\0" + content;
    return Util.generateRawSHAHash(rawObject, "SHA-1");
  }
}
