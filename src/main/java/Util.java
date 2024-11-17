import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.DeflaterOutputStream;

public class Util {
  private Util() {
  }

  public static int toInt(byte[] byteArray) {
    int result = 0;

    for (byte b : byteArray) {
      // Shift existing bits and add current byte
      result = (result << 8) | (b & 0xFF);
    }

    return result;
  }

  public static String bytesToHexString(byte[] bytes) {
    StringBuilder hexOutput = new StringBuilder();

    for (int i = 0; i < bytes.length; i++) {
      hexOutput.append(String.format("%02x ", bytes[i]));
    }

    return hexOutput.toString();
  }

  public static String generateSHAHash(String input, String algorithm) throws NoSuchAlgorithmException {
    MessageDigest digest = MessageDigest.getInstance(algorithm);
    byte[] hashBytes = digest.digest(input.getBytes());
    StringBuilder hexString = new StringBuilder();

    for (byte b : hashBytes) {
      hexString.append(String.format("%02x", b));
    }

    return hexString.toString();
  }

  public static void writeCompressedDataToFile(String data, File file) throws IOException {
    try (FileOutputStream fileOutputStream = new FileOutputStream(file);
        DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(fileOutputStream)) {
      deflaterOutputStream.write(data.getBytes());
    }
  }
}
