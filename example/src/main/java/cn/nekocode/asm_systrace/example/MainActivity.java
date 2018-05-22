package cn.nekocode.asm_systrace.example;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SomeClass.a();

        getWindow().getDecorView().post(new Runnable() {
            @Override
            public void run() {
                SomeClass.b();
            }
        });
    }
}
