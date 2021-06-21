package org.apache.ibatis.reflection.invoker;

import org.apache.logging.log4j.core.util.Assert;
import org.apache.logging.log4j.core.util.JsonUtils;
import sun.plugin2.util.PojoUtil;

import java.lang.reflect.Field;

/**
 * @author ：lopez
 * @date ：Created in 2021/6/20 17:05
 * @description：
 * @modified By：
 */
public class InvokerTest {

  public static void main(String[] args) {

    Class<?> clazz = Animal.class;
    Field[] fields =  clazz.getFields();
    Field[] declareFields = clazz.getDeclaredFields();
    Animal animal = new Animal("狗", "狗腿");

    // 1、GetFieldInvoker：当字段没有get方法时，可以用GetFieldInvoker获取字段的值
    for(int i = 0; i < declareFields.length; i++) {
      GetFieldInvoker getFieldInvoker = new GetFieldInvoker(declareFields[i]);
      try {
        Object obj = getFieldInvoker.invoke(animal, new Object[]{"test"});
        Assert.isEmpty(obj);
      } catch(IllegalAccessException e) {
        e.printStackTrace();
      }
    }

    // 2、SetFieldInvoker:当class的字段没有set方法时，可以用SetFieldInvoker设置字段的值
    Field nameField = null;
    Field footField = null;
    try {
      nameField = clazz.getDeclaredField("name");
      footField = clazz.getDeclaredField("foot");
    } catch(NoSuchFieldException e) {
      e.printStackTrace();
    }
    SetFieldInvoker setNameFieldInvoker = new SetFieldInvoker(nameField);
    SetFieldInvoker setFootFieldInvoker = new SetFieldInvoker(footField);
    try {
      setNameFieldInvoker.invoke(animal, new Object[]{"猫"});
      setFootFieldInvoker.invoke(animal, new Object[]{"猫腿"});
    } catch(IllegalAccessException e) {
      e.printStackTrace();
    }
    System.out.println(PojoUtil.toJson(animal));


  }

}


class Animal {

  private String name;

  private String foot;

  public Animal() {
  }

  public Animal(String name, String foot) {
    this.name = name;
    this.foot = foot;
  }

}
