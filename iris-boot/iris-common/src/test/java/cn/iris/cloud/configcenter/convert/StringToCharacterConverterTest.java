package cn.iris.cloud.configcenter.convert;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringToCharacterConverterTest {
  private StringToCharacterConverter c = new StringToCharacterConverter();

  @Test
  public void testCharactor() {
    assertEquals(' ', c.convert(" "));
    assertEquals('1', c.convert("1"));
    assertEquals('x', c.convert("x"));
  }

  @Test
  public void testNull() {
    assertNull(c.convert(""));
  }

  @Test
  public void testException() {
    assertThrows(NullPointerException.class, () -> c.convert(null));
    assertThrows(IllegalArgumentException.class, () -> c.convert("12"));
    assertThrows(IllegalArgumentException.class, () -> c.convert("123"));
  }
}