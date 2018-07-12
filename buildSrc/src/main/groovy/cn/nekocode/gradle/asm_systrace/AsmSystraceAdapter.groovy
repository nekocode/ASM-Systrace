/*
 * Copyright 2018. nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.gradle.asm_systrace

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class AsmSystraceAdapter extends ClassVisitor {
    MethodFilter filter


    AsmSystraceAdapter(ClassVisitor cv, MethodFilter filter) {
        super(Opcodes.ASM5, cv)
        this.filter = filter
    }

    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        final MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions)
        final String tagName = filter.filterMethod(name, desc)
        return tagName == null ? mv : (mv != null ? new MethodAdapter(api, mv, access, name, desc, tagName) : null)
    }


    class MethodAdapter extends AdviceAdapter {
        String tagName


        protected MethodAdapter(int api, MethodVisitor mv, int access, String name, String desc, String tagName) {
            super(api, mv, access, name, desc)
            this.tagName = tagName
        }

        @Override
        protected void onMethodEnter() {
            mv.visitLdcInsn(tagName)
            mv.visitMethodInsn(INVOKESTATIC, "android/os/Trace", "beginSection", "(Ljava/lang/String;)V", false)
        }

        @Override
        protected void onMethodExit(int opcode) {
            mv.visitMethodInsn(INVOKESTATIC, "android/os/Trace", "endSection", "()V", false)
        }
    }

    interface MethodFilter {
        String filterMethod(String methodName, String methodDesc)
    }
}
