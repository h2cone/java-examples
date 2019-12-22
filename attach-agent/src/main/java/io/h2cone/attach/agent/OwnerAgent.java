package io.h2cone.attach.agent;

import java.lang.instrument.Instrumentation;

public class OwnerAgent {

    public static void agentmain(String agentArgs, Instrumentation inst) throws Exception {
        System.out.println("agentmain agentArgs: " + agentArgs);
        System.out.println("agentmain inst: " + inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) throws Exception {
        System.out.println("premain agentArgs: " + agentArgs);
        System.out.println("premain inst: " + inst);
    }
}
