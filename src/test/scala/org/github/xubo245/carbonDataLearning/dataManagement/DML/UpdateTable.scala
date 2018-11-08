/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.github.xubo245.carbonDataLearning.dataManagement.DML

import java.io.File

import org.apache.carbondata.core.constants.CarbonCommonConstants
import org.apache.carbondata.core.util.CarbonProperties
import org.apache.spark.sql.SparkSession

object UpdateTable {
  def main(args: Array[String]): Unit = {
    val rootPath = new File(this.getClass.getResource("/").getPath
      + "../../").getCanonicalPath
    val storeLocation = s"$rootPath/examples/spark2/target/store"
    val warehouse = s"$rootPath/examples/spark2/target/warehouse"
    val metastoredb = s"$rootPath/examples/spark2/target"

    CarbonProperties.getInstance()
      .addProperty(CarbonCommonConstants.CARBON_TIMESTAMP_FORMAT, "yyyy/MM/dd HH:mm:ss")
      .addProperty(CarbonCommonConstants.CARBON_DATE_FORMAT, "yyyy/MM/dd")

    import org.apache.spark.sql.CarbonSession._
    val carbon = SparkSession
      .builder()
      .master("local")
      .appName("CarbonSessionExample")
      .config("spark.sql.warehouse.dir", warehouse)
      .config("spark.driver.host", "localhost")
      .getOrCreateCarbonSession(storeLocation, metastoredb)
    carbon.sparkContext.setLogLevel("ERROR")
    import carbon._
    val path = s"$rootPath/src/main/resources/sample.csv"

    sql("DROP TABLE IF EXISTS test_table")
    sql("CREATE TABLE IF NOT EXISTS test_table(id string,name string,city string,age Int) STORED BY 'carbondata' ")
    carbon.sql(s"""LOAD DATA INPATH '$path' INTO TABLE test_table""")

    val path2 = s"$rootPath/src/main/resources/sample2.csv"

    sql("DROP TABLE IF EXISTS test_table2")
    sql("CREATE TABLE IF NOT EXISTS test_table2(id string,name string,city string,age Int) STORED BY 'carbondata' ")
    carbon.sql(s"""LOAD DATA INPATH '$path2' INTO TABLE test_table2""")


    carbon.sql("SELECT * FROM test_table").show()
    sql("UPDATE test_table SET (test_table.age) = (11) where test_table.id='1'").show() // need to show
    sql("SELECT * FROM test_table").show()
    sql("UPDATE test_table SET (city,age) = ('changsha',10) where test_table.id='1'").show() // need to show
    sql("SELECT * FROM test_table").show()
    sql("UPDATE test_table SET (city,age) = ('beijing',age+1)").show() // need to show
    sql("SELECT * FROM test_table").show()


    sql("UPDATE test_table SET (city,age) = ('shanghai',age+10) where test_table.id='1' and EXISTS (select * from test_table o where o.age>10)").show() // need to show
    sql("SELECT * FROM test_table").show()

    sql("UPDATE test_table SET (city,age) = ('shanghai',age+10) where test_table.id='1' and EXISTS (select * from test_table o where o.age>100)").show() // need to show
    sql("SELECT * FROM test_table").show()

    //    carbon.sql("SELECT * FROM test_table2").show()
    //    sql("UPDATE test_table d SET (age) = (select s.age from test_table s where d.city=s.city) where EXISTS (select * from test_table o where o.age>30)").show()   // need to show
    //    sql("SELECT * FROM test_table").show()
    //    sc.stop()
    carbon.stop()

  }

}
