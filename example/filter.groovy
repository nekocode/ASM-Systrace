static String filter(String className, String methodName, String methodDesc) {
    if (!className.startsWith('cn/nekocode')) return null // Skip this method
    return className.split('/').last() + '#' + methodName // Return tracing tag
}