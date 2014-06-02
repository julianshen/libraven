/*
 * Copyright [2014]
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

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class BCoderChecker {
    private List generateSimpleList() {
        ArrayList list = new ArrayList();
        list.add("1");
        list.add("23");
        list.add(123);
        list.add("4");
        list.add(0);
        return list;
    }

    private Map generateSimpleMap1() {
        Map map = new TreeMap();
        map.put("key1", "val1");
        map.put("key2", 2);
        map.put("key3", "val2");
        map.put("key4", 4);
        return map;
    }

    private Map generateSimpleMap2() {
        Map map = new HashMap();
        map.put("publisher", "bob");
        map.put("publisher-webpage", "www.example.com");
        map.put("publisher.location", "home");
        return map;
    }

    private Map generateComplexMap() {
        Map map = new HashMap();
        map.put("name", "Julian");
        List emails = new LinkedList();
        emails.add("a@example.com");
        emails.add("b@aaa.com");
        map.put("emails", emails);

        Map attach = new TreeMap();
        attach.put("a", 1);
        attach.put("b", 3);

        List c = new ArrayList();
        c.add(4);
        c.add(5);
        c.add("abc");
        attach.put("c", c);
        map.put("attach", attach);
        return map;
    }

    @Test
    public void checkEncodeBasicValue() {
        Assert.assertEquals("encode integer", "i10e", BEncoder.encode(10).toString());
        Assert.assertEquals("encode string", "7:bencode", BEncoder.encode("bencode").toString());
    }

    @Test
    public void checkEncodeSimpleList() {
        Assert.assertEquals("encode list", "l1:12:23i123e1:4i0ee", BEncoder.encode(generateSimpleList()).toString());
    }

    @Test
    public void checkEncodeSimpleMap() {
        Assert.assertEquals("encode map", "d4:key14:val14:key2i2e4:key34:val24:key4i4ee", BEncoder.encode(generateSimpleMap1()).toString());
        Assert.assertEquals("encode map 2", "d9:publisher3:bob17:publisher-webpage15:www.example.com18:publisher.location4:homee", BEncoder.encode(generateSimpleMap2()).toString());
    }

    @Test
    public void checkEncodeComplex() {
        Assert.assertEquals("encode map complex", "d6:attachd1:ai1e1:bi3e1:cli4ei5e3:abcee6:emailsl13:a@example.com9:b@aaa.come4:name6:Juliane", BEncoder.encode(generateComplexMap()).toString());
    }

    @Test
    public void checkDecodeInteger() throws IOException {
        Assert.assertEquals((new BDecoder("i42e")).decode(), 42l);

        try {
            (new BDecoder("i233")).decode();
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), IllegalStateException.class);
        }
    }

    @Test
    public void checkDecodeString() throws IOException {
        Assert.assertEquals(new BDecoder("14:This is a book").decode().toString(), "This is a book");

        try {
            new BDecoder("23a:3").decode();
            Assert.fail("Exception should be thrown for wrong format");
        } catch (Exception e) {
            Assert.assertEquals(e.getClass(), IllegalStateException.class);
        }
    }

    @Test
    public void checkDecodeList() throws IOException {
        List list = (List) new BDecoder("li23e2:xxi12e4:xxxxe").decode();
    }

    @Test
    public void checkDecodeListList() throws IOException {
        ArrayList<List<String>> list = new ArrayList<List<String>>();

        list.add(Arrays.asList("a", "b", "c"));
        list.add(Arrays.asList("d", "e"));
        list.add(Arrays.asList("1e", "23e", "4"));
        System.out.println("Encode list in list: " + BEncoder.encode(list));

        List result = (List) (new BDecoder(BEncoder.encode(list).toString())).decode();


    }

    @Test
    public void checkDecodeMap() throws IOException {
        Map map = (Map) new BDecoder("d2:xxi42e3:yyy3:xxx1:ai555ee").decode();
    }

    @Test
    public void checkDecodeComplexMap() throws IOException {
        new BDecoder("d6:attachd1:ai1e1:bi3e1:cli4ei5e3:abcee6:emailsl13:a@example.com9:b@aaa.come4:name6:Juliane").decode();
    }
}
