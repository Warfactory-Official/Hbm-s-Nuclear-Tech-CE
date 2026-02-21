package com.hbm.processor;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class SuppressCheckedExceptionsPluginTest {
    private static final Pattern UNREPORTED_EXCEPTION_PATTERN = Pattern.compile("^.*:(\\d+):(\\d+):\\s+compiler\\.err\\.unreported\\.exception[^:]*:.*$");

    private static CompilationResult compile(String className, String source) throws IOException {
        Path workDir = Files.createTempDirectory("suppress-checked-exceptions");
        Path sourceFile = writeSource(workDir, className, source);
        Path outputDir = Files.createDirectories(workDir.resolve("out"));

        List<String> command = new ArrayList<>();
        command.add(findJavac().toString());
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.api=ALL-UNNAMED");
        command.add("-J--add-exports=jdk.compiler/com.sun.tools.javac.util=ALL-UNNAMED");
        command.add("--release");
        command.add("25");
        command.add("-proc:none");
        command.add("-Xplugin:suppress-checked-exceptions");
        command.add("-XDrawDiagnostics");
        command.add("-classpath");
        command.add(findProcessorJar().toString());
        command.add("-d");
        command.add(outputDir.toString());
        command.add(sourceFile.toString());

        ProcessBuilder builder = new ProcessBuilder(command);
        builder.redirectErrorStream(true);
        Process process = builder.start();
        String output;
        try {
            output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            process.destroyForcibly();
            throw e;
        }

        int exitCode;
        try {
            exitCode = process.waitFor();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while waiting for javac.", e);
        }

        return new CompilationResult(exitCode == 0, output);
    }

    private static long lineOfOccurrence(String source, String needle, int occurrence) {
        int index = -1;
        int from = 0;
        for (int i = 0; i < occurrence; i++) {
            index = source.indexOf(needle, from);
            assertTrue(index >= 0, "Needle not found: " + needle);
            from = index + needle.length();
        }
        long line = source.substring(0, index).chars().filter(ch -> ch == '\n').count() + 1;
        return line;
    }

    private static List<DiagnosticLine> unreportedExceptions(String output) {
        List<DiagnosticLine> matches = new ArrayList<>();
        for (String line : output.split("\\R")) {
            Matcher matcher = UNREPORTED_EXCEPTION_PATTERN.matcher(line);
            if (matcher.matches()) {
                long lineNumber = Long.parseLong(matcher.group(1));
                matches.add(new DiagnosticLine(lineNumber, line));
            }
        }
        return matches;
    }

    private static Path writeSource(Path baseDir, String className, String source) throws IOException {
        int lastDot = className.lastIndexOf('.');
        String packageName = lastDot == -1 ? "" : className.substring(0, lastDot);
        String simpleName = className.substring(lastDot + 1);
        Path packageDir = packageName.isEmpty() ? baseDir : baseDir.resolve(packageName.replace('.', '/'));
        Files.createDirectories(packageDir);
        Path sourceFile = packageDir.resolve(simpleName + ".java");
        Files.writeString(sourceFile, source, StandardCharsets.UTF_8);
        return sourceFile;
    }

    private static Path findJavac() {
        String javaHome = System.getProperty("java.home");
        Path javac = Path.of(javaHome, "bin", isWindows() ? "javac.exe" : "javac");
        if (!Files.exists(javac)) {
            javac = Path.of(javaHome).getParent().resolve("bin").resolve(isWindows() ? "javac.exe" : "javac");
        }
        assertTrue(Files.exists(javac), "javac not found under " + javaHome);
        return javac;
    }

    private static Path findProcessorJar() {
        Path root = Path.of(System.getProperty("user.dir"));
        Path jar = root.resolve("processor").resolve("build").resolve("libs").resolve("processor.jar");
        assertTrue(Files.exists(jar), "processor.jar not found at " + jar);
        return jar;
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Test
    void annotatedClassSuppresses() throws Exception {
        String source = """
                package testcases;
                import com.hbm.interfaces.SuppressCheckedExceptions;
                import java.io.IOException;
                
                @SuppressCheckedExceptions
                public class AnnotatedOuter {
                    public void run() {
                        throw new IOException();
                    }
                }
                """;

        CompilationResult result = compile("testcases.AnnotatedOuter", source);

        assertTrue(result.success(), result.output());
        assertTrue(unreportedExceptions(result.output()).isEmpty(), result.output());
    }

    @Test
    void unannotatedClassFails() throws Exception {
        String source = """
                package testcases;
                import java.io.IOException;
                
                public class Plain {
                    public void run() {
                        throw new IOException();
                    }
                }
                """;

        CompilationResult result = compile("testcases.Plain", source);
        List<DiagnosticLine> unreported = unreportedExceptions(result.output());

        assertFalse(result.success(), "Expected compilation to fail.");
        assertFalse(unreported.isEmpty(), result.output());
    }

    @Test
    void annotatedOuterDoesNotSuppressInner() throws Exception {
        String source = """
                package testcases;
                import com.hbm.interfaces.SuppressCheckedExceptions;
                import java.io.IOException;
                
                @SuppressCheckedExceptions
                public class OuterWithInner {
                    public void outer() {
                        throw new IOException();
                    }
                
                    class Inner {
                        public void inner() {
                            throw new IOException();
                        }
                    }
                }
                """;

        CompilationResult result = compile("testcases.OuterWithInner", source);
        List<DiagnosticLine> unreported = unreportedExceptions(result.output());
        long innerLine = lineOfOccurrence(source, "throw new IOException();", 2);

        assertFalse(result.success(), "Expected compilation to fail.");
        assertEquals(1, unreported.size(), result.output());
        assertEquals(innerLine, unreported.get(0).line(), result.output());
    }

    @Test
    void annotatedInnerSuppressesOnlyInner() throws Exception {
        String source = """
                package testcases;
                import com.hbm.interfaces.SuppressCheckedExceptions;
                import java.io.IOException;
                
                public class OuterPlain {
                    public void outer() {
                        throw new IOException();
                    }
                
                    @SuppressCheckedExceptions
                    class Inner {
                        public void inner() {
                            throw new IOException();
                        }
                    }
                }
                """;

        CompilationResult result = compile("testcases.OuterPlain", source);
        List<DiagnosticLine> unreported = unreportedExceptions(result.output());
        long outerLine = lineOfOccurrence(source, "throw new IOException();", 1);

        assertFalse(result.success(), "Expected compilation to fail.");
        assertEquals(1, unreported.size(), result.output());
        assertEquals(outerLine, unreported.get(0).line(), result.output());
    }

    private record CompilationResult(boolean success, String output) {
    }

    private record DiagnosticLine(long line, String text) {
    }
}
