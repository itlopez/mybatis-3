/**
 *    Copyright 2009-2019 the original author or authors.
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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;
import org.apache.ibatis.reflection.wrapper.BeanWrapper;
import org.apache.ibatis.reflection.wrapper.CollectionWrapper;
import org.apache.ibatis.reflection.wrapper.MapWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapper;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;

/**
 * 对象元数据(相当于MetaObject -> BeanWrapper -> MetaClass -> Reflector)
 *  1、Mybatis对象操作的工具类，等同于MetaClass
 *  2、提供了基于对复杂的属性表达式为对象的属性值的获得和设置等方法。
 *  3、通过组合的方式(封装ObjectWrapper)，对BaseWrapper操作进一步增强。
 *  4、MetaObject是一个对象包装器，其性质上有点类似ASF提供的commons类库，其中包装了对象的元数据信息，对象本身，对象反射工厂，对象包装器工厂等。
 *    使得根据OGNL表达式设置或者获取对象的属性更为便利，也可以更加方便的判断对象中是否包含指定属性、指定属性是否具有getter、setter等。
 *    主要的功能是通过其ObjectWrapper类型的属性完成的，它包装了操作对象元数据以及对象本身的主要接口，操作标准对象的实现是BeanWrapper。
 *    BeanWrapper类型有个MetaClass类型的属性，MetaClass中有个Reflector属性，其中包含了可读、可写的属性、方法以及构造器信息。
 *
 *   1、通过组合的方式(封装ObjectWrapper)，对BaseWrapper操作进一步增强
 * @author Clinton Begin
 */
public class MetaObject {

  // 原始对象
  private final Object originalObject;

  // 这里主要组合了objectWrapper，包装过的对象，主要的功能是通过其ObjectWrapper类型的属性完成的，它包装了操作对象元数据以及对象本身的主要接口，操作标准对象的实现是BeanWrapper
  private final ObjectWrapper objectWrapper;

  // 对象工厂
  private final ObjectFactory objectFactory;
  // 包装过的对象工厂
  private final ObjectWrapperFactory objectWrapperFactory;
  // 映射工厂
  private final ReflectorFactory reflectorFactory;

  private MetaObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    this.originalObject = object;
    this.objectFactory = objectFactory;
    this.objectWrapperFactory = objectWrapperFactory;
    this.reflectorFactory = reflectorFactory;

    if (object instanceof ObjectWrapper) {
      this.objectWrapper = (ObjectWrapper) object;
    } else if (objectWrapperFactory.hasWrapperFor(object)) {
      this.objectWrapper = objectWrapperFactory.getWrapperFor(this, object);
    } else if (object instanceof Map) {
      this.objectWrapper = new MapWrapper(this, (Map) object);
    } else if (object instanceof Collection) {
      this.objectWrapper = new CollectionWrapper(this, (Collection) object);
    } else {
      this.objectWrapper = new BeanWrapper(this, object);
    }
  }

  public static MetaObject forObject(Object object, ObjectFactory objectFactory, ObjectWrapperFactory objectWrapperFactory, ReflectorFactory reflectorFactory) {
    if (object == null) {
      return SystemMetaObject.NULL_META_OBJECT;
    } else {
      return new MetaObject(object, objectFactory, objectWrapperFactory, reflectorFactory);
    }
  }

  public ObjectFactory getObjectFactory() {
    return objectFactory;
  }

  public ObjectWrapperFactory getObjectWrapperFactory() {
    return objectWrapperFactory;
  }

  public ReflectorFactory getReflectorFactory() {
    return reflectorFactory;
  }

  public Object getOriginalObject() {
    return originalObject;
  }

  public String findProperty(String propName, boolean useCamelCaseMapping) {
    return objectWrapper.findProperty(propName, useCamelCaseMapping);
  }

  public String[] getGetterNames() {
    return objectWrapper.getGetterNames();
  }

  public String[] getSetterNames() {
    return objectWrapper.getSetterNames();
  }

  public Class<?> getSetterType(String name) {
    return objectWrapper.getSetterType(name);
  }

  public Class<?> getGetterType(String name) {
    return objectWrapper.getGetterType(name);
  }

  public boolean hasSetter(String name) {
    return objectWrapper.hasSetter(name);
  }

  public boolean hasGetter(String name) {
    return objectWrapper.hasGetter(name);
  }

  public Object getValue(String name) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    // name中是否含有. , 如people[0].name，要的是people的第一个对象中的name的值
    if (prop.hasNext()) {
      // 根据下标或者key获取MetaObject，递归判断子表达式 children ，获取值，if里的逻辑只是为了做递归，最终走的还是objectWrapper.get(prop)
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        return null;
      } else {
        return metaValue.getValue(prop.getChildren());
      }
    } else {
      // 核心方法，就是根据属性表达式，获取值
      return objectWrapper.get(prop);
    }
  }

  /**
   * 根据属性表达式set值
   * @param name
   * @param value
   */
  public void setValue(String name, Object value) {
    PropertyTokenizer prop = new PropertyTokenizer(name);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getIndexedName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        if (value == null) {
          // don't instantiate child path if value is null
          return;
        } else {
          metaValue = objectWrapper.instantiatePropertyValue(name, prop, objectFactory);
        }
      }
      metaValue.setValue(prop.getChildren(), value);
    } else {
      objectWrapper.set(prop, value);
    }
  }

  public MetaObject metaObjectForProperty(String name) {
    Object value = getValue(name);
    return MetaObject.forObject(value, objectFactory, objectWrapperFactory, reflectorFactory);
  }

  public ObjectWrapper getObjectWrapper() {
    return objectWrapper;
  }

  public boolean isCollection() {
    return objectWrapper.isCollection();
  }

  public void add(Object element) {
    objectWrapper.add(element);
  }

  public <E> void addAll(List<E> list) {
    objectWrapper.addAll(list);
  }

}
