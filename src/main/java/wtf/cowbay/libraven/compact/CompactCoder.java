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

package wtf.cowbay.libraven.compact;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CompactCoder {
    public static final Pattern IP_ADDRESS_WITH_PORT
            = Pattern.compile(
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))\\:(\\d+)");

    public static List<String> compact2strings(String hexString) throws DecoderException {
        return compact2strings(Hex.decodeHex(hexString.toCharArray()));
    }

    public static List<String> compact2strings(byte[] byteArray) {
        if (byteArray.length % 6 != 0) {
            throw new IllegalArgumentException("length of byte array must be multiples of 6");
        }

        int i = 0;
        StringBuffer buf = new StringBuffer();
        ArrayList<String> result = new ArrayList<String>();
        while (i < byteArray.length) {
            int port = ((byteArray[i + 4] & 0xFF) << 8) | (byteArray[i + 5] & 0xFF);
            buf.setLength(0);
            buf.append(byteArray[0]).append(".");
            buf.append(byteArray[1]).append(".");
            buf.append(byteArray[2]).append(".");
            buf.append(byteArray[3]).append(":");
            buf.append(port);

            result.add(buf.toString());
            i += 6;
        }

        return result;
    }

    public static byte[] strings2compact(List<String> ips) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        for (String ip : ips) {
            Matcher matcher = IP_ADDRESS_WITH_PORT.matcher(ip);

            if (!matcher.find()) {
                throw new IllegalArgumentException(ip + " is not a valide ip address(with port)");
            } else {
                out.write(Byte.parseByte(matcher.group(2)));
                out.write(Byte.parseByte(matcher.group(3)));
                out.write(Byte.parseByte(matcher.group(4)));
                out.write(Byte.parseByte(matcher.group(5)));

                //port
                int port = Integer.parseInt(matcher.group(6));
                out.write(port >> 8);
                out.write(port & 0xFF);
            }
        }

        return out.toByteArray();
    }
}
