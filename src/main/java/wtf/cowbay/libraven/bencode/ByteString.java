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

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

public class ByteString {
    private byte[] value;

    public ByteString(int size) {
        value = new byte[size];
    }

    public ByteString(String str) {
        value = str.getBytes();
    }

    public ByteString(byte[] v) {
        value = v;
    }

    public void set(int index, byte v) {
        value[index] = v;
    }

    public void set(int startIndex, byte[] val) {
        System.arraycopy(val, 0, value, startIndex, val.length);
    }

    public byte[] value() {
        return Arrays.copyOf(value, value.length);
    }

    public String toString() {
        return new String(value);
    }

    public String toString(String encoding) throws UnsupportedEncodingException {
        return new String(value, encoding);
    }

    public int length() {
        return value.length;
    }
}
