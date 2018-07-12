# README
[![Apache 2.0 License](https://img.shields.io/badge/license-Apache%202.0-blue.svg?style=flat)](http://www.apache.org/licenses/LICENSE-2.0.html) [![Release](https://jitpack.io/v/nekocode/ASM-Systrace.svg)](https://jitpack.io/#nekocode/ASM-Systrace)

This plugin can inject `Trace.beginSection()` & `Trace.endSection` to the methods which you specify in build-time. It's useful when you want to trace the methods of third-party libraries.

In addition, this plugin supports incremental work. So its build performance is not bad.

## Intergation

Intergate this gralde plugin:

```gradle
buildscript {
    repositories {
        maven { url "https://jitpack.io" }
    }
    dependencies {
        classpath "com.github.nekocode:ASM-Systrace:${lastest-verion}"
    }
}
```

Create a groovy script and define two filter methods inside:

```groovy
// Only handle the class you specify can speed up the build
static boolean filterClass(
        String className // Example: android/support/v4/app/Fragment
) {
    // Return true if you want to continue filter methods in this class
    return className.startsWith('cn/nekocode/asm_systrace/example/SomeClass')
}

static String filterMethod(
        String className, // Example: android/support/v4/app/Fragment
        String methodName, // Example: getLayoutInflater
        String methodDesc // Example: (Landroid/os/Bundle;)Landroid/view/LayoutInflater
) {
    if (methodName != 'b') return null // Retrun null to skip this method
    return className.split('/').last() + '#' + methodName // Return tracing tag
}
```

Apply and configure the plugin:

```gralde
apply plugin: 'asm-systrace'

asmSystrace {
    filterScript = project.file("filter.groovy") // The script you created in the pervious step
}
```
