package top.whalefall.enhancer;

import net.bytebuddy.asm.Advice;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author Liu Yu
 * @date 2024-11-15 16:03:19
 */
public class TimingAdvice {

    /**
     * 方法进入时
     * @return
     */
    @Advice.OnMethodEnter
    static long enter() {
        return System.nanoTime();
    }

    /**
     * 方法退出时，统计耗时
     * @param time
     */
    @Advice.OnMethodExit
    static void exit(@Advice.Enter long time,
                     @Advice.Origin("#t") String className,
                     @Advice.Origin("#m") String methodName,
                     @AgentParam("agent.log") String fileName) {
        String str = methodName + "@" + className + "耗时为: " + (System.nanoTime() - time) + "纳秒\n";
        try {
            FileUtils.writeStringToFile(new File(fileName), str, StandardCharsets.UTF_8, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
