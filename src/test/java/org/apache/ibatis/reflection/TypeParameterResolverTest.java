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

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.*;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.ibatis.reflection.typeparam.Calculator;
import org.apache.ibatis.reflection.typeparam.Calculator.SubCalculator;
import org.apache.ibatis.reflection.typeparam.Level0Mapper;
import org.apache.ibatis.reflection.typeparam.Level0Mapper.Level0InnerMapper;
import org.apache.ibatis.reflection.typeparam.Level1Mapper;
import org.apache.ibatis.reflection.typeparam.Level2Mapper;
import org.junit.jupiter.api.Test;

class TypeParameterResolverTest {

  // 1、包装类型、基本类型、原始类型，用TypeParameterResolver返回来的，保持不变
  @Test
  void testReturn_Lv0SimpleClass() throws Exception {
    Class<?> clazz = Level0Mapper.class;
    Method method = clazz.getMethod("simpleSelect");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Double.class, result);
  }

  @Test
  void testReturn_SimpleVoid() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectVoid", Integer.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(void.class, result);
  }

  @Test
  void testReturn_SimplePrimitive() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectPrimitive", int.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(double.class, result);
  }

  @Test
  void testReturn_SimpleClass() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelect");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Double.class, result);
  }

  // 2、ParameterizedType表示的是参数化类型，例如List<String>、Map<Integer，String>、Service<User>这种带有泛型的类型
          //例子：1、Calculator.java中，类定义了T,即Calculator<T>，同理Map<K,V>、List<E>这些都有
  //      2.1、Type getRawType()一一返回参数化类型中的原始类型，例如List<String ＞ 的原始类型为List 。
  //      2.2、Type[] getActualTypeArguments()一一获取参数化类型的类型变量或是实际类型列
  //            表，例如Map<Integer, String＞ 的实际泛型列表Integer 和String 。需要注意的是，
  //            该列表的元素类型都是Type ，也就是说，可能存在多层嵌套的情况。

  @Test
  void testReturn_SimpleList() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectList");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(List.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertEquals(Double.class, paramType.getActualTypeArguments()[0]);
  }

  @Test
  void testReturn_SimpleMap() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectMap");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(Map.class, paramType.getRawType());
    assertEquals(2, paramType.getActualTypeArguments().length);
    assertEquals(Integer.class, paramType.getActualTypeArguments()[0]);
    assertEquals(Double.class, paramType.getActualTypeArguments()[1]);
  }

  // 3、WildcardType表示通配符泛型，例如? extends Number和? super Integer。
  //        Type[] getUpperBounds()破折号泛型变量的上界。
  //        Type[] getLowerBounds()破折号泛型变量的下界。
  @Test
  void testReturn_SimpleWildcard() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectWildcard");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(List.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertTrue(paramType.getActualTypeArguments()[0] instanceof WildcardType);
    WildcardType wildcard = (WildcardType) paramType.getActualTypeArguments()[0];
    assertEquals(String.class, wildcard.getUpperBounds()[0]);
  }

  @Test
  void testReturn_SimpleArray() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectArray");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(resultClass.isArray());
    assertEquals(String.class, resultClass.getComponentType());
  }

  @Test
  void testReturn_SimpleArrayOfArray() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectArrayOfArray");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(resultClass.isArray());
    assertTrue(resultClass.getComponentType().isArray());
    assertEquals(String.class, resultClass.getComponentType().getComponentType());
  }

  // 注：<K extends Calculator<?>> K simpleSelectTypeVar()中，K实际上是继承 Calculator<?>,因为Caculator是 Calculator<T>,所以是ParameterizedType
  //而？代表实际类型是通配符泛型
  @Test
  void testReturn_SimpleTypeVar() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectTypeVar");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(Calculator.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertTrue(paramType.getActualTypeArguments()[0] instanceof WildcardType);
  }

  @Test
  void testReturn_Lv1Class() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(String.class, result);
  }

  @Test
  void testReturnM() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectM", Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(TypeVariable.class, result);
  }



  @Test
  void testReturn_Lv2CustomClass() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectCalculator", Calculator.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(Calculator.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
  }

  @Test
  void testReturn_Lv2CustomClassList() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectCalculatorList");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramTypeOuter = (ParameterizedType) result;
    assertEquals(List.class, paramTypeOuter.getRawType());
    assertEquals(1, paramTypeOuter.getActualTypeArguments().length);
    ParameterizedType paramTypeInner = (ParameterizedType) paramTypeOuter.getActualTypeArguments()[0];
    assertEquals(Calculator.class, paramTypeInner.getRawType());
    assertEquals(Date.class, paramTypeInner.getActualTypeArguments()[0]);
  }

  @Test
  void testReturn_Lv0InnerClass() throws Exception {
    Class<?> clazz = Level0InnerMapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Float.class, result);
  }

  @Test
  void testReturn_Lv2Class() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(String.class, result);
  }

  @Test
  void testReturn_Lv1List() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectList", Object.class, Object.class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType type = (ParameterizedType) result;
    assertEquals(List.class, type.getRawType());
    assertEquals(1, type.getActualTypeArguments().length);
    assertEquals(String.class, type.getActualTypeArguments()[0]);
  }

  @Test
  void testReturn_Lv1Array() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectArray", List[].class);
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(resultClass.isArray());
    assertEquals(String.class, resultClass.getComponentType());
  }

  @Test
  void testReturn_Lv2ArrayOfArray() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectArrayOfArray");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof Class);
    Class<?> resultClass = (Class<?>) result;
    assertTrue(result instanceof Class);
    assertTrue(resultClass.isArray());
    assertTrue(resultClass.getComponentType().isArray());
    assertEquals(String.class, resultClass.getComponentType().getComponentType());
  }

  @Test
  void testReturn_Lv2ArrayOfList() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectArrayOfList");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof GenericArrayType);
    GenericArrayType genericArrayType = (GenericArrayType) result;
    assertTrue(genericArrayType.getGenericComponentType() instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) genericArrayType.getGenericComponentType();
    assertEquals(List.class, paramType.getRawType());
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
  }

  @Test
  void testReturn_Lv2WildcardList() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectWildcardList");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType type = (ParameterizedType) result;
    assertEquals(List.class, type.getRawType());
    assertEquals(1, type.getActualTypeArguments().length);
    assertTrue(type.getActualTypeArguments()[0] instanceof WildcardType);
    WildcardType wildcard = (WildcardType) type.getActualTypeArguments()[0];
    assertEquals(0, wildcard.getLowerBounds().length);
    assertEquals(1, wildcard.getUpperBounds().length);
    assertEquals(String.class, wildcard.getUpperBounds()[0]);
  }

  @Test
  void testReturn_LV2Map() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectMap");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertTrue(result instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result;
    assertEquals(Map.class, paramType.getRawType());
    assertEquals(2, paramType.getActualTypeArguments().length);
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
    assertEquals(Integer.class, paramType.getActualTypeArguments()[1]);
  }

  @Test
  void testReturn_Subclass() throws Exception {
    Class<?> clazz = SubCalculator.class;
    Method method = clazz.getMethod("getId");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(String.class, result);
  }

  @Test
  void testParam_Primitive() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("simpleSelectPrimitive", int.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(1, result.length);
    assertEquals(int.class, result[0]);
  }

  @Test
  void testParam_Simple() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("simpleSelectVoid", Integer.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(1, result.length);
    assertEquals(Integer.class, result[0]);
  }

  @Test
  void testParam_Lv1Single() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(1, result.length);
    assertEquals(String.class, result[0]);
  }

  @Test
  void testParam_Lv2Single() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("select", Object.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(1, result.length);
    assertEquals(String.class, result[0]);
  }

  @Test
  void testParam_Lv2Multiple() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectList", Object.class, Object.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(2, result.length);
    assertEquals(Integer.class, result[0]);
    assertEquals(String.class, result[1]);
  }

  @Test
  void testParam_Lv2CustomClass() throws Exception {
    Class<?> clazz = Level2Mapper.class;
    Method method = clazz.getMethod("selectCalculator", Calculator.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(1, result.length);
    assertTrue(result[0] instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) result[0];
    assertEquals(Calculator.class, paramType.getRawType());
    assertEquals(1, paramType.getActualTypeArguments().length);
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
  }

  @Test
  void testParam_Lv1Array() throws Exception {
    Class<?> clazz = Level1Mapper.class;
    Method method = clazz.getMethod("selectArray", List[].class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertTrue(result[0] instanceof GenericArrayType);
    GenericArrayType genericArrayType = (GenericArrayType) result[0];
    assertTrue(genericArrayType.getGenericComponentType() instanceof ParameterizedType);
    ParameterizedType paramType = (ParameterizedType) genericArrayType.getGenericComponentType();
    assertEquals(List.class, paramType.getRawType());
    assertEquals(String.class, paramType.getActualTypeArguments()[0]);
  }

  @Test
  void testParam_Subclass() throws Exception {
    Class<?> clazz = SubCalculator.class;
    Method method = clazz.getMethod("setId", Object.class);
    Type[] result = TypeParameterResolver.resolveParamTypes(method, clazz);
    assertEquals(String.class, result[0]);
  }

  @Test
  void testReturn_Anonymous() throws Exception {
    Calculator<?> instance = new Calculator<Integer>();
    Class<?> clazz = instance.getClass();
    Method method = clazz.getMethod("getId");
    Type result = TypeParameterResolver.resolveReturnType(method, clazz);
    assertEquals(Object.class, result);
  }

  @Test
  void testField_GenericField() throws Exception {
    Class<?> clazz = SubCalculator.class;
    Class<?> declaredClass = Calculator.class;
    Field field = declaredClass.getDeclaredField("fld");
    Type result = TypeParameterResolver.resolveFieldType(field, clazz);
    assertEquals(String.class, result);
  }

  @Test
  void testReturnParam_WildcardWithUpperBounds() throws Exception {
    class Key {
    }
    @SuppressWarnings("unused")
    class KeyBean<S extends Key & Cloneable, T extends Key> {
      private S key1;
      private T key2;

      public S getKey1() {
        return key1;
      }

      public void setKey1(S key1) {
        this.key1 = key1;
      }

      public T getKey2() {
        return key2;
      }

      public void setKey2(T key2) {
        this.key2 = key2;
      }
    }
    Class<?> clazz = KeyBean.class;
    Method getter1 = clazz.getMethod("getKey1");
    assertEquals(Key.class, TypeParameterResolver.resolveReturnType(getter1, clazz));
    Method setter1 = clazz.getMethod("setKey1", Key.class);
    assertEquals(Key.class, TypeParameterResolver.resolveParamTypes(setter1, clazz)[0]);
    Method getter2 = clazz.getMethod("getKey2");
    assertEquals(Key.class, TypeParameterResolver.resolveReturnType(getter2, clazz));
    Method setter2 = clazz.getMethod("setKey2", Key.class);
    assertEquals(Key.class, TypeParameterResolver.resolveParamTypes(setter2, clazz)[0]);
  }

  @Test
  void testDeepHierarchy() throws Exception {
    @SuppressWarnings("unused")
    abstract class A<S> {
      protected S id;
      public S getId() { return this.id;}
      public void setId(S id) {this.id = id;}
    }
    abstract class B<T> extends A<T> {}
    abstract class C<U> extends B<U> {}
    class D extends C<Integer> {}
    Class<?> clazz = D.class;
    Method method = clazz.getMethod("getId");
    assertEquals(Integer.class, TypeParameterResolver.resolveReturnType(method, clazz));
    Field field = A.class.getDeclaredField("id");
    assertEquals(Integer.class, TypeParameterResolver.resolveFieldType(field, clazz));
  }

  @Test
  void shouldTypeVariablesBeComparedWithEquals() throws Exception {
    // #1794
    ExecutorService executor = Executors.newFixedThreadPool(2);
    Future<Type> futureA = executor.submit(() -> {
      Type retType = TypeParameterResolver.resolveReturnType(IfaceA.class.getMethods()[0], IfaceA.class);
      return ((ParameterizedType) retType).getActualTypeArguments()[0];
    });
    Future<Type> futureB = executor.submit(() -> {
      Type retType = TypeParameterResolver.resolveReturnType(IfaceB.class.getMethods()[0], IfaceB.class);
      return ((ParameterizedType) retType).getActualTypeArguments()[0];
    });
    assertEquals(AA.class, futureA.get());
    assertEquals(BB.class, futureB.get());
    executor.shutdown();
  }

  // @formatter:off
  class AA {}
  class BB {}
  interface IfaceA extends ParentIface<AA> {}
  interface IfaceB extends ParentIface<BB> {}
  interface ParentIface<T> {List<T> m();}
  // @formatter:on
}
