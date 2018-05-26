package cn.nekocode.asm_systrace.example;

import android.os.Trace;

public class SomeClass {
    public static void a() {
        Trace.beginSection("a");
        new SomeClass().c();
        Trace.endSection();
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
