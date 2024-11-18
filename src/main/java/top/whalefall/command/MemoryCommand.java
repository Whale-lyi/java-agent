package top.whalefall.command;

import com.sun.management.HotSpotDiagnosticMXBean;

import java.io.IOException;
import java.lang.management.BufferPoolMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author Liu Yu
 * @date 2024-11-14 17:03:52
 */
public class MemoryCommand {

    /**
     * 生成内存快照
     */
    public static void headDump() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        HotSpotDiagnosticMXBean hotSpotDiagnosticMXBean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        try {
            hotSpotDiagnosticMXBean.dumpHeap(simpleDateFormat.format(new Date()) + ".hprof", true);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 打印所有内存信息
     */
    public static void printMemory() {
        List<MemoryPoolMXBean> memoryPoolMXBeans = ManagementFactory.getMemoryPoolMXBeans();
        // 堆内存
        System.out.println("堆内存：");
        getMemoryInfo(memoryPoolMXBeans, MemoryType.HEAP);
        // 非堆内存
        System.out.println("非堆内存：");
        getMemoryInfo(memoryPoolMXBeans, MemoryType.NON_HEAP);
        // 打印nio相关内存
        System.out.println("nio相关内存：");
        try {
            Class clazz = Class.forName("java.lang.management.BufferPoolMXBean");
            List<BufferPoolMXBean> bufferPoolMXBeans = ManagementFactory.getPlatformMXBeans(clazz);
            for (BufferPoolMXBean bufferPoolMXBean : bufferPoolMXBeans) {
                String str = "name:" + bufferPoolMXBean.getName() +
                        " used:" + bufferPoolMXBean.getMemoryUsed() / 1024 / 1024 + "m" +
                        " capacity:" + bufferPoolMXBean.getTotalCapacity() / 1024 / 1024 + "m";
                System.out.println(str);
            }
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static void getMemoryInfo(List<MemoryPoolMXBean> memoryPoolMXBeans, MemoryType heap) {
        memoryPoolMXBeans.stream()
                .filter(x -> x.getType().equals(heap))
                .forEach(x -> {
                    String str = "name:" + x.getName() +
                            " used:" + x.getUsage().getUsed() / 1024 / 1024 + "m" +
                            " committed:" + x.getUsage().getCommitted() / 1024 / 1024 + "m" +
                            " max:" + x.getUsage().getMax() / 1024 / 1024 + "m";
                    System.out.println(str);
                });
    }
}
