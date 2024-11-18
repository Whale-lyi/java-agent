package top.whalefall.command;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * @author Liu Yu
 * @date 2024-11-15 11:24:12
 */
public class ThreadCommand {

    /**
     * 获取线程运行信息
     */
    public static void printThreadInfo() {
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(threadMXBean.isObjectMonitorUsageSupported(),
                threadMXBean.isSynchronizerUsageSupported());

        for (ThreadInfo threadInfo : threadInfos) {
            String str = "name: " + threadInfo.getThreadName() +
                    " threadId: " + threadInfo.getThreadId() +
                    " threadState: " + threadInfo.getThreadState();
            System.out.println(str);
            // 打印栈信息
            StackTraceElement[] stackTrace = threadInfo.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                System.out.println(stackTraceElement);
            }
        }
    }
}
