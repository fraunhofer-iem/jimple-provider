package de.fraunhofer.iem;

import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class JimpleProviderBuilder {
    private String jpAppClassPath;
    private PreTransformer jpPreTransformer;
    private JimpleProvider jimpleProvider;

    public JimpleProviderBuilder appClassPath(String appClassPath) {
        this.jpAppClassPath = appClassPath;
        return this;
    }

    public JimpleProviderBuilder preTransformer(PreTransformer preTransformer) {
        this.jpPreTransformer = preTransformer;
        return this;
    }

    public JimpleProvider build(boolean isMinimalSoot) throws IOException {
        if (this.jpAppClassPath == null || this.jpAppClassPath.isEmpty()) {
            throw new RuntimeException("App class path is not given. Please set the app class path before building.");
        }

        if (!Files.exists(Paths.get(this.jpAppClassPath)) ||
                !Files.isDirectory(Paths.get(this.jpAppClassPath))) {
            throw new RuntimeException("Given app class path is not valid.");
        }

        if (jpPreTransformer == null) {
            jpPreTransformer = PreTransformer.NONE;
        }

        val completeAppClasses = new FilesUtils().getClassesAsList(this.jpAppClassPath);
        jimpleProvider = JimpleProvider.getInstance(jpAppClassPath, jpPreTransformer, completeAppClasses);
        jimpleProvider.preTasks(isMinimalSoot);

        return jimpleProvider;
    }

    public JimpleProvider build() throws IOException {
        return build(false);
    }

    public void close() {
        if (jimpleProvider != null) {
            jimpleProvider.postTasks();
        }
    }
}
