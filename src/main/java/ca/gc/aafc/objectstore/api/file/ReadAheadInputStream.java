package ca.gc.aafc.objectstore.api.file;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.SequenceInputStream;

/**
 * {@link InputStream} wrapper where a number of bytes can read ahead.
 * This class will expose 2 {@link InputStream}, the read ahead portion and the complete {@link InputStream}.
 * Not thread-safe.
 */
@Builder
@Getter
public class ReadAheadInputStream {

  private final ByteArrayInputStream readAheadBuffer;
  private final InputStream inputStream;

  public static ReadAheadInputStream from(@NonNull InputStream is, int readAheadByte) throws IOException {
    // Read the beginning of the Stream to allow Tika to detect the mediaType
    byte[] buffer = new byte[readAheadByte];
    int length = is.read(buffer);
    ByteArrayInputStream bais = new ByteArrayInputStream(buffer, 0, length);
    return ReadAheadInputStream.builder().readAheadBuffer(bais).inputStream(new SequenceInputStream(bais, is)).build();
  }

  /**
   * Get the read ahead buffer with the possibility to reset it before usage.
   * @param reset should the read ahead buffer be reset or no
   * @return
   */
  public ByteArrayInputStream getReadAheadBuffer(boolean reset) {
    if (reset && readAheadBuffer != null) {
      readAheadBuffer.reset();
    }
    return readAheadBuffer;
  }

}
