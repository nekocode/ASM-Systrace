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

import com.android.annotations.NonNull
import com.android.annotations.Nullable
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

/**
 * @author nekocode (nekocode.cn@gmail.com)
 */
class AsmSystraceTransform implements CustomTransform {

    @NonNull
    @Override
    String getName() {
        return 'systrace'
    }

    @Nullable
    @Override
    File getSecondaryFile() {
        return null
    }

    @Override
    void transform(@NonNull InputStream is, @NonNull OutputStream os) {
        final ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_MAXS)

        try {
            final ClassReader cr = new ClassReader(is)
            final ClassVisitor visitor = new AsmSystraceAdapter(writer, cr.getClassName())

            cr.accept(visitor, ClassReader.EXPAND_FRAMES)
            os.write(writer.toByteArray())

        } catch (IOException e) {
            throw new UncheckedIOException(e)
        }
    }
}
