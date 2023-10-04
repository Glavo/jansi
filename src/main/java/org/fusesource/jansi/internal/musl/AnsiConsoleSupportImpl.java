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

import org.fusesource.jansi.internal.AnsiConsoleSupport;
import org.fusesource.jansi.internal.Stty;
import org.graalvm.nativeimage.c.type.CTypeConversion;

public final class AnsiConsoleSupportImpl extends AnsiConsoleSupport {

    public AnsiConsoleSupportImpl() {
        super("musl");
    }

    @Override
    protected CLibrary createCLibrary() {
        return new CLibrary() {
            private final String stdoutTty = CTypeConversion.toJavaString(PosixCLibrary.ttyname(STDOUT_FILENO));
            private final String stderrTty = CTypeConversion.toJavaString(PosixCLibrary.ttyname(STDERR_FILENO));

            @Override
            public short getTerminalWidth(int fd) {
                String ttyName;
                if (fd == STDOUT_FILENO) {
                    ttyName = stdoutTty;
                } else if (fd == STDERR_FILENO) {
                    ttyName = stderrTty;
                } else {
                    return 0;
                }

                int width = Stty.getTerminalWidth(ttyName);
                return width >= 0 && width <= Short.MAX_VALUE ? (short) width : 0;
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
