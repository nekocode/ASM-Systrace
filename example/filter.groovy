static String filter(
        String className, // Example: android/support/v4/app/Fragment
        String methodName, // Example: getLayoutInflater
        String methodDesc // Example: (Landroid/os/Bundle;)Landroid/view/LayoutInflater
) {
    if (!className.startsWith('cn/nekocode')) return null // Retrun null to skip this method
    return className.split('/').last() + '#' + methodName // Return tracing tag
}