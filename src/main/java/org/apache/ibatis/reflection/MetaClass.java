/**
 *    Copyright 2009-2020 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import org.apache.ibatis.reflection.invoker.GetFieldInvoker;
import org.apache.ibatis.reflection.invoker.Invoker;
import org.apache.ibatis.reflection.invoker.MethodInvoker;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 *  基于Reflector和PropertyTokenizer组合使用，实现了对复杂的属性表达式的解析
 */
public class MetaClass {

  // reflector工厂，用于创建Reflector对象
  private final ReflectorFactory reflectorFactory;

  // 类的class信息（ 反射器，在创建MetaClass时会指定一个类，该Reflector对象用于记录该类相关的元信息）
  private final Reflector reflector;

  private MetaClass(Class<?> type, ReflectorFactory reflectorFactory) {
    this.reflectorFactory = reflectorFactory;
    this.reflector = reflectorFactory.findForClass(type);
  }

  public static MetaClass forClass(Class<?> type, ReflectorFactory reflectorFactory) {
    return new MetaClass(type, reflectorFactory);
  }

  /**
   * 为类属性创建MetaClass对象
   * 例如：private String name;
   *       private People people;
   * @param name
   * @return
   */
  public MetaClass metaClassForProperty(String name) {
    Class<?> propType = reflector.getGetterType(name);
    return MetaClass.forClass(propType, reflectorFactory);
  }

  /**
   * 根据属性表达式（有可能不规范，大小写混杂）查找属性名
   * @param name
   * @return
   */
  public String findProperty(String name) {
    StringBuilder prop = buildProperty(name, new StringBuilder());
    return prop.length() > 0 ? prop.toString() : null;
  }

  /**
   * 同上，只是支持驼峰式
   * @param name
   * @param useCamelCaseMapping
   * @return
   */
  public String findProperty(String name, boolean useCamelCaseMapping) {
    if (useCamelCaseMapping) {
      name = name.replace("_", "");
    }
    return findProperty(name);
  }

  /**
   * 获取指定类的可读属性数组
   * @return
   */
  public String[] getGetterNames() {
    return reflector.getGetablePropertyNames();
  }

  /**
   * 获取指定类的可写属性数组
   * @return
   */
  public String[] getSetterNames() {
    return reflector.getSetablePropertyNames();
  }

  /**
   * 通过表达式 找到对应属性 的setter方法参数类型,例如：people[0].age就是返回的类型就是int.class;people[0].name返回类型是String.class
   * @param name
   * @return
   */
  public Class<?> getSetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop.getName());
      return metaProp.getSetterType(prop.getChildren());
    } else {
      return reflector.getSetterType(prop.getName());
    }
  }

  /**
   * 通过表达式 找到对应属性 的getter方法参数类型
   * @param name
   * @return
   */
  public Class<?> getGetterType(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaClass metaProp = metaClassForProperty(prop);
      return metaProp.getGetterType(prop.getChildren());
    }
    // issue #506. Resolve the type inside a Collection Object
    return getGetterType(prop);
  }

  private MetaClass metaClassForProperty(PropertyTokenizer prop) {
    Class<?> propType = getGetterType(prop);
    return MetaClass.forClass(propType, reflectorFactory);
  }

  /**
   *  获取getter类型，如果prop有下标且为集合则获取泛型类型(如：Collection<T>，获取的泛型类型T)
   *  比如 order[0]（此种表达式表示order是List） 没有子表达式，会调用本方法处理，假设 order 属性的类型为 List<String>，则 order[0] 表示列表中的第一个元素，其属性类型应为 String。
   * @param prop
   * @return
   */
  private Class<?> getGetterType(PropertyTokenizer prop) {
    Class<?> type = reflector.getGetterType(prop.getName());
    // 如果type是Collection的子类 并且 prop的索引值不为空（即如果type是List<String>这些的）
    if (prop.getIndex() != null && Collection.class.isAssignableFrom(type)) {
      Type returnType = getGenericGetterType(prop.getName());
      if (returnType instanceof ParameterizedType) {
        Type[] actualTypeArguments = ((ParameterizedType) returnType).getActualTypeArguments();
        // Collection<T>最多只有一个泛型
        if (actualTypeArguments != null && actualTypeArguments.length == 1) {
          returnType = actualTypeArguments[0];
          if (returnType instanceof Class) {
            type = (Class<?>) returnType;
          } else if (returnType instanceof ParameterizedType) {
            type = (Class<?>) ((ParameterizedType) returnType).getRawType();
          }
        }
      }
    }
    return type;
  }

  /**
   * 获取属性的泛型类型，即 上面的order[0](order是List)例子，在这里会传入order，TypeParameterResolver.resolveReturnType()会解析到该类型是ParameterizedType
   * @param propertyName
   * @return
   */
  private Type getGenericGetterType(String propertyName) {
    try {
      // 根据字段属性名获取invoker，该invoker有可能是MethodInvoker，也有可能是GetFieldInvoker（详细看Reflector类中的getMethods）
      Invoker invoker = reflector.getGetInvoker(propertyName);
      if (invoker instanceof MethodInvoker) {
        // 获取MethodInvoker的Method字段，因为没有对外访问的方法，通过获取Field declaredMethod，获取到 Method method
        Field declaredMethod = MethodInvoker.class.getDeclaredField("method");
        declaredMethod.setAccessible(true);
        Method method = (Method) declaredMethod.get(invoker);// field.get(Object)
        // 获取field的值，即获取到MethodInvoker里面的method，method里面包含了返回类型是什么（因为是getter方法，返回类型就是字段的类型）

        return TypeParameterResolver.resolveReturnType(method, reflector.getType());
      } else if (invoker instanceof GetFieldInvoker) {
        Field declaredField = GetFieldInvoker.class.getDeclaredField("field");
        declaredField.setAccessible(true);
        Field field = (Field) declaredField.get(invoker);
        return TypeParameterResolver.resolveFieldType(field, reflector.getType());
      }
    } catch (NoSuchFieldException | IllegalAccessException e) {
      // Ignored
    }
    return null;
  }

  /**
   * 判断类的Setter方法是否含有入参，支持people[0].name
   * @param name
   * @return
   */
  public boolean hasSetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (reflector.hasSetter(prop.getName())) {
        MetaClass metaProp = metaClassForProperty(prop.getName());
        return metaProp.hasSetter(prop.getChildren());
      } else {
        return false;
      }
    } else {
      return reflector.hasSetter(prop.getName());
    }
  }

  /**
   *  判断类的Getter方法是否含有入参，支持people[0].name
   * @param name
   * @return
   */
  public boolean hasGetter(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      if (reflector.hasGetter(prop.getName())) {
        MetaClass metaProp = metaClassForProperty(prop);
        return metaProp.hasGetter(prop.getChildren());
      } else {
        return false;
      }
    } else {
      return reflector.hasGetter(prop.getName());
    }
  }

  public Invoker getGetInvoker(String name) {
    return reflector.getGetInvoker(name);
  }

  public Invoker getSetInvoker(String name) {
    return reflector.getSetInvoker(name);
  }

  /**
   * 根据 属性名 获取 标准的属性名（这里是有嵌套，如：people.Name，出参：people.name）
   * @param name
   * @param builder
   * @return
   */
  private StringBuilder buildProperty(String name, StringBuilder builder) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      String propertyName = reflector.findPropertyName(prop.getName());
      if (propertyName != null) {
        builder.append(propertyName);
        builder.append(".");
        MetaClass metaProp = metaClassForProperty(propertyName);
        metaProp.buildProperty(prop.getChildren(), builder);
      }
    } else {
      String propertyName = reflector.findPropertyName(name);
      if (propertyName != null) {
        builder.append(propertyName);
      }
    }
    return builder;
  }

  public boolean hasDefaultConstructor() {
    return reflector.hasDefaultConstructor();
  }

}
