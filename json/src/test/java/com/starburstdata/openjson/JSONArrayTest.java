/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.starburstdata.openjson;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * This black box test was written without inspecting the non-free org.json sourcecode.
 */
public class JSONArrayTest {
    @Test
    public void testEmptyArray() throws JSONException {
        JSONArray array = new JSONArray();
        assertEquals(0, array.length());
        assertEquals("", array.join(" AND "));
        try {
            array.get(0);
            fail();
        } catch (JSONException ignored) {
        }
        try {
            array.getBoolean(0);
            fail();
        } catch (JSONException ignored) {
        }

        assertEquals("[]", array.toString());
        assertEquals("[]", array.toString(4));

        // out of bounds is co-opted with defaulting
        assertTrue(array.isNull(0));
        assertNull(array.opt(0));
        assertFalse(array.optBoolean(0));
        assertTrue(array.optBoolean(0, true));

        // bogus (but documented) behaviour: returns null rather than an empty object!
        assertNull(array.toJSONObject(new JSONArray()));
    }

    @Test
    public void testEqualsAndHashCode() throws JSONException {
        JSONArray a = new JSONArray();
        JSONArray b = new JSONArray();
        assertEquals(a, b);
        assertEquals("equals() not consistent with hashCode()", a.hashCode(), b.hashCode());

        a.put(true);
        a.put(false);
        b.put(true);
        b.put(false);
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        b.put(true);
        assertNotEquals(a, b);
        assertTrue(a.hashCode() != b.hashCode());
    }

    @Test
    public void testBooleans() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(true);
        array.put(false);
        array.put(2, false);
        array.put(3, false);
        array.put(2, true);
        assertEquals("[true,false,true,false]", array.toString());
        assertEquals(4, array.length());
        assertEquals(Boolean.TRUE, array.get(0));
        assertEquals(Boolean.FALSE, array.get(1));
        assertEquals(Boolean.TRUE, array.get(2));
        assertEquals(Boolean.FALSE, array.get(3));
        assertFalse(array.isNull(0));
        assertFalse(array.isNull(1));
        assertFalse(array.isNull(2));
        assertFalse(array.isNull(3));
        assertTrue(array.optBoolean(0));
        assertFalse(array.optBoolean(1, true));
        assertTrue(array.optBoolean(2, false));
        assertFalse(array.optBoolean(3));
        assertEquals("true", array.getString(0));
        assertEquals("false", array.getString(1));
        assertEquals("true", array.optString(2));
        assertEquals("false", array.optString(3, "x"));
        assertEquals("[\n     true,\n     false,\n     true,\n     false\n]", array.toString(5));

        JSONArray other = new JSONArray();
        other.put(true);
        other.put(false);
        other.put(true);
        other.put(false);
        assertEquals(array, other);
        other.put(true);
        assertNotEquals(array, other);

        other = new JSONArray();
        other.put("true");
        other.put("false");
        other.put("truE");
        other.put("FALSE");
        assertNotEquals(array, other);
        assertNotEquals(other, array);
        assertTrue(other.getBoolean(0));
        assertFalse(other.optBoolean(1, true));
        assertTrue(other.optBoolean(2));
        assertFalse(other.getBoolean(3));
    }


    @Test
    public void testCoerceStringToNumber() throws JSONException {
        JSONArray array = new JSONArray();
        array.put("1");
        array.put("-1");
        assertEquals(1, array.getInt(0));
        assertEquals(-1, array.getInt(1));
    }

    @Test
    public void testCoerceStringToNumberWithFail() throws JSONException {
        JSONArray array = new JSONArray();
        array.put("not a number");
        try {
            array.getLong(0);
            fail();
        } catch (JSONException ignored) {
        }
        assertEquals(0, array.optLong(0));
    }


    // http://code.google.com/p/android/issues/detail?id=16411
    @Test
    public void testCoerceStringToBoolean() throws JSONException {
        JSONArray array = new JSONArray();
        array.put("maybe");
        try {
            array.getBoolean(0);
            fail();
        } catch (JSONException ignored) {
        }
        assertFalse(array.optBoolean(0));
        assertTrue(array.optBoolean(0, true));
    }

    @Test
    public void testNulls() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(3, null);
        array.put(0, JSONObject.NULL);
        assertEquals(4, array.length());
        assertEquals("[null,null,null,null]", array.toString());

        // there's 2 ways to represent null; each behaves differently!
        assertEquals(JSONObject.NULL, array.get(0));
        try {
            array.get(1);
            fail();
        } catch (JSONException ignored) {
        }
        try {
            array.get(2);
            fail();
        } catch (JSONException ignored) {
        }
        try {
            array.get(3);
            fail();
        } catch (JSONException ignored) {
        }
        assertEquals(JSONObject.NULL, array.opt(0));
        assertNull(array.opt(1));
        assertNull(array.opt(2));
        assertNull(array.opt(3));
        assertTrue(array.isNull(0));
        assertTrue(array.isNull(1));
        assertTrue(array.isNull(2));
        assertTrue(array.isNull(3));
        assertEquals("null", array.optString(0));
        assertEquals("", array.optString(1));
        assertEquals("", array.optString(2));
        assertEquals("", array.optString(3));
    }

    /**
     * Our behaviour is questioned by this bug:
     * http://code.google.com/p/android/issues/detail?id=7257
     */
    @Test
    public void testParseNullYieldsJSONObjectNull() throws JSONException {
        JSONArray array = new JSONArray("[\"null\",null]");
        array.put(null);
        assertEquals("null", array.get(0));
        assertEquals(JSONObject.NULL, array.get(1));
        try {
            array.get(2);
            fail();
        } catch (JSONException ignored) {
        }
        assertEquals("null", array.getString(0));
        assertEquals("null", array.getString(1));
        try {
            array.getString(2);
            fail();
        } catch (JSONException ignored) {
        }
    }

    @Test
    public void testNumbers() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(Double.MIN_VALUE);
        array.put(9223372036854775806L);
        array.put(Double.MAX_VALUE);
        array.put(-0d);
        JSONObject objElement = new JSONObject();
        array.put(objElement);
        JSONArray arrElement = new JSONArray();
        array.put(arrElement);
        array.put(Integer.MIN_VALUE);
        assertEquals(7, array.length());

        // toString() and getString(int) return different values for -0d
        assertEquals("[4.9E-324,9223372036854775806,1.7976931348623157E308,-0,{},[],-2147483648]", array.toString());

        assertEquals(Double.MIN_VALUE, array.get(0));
        assertEquals(9223372036854775806L, array.get(1));
        assertEquals(Double.MAX_VALUE, array.get(2));
        assertEquals(-0d, array.get(3));
        assertEquals(Double.MIN_VALUE, array.getDouble(0), 0);
        assertEquals(9.223372036854776E18, array.getDouble(1), 0);
        assertEquals(Double.MAX_VALUE, array.getDouble(2), 0);
        assertEquals(-0d, array.getDouble(3), 0);
        assertEquals(0, array.getLong(0));
        assertEquals(9223372036854775806L, array.getLong(1));
        assertEquals(Long.MAX_VALUE, array.getLong(2));
        assertEquals(0, array.getLong(3));
        assertEquals(0, array.getInt(0));
        assertEquals(-2, array.getInt(1));
        assertEquals(Integer.MAX_VALUE, array.getInt(2));
        assertEquals(0, array.getInt(3));
        assertEquals(Double.MIN_VALUE, array.opt(0));
        assertEquals(Double.MIN_VALUE, array.optDouble(0), 0);
        assertEquals(0, array.optLong(0, 1L));
        assertEquals(0, array.optInt(0, 1));
        assertEquals("4.9E-324", array.getString(0));
        assertEquals("9223372036854775806", array.getString(1));
        assertEquals("1.7976931348623157E308", array.getString(2));
        assertEquals(objElement, array.getJSONObject(4));
        assertEquals(arrElement, array.getJSONArray(5));
        assertEquals(Integer.MIN_VALUE, array.getInt(6));

        JSONArray other = new JSONArray();
        other.put(Double.MIN_VALUE);
        other.put(9223372036854775806L);
        other.put(Double.MAX_VALUE);
        other.put(-0d);
        other.put(objElement);
        other.put(arrElement);
        other.put(Integer.MIN_VALUE);
        assertEquals(array, other);
        other.put(0, 0L);
        other.put(6, Integer.MIN_VALUE);
        assertNotEquals(array, other);
    }

    @Test
    public void testStrings() throws JSONException {
        JSONArray array = new JSONArray();
        array.put("true");
        array.put("5.5");
        array.put("9223372036854775806");
        array.put("null");
        array.put("5\"8' tall");
        assertEquals(5, array.length());
        assertEquals("[\"true\",\"5.5\",\"9223372036854775806\",\"null\",\"5\\\"8' tall\"]",
                array.toString());

        // although the documentation doesn't mention it, join() escapes text and wraps
        // strings in quotes
        assertEquals("\"true\" \"5.5\" \"9223372036854775806\" \"null\" \"5\\\"8' tall\"",
                array.join(" "));

        assertEquals("true", array.get(0));
        assertEquals("null", array.getString(3));
        assertEquals("5\"8' tall", array.getString(4));
        assertEquals("true", array.opt(0));
        assertEquals("5.5", array.optString(1));
        assertEquals("9223372036854775806", array.optString(2, null));
        assertEquals("null", array.optString(3, "-1"));
        assertFalse(array.isNull(0));
        assertFalse(array.isNull(3));

        assertTrue(array.getBoolean(0));
        assertTrue(array.optBoolean(0));
        assertTrue(array.optBoolean(0, false));
        assertEquals(0, array.optInt(0));
        assertEquals(-2, array.optInt(0, -2));

        assertEquals(5.5d, array.getDouble(1), 0);
        assertEquals(5L, array.getLong(1));
        assertEquals(5, array.getInt(1));
        assertEquals(5, array.optInt(1, 3));

        // The last digit of the string is a 6 but getLong returns a 7. It's probably parsing as a
        // double and then converting that to a long. This is consistent with JavaScript.
        assertEquals(9223372036854775807L, array.getLong(2));
        assertEquals(9.223372036854776E18, array.getDouble(2), 0);
        assertEquals(Integer.MAX_VALUE, array.getInt(2));

        assertFalse(array.isNull(3));
        try {
            array.getDouble(3);
            fail();
        } catch (JSONException e) {
            // expected
        }
        assertEquals(Double.NaN, array.optDouble(3), 0);
        assertEquals(-1.0d, array.optDouble(3, -1.0d), 0);
    }

    @Test
    public void testJoin() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(null);
        assertEquals("null", array.join(" & "));
        array.put("\"");
        assertEquals("null & \"\\\"\"", array.join(" & "));
        array.put(5);
        assertEquals("null & \"\\\"\" & 5", array.join(" & "));
        array.put(true);
        assertEquals("null & \"\\\"\" & 5 & true", array.join(" & "));
        array.put(new JSONArray(Arrays.asList(true, false)));
        assertEquals("null & \"\\\"\" & 5 & true & [true,false]", array.join(" & "));
        array.put(new JSONObject(Collections.singletonMap("x", 6)));
        assertEquals("null & \"\\\"\" & 5 & true & [true,false] & {\"x\":6}", array.join(" & "));
    }

    @Test
    public void testJoinWithNull() throws JSONException {
        JSONArray array = new JSONArray(Arrays.asList(5, 6));
        assertEquals("5null6", array.join(null));
    }

    @Test
    public void testJoinWithSpecialCharacters() throws JSONException {
        JSONArray array = new JSONArray(Arrays.asList(5, 6));
        assertEquals("5\"6", array.join("\""));
    }

    @Test
    public void testToJSONObject() throws JSONException {
        JSONArray keys = new JSONArray();
        keys.put("a");
        keys.put("b");

        JSONArray values = new JSONArray();
        values.put(5.5d);
        values.put(false);

        JSONObject object = values.toJSONObject(keys);
        assertEquals(5.5d, object.get("a"));
        assertEquals(false, object.get("b"));

        keys.put(0, "a");
        values.put(0, 11.0d);
        assertEquals(5.5d, object.get("a"));
    }

    @Test
    public void testToJSONObjectWithNulls() throws JSONException {
        JSONArray keys = new JSONArray();
        keys.put("a");
        keys.put("b");

        JSONArray values = new JSONArray();
        values.put(5.5d);
        values.put(null);

        // null values are stripped!
        JSONObject object = values.toJSONObject(keys);
        assertEquals(1, object.length());
        assertFalse(object.has("b"));
        assertEquals("{\"a\":5.5}", object.toString());
    }

    @Test
    public void testToJSONObjectMoreNamesThanValues() throws JSONException {
        JSONArray keys = new JSONArray();
        keys.put("a");
        keys.put("b");
        JSONArray values = new JSONArray();
        values.put(5.5d);
        JSONObject object = values.toJSONObject(keys);
        assertEquals(1, object.length());
        assertEquals(5.5d, object.get("a"));
    }

    @Test
    public void testToJSONObjectMoreValuesThanNames() throws JSONException {
        JSONArray keys = new JSONArray();
        keys.put("a");
        JSONArray values = new JSONArray();
        values.put(5.5d);
        values.put(11.0d);
        JSONObject object = values.toJSONObject(keys);
        assertEquals(1, object.length());
        assertEquals(5.5d, object.get("a"));
    }

    @Test
    public void testToJSONObjectNullKey() throws JSONException {
        JSONArray keys = new JSONArray();
        keys.put(JSONObject.NULL);
        JSONArray values = new JSONArray();
        values.put(5.5d);
        JSONObject object = values.toJSONObject(keys);
        assertEquals(1, object.length());
        assertEquals(5.5d, object.get("null"));
    }

    @Test
    public void putCollection() {
        JSONArray array = new JSONArray();
        array.put(Arrays.asList(1, 2, 3));
        array.put(2, Arrays.asList(3, 2, 1));

        assertEquals(3, array.length());
        assertTrue(array.isNull(1));

        JSONArray list0 = array.getJSONArray(0);
        assertEquals(3, list0.length());
        assertEquals(1, list0.getInt(0));
        assertEquals(2, list0.getInt(1));
        assertEquals(3, list0.get(2));

        JSONArray list2 = array.getJSONArray(2);
        assertEquals(3, list2.length());
        assertEquals(3, list2.getInt(0));
        assertEquals(2, list2.getInt(1));
        assertEquals(1, list2.get(2));
    }

    @Test
    public void putCollectionNull() {
        JSONArray array = new JSONArray();
        Collection c = null;
        array.put(c);
        assertEquals(1, array.length());
        assertTrue(array.isNull(0));
    }

    @Test
    public void testPutUnsupportedNumbers() throws JSONException {
        JSONArray array = new JSONArray();

        try {
            array.put(Double.NaN);
            fail();
        } catch (JSONException e) {
            // expected
        }
        try {
            array.put(0, Double.NEGATIVE_INFINITY);
            fail();
        } catch (JSONException e) {
            // expected
        }
        try {
            array.put(0, Double.POSITIVE_INFINITY);
            fail();
        } catch (JSONException e) {
            // expected
        }
    }

    @Test
    public void testPutUnsupportedNumbersAsObject() throws JSONException {
        JSONArray array = new JSONArray();
        array.put(Double.valueOf(Double.NaN));
        array.put(Double.valueOf(Double.NEGATIVE_INFINITY));
        array.put(Double.valueOf(Double.POSITIVE_INFINITY));
        assertNull(array.toString());
    }

    /**
     * Although JSONArray is usually defensive about which numbers it accepts,
     * it doesn't check inputs in its constructor.
     */
    @Test
    public void testCreateWithUnsupportedNumbers() throws JSONException {
        JSONArray array = new JSONArray(Arrays.asList(5.5, Double.NaN));
        assertEquals(2, array.length());
        assertEquals(5.5, array.getDouble(0), 0);
        assertEquals(Double.NaN, array.getDouble(1), 0);
    }

    @Test
    public void testToStringWithUnsupportedNumbers() throws JSONException {
        // when the array contains an unsupported number, toString returns null!
        JSONArray array = new JSONArray(Arrays.asList(5.5, Double.NaN));
        assertNull(array.toString());
    }

    @Test
    public void testToStringWithNulls() throws JSONException {
        // when the array contains an unsupported number, toString returns null!
        JSONArray array = new JSONArray(Arrays.asList(
                new JSONObject()
                    .put("a", "A String")
                    .put("n", 666)
                    .put("null", null)
                    .put("o", new JSONObject()
                            .put("b", "B String")
                            .put("null", null)
                            .put("bool", false))
                , new JSONObject()
                    .put("a", "A String")
                    .put("n", 666)
                    .put("null", null)
                ));
        assertEquals("[{\"a\":\"A String\",\"n\":666,\"o\":{\"b\":\"B String\",\"bool\":false}},{\"a\":\"A String\",\"n\":666}]", array.toString());
    }

    @Test
    public void testToString() {
        JSONObjectTest.MyPojo2 myPojo = new JSONObjectTest.MyPojo2();

        String json = new JSONArray().put(myPojo).toString();

        assertEquals("[{\"myProp3\":\"value3\",\"myProp4\":\"value4\",\"myProp5\":\"value5\"}]", json);
    }

    @Test
    public void testEmptyCollectionConstructor() throws JSONException {
        JSONArray array1 = new JSONArray(Collections.emptyList());
        assertEquals(0, array1.length());

        JSONArray array2 = new JSONArray((Collection) null);
        assertEquals(0, array2.length());
    }

    @Test
    public void testListConstructorCopiesContents() throws JSONException {
        // have to use asList instead of Collections.singleton() to allow mutation
        //noinspection ArraysAsListWithZeroOrOneArgument
        List<Object> contents = Arrays.<Object>asList(5);
        JSONArray array = new JSONArray(contents);
        contents.set(0, 10);
        assertEquals(5, array.get(0));
    }

    @Test(expected = NullPointerException.class)
    public void testObjectConstructorNull() throws JSONException {
        new JSONArray((Object) null);
    }

    @Test
    public void testTokenerConstructor() throws JSONException {
        JSONArray object = new JSONArray(new JSONTokener("[false]"));
        assertEquals(1, object.length());
        assertEquals(false, object.get(0));
    }

    @Test(expected = JSONException.class)
    public void testTokenerConstructorWrongType() throws JSONException {
        new JSONArray(new JSONTokener("{\"foo\": false}"));
    }

    @Test(expected = NullPointerException.class)
    public void testTokenerConstructorNull() throws JSONException {
        new JSONArray((JSONTokener) null);
    }

    @Test
    public void testTokenerConstructorParseFail() {
        try {
            new JSONArray(new JSONTokener("["));
            fail();
        } catch (JSONException ignored) {
        } catch (StackOverflowError e) {
            fail("Stack overflowed on input: \"[\"");
        }
    }

    @Test
    public void testStringConstructor() throws JSONException {
        JSONArray object = new JSONArray("[false]");
        assertEquals(1, object.length());
        assertEquals(false, object.get(0));
    }

    @Test
    public void testStringConstructorWrongType() throws JSONException {
        try {
            new JSONArray("{\"foo\": false}");
            fail();
        } catch (JSONException ignored) {
        }
        try {
            new JSONArray(new Object());
            fail();
        } catch (JSONException ignored) {
        }
    }

    @Test(expected = NullPointerException.class)
    public void testStringConstructorNull() throws JSONException {
        new JSONArray((String) null);
    }

    @Test
    public void testStringConstructorParseFail() {
        try {
            new JSONArray("[");
            fail();
        } catch (JSONException ignored) {
        } catch (StackOverflowError e) {
            fail("Stack overflowed on input: \"[\"");
        }
    }

    @Test
    public void testCreate() throws JSONException {
        JSONArray array = new JSONArray(Arrays.asList(5.5, true));
        assertEquals(2, array.length());
        assertEquals(5.5, array.getDouble(0), 0.0);
        assertEquals(true, array.get(1));
        assertEquals("[5.5,true]", array.toString());
    }

    @Test
    public void testAccessOutOfBounds() throws JSONException {
        JSONArray array = new JSONArray();
        array.put("foo");
        assertNull(array.opt(3));
        assertNull(array.opt(-3));
        assertEquals("", array.optString(3));
        assertEquals("", array.optString(-3));
        try {
            array.get(3);
            fail();
        } catch (JSONException ignored) {
        }
        try {
            array.get(-3);
            fail();
        } catch (JSONException ignored) {
        }
        try {
            array.getString(3);
            fail();
        } catch (JSONException ignored) {
        }
        try {
            array.getString(-3);
            fail();
        } catch (JSONException ignored) {
        }
    }

    @Test
    public void test_remove() {
        JSONArray a = new JSONArray();
        assertNull(a.remove(-1));
        assertNull(a.remove(0));

        a.put("hello");
        assertNull(a.remove(-1));
        assertNull(a.remove(1));
        assertEquals("hello", a.remove(0));
        assertNull(a.remove(0));
    }

    enum MyEnum {A, B, C}

    // https://code.google.com/p/android/issues/detail?id=62539
    // but changed in open-json to return toString for all enums
    @Test
    public void testEnums() {
        // This works because it's in java.* and any class in there falls back to toString.
        JSONArray a1 = new JSONArray(java.lang.annotation.RetentionPolicy.values());
        assertEquals("[\"SOURCE\",\"CLASS\",\"RUNTIME\"]", a1.toString());

        // This doesn't because it's not.
        JSONArray a2 = new JSONArray(MyEnum.values());
        assertEquals("[\"A\",\"B\",\"C\"]", a2.toString());
    }

    @Test
    public void testIterator() {
        JSONArray a = new JSONArray();
        a.put(1234);
        a.put("foo");
        Iterator<Object> iterator = a.iterator();
        assertEquals(1234, iterator.next());
        assertEquals("foo", iterator.next());
        assertFalse(iterator.hasNext());
    }
}
