package utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public final class InputStreamInflater {
  private InputStreamInflater() {
  }

  public static final byte[] inflate(final InputStream compressedInputStream) throws IOException {
    final Inflater inflater = new Inflater();

    final InflaterInputStream inflaterInputStream = new InflaterInputStream(compressedInputStream, inflater);
    final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    final byte[] buffer = new byte[8192];
    int bytesRead;
    while ((bytesRead = inflaterInputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, bytesRead);
    }

    inflater.end();

    return outputStream.toByteArray();
  }

  public static final byte[] inflate(final ByteBuffer compressedByteBuffer, final int length)
      throws DataFormatException {
    final Inflater inflater = new Inflater();

    inflater.setInput(compressedByteBuffer);

    final byte[] objectData = new byte[length];
    inflater.inflate(objectData);

    inflater.end();

    return objectData;
  }
}
