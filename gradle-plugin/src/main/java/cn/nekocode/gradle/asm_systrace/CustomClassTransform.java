/*
 * Copyright (C) 2017 The Android Open Source Project
 * Copyright 2018. nekocode (nekocode.cn@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cn.nekocode.gradle.asm_systrace;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.build.api.transform.DirectoryInput;
import com.android.build.api.transform.Format;
import com.android.build.api.transform.JarInput;
import com.android.build.api.transform.QualifiedContent;
import com.android.build.api.transform.SecondaryFile;
import com.android.build.api.transform.Status;
import com.android.build.api.transform.Transform;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformInvocation;
import com.android.build.api.transform.TransformOutputProvider;
import com.android.build.gradle.internal.pipeline.TransformManager;
import com.android.utils.FileUtils;
import com.google.common.collect.ImmutableSet;
import com.google.common.io.Files;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import static com.android.build.api.transform.QualifiedContent.DefaultContentType.CLASSES;
import static com.android.build.gradle.internal.pipeline.ExtendedContentType.NATIVE_LIBS;

/**
 * Copy and modify from {@link com.android.build.gradle.internal.transforms.CustomClassTransform}
 *
 * @author nekocode (nekocode.cn@gmail.com)
 */
public class CustomClassTransform extends Transform {

    @NonNull
    private final CustomTransform transform;

    public CustomClassTransform(@NonNull CustomTransform transform) {
        this.transform = transform;
    }

    @NonNull
    @Override
    public String getName() {
        return transform.getName();
    }

    @NonNull
    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS;
    }

    @NonNull
    @Override
    public Set<QualifiedContent.ContentType> getOutputTypes() {
        return ImmutableSet.of(CLASSES, NATIVE_LIBS);
    }

    @NonNull
    @Override
    public Set<QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT;
    }

    @NonNull
    @Override
    public Collection<SecondaryFile> getSecondaryFiles() {
        final File secondaryFile = transform.getSecondaryFile();
        return secondaryFile == null ? ImmutableSet.of() :
                ImmutableSet.of(SecondaryFile.nonIncremental(secondaryFile));
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(@NonNull TransformInvocation invocation)
            throws InterruptedException, IOException {
        final TransformOutputProvider outputProvider = invocation.getOutputProvider();
        assert outputProvider != null;

        // Output the resources, we only do this if this is not incremental,
        // as the secondary file is will trigger a full build if modified.
        if (!invocation.isIncremental()) {
            outputProvider.deleteAll();
        }


        for (TransformInput ti : invocation.getInputs()) {
            for (JarInput jarInput : ti.getJarInputs()) {
                File inputJar = jarInput.getFile();
                File outputJar =
                        outputProvider.getContentLocation(
                                jarInput.getName(),
                                jarInput.getContentTypes(),
                                jarInput.getScopes(),
                                Format.JAR);

                if (invocation.isIncremental()) {
                    switch (jarInput.getStatus()) {
                        case NOTCHANGED:
                            break;
                        case ADDED:
                        case CHANGED:
                            transformJar(inputJar, outputJar);
                            break;
                        case REMOVED:
                            FileUtils.delete(outputJar);
                            break;
                    }
                } else {
                    transformJar(inputJar, outputJar);
                }
            }
            for (DirectoryInput di : ti.getDirectoryInputs()) {
                File inputDir = di.getFile();
                File outputDir =
                        outputProvider.getContentLocation(
                                di.getName(),
                                di.getContentTypes(),
                                di.getScopes(),
                                Format.DIRECTORY);
                if (invocation.isIncremental()) {
                    for (Map.Entry<File, Status> entry : di.getChangedFiles().entrySet()) {
                        File inputFile = entry.getKey();
                        switch (entry.getValue()) {
                            case NOTCHANGED:
                                break;
                            case ADDED:
                            case CHANGED:
                                if (!inputFile.isDirectory()
                                        && inputFile.getName()
                                        .endsWith(SdkConstants.DOT_CLASS)) {
                                    File out = toOutputFile(outputDir, inputDir, inputFile);
                                    transformFile(inputFile, out);
                                }
                                break;
                            case REMOVED:
                                File outputFile = toOutputFile(outputDir, inputDir, inputFile);
                                FileUtils.deleteIfExists(outputFile);
                                break;
                        }
                    }
                } else {
                    for (File in : FileUtils.getAllFiles(inputDir)) {
                        if (in.getName().endsWith(SdkConstants.DOT_CLASS)) {
                            File out = toOutputFile(outputDir, inputDir, in);
                            transformFile(in, out);
                        }
                    }
                }
            }
        }
    }

    private void transformJar(File inputJar, File outputJar) throws IOException {
        Files.createParentDirs(outputJar);
        try (FileInputStream fis = new FileInputStream(inputJar);
             ZipInputStream zis = new ZipInputStream(fis);
             FileOutputStream fos = new FileOutputStream(outputJar);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                if (!entry.isDirectory() && entry.getName().endsWith(SdkConstants.DOT_CLASS)) {
                    zos.putNextEntry(new ZipEntry(entry.getName()));
                    transform(zis, zos);
                } else {
                    // Do not copy resources
                }
                entry = zis.getNextEntry();
            }
        }
    }

    private void transformFile(File inputFile, File outputFile) throws IOException {
        Files.createParentDirs(outputFile);
        try (FileInputStream fis = new FileInputStream(inputFile);
            FileOutputStream fos = new FileOutputStream(outputFile)) {
            transform(fis, fos);
        }
    }

    @NonNull
    private static File toOutputFile(File outputDir, File inputDir, File inputFile) {
        return new File(outputDir, FileUtils.relativePossiblyNonExistingPath(inputFile, inputDir));
    }

    private void transform(InputStream in, OutputStream out) throws IOException {
        try {
            transform.transform(in, out);
        } catch (UncheckedIOException e) {
            throw e.getCause();
        }
    }
}