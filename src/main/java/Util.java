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

}
