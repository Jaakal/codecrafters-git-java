import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

public class ObjectInflater {
  public static byte[] inflateObject(InputStream compressedInputStream) throws IOException {
    Inflater inflater = new Inflater();

    InflaterInputStream inflaterInputStream = new InflaterInputStream(compressedInputStream, inflater);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    byte[] buffer = new byte[8192];
    int bytesRead;
    while ((bytesRead = inflaterInputStream.read(buffer)) != -1) {
      outputStream.write(buffer, 0, bytesRead);
    }

    inflater.end();
    return outputStream.toByteArray();
  }
}
