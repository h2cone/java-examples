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

package io.h2cone.network.staff;

public class HandlerChain<Request, Response> {
    private HandlerNode<Request, Response> first, last;

    static class HandlerNode<Request, Response> {
        Handler<Request, Response> handler;
        HandlerNode<Request, Response> next;

        public HandlerNode(Handler<Request, Response> handler, HandlerNode<Request, Response> next) {
            this.handler = handler;
            this.next = next;
        }

        public void handOff(Request request, Response response) {
            if (next == null) {
                return;
            }
            next.handler.handle(request, response, next);
        }
    }

    public void addLast(Handler<Request, Response> handler) {
        if (first == null || last == null) {
            first = new HandlerNode<>(handler, null);
            last = first;
        } else {
            HandlerNode<Request, Response> oldLast = last;
            last = new HandlerNode<>(handler, null);
            oldLast.next = last;
        }
    }

    public void handle(Request request, Response response) {
        if (first == null) {
            return;
        }
        first.handler.handle(request, response, first);
    }

    public static void main(String[] args) {
        HandlerChain<Object, Object> chain = new HandlerChain<>();
        chain.addLast(new FirstCustomHandler());
        chain.addLast(new SecondCustomHandler());

        Object request = new Object();
        Object response = new Object();
        chain.handle(request, response);
    }
}

abstract class Handler<Request, Response> {
    protected abstract void handle(Request request, Response response, HandlerChain.HandlerNode<Request, Response> current);
}


class FirstCustomHandler extends Handler<Object, Object> {

    @Override
    protected void handle(Object request, Object response, HandlerChain.HandlerNode<Object, Object> current) {
        System.out.printf("%s is handled by %s\n", request, this);
        current.handOff(request, response);
    }
}

class SecondCustomHandler extends Handler<Object, Object> {

    @Override
    protected void handle(Object request, Object response, HandlerChain.HandlerNode<Object, Object> current) {
        System.out.printf("%s is handled by %s\n", request, this);
        current.handOff(request, response);
    }
}
