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
package org.apache.ibatis.reflection.wrapper;

import java.util.List;

import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.property.PropertyTokenizer;

/**
 * @author Clinton Begin
 */
public interface ObjectWrapper {

  /**
   * 根据属性名称获取对象属性值
   *  1.如果封装的是普通的Bean对象，则调用相应属性的getter方法
   *  2.如果封装的集合类，则获取指定key或下标对应的value
   * @param prop
   * @return
   */
  Object get(PropertyTokenizer prop);

  /**
   * 设置对象属性值
   * @param prop
   * @param value
   */
  void set(PropertyTokenizer prop, Object value);

  /**
   * 查询属性名称
   * @param name
   * @param useCamelCaseMapping
   * @return
   */
  String findProperty(String name, boolean useCamelCaseMapping);

  /**
   * 获取对象的可读属性数组
   * @return
   */
  String[] getGetterNames();

  /**
   * 获取对象的可写属性数组
   * @return
   */
  String[] getSetterNames();

  /**
   * 根据属性表达式获取对应的setter方法的参数类型（属性可为：peoples[0].name）
   * @param name
   * @return
   */
  Class<?> getSetterType(String name);

  /**
   * 根据属性表达式获取对应的getter方法的返回值类型(属性可为：peoples[0].name)
   * @param name
   * @return
   */
  Class<?> getGetterType(String name);

  /**
   * 是否有该属性表达式对应的setter方法
   * @param name
   * @return
   */
  boolean hasSetter(String name);

  /**
   * 是否有该属性表达式对应的getter方法
   * @param name
   * @return
   */
  boolean hasGetter(String name);

  /**
   *
   *   根据属性表达式实例化对象，并set到当前对象
   * 	 主要作用于初始化对象属性也是对象的场景
   * @param name
   * @param prop
   * @param objectFactory
   * @return
   */
  MetaObject instantiatePropertyValue(String name, PropertyTokenizer prop, ObjectFactory objectFactory);


  // 以下方法是针对collection
  boolean isCollection();

  void add(Object element);

  <E> void addAll(List<E> element);

}
