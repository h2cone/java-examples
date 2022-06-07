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

package io.h2cone.proxy.cglib;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.Arrays;

public class PersonServiceTest {

    @Test
    public void sayHello() {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(PersonService.class);
        enhancer.setCallback(new PersonServiceInterceptor());
        PersonService service = (PersonService) enhancer.create();

        String result = service.sayHello("World");
        Assert.assertEquals("Hello, World", result);
    }

    static class PersonServiceInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            System.out.printf("obj class: %s\n", obj.getClass());
            System.out.printf("method: %s\n", method);
            System.out.printf("args: %s\n", Arrays.toString(args));
            System.out.printf("method proxy: %s\n", proxy);

            System.out.println("Before invoke");

            Object result = proxy.invokeSuper(obj, args);
            System.out.println(result);

            System.out.println("After invoke");
            return result;
        }
    }
}
