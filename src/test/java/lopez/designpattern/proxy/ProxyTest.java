package lopez.designpattern.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author ：lopez
 * @date ：Created in 2021/6/17 19:32
 * @description：
 * @modified By：
 */


public class ProxyTest {

  public static void main(String[] args) {
    // Proxy.newProxyInstance代理Dog（这里需要传Animal接口，jdk只能代理接口）执行吃东西
    AnimalInvocationHandler animalInvocationHandler = new AnimalInvocationHandler(new Dog());
    Animal animal = (Animal)Proxy.newProxyInstance(ProxyTest.class.getClassLoader(), new Class[] {Animal.class}, animalInvocationHandler);
    animal.eat("吃东西");
  }
}

interface Animal {

  String eat(String something);
}



class Dog implements Animal {

  @Override
  public String eat(String something) {

    System.out.println("狗吃" + something);
    return something;
  }
}




class AnimalInvocationHandler implements InvocationHandler {

  private Object target;

  public AnimalInvocationHandler(Object target) {
    this.target = target;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    System.out.println("动物" + method.getName());
    return method.invoke(target, args);
  }
}
