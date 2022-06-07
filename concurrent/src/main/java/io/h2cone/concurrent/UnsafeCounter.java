/*
 * Copyright 2020 huangh https://github.com/h2cone
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

package io.h2cone.concurrent;

import sun.misc.Unsafe;

import java.lang.reflect.Field;

public class UnsafeCounter {
    static Unsafe unsafe;

    volatile long value;
    /**
     * represents the address of a variable
     */
    static long valueOffset;

    static {
        try {
            Field theUnsafeField = Unsafe.class.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            unsafe = (Unsafe) theUnsafeField.get(null);

            Field valueField = UnsafeCounter.class.getDeclaredField("value");
            valueOffset = unsafe.objectFieldOffset(valueField);
        } catch (NoSuchFieldException | IllegalAccessException e) {
        }
    }

    public long incrementAndGet() {
        long current;
        do {
            current = unsafe.getLongVolatile(this, valueOffset);
        } while (!unsafe.compareAndSwapLong(this, valueOffset, current, current + 1));
        return current;
    }
}
