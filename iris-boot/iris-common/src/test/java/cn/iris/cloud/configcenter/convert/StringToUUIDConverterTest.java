package cn.iris.cloud.configcenter.convert;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringToUUIDConverterTest {
  private StringToUUIDConverter c = new StringToUUIDConverter();

  @Test
  public void testUUID() {
    final UUID u = UUID.randomUUID();
    assertEquals(u, c.convert(u.toString()));
  }

  @Test
  public void testNull() {
    assertNull(c.convert(null));
    assertNull(c.convert(""));
    assertNull(c.convert("  "));
  }
}