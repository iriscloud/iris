package cn.iris.cloud.configcenter.convert;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.charset.UnsupportedCharsetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StringToCharsetConverterTest {
  private StringToCharsetConverter c = new StringToCharsetConverter();

  @Test
  public void testStandardCharset() {
    assertEquals(StandardCharsets.UTF_8, c.convert("utf8"));
    assertEquals(StandardCharsets.UTF_8, c.convert("UTF8"));
    assertEquals(StandardCharsets.UTF_8, c.convert("utf-8"));
    assertEquals(StandardCharsets.UTF_8, c.convert("UTF-8"));
  }

  @Test
  public void testUnsupportedCharset() {
    assertThrows(UnsupportedCharsetException.class, () -> c.convert("utf_8"));
    assertThrows(UnsupportedCharsetException.class, () -> c.convert("UTF_8"));
  }
}