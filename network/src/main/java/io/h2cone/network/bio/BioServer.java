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
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class BioServer {
    public static int DELAY_TIME = 2000;

    static class Server implements Runnable {
        final int port;
        final Executor executor;
        final Processable processable;

        public Server(int port, Executor executor, Processable processable) {
            this.port = port;
            this.executor = executor;
            this.processable = processable;
        }

        @Override
        public void run() {
            try {
                ServerSocket serverSocket = new ServerSocket(port);
                while (!Thread.interrupted()) {
                    Socket socket = serverSocket.accept();
                    executor.execute(new Handler(socket, processable));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        static class Handler implements Runnable {
            final Socket socket;
            final Processable processable;

            public Handler(Socket socket, Processable processable) {
                this.socket = socket;
                this.processable = processable;
            }

            @Override
            public void run() {
                processable.process(socket);
            }
        }

        @Deprecated
        interface Processable {
            void process(Socket socket);
        }
    }

    public static void main(String[] args) {
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);

        Server server = new Server(port, Executors.newCachedThreadPool(), (socket) -> {
            try (InputStream input = socket.getInputStream(); OutputStream output = socket.getOutputStream()) {
                // read
                int len;
                byte[] buf = new byte[1024];
                if ((len = input.read(buf)) != -1) {
                    String msg = new String(buf, 0, len);
                    System.out.printf("%s receive '%s' from %s\n", Thread.currentThread().getName(), msg, socket.toString());
                    // consuming
                    Thread.sleep(DELAY_TIME);
                    // write
                    msg = String.format("i am %s", Thread.currentThread().getName());
                    output.write(msg.getBytes());
                    output.flush();
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        new Thread(server).start();

        System.out.printf("server running on %s\n", port);
    }
}
