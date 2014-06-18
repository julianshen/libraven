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

package org.mj.libraven.torrent;

import org.apache.commons.codec.digest.DigestUtils;
import org.mj.libraven.bencode.BDecoder;
import org.mj.libraven.bencode.BEncoder;
import org.mj.libraven.bencode.ByteString;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class TorrentMeta {
    public Info info = new Info();
    public List<String> announce = new ArrayList<String>(); //madatory
    public List<List<String>> announceList = new ArrayList<List<String>>(); //optional
    public long created; //optional
    public String comment; //optional
    public String createdBy; //optional
    public String encoding; //optional

    public String infoHash; //generated

    private static void ensureField(Map map, String fieldName) throws IOException {
        if (!map.containsKey(fieldName)) {
            throw new IOException("Invalid torrent file: " + fieldName + " not existed");
        }
    }

    public static TorrentMeta loadFromFile(String fileName) throws IOException {
        return load(new FileInputStream(fileName));
    }

    public static TorrentMeta load(InputStream in) throws IOException {
        Map map = (Map) new BDecoder(in).decode();

        TorrentMeta meta = new TorrentMeta();

        ensureField(map, "info");
        Map infoMap = (Map) map.get("info");

        ensureField(infoMap, "name");
        ensureField(infoMap, "piece length");
        ensureField(infoMap, "pieces");

        meta.info.name = infoMap.get("name").toString();
        meta.info.piece_len = (Long) infoMap.get("piece length");
        ByteString pieces = (ByteString) infoMap.get("pieces");

        //Process pieces
        if (pieces.length() % 20 != 0) {
            throw new IOException("Invalid torrent file: pieces should be an array of 20-byte SHA-1");
        }

        byte[] bytes = pieces.value();
        int i = 0;
        while (i < bytes.length) {
            ByteString piece = new ByteString(Arrays.copyOfRange(bytes, i, i + 20));
            meta.info.pieces.add(piece);
            i += 20;
        }

        //Process files
        if (infoMap.containsKey("files")) { //Multiple file mode
            List fileList = (List) infoMap.get("files");
            for (Object obj : fileList) {
                Map fileMap = (Map) obj;
                ensureField(fileMap, "path");
                ensureField(fileMap, "length");

                FileInfo fileInfo = new FileInfo();
                fileInfo.path = fileMap.get("path").toString();
                fileInfo.length = (Long) fileMap.get("length");

                //Optional (md5sum)
                if (fileMap.containsKey("md5sum")) {
                    fileInfo.md5sum = fileMap.get("md5sum").toString();
                }
                meta.info.files.add(fileInfo);
            }
        } else { //Single file mode
            ensureField(infoMap, "length");

            FileInfo fileInfo = new FileInfo();
            fileInfo.length = (Long) infoMap.get("length");
            fileInfo.path = meta.info.name;

            meta.info.files.add(fileInfo);
        }

        //Process announce
        ensureField(map, "announce");

        Object annObj = map.get("announce"); //single item of annource might be a string

        if (annObj instanceof ByteString) {
            meta.announce.add(annObj.toString());
        } else {
            List ann = (List) annObj;
            for (Object obj : ann) {
                meta.announce.add(obj.toString());
            }
        }

        //Optional info
        if (infoMap.containsKey("private")) {
            meta.info.isPrivate = (Long) infoMap.get("private") == 1;
        }

        if (infoMap.containsKey("creation date")) {
            meta.created = (Long) infoMap.get("creation date");
        }

        if (infoMap.containsKey("announce-list")) {
            List annList = (List) infoMap.get("announce-list");

            for (Object obj : annList) {
                List strList = (List) obj;
                ArrayList<String> list = new ArrayList<String>();
                for (Object str : strList) {
                    list.add(str.toString());
                }
                meta.announceList.add(list);
            }
        }

        if (infoMap.containsKey("creation by")) {
            meta.createdBy = infoMap.get("creation by").toString();
        }

        if (infoMap.containsKey("comment")) {
            meta.comment = infoMap.get("comment").toString();
        }

        meta.infoHash = generateInfoHash(infoMap);

        return meta;
    }

    private static String generateInfoHash(Map infoMap) {
        byte[] encodedInfo = BEncoder.encode(infoMap).value();

        return DigestUtils.sha1Hex(encodedInfo);
    }

    public List<String> getAnnounces() {
        ArrayList<String> result = new ArrayList<String>();

        for (String announceItem : announce) {
            result.add(announceItem);
        }

        for (List<String> announceItems : announceList) {
            for (String announceItem : announceItems) {
                result.add(announceItem);
            }
        }

        return result;
    }

    public static class FileInfo {
        public String path = null;
        public long length;
        public String md5sum = null; //optional
    }

    static class Info {
        public String name; //In single file node: file name. In multiple file mode: path name
        public long piece_len; //piece length manatory
        public List<ByteString> pieces = new ArrayList<ByteString>();
        public boolean isPrivate = false;
        public List<FileInfo> files = new ArrayList<FileInfo>();
    }
}
