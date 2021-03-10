package org.openx.data.udf;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hive.ql.exec.UDFArgumentException;
import org.apache.hadoop.hive.ql.metadata.HiveException;
import org.apache.hadoop.hive.ql.udf.generic.GenericUDF;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.objectinspector.ObjectInspector;
import org.apache.hadoop.hive.serde2.objectinspector.primitive.PrimitiveObjectInspectorFactory;
import org.openx.data.jsonserde.JsonSerDe;

import java.util.Properties;

public class JsonUDF extends GenericUDF {

    ObjectInspector inOi;
    JsonSerDe serde = new JsonSerDe();

    @Override
    public ObjectInspector initialize(ObjectInspector[] arguments) throws UDFArgumentException {
        if (arguments.length != 1) throw new UDFArgumentException("Function takes one argument");
        inOi = arguments[0];
        try {
            Properties props = new Properties();
            props.setProperty(serdeConstants.LIST_COLUMNS,"col1");
            props.setProperty(serdeConstants.LIST_COLUMN_TYPES, inOi.getTypeName());
            serde.initialize(new Configuration(), props);
        } catch (SerDeException ex) {
            throw new UDFArgumentException(ex);
        }
        return PrimitiveObjectInspectorFactory.javaStringObjectInspector;
    }

    @Override
    // The evaluate() method. The input is passed in as an array of DeferredObjects, so that
    // computation is not wasted on deserializing them if they're not actually used
    public Object evaluate(DeferredObject[] arguments) throws HiveException {

        Object obj = arguments[0].get();

        return serde.serializeField(obj, inOi);

    }

    @Override
    public String getDisplayString(String[] children) {
        assert children.length > 0;

        StringBuilder sb = new StringBuilder();
        sb.append("JsonUDF(");
        sb.append(children[0]);
        sb.append(")");

        return sb.toString();
    }
}
