package io.h2cone.attach.app;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;
import org.junit.Test;

import java.io.IOException;

public class DogMainTest {

    @Test
    public void attach() throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        String pid = "pid";
        VirtualMachine vm = VirtualMachine.attach(pid);

        String agentJarPath = "path to agent.jar";
        String options = "Hello, Dog";
        vm.loadAgent(agentJarPath, options);
        vm.detach();
    }
}
