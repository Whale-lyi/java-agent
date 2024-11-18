package top.whalefall;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import top.whalefall.command.ClassCommand;
import top.whalefall.command.MemoryCommand;
import top.whalefall.command.ThreadCommand;
import top.whalefall.enhancer.AgentParam;
import top.whalefall.enhancer.MyAdvice;
import top.whalefall.enhancer.TimingAdvice;

import java.lang.instrument.Instrumentation;
import java.util.Scanner;

/**
 * @author Liu Yu
 * @date 2024-11-14 15:29:45
 */
public class AgentMain {

    public static void premain(String agentArgs, Instrumentation inst) {
        // 使用byte-buddy增强
        new AgentBuilder.Default()
                .disableClassFormatChanges() // 禁止处理时修改类名
                .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION) // 处理时使用retransform增强
                .with(new AgentBuilder.Listener.WithTransformationsOnly(AgentBuilder.Listener.StreamWriting.toSystemOut())) // 打印出错误日志
                .type(ElementMatchers.isAnnotatedWith(ElementMatchers.named("org.springframework.web.bind.annotation.RestController")
                        .or(ElementMatchers.named("org.springframework.web.bind.annotation.Controller")))) // 匹配哪些类
                .transform(((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                        builder.visit(Advice
                                        .withCustomMapping()
                                        .bind(AgentParam.class, agentArgs)
                                        .to(TimingAdvice.class)
                                        .on(ElementMatchers.any())))) // 增强，使用MyAdvice通知，对所有方法都进行增强
                .installOn(inst);
    }

    public static void agentmain(String agentArgs, Instrumentation inst) {
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("""
                    菜单:
                    1. 查看内存使用情况
                    2. 生成堆内存快照
                    3. 打印栈信息
                    4. 打印类加载器
                    5. 打印类源码
                    6. 打印方法的参数和耗时
                    7. 退出""");
            String input = scanner.next();
            switch (input) {
                case "1" -> MemoryCommand.printMemory();
                case "2" -> MemoryCommand.headDump();
                case "3" -> ThreadCommand.printThreadInfo();
                case "4" -> ClassCommand.printAllClassLoader(inst);
                case "5" -> ClassCommand.printClassSourceCode(inst);
                case "6" -> ClassCommand.enhanceClass(inst);
                case "7" -> {
                    return;
                }
            }
        }
    }

}
