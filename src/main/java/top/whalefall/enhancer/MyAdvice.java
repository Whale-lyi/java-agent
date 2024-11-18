package top.whalefall.enhancer;

import net.bytebuddy.asm.Advice;

import java.util.Objects;

/**
 * @author Liu Yu
 * @date 2024-11-15 16:03:19
 */
public class MyAdvice {

    /**
     * 方法进入时
     * @return
     */
    @Advice.OnMethodEnter
    static long enter(@Advice.AllArguments Object[] args) {
        if (Objects.nonNull(args)) {
            for (int i = 0; i < args.length; i++) {
                System.out.println("参数:" + i + " 内容:" + args[i]);
            }
        }

        return System.nanoTime();
    }

    /**
     * 方法退出时，统计耗时
     * @param time
     */
    @Advice.OnMethodExit
    static void exit(@Advice.Enter long time) {
        System.out.println("耗时为: " + (System.nanoTime() - time) + "纳秒");
    }
}
