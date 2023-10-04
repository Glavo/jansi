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
package org.fusesource.jansi.internal.stty;

import org.fusesource.jansi.internal.AnsiConsoleSupport;

public final class AnsiConsoleSupportImpl extends AnsiConsoleSupport {
    public AnsiConsoleSupportImpl(String providerName) {
        super("stty");
    }

    @Override
    protected CLibrary createCLibrary() {

        return new CLibrary() {

            @Override
            public short getTerminalWidth(int fd) {
                return 0;
            }

            @Override
            public int isTty(int fd) {
                return 0;
            }
        };
    }

    @Override
    protected Kernel32 createKernel32() {
        throw new UnsupportedOperationException();
    }
}
