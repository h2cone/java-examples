/*
 * Copyright 2020 hehuang https://github.com/h2cone
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

package io.h2cone.network.bio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class BioClient {
    public static int NUMBER_OF_CLIENTS = 8;

    public static void main(String[] args) {
        String host = args.length == 0 ? "127.0.0.1" : args[0];
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[1]);

        Runnable runnable = () -> {
            try {
                Socket socket = new Socket(host, port);
                try (OutputStream output = socket.getOutputStream(); InputStream input = socket.getInputStream()) {
                    // write
                    String msg = String.format("i am %s", Thread.currentThread().getName());
                    output.write(msg.getBytes());
                    output.flush();
                    // read
                    int len;
                    byte[] buf = new byte[1024];
                    if ((len = input.read(buf)) != -1) {
                        msg = new String(buf, 0, len);
                        System.out.printf("%s receive '%s' from %s\n", Thread.currentThread().getName(), msg, socket.toString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        };
        for (int i = 0; i < NUMBER_OF_CLIENTS; i++) {
            new Thread(runnable).start();
        }
    }
}
