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
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class BEncoder {

    /**
     * Encode a string using bencode
     *
     * @param val
     * @return
     */
    public static ByteString encode(String val) {
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

    /**
     * Encode a integer using bencode
     *
     * @param val
     * @return
     */
    public static ByteString encode(int val) {
        return encode((long) val);
    }

    /**
     * Encode a integer using bencode
     *
     * @param val
     * @return
     */
    public static ByteString encode(long val) {
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
    public static ByteString encode(List list) {
        if (list == null || list.size() == 0) {
            return null;
        }

        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        buf.write('l');
        for (Object obj : list) {
            if (obj instanceof List) {
                writeByteString(buf, encode((List) obj));
            } else if (obj instanceof Map) {
                writeByteString(buf, encode((Map) obj));
            } else if (obj instanceof Number) {
                if (obj instanceof Float || obj instanceof Double) {
                    throw new UnsupportedOperationException("List item cannot be floating point number");
                }

                writeByteString(buf, encode(((Number) obj).longValue()));
            } else if (obj instanceof String) {
                writeByteString(buf, encode((String) obj));
            } else {
                if (obj == null) {
                    throw new NullPointerException("List item cannot be null");
                }

                throw new UnsupportedOperationException("Unsupported class: " + obj.getClass() + " for list");
            }
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
    public static ByteString encode(Map map) {
        if (map == null || map.size() == 0) {
            return null;
        }

        if (!(map instanceof SortedMap)) {
            map = new TreeMap(map);
        }

        ByteArrayOutputStream buf = new ByteArrayOutputStream();

        buf.write('d');

        for (Object keyObj : map.keySet()) {
            String key = (String) keyObj;
            writeByteString(buf, encode(key));

            Object obj = map.get(key);
            if (obj instanceof List) {
                writeByteString(buf, encode((List) obj));
            } else if (obj instanceof Map) {
                writeByteString(buf, encode((Map) obj));
            } else if (obj instanceof Number) {
                if (obj instanceof Float || obj instanceof Double) {
                    throw new UnsupportedOperationException("List item cannot be floating point number");
                }

                writeByteString(buf, encode(((Number) obj).longValue()));
            } else if (obj instanceof String) {
                writeByteString(buf, encode((String) obj));
            } else {
                if (obj == null) {
                    throw new NullPointerException("List item cannot be null");
                }

                throw new UnsupportedOperationException("Unsupported class: " + obj.getClass() + " for list");
            }
        }

        buf.write('e');

        return new ByteString(buf.toString());

    }

}
