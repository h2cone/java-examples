/*
 * Copyright 2019 hehuang https://github.com/h2cone
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

package io.h2cone.inst.agent;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

public class ElapsedTimeTransformer implements ClassFileTransformer {
    private String agentArgs;

    public ElapsedTimeTransformer() {
    }

    public ElapsedTimeTransformer(String agentArgs) {
        this.agentArgs = agentArgs;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        byte[] bytecode = classfileBuffer;
        if (className.equals(agentArgs)) {
            ClassPool classPool = ClassPool.getDefault();
            try {
                CtClass ctClass = classPool.makeClass(new ByteArrayInputStream(classfileBuffer));
                CtMethod[] methods = ctClass.getDeclaredMethods();
                for (CtMethod method : methods) {
                    method.addLocalVariable("begin", CtClass.longType);
                    method.addLocalVariable("end", CtClass.longType);

                    method.insertBefore("begin = System.nanoTime();");
                    method.insertAfter("end = System.nanoTime();");
                    String methodName = method.getLongName();
                    String x = "System.out.println(\"" + methodName + "\" + \": \" + (end - begin) + \" ns\"" + ");";
                    method.insertAfter(x);
                }
                bytecode = ctClass.toBytecode();
                ctClass.detach();
            } catch (IOException | CannotCompileException e) {
                e.printStackTrace();
            }
        }
        return bytecode;
    }
}
