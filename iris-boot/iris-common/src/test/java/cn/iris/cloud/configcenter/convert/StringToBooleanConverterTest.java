package cn.iris.cloud.configcenter.convert;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StringToBooleanConverterTest {
  private StringToBooleanConverter c = new StringToBooleanConverter();

  @Test
  public void testTrue() {
    assertTrue(c.convert("TRUE"));
    assertTrue(c.convert("True"));
    assertTrue(c.convert("true"));
    assertTrue(c.convert("ON"));
    assertTrue(c.convert("On"));
    assertTrue(c.convert("on"));
    assertTrue(c.convert("YES"));
    assertTrue(c.convert("Yes"));
    assertTrue(c.convert("yes"));
    assertTrue(c.convert("1"));
  }

  @Test
  public void testFalse() {
    assertFalse(c.convert("FALSE"));
    assertFalse(c.convert("False"));
    assertFalse(c.convert("false"));
    assertFalse(c.convert("OFF"));
    assertFalse(c.convert("Off"));
    assertFalse(c.convert("off"));
    assertFalse(c.convert("NO"));
    assertFalse(c.convert("No"));
    assertFalse(c.convert("no"));
    assertFalse(c.convert("0"));
  }

  @Test
  public void testNull() {
    assertNull(c.convert(""));
    assertNull(c.convert("  "));
    assertNull(c.convert("\t"));
  }

  @Test
  public void testException() {
    assertThrows(NullPointerException.class, () -> c.convert(null));
    assertThrows(IllegalArgumentException.class, () -> c.convert("invalid"));
  }
}