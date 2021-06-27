/**
 *    Copyright 2009-2015 the original author or authors.
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
package org.apache.ibatis.datasource.pooled;

import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

/**
 * @author Clinton Begin
 */
public class PooledDataSourceFactory extends UnpooledDataSourceFactory {

  /**
   * 池化数据源工厂继承了非池化的数据源工厂，这里只是在构造器new了一个池化数据源
   */
  public PooledDataSourceFactory() {
    this.dataSource = new PooledDataSource();
  }

}
