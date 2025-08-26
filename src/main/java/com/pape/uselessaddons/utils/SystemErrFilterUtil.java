package com.pape.uselessaddons.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

public final class SystemErrFilterUtil {
    // keep original so we can forward everything else and restore later
    private static volatile PrintStream originalErr;
    private static volatile PrintStream filteringErr;

    // patterns for the error msgs we want to filter out of the logs
    private static final Pattern[] START_PATTERNS = new Pattern[] {
        Pattern.compile(".*meteordevelopment\\.meteorclient\\.utils\\.network\\.Http.*"),
        Pattern.compile(".*GOAWAY received.*"),
        Pattern.compile(".*Http\\$Request\\._sendResponse.*"),
        Pattern.compile(".*HttpClientImpl\\.send.*"),
        Pattern.compile(".*Expected BEGIN_OBJECT but was STRING.*"),
        Pattern.compile(".*com\\.google\\.gson.*"),
        Pattern.compile(".*See https://github\\.com/google/gson/blob/main/Troubleshooting\\.md#unexpected-json-structure.*")
    };

    // install once (idempotent)
    public static synchronized void install() {
        if (filteringErr != null) return; // already installed
        originalErr = System.err;
        filteringErr = new PrintStream(new FilteringOutputStream(originalErr), /*autoFlush*/ true, StandardCharsets.UTF_8);
        System.setErr(filteringErr);
    }

    // uninstall and restore original
    public static synchronized void uninstall() {
        if (originalErr == null) return;
        try {
            System.setErr(originalErr);
        } finally {
            try { filteringErr.close(); } catch (Exception ignored) {}
            filteringErr = null;
            originalErr = null;
        }
    }

    // Custom OutputStream that buffers bytes into lines and filters them.
    private static final class FilteringOutputStream extends OutputStream {
        private final PrintStream delegate;
        private final StringBuilder lineBuffer = new StringBuilder();
        // If true, we're currently suppressing stack-trace lines
        private volatile boolean suppressingStack = false;
        // A short-lived list used to check multi-line emissions (safe for concurrent writes)
        private final List<Pattern> startPatterns = new CopyOnWriteArrayList<>();

        FilteringOutputStream(PrintStream delegate) {
            this.delegate = delegate;
            for (Pattern p : START_PATTERNS) startPatterns.add(p);
        }

        @Override
        public synchronized void write(int b) throws IOException {
            char c = (char) b;
            lineBuffer.append(c);
            if (c == '\n') {
                processLine(lineBuffer.toString());
                lineBuffer.setLength(0);
            }
        }

        @Override
        public synchronized void write(byte[] b, int off, int len) throws IOException {
            // append chunk; split into lines as encountered
            int end = off + len;
            for (int i = off; i < end; i++) {
                char c = (char) b[i];
                lineBuffer.append(c);
                if (c == '\n') {
                    processLine(lineBuffer.toString());
                    lineBuffer.setLength(0);
                }
            }
        }

        @Override
        public void flush() throws IOException {
            synchronized (this) {
                if (lineBuffer.length() > 0) {
                    processLine(lineBuffer.toString());
                    lineBuffer.setLength(0);
                }
            }
            delegate.flush();
        }

        @Override
        public void close() throws IOException {
            flush();
            delegate.close();
        }

        // Determine whether to suppress or forward this line
        private void processLine(String rawLine) {
            String line = rawLine.replaceAll("\\r?\\n$", ""); // trim newline for checks

            // If currently suppressing stack frames, drop indented "at ..." and "Caused by:" lines
            if (suppressingStack) {
                String trimmed = line.trim();
                if (trimmed.startsWith("at ") || trimmed.startsWith("\tat ") || trimmed.startsWith("Caused by:") || trimmed.startsWith("... ")) {
                    // drop
                    return;
                } else {
                    // end suppression once a non-stack-trace line appears
                    suppressingStack = false;
                }
            }

            // If line matches any start pattern -> begin suppression
            for (Pattern p : startPatterns) {
                if (p.matcher(line).find()) {
                    // start suppressing subsequent stack frames
                    suppressingStack = true;
                    // drop this line as well
                    return;
                }
            }

            // Otherwise forward the line to original stderr (including newline)
            delegate.print(rawLine);
        }
    }
}
