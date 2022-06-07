/*
 * Copyright 2019 huangh https://github.com/h2cone
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

package io.h2cone.proxy.jdk;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;

public class PersonServiceTest {

    @Test
    public void helloWorld() {
        PersonService service = new SimplePersonService();
        String result = service.sayHello("World");
        Assert.assertEquals("Hello, World", result);
    }

    @Test
    public void sayHello() {
        SimplePersonService target = new SimplePersonService();

        PersonService proxy = (PersonService) Proxy.newProxyInstance(target.getClass().getClassLoader(),
                target.getClass().getInterfaces(),
                new PersonServiceHandler(target));

        String result = proxy.sayHello("World");
        Assert.assertEquals("Hello, World", result);
    }

    static class PersonServiceHandler implements InvocationHandler {
        Object target;

        PersonServiceHandler() {
        }

        PersonServiceHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.printf("proxy class: %s\n", proxy.getClass());
            System.out.printf("method: %s\n", method);
            System.out.printf("args: %s\n", Arrays.toString(args));

            if (target != null) {
                System.out.println("Before invoke");

                Object result = method.invoke(target, args);
                System.out.println(result);

                System.out.println("After invoke");
                return result;
            }
            return null;
        }
    }
}
