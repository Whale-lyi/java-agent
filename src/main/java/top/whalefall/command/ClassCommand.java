package top.whalefall.command;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.asm.Advice;
import net.bytebuddy.matcher.ElementMatchers;
import org.jd.core.v1.ClassFileToJavaSourceDecompiler;
import org.jd.core.v1.api.loader.Loader;
import org.jd.core.v1.api.loader.LoaderException;
import org.jd.core.v1.api.printer.Printer;
import top.whalefall.enhancer.AsmEnhancer;
import top.whalefall.enhancer.MyAdvice;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Liu Yu
 * @date 2024-11-15 11:39:04
 */
public class ClassCommand {

    /**
     * 对类进行增强，统计执行耗时
     * @param inst
     */
    public static void enhanceClass(Instrumentation inst) {
        // 用户输入类名
        System.out.print("请输入类名: ");
        Scanner scanner = new Scanner(System.in);
        String className = scanner.next();
        // 根据类名获取class对象
        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        for (Class clazz : allLoadedClasses) {
            if (clazz.getName().equals(className)) {
                // 使用byte-buddy增强
                new AgentBuilder.Default()
                        .disableClassFormatChanges() // 禁止处理时修改类名
                        .with(AgentBuilder.RedefinitionStrategy.RETRANSFORMATION) // 处理时使用retransform增强
                        .with(new AgentBuilder.Listener.WithTransformationsOnly(AgentBuilder.Listener.StreamWriting.toSystemOut())) // 打印出错误日志
                        .type(ElementMatchers.named(className)) // 匹配哪些类
                        .transform(((builder, typeDescription, classLoader, javaModule, protectionDomain) ->
                                builder.visit(Advice.to(MyAdvice.class).on(ElementMatchers.any())))) // 增强，使用MyAdvice通知，对所有方法都进行增强
                        .installOn(inst);
                // asmEnhance(inst, clazz);
            }
        }
    }

    /**
     * asm增强
     * @param inst
     * @param clazz
     */
    private static void asmEnhance(Instrumentation inst, Class clazz) {
        // 1. 添加转换器
        ClassFileTransformer classFileTransformer = new ClassFileTransformer() {
            @Override
            public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                // 通过ASM对类进行增强，返回字节码信息
                return AsmEnhancer.enhanceClass(classfileBuffer);
            }
        };
        inst.addTransformer(classFileTransformer, true);
        // 2. 触发转换
        try {
            inst.retransformClasses(clazz);
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        } finally {
            // 3. 删除转换器
            inst.removeTransformer(classFileTransformer);
        }
    }


    /**
     * 打印类的源代码
     * @param inst
     */
    public static void printClassSourceCode(Instrumentation inst) {
        // 用户输入类名
        System.out.print("请输入类名: ");
        Scanner scanner = new Scanner(System.in);
        String className = scanner.next();
        // 根据类名获取class对象
        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        for (Class clazz : allLoadedClasses) {
            if (clazz.getName().equals(className)) {
                // 1. 添加转换器
                ClassFileTransformer classFileTransformer = new ClassFileTransformer() {
                    @Override
                    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
                        try {
                            printJDCoreSourceCode(classfileBuffer, className);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return ClassFileTransformer.super.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
                    }
                };
                inst.addTransformer(classFileTransformer, true);
                // 2. 触发转换
                try {
                    inst.retransformClasses(clazz);
                } catch (UnmodifiableClassException e) {
                    e.printStackTrace();
                } finally {
                    // 3. 删除转换器
                    inst.removeTransformer(classFileTransformer);
                }
            }
        }
    }

    private static void printJDCoreSourceCode(byte[] bytes, String className) throws Exception {
        // loader对象
        Loader loader = new Loader() {
            @Override
            public byte[] load(String internalName) throws LoaderException {
                return bytes;
            }

            @Override
            public boolean canLoad(String internalName) {
                return true;
            }
        };
        // printer对象
        Printer printer = new Printer() {
            protected static final String TAB = "  ";
            protected static final String NEWLINE = "\n";

            protected int indentationCount = 0;
            protected StringBuilder sb = new StringBuilder();

            @Override public String toString() { return sb.toString(); }

            @Override public void start(int maxLineNumber, int majorVersion, int minorVersion) {}
            @Override public void end() {
                System.out.println(sb);
            }

            @Override public void printText(String text) { sb.append(text); }
            @Override public void printNumericConstant(String constant) { sb.append(constant); }
            @Override public void printStringConstant(String constant, String ownerInternalName) { sb.append(constant); }
            @Override public void printKeyword(String keyword) { sb.append(keyword); }
            @Override public void printDeclaration(int type, String internalTypeName, String name, String descriptor) { sb.append(name); }
            @Override public void printReference(int type, String internalTypeName, String name, String descriptor, String ownerInternalName) { sb.append(name); }

            @Override public void indent() { this.indentationCount++; }
            @Override public void unindent() { this.indentationCount--; }

            @Override public void startLine(int lineNumber) { for (int i=0; i<indentationCount; i++) sb.append(TAB); }
            @Override public void endLine() { sb.append(NEWLINE); }
            @Override public void extraLine(int count) { while (count-- > 0) sb.append(NEWLINE); }

            @Override public void startMarker(int type) {}
            @Override public void endMarker(int type) {}
        };

        // 通过jd-core方法打印
        ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
        decompiler.decompile(loader, printer, className);
    }

    /**
     * 打印所有类加载器
     * @param inst
     */
    public static void printAllClassLoader(Instrumentation inst) {
        Set<ClassLoader> classLoaders = new HashSet<>();
        // 获取所有类
        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        for (Class clazz : allLoadedClasses) {
            classLoaders.add(clazz.getClassLoader());
        }
        // 打印类加载器
        String str = classLoaders.stream()
                .map(x -> {
                    if (Objects.isNull(x)) {
                        return "BootStrapClassLoader";
                    }
                    return x.getName();
                })
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String::compareTo)
                .collect(Collectors.joining(","));
        System.out.println(str);
    }
}
