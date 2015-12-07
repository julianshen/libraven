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

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import wtf.cowbay.libraven.torrent.TorrentMeta;

import java.io.IOException;
import java.util.List;

public class TorrentFileChecker {
    @Test
    public void checkTorrentFile() throws IOException {
        System.out.println("==ENCODE INFO===");
        TorrentMeta torrent = TorrentMeta.loadFromFile(FileUtils.toFile(getClass().getResource("/test.torrent")).getAbsolutePath());

        System.out.println("Name" + torrent.info.name);
        System.out.println("Info hash: " + torrent.infoHash);

        List<String> announces = torrent.announces;
        for(String announce:announces) {
            System.out.println("Announce: " + announce);
        }

        System.out.println("================");
    }

    @Test
    public void checkTorrentStream() throws IOException {
        TorrentMeta.load(getClass().getResourceAsStream("/test.torrent"));
    }
}
