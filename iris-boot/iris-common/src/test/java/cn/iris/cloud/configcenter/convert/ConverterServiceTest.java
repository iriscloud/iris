package cn.iris.cloud.configcenter.convert;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Currency;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * ConverterServiceTest
 *
 **/
class ConverterServiceTest {
    private ConverterService c = ConverterService.getInstance();

    @Test
    public void testConvertBoolean() {
        assertTrue(c.convert(Boolean.class, "true"));
    }

    @Test
    public void testConvertCharacter() {
        assertEquals('c', c.convert(Character.class, "c"));
    }

    @Test
    public void testConvertCharset() {
        assertEquals(StandardCharsets.UTF_8, c.convert(Charset.class, "utf8"));
    }

    @Test
    public void testConvertUUID() {
        UUID u = UUID.randomUUID();
        assertEquals(u, c.convert(UUID.class, u.toString()));
    }

    @Test
    public void testConvertCurrency() {
        Currency y = Currency.getInstance("CNY");
        assertEquals(y, c.convert(Currency.class, y.getCurrencyCode()));
    }

    @Test
    void testConvertObject() throws NoSuchFieldException {

        String l = "[1,2,3]";
        Field f_appid = ConfigConverterTest.class.getDeclaredField("appId");
        Object o = c.convert(f_appid.getType(),f_appid.getGenericType(),l);
        Assertions.assertTrue(o instanceof List);
        List<String> list = (List<String>) o;
        Assertions.assertSame(list.size(),3);
        Assertions.assertEquals(list.get(0),"1");
        Assertions.assertEquals(list.get(1),"2");
        Assertions.assertEquals(list.get(2),"3");

        String userstr = "[{\"name\":\"张三\",\"age\":\"30\",\"sex\":\"男\",\"son\":{\"name\":\"张小三\",\"age\":\"3\",\"sex\":\"女\"}},{\"name\":\"李四\",\"age\":\"40\",\"sex\":\"男\",\"son\":{\"name\":\"李小四\",\"age\":\"4\",\"sex\":\"男\"}}]";
        Field f_users = ConfigConverterTest.class.getDeclaredField("users");
        Object o_users = c.convert(f_users.getType(),f_users.getGenericType(),userstr);
        Assertions.assertTrue(o_users instanceof List);
        List<ConfigUserTest> configUserList = (List<ConfigUserTest>) o_users;
        Assertions.assertSame(configUserList.size(),2);
        Assertions.assertEquals(configUserList.get(0).getName(),"张三");
        Assertions.assertEquals(configUserList.get(0).getSon().getName(),"张小三");
        Assertions.assertEquals(configUserList.get(1).getName(),"李四");
        Assertions.assertEquals(configUserList.get(1).getSon().getName(),"李小四");
    }

    public static class ConfigConverterTest{

        private List<String> appId;

        private List<ConfigUserTest> users;

        public List<String> getAppId() {
            return appId;
        }

        public void setAppId(List<String> appId) {
            this.appId = appId;
        }

        public List<ConfigUserTest> getUsers() {
            return users;
        }

        public void setUsers(
            List<ConfigUserTest> users) {
            this.users = users;
        }
    }

    public static class ConfigUserTest{
        private String name;
        private Integer age;
        private ConfigUserTest son;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Integer getAge() {
            return age;
        }

        public void setAge(Integer age) {
            this.age = age;
        }

        public ConfigUserTest getSon() {
            return son;
        }

        public void setSon(ConfigUserTest son) {
            this.son = son;
        }
    }

}