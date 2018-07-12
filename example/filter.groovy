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