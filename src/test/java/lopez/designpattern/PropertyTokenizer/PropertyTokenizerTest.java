package lopez.designpattern.PropertyTokenizer;

import org.apache.ibatis.reflection.property.PropertyTokenizer;

import static org.junit.Assert.assertEquals;

/**
 * @author ：lopez
 * @date ：Created in 2021/6/21 19:57
 * @description：
 * @modified By：
 */
public class PropertyTokenizerTest {

  public static void main(String[] args) {
    PropertyTokenizer propertyTokenizer = new PropertyTokenizer("people[0].name");
    assertEquals("name", propertyTokenizer.getChildren());
    assertEquals("people", propertyTokenizer.getName());
    assertEquals("0", propertyTokenizer.getIndex());
    assertEquals("people[0]", propertyTokenizer.getIndexedName());
  }
}
