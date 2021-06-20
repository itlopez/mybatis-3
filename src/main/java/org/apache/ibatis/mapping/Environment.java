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
package org.apache.ibatis.mapping;

import javax.sql.DataSource;

import org.apache.ibatis.transaction.TransactionFactory;

/**
 * @author Clinton Begin
 *
 *
 *  environments里的default属性表示默认的数据库环境，与某个environment的id相对应。
 *
 *  1)environment环境变量
 *  environment通过id属性与其他数据库环境区别。它有两个子节点：
 *  a)transactionManager事务管理器
 *     在MyBatis中有两种事务管理器类型（也就是type=”[JDBC|MANAGED]”）：
 *     JDBC–这个配置直接简单使用了JDBC的提交和回滚设置。它依赖于从数据源得到的连接来管理事务范围。
 *     MANAGED–这个配置几乎没做什么。它从来不提交或回滚一个连接。而它会让容器来管理事务的整个生命周期（比如Spring或JEE应用服务器的上下文）
 *
 *  b)dataSource数据源
 *  在MyBatis中有三种数据源类型（也就是type=”[UNPOOLED | POOLED| JNDI]”）：
 *     UNPOOLED –这个数据源的实现是每次被请求时简单打开和关闭连接，需要配置的属性：
 *     driver – 这是JDBC驱动的Java类的完全限定名
 *     url – 这是数据库的JDBC URL地址。
 *     username – 登录数据库的用户名。
 *     password – 登录数据库的密码。
 *     defaultTransactionIsolationLevel – 默认的连接事务隔离级别。
 *     POOLED –mybatis实现的简单的数据库连接池类型，它使得数据库连接可被复用，不必在每次请求时都去创建一个物理的连接。
 *     JNDI – 通过jndi从tomcat之类的容器里获取数据源。
 *      <environments default="development">
 *          <environment id="development">
 *              <transactionManager type="JDBC" />
 *              <dataSource type="POOLED">
 *                  <property name="driver" value="${spring.datasource.driver-class-name}"/>
 *                  <property name="url" value="${spring.datasource.url}"/>
 *                  <property name="username" value="${spring.datasource.username}"/>
 *                  <property name="password" value="${spring.datasource.password}"/>
 *              </dataSource>
 *          </environment>
 *      </environments>
 *
 */
public final class Environment {
  private final String id;
  private final TransactionFactory transactionFactory;
  private final DataSource dataSource;

  public Environment(String id, TransactionFactory transactionFactory, DataSource dataSource) {
    if (id == null) {
      throw new IllegalArgumentException("Parameter 'id' must not be null");
    }
    if (transactionFactory == null) {
      throw new IllegalArgumentException("Parameter 'transactionFactory' must not be null");
    }
    this.id = id;
    if (dataSource == null) {
      throw new IllegalArgumentException("Parameter 'dataSource' must not be null");
    }
    this.transactionFactory = transactionFactory;
    this.dataSource = dataSource;
  }

  /**
   * builder模式
   */
  public static class Builder {
    private final String id;
    private TransactionFactory transactionFactory;
    private DataSource dataSource;

    public Builder(String id) {
      this.id = id;
    }

    public Builder transactionFactory(TransactionFactory transactionFactory) {
      this.transactionFactory = transactionFactory;
      return this;
    }

    public Builder dataSource(DataSource dataSource) {
      this.dataSource = dataSource;
      return this;
    }

    public String id() {
      return this.id;
    }

    public Environment build() {
      return new Environment(this.id, this.transactionFactory, this.dataSource);
    }

  }

  public String getId() {
    return this.id;
  }

  public TransactionFactory getTransactionFactory() {
    return this.transactionFactory;
  }

  public DataSource getDataSource() {
    return this.dataSource;
  }

}
