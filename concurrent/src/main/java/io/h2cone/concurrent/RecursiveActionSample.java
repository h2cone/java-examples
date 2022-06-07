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

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Copy from https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/RecursiveAction.html
 */
public class RecursiveActionSample {

    /**
     * Here is a simple but complete ForkJoin sort that sorts a given long[] array
     */
    static class SortTask extends RecursiveAction {
        final long[] array;
        final int lo, hi;

        SortTask(long[] array, int lo, int hi) {
            this.array = array;
            this.lo = lo;
            this.hi = hi;
        }

        SortTask(long[] array) {
            this(array, 0, array.length);
        }

        @Override
        protected void compute() {
            if (hi - lo < THRESHOLD) {
                sortSequentially(lo, hi);
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new SortTask(array, lo, mid), new SortTask(array, mid, hi));
                merge(lo, mid, hi);
            }
        }

        // implementation details follow:
        static final int THRESHOLD = 1000;

        void sortSequentially(int lo, int hi) {
            Arrays.sort(array, lo, hi);
        }

        void merge(int lo, int mid, int hi) {
            long[] buf = Arrays.copyOfRange(array, lo, mid);
            for (int i = 0, j = lo, k = mid; i < buf.length; j++) {
                array[j] = (k == hi || buf[i] < array[k]) ? buf[i++] : array[k++];
            }
        }
    }

    /**
     * As a more concrete simple example, the following task increments each element of an array
     */
    static class IncrementTask extends RecursiveAction {
        final long[] array;
        final int lo, hi;

        IncrementTask(long[] array, int lo, int hi) {
            this.array = array;
            this.lo = lo;
            this.hi = hi;
        }

        @Override
        protected void compute() {
            if (hi - lo < SortTask.THRESHOLD) {
                for (int i = lo; i < hi; ++i) {
                    array[i]++;
                }
            } else {
                int mid = (lo + hi) >>> 1;
                invokeAll(new IncrementTask(array, lo, mid),
                        new IncrementTask(array, mid, hi));
            }
        }
    }

    /**
     * The following example illustrates some refinements and idioms
     * that may lead to better performance: RecursiveActions need not be
     * fully recursive, so long as they maintain the basic
     * divide-and-conquer approach. Here is a class that sums the squares
     * of each element of a double array, by subdividing out only the
     * right-hand-sides of repeated divisions by two, and keeping track of
     * them with a chain of {@code next} references. It uses a dynamic
     * threshold based on method {@code getSurplusQueuedTaskCount}, but
     * counterbalances potential excess partitioning by directly
     * performing leaf actions on unstolen tasks rather than further
     * subdividing.
     */
    double sumOfSquares(ForkJoinPool pool, double[] array) {
        int n = array.length;
        Applyer a = new Applyer(array, 0, n, null);
        pool.invoke(a);
        return a.result;
    }

    class Applyer extends RecursiveAction {
        final double[] array;
        final int lo, hi;
        double result;
        Applyer next; // keeps track of right-hand-side tasks

        Applyer(double[] array, int lo, int hi, Applyer next) {
            this.array = array;
            this.lo = lo;
            this.hi = hi;
            this.next = next;
        }

        double atLeaf(int l, int h) {
            double sum = 0;
            for (int i = l; i < h; ++i) {   // perform leftmost base step
                sum += array[i] * array[i];
            }
            return sum;
        }

        @Override
        protected void compute() {
            int l = lo;
            int h = hi;
            Applyer right = null;
            while (h - l > 1 && getSurplusQueuedTaskCount() <= 3) {
                int mid = (l + h) >>> 1;
                right = new Applyer(array, mid, h, right);
                right.fork();
                h = mid;
            }
            double sum = atLeaf(l, h);
            while (right != null) {
                if (right.tryUnfork()) {    // directly calculate if not stolen
                    sum += right.atLeaf(right.lo, right.hi);
                } else {
                    right.join();
                    sum += right.result;
                }
                right = right.next;
            }
            result = sum;
        }
    }
}
