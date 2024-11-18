package top.whalefall;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;

/**
 * @author Liu Yu
 * @date 2024-11-14 15:50:29
 */
public class AttachMain {
    public static void main(String[] args) throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {
        // 获取进程列表，让用户手动进行输入
        // 1. 执行jps命令
        Process jps = Runtime.getRuntime().exec("jps");
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(jps.getInputStream()))) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                System.out.println(line);
            }
        }
        // 2. 输入进程id
        Scanner scanner = new Scanner(System.in);
        String processId = scanner.next();

        // 获取进程虚拟机对象
        VirtualMachine vm = VirtualMachine.attach(processId);
        // 执行agentmain
        vm.loadAgent("D:\\WorkSpace\\IdeaProjects\\java-agent\\target\\java-agent-1.0-SNAPSHOT-jar-with-dependencies.jar");
    }
}
