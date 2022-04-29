/*======================================================================*
 * Copyright (c) 2011, OpenX Technologies, Inc. All rights reserved.    *
 *                                                                      *
 * Licensed under the New BSD License (the "License"); you may not use  *
 * this file except in compliance with the License. Unless required     *
 * by applicable law or agreed to in writing, software distributed      *
 * under the License is distributed on an "AS IS" BASIS, WITHOUT        *
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.     *
 * See the License for the specific language governing permissions and  *
 * limitations under the License. See accompanying LICENSE file.        *
 *======================================================================*/


package org.openx.data.jsonserde;

import io.starburst.openjson.JSONObject;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.objectinspector.MapObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.StructField;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.StringObjectInspector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.junit.Before;
import org.junit.Test;

import java.util.Properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 *
 * @author rcongiu
 */
public class JsonMapTest {
     static JsonSerDe instance;

  @Before
  public void setUp() throws Exception {
    initialize();
  }

  static public void initialize() throws Exception {
    instance = new JsonSerDe();
    Configuration conf = null;
    Properties tbl = new Properties();
    // from google video API
    tbl.setProperty(serdeConstants.LIST_COLUMNS, "country,languages,religions");
    tbl.setProperty(serdeConstants.LIST_COLUMN_TYPES, "string,string,map<string,string>".toLowerCase());

     instance.initialize(conf, tbl);
  }


  @Test
  public void testDeSerializeEmptyMap() throws Exception {
    // Test that timestamp object can be deserialized
    Writable[] wa = new Writable[] {
            new Text("{\"country\":\"Switzerland\",\"languages\":\"Italian\",\"religions\":\"\"}")
    };

    for (Writable w : wa ) {
      JSONObject result = (JSONObject) instance.deserialize(w);

      StructObjectInspector soi = (StructObjectInspector) instance.getObjectInspector();

      StructField sfr = soi.getStructFieldRef("religions");

      assertEquals(sfr.getFieldObjectInspector().getCategory(), ObjectInspector.Category.MAP);

      MapObjectInspector moi = (MapObjectInspector) sfr.getFieldObjectInspector();

      Object val = soi.getStructFieldData(result, sfr);

      assertEquals(-1, moi.getMapSize(val));
    }
    
  }


  @Test
  public void testDeSerializeMap() throws Exception {
    // Test that timestamp object can be deserialized
    Writable w = new Text("{\"country\":\"Switzerland\",\"languages\":\"Italian\",\"religions\": { \"f\": \"v\", \"n\":null} }");

    JSONObject result = (JSONObject) instance.deserialize(w);

    StructObjectInspector soi = (StructObjectInspector) instance.getObjectInspector();

    StructField sfr = soi.getStructFieldRef("religions");

    assertEquals(sfr.getFieldObjectInspector().getCategory(),ObjectInspector.Category.MAP);

    MapObjectInspector moi = (MapObjectInspector) sfr.getFieldObjectInspector();

    Object val =  soi.getStructFieldData(result, sfr) ;

    assertEquals(2, moi.getMapSize(val));
    assertEquals("v", moi.getMapValueElement(val, "f"));

    ObjectInspector voi = moi.getMapValueObjectInspector();
      assertTrue(voi instanceof StringObjectInspector);
      StringObjectInspector svoi = (StringObjectInspector) voi;
    assertNull( svoi.getPrimitiveJavaObject(moi.getMapValueElement(val,"n")));

  }


}
