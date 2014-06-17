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


import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;

public class BDecoder {
    BufferedInputStream in;

    public BDecoder(String str) {
        this(str.getBytes());
    }

    public BDecoder(byte[] bytes) {
        in = new BufferedInputStream(new ByteArrayInputStream(bytes));
    }

    public BDecoder(InputStream in) {
        this.in = new BufferedInputStream(in);
    }


    private ByteString decodeString() throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b = -1;

        while ((b = in.read()) != -1) {
            if (b - '0' < 10 && b - '0' >= 0) {
                out.write(b);
            } else if (b == ':') {
                break;
            } else {
                throw new IllegalStateException("Wrong string format");
            }
        }

        int strLen = Integer.parseInt(new String(out.toByteArray()));
        int i = 0;
        byte[] buf = new byte[strLen];

        ByteBuffer outBuf = ByteBuffer.allocate(strLen);

        while (i < strLen) {
            int n = in.read(buf);
            outBuf.put(buf, 0, n);
            i += n;
        }

        return new ByteString(outBuf.array());
    }

    private long decodeInteger() throws IOException {
        if (in.read() != 'i') {
            throw new IllegalStateException("Integer should start with 'i'");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int b = -1;
        boolean firstByte = true;

        while ((b = in.read()) != -1) {
            if (b - '0' < 10 && b - '0' >= 0) {
                firstByte = false;
                out.write(b);
            } else if (b == '-') {
                if (firstByte) {
                    firstByte = false;
                    out.write(b);
                } else {
                    throw new IllegalStateException("Wrong integer format");
                }
            } else if (b == 'e') {
                break;
            } else {
                throw new IllegalStateException("Wrong integer format");
            }
        }

        return Long.parseLong(out.toString());
    }

    private ArrayList decodeList() throws IOException {
        if (in.read() != 'l') {
            throw new IllegalStateException("List should start with 'l'");
        }

        ArrayList list = new ArrayList();
        int b = -1;
        in.mark(1);
        while ((b = in.read()) != -1) {
            in.reset();
            Object result;
            switch (b) {
                case 'i':
                    result = decodeInteger();
                    break;
                case 'l':
                    result = decodeList();
                    break;
                case 'd':
                    result = decodeMap();
                    break;
                case 'e':
                    in.read(); //consume the 'e'
                    return list;
                default:
                    result = decodeString();
            }

            if (result != null) {
                list.add(result);
            }
            in.mark(1);
        }

        return list; //end of stream without encouter an 'e'. treat as end of list
    }

    private HashMap decodeMap() throws IOException {
        if (in.read() != 'd') {
            throw new IllegalStateException("Dictionary should start with 'd'");
        }

        HashMap map = new HashMap();
        int b = -1;
        while (true) {
            in.mark(1);
            b = in.read();
            if (b == -1 || b == 'e') {
                break;
            }
            in.reset();

            ByteString keyResult = decodeString();
            String key = keyResult.toString();

            in.mark(1);
            b = in.read();
            in.reset();
            Object result;

            switch (b) {
                case 'i':
                    result = decodeInteger();
                    break;
                case 'l':
                    result = decodeList();
                    break;
                case 'd':
                    result = decodeMap();
                    break;
                default:
                    result = decodeString();
            }

            map.put(key, result);
        }

        return map;
    }

    public Object decode() throws IOException {
        //peek first byte
        in.mark(1);
        int c = in.read();
        in.reset();

        switch (c) {
            case 'i':
                return decodeInteger();
            case 'l':
                return decodeList();
            case 'd':
                return decodeMap();
            default:
                if (c - '0' < 10) {
                    return decodeString();
                } else {
                    throw new IllegalStateException("Wrong format");
                }
        }
    }

}
