/*
 * Copyright (C) 2009-2023 the original author(s).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.jansi.internal.musl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import org.fusesource.jansi.internal.AnsiConsoleSupport;
import org.graalvm.nativeimage.c.type.CTypeConversion;

import static java.nio.charset.StandardCharsets.US_ASCII;

public final class AnsiConsoleSupportImpl extends AnsiConsoleSupport {

    public AnsiConsoleSupportImpl() {
        super("musl");
    }

    @Override
    protected CLibrary createCLibrary() {
        return new CLibrary() {
            private final String[] command;
            private final String stdoutTty =
                    CTypeConversion.toJavaString(PosixCLibrary.ttyname(CLibrary.STDOUT_FILENO));
            private final String stderrTty =
                    CTypeConversion.toJavaString(PosixCLibrary.ttyname(CLibrary.STDERR_FILENO));

            {
                String stty = "stty";

                String path = System.getenv("PATH");
                if (path != null) {
                    for (String p : path.split(File.pathSeparator)) {
                        Path sttyFile = Paths.get(p, "stty");
                        if (Files.isExecutable(sttyFile)) {
                            stty = sttyFile.toAbsolutePath().normalize().toString();
                        }
                    }
                }

                command = new String[] {stty, "size"};
            }

            @Override
            public short getTerminalWidth(int fd) {
                if (isTty(fd) == 0) {
                    return 0;
                }

                Process process = null;
                try {
                    process = new ProcessBuilder(command)
                            .redirectInput(ProcessBuilder.Redirect.INHERIT)
                            .start();

                    if (process.waitFor(10, TimeUnit.SECONDS) && process.exitValue() == 0) {
                        try (InputStream inputStream = process.getInputStream()) {
                            // If the result is longer than that, then it's most likely not in the format we expected
                            int MAX_RESULT_LENGTH = 12;
                            byte[] buffer = new byte[MAX_RESULT_LENGTH + 1];
                            int totalRead = 0;
                            int read;
                            while ((read = inputStream.read(buffer, totalRead, buffer.length - totalRead)) > 0) {
                                totalRead += read;
                            }

                            if (totalRead <= MAX_RESULT_LENGTH) {
                                String str = new String(buffer, 0, totalRead, US_ASCII).trim();

                                int idx = str.indexOf(' ');
                                if (idx > 0) {
                                    return Short.parseShort(str.substring(idx + 1));
                                }
                            }
                        }
                    }
                } catch (IOException | InterruptedException | NumberFormatException ignored) {
                } finally {
                    if (process != null) {
                        process.destroy();
                    }
                }

                return 0;
            }

            @Override
            public int isTty(int fd) {
                return PosixCLibrary.isatty(fd);
            }
        };
    }

    @Override
    protected Kernel32 createKernel32() {
        throw new UnsupportedOperationException();
    }
}
