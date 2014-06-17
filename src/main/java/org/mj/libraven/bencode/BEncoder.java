/*
 * Copyright 2014 Julian Shen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mj.libraven.bencode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

public class BEncoder {

    public static <E> ByteString encode(E val) {
        if (val == null) {
            return null;
        }

        if (val instanceof String) {
            return _encode((String) val);
        } else if (val instanceof ByteString) {
            return _encode(((ByteString) val).value());
        } else if (val instanceof byte[]) {
            return _encode((byte[]) val);
        } else if (val instanceof Number) {
            if (val instanceof Float || val instanceof Double) {
                throw new UnsupportedOperationException("Cannot be floating point number");
            }
            return _encode(((Number) val).longValue());
        } else if (val instanceof List) {
            return _encode((List) val);
        } else if (val instanceof Map) {
            return _encode((Map) val);
        }

        throw new UnsupportedOperationException("Unsupported type");
    }

    /**
     * Encode a string using bencode
     *
     * @param val
     * @return
     */
    private static ByteString _encode(String val) {
        if (val == null) {
            return null;
        }
        StringBuffer buf = new StringBuffer();
        ByteString byteString = new ByteString(val);
        buf.append(byteString.length());
        buf.append(':');
        ByteString result = new ByteString(buf.length() + byteString.length());
        result.set(0, buf.toString().getBytes());
        result.set(buf.toString().getBytes().length, byteString.value());

        return result;
    }

    private static ByteString _encode(byte[] val) {
        if (val == null) {
            return null;
        }
        StringBuffer len = new StringBuffer(String.valueOf(val.length));
        len.append(":");

        ByteString result = new ByteString(len.length() + val.length);
        result.set(0, len.toString().getBytes());
        result.set(len.length(), val);

        return result;
    }


    /**
     * Encode a integer using bencode
     *
     * @param val
     * @return
     */
    private static ByteString _encode(long val) {
        StringBuffer buf = new StringBuffer();
        buf.append('i');
        buf.append(val);
        buf.append('e');

        return new ByteString(buf.toString());
    }

    private static void writeByteString(ByteArrayOutputStream out, ByteString bs) {
        if (out != null && bs != null) {
            try {
                out.write(bs.value());
            } catch (IOException e) {
                //ignore
            }
        }
    }

    /**
     * Encode a List using bencode. Items in list can be either string, integer, map, or another list.
     *
     * @param list
     * @return
     */
    private static ByteString _encode(List list) {
        if (list == null || list.size() == 0) {
            return null;
        }

        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        buf.write('l');
        for (Object obj : list) {
            writeByteString(buf, encode(obj));
        }
        buf.write('e');
        return new ByteString(buf.toString());

    }

    /**
     * Encode a Map using bencode. Items in list can be either string, integer, list, or another map. Key of this map should be a string.
     *
     * @param map
     * @return
     */
    private static ByteString _encode(Map map) {
        if (map == null || map.size() == 0) {
            return null;
        }

        if (!(map instanceof SortedMap)) {
            map = new TreeMap(map);
        }

        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        buf.write('d');

        TreeSet keySet = new TreeSet(map.keySet());
        for (Object keyObj : keySet) {
            String key = (String) keyObj;
            writeByteString(buf, encode(key));

            Object obj = map.get(key);
            ByteString bs = encode(obj);
            writeByteString(buf, bs);
        }

        buf.write('e');

        return new ByteString(buf.toByteArray());
    }

}
