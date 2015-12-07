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

package wtf.cowbay.libraven.bencode;

import org.apache.commons.codec.binary.Hex;
import org.junit.Assert;
import org.junit.Test;
import wtf.cowbay.libraven.compact.CompactCoder;

import java.util.List;


public class CompactCoderChecker {
    @Test
    public void checkCompact() {
        System.out.println((byte) 0x80);
        byte[] src = {0x0A, 0x0A, 0x0A, 0x05, 0x00, (byte) 0x80};
        List<String> result = CompactCoder.compact2strings(src);
        for (String r : result) {
            System.out.println(r);
        }

        byte[] compacts = CompactCoder.strings2compact(result);

        Assert.assertArrayEquals(src, compacts);

        String hexResult = Hex.encodeHexString(compacts);
        System.out.println(hexResult);

        result = CompactCoder.compact2strings(compacts);
        Assert.assertEquals(result.get(0), "10.10.10.5:128");
    }
}
