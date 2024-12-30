package utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public final class DataUtils {
  private DataUtils() {
  }

  public static final String bytesToHex(final byte[] input) {
    final StringBuilder hexString = new StringBuilder();

    for (final byte byteValue : input) {
      hexString.append(String.format("%02x", byteValue));
    }

    return hexString.toString();
  }

  public static final byte[] generateSha1Hash(byte[] input) throws NoSuchAlgorithmException {
    final MessageDigest digest = MessageDigest.getInstance("SHA-1");
    return digest.digest(input);
  }

  public static final String generateTimestamp() {
    final long unixTimestamp = Instant.now().getEpochSecond();
    final ZonedDateTime now = ZonedDateTime.now(ZoneId.of("Europe/Tallinn"));
    final String timeZoneOffset = DateTimeFormatter.ofPattern("XXX").format(now);
    return unixTimestamp + " " + timeZoneOffset;
  }
}
