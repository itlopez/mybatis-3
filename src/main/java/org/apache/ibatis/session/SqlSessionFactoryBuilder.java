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
package org.apache.ibatis.session;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Properties;

import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.exceptions.ExceptionFactory;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.session.defaults.DefaultSqlSessionFactory;

/**
 * Builds {@link SqlSession} instances.
 *
 * @author Clinton Begin
 */
public class SqlSessionFactoryBuilder {

  public SqlSessionFactory build(Reader reader) {
    return build(reader, null, null);
  }

  public SqlSessionFactory build(Reader reader, String environment) {
    return build(reader, environment, null);
  }

  public SqlSessionFactory build(Reader reader, Properties properties) {
    return build(reader, null, properties);
  }

  /**
   * 	<environments default="development">
   * 		<environment id="development-hsql">
   * 			<transactionManager type="JDBC" />
   * 			<dataSource type="UNPOOLED">
   * 				<property name="driver" value="org.hsqldb.jdbcDriver" />
   * 				<property name="url" value="jdbc:hsqldb:mem:repeatable" />
   * 				<property name="username" value="sa" />
   * 			</dataSource>
   * 		</environment>
   * 		<environment id="development-derby">
   * 			<transactionManager type="JDBC" />
   * 			<dataSource type="UNPOOLED">
   * 				<property name="driver" value="org.apache.derby.jdbc.EmbeddedDriver" />
   * 				<property name="url" value="jdbc:derby:target/derby/repeatable;create=true" />
   * 				<property name="username" value="" />
   * 			</dataSource>
   * 		</environment>
   * 		<environment id="development-h2">
   * 			<transactionManager type="JDBC" />
   * 			<dataSource type="UNPOOLED">
   * 				<property name="driver" value="org.h2.Driver" />
   * 				<property name="url" value="jdbc:h2:mem:repeatable;DB_CLOSE_DELAY=-1" />
   * 				<property name="username" value="sa" />
   * 			</dataSource>
   * 		</environment>
   * 核心：sqlSessionFactory的入口，此处需要读取mybatis-config的配置，并解析成Configuration类配置
   * @param reader
   * @param environment 如上：这里的enviroment指的是development-hsql、development-derby、development-h2
   * @param properties 如上：属性指的是：driver、url、username
   * @return
   */
  public SqlSessionFactory build(Reader reader, String environment, Properties properties) {
    try {
      XMLConfigBuilder parser = new XMLConfigBuilder(reader, environment, properties);
      // parser.parse()解析成Configuration类
      return build(parser.parse());
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
      ErrorContext.instance().reset();
      try {
        reader.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }

  public SqlSessionFactory build(InputStream inputStream) {
    return build(inputStream, null, null);
  }

  public SqlSessionFactory build(InputStream inputStream, String environment) {
    return build(inputStream, environment, null);
  }

  public SqlSessionFactory build(InputStream inputStream, Properties properties) {
    return build(inputStream, null, properties);
  }

  public SqlSessionFactory build(InputStream inputStream, String environment, Properties properties) {
    try {
      XMLConfigBuilder parser = new XMLConfigBuilder(inputStream, environment, properties);
      return build(parser.parse());
    } catch (Exception e) {
      throw ExceptionFactory.wrapException("Error building SqlSession.", e);
    } finally {
      ErrorContext.instance().reset();
      try {
        inputStream.close();
      } catch (IOException e) {
        // Intentionally ignore. Prefer previous error.
      }
    }
  }

  public SqlSessionFactory build(Configuration config) {
    return new DefaultSqlSessionFactory(config);
  }

}
