package lopez.designpattern.reflector;

import org.apache.ibatis.reflection.DefaultReflectorFactory;
import org.apache.ibatis.reflection.Reflector;
import org.apache.ibatis.reflection.ReflectorFactory;
import org.junit.Assert;

import java.lang.reflect.Method;

/**
 * @author ：lopez
 * @date ：Created in 2021/6/20 18:35
 * @description：
 * @modified By：
 */
public class ReflectorTest {

  public static void main(String[] args) {
    DefaultReflectorFactory defaultReflectorFactory = new DefaultReflectorFactory();
    Reflector reflector = defaultReflectorFactory.findForClass(AbstractAnimal.class);
    // 通过getDeclaredMethods()获取抽象类的方法时，只能获取抽象类本身的方法，获取不到抽象类对应接口的方法。
    Method[] methods = AbstractAnimal.class.getDeclaredMethods();

    // 获取抽象类对应接口的方法
    Class<?>[] classes = AbstractAnimal.class.getInterfaces();
    Method[] methodsArray = classes[0].getMethods();
    Assert.assertNotNull(methods);
    Assert.assertNotNull(methodsArray);

    // 获取不到接口，只会获取父类
    Class clazz = AbstractAnimal.class.getSuperclass();
    System.out.println(clazz);
  }

  interface Animal {
    String eat(String something);
  }

  public abstract class AbstractAnimal implements Animal{

    private String myName(){
      return "dog";
    }
  }
}
