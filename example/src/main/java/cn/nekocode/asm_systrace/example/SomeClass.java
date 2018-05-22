package cn.nekocode.asm_systrace.example;

/**
 * @author nekcode @ Zhihu Inc.
 * @since 2018/05/22
 */
public class SomeClass {
    public static void a() {
        new SomeClass().c();
    }

    public static void b() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {
        }
    }

    private void c() {
        new InnerClass().d();
    }

    private class InnerClass {
        private void d() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
            }
        }
    }
}
