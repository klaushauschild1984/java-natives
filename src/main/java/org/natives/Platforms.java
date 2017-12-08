/*
 * Java Natives Copyright (C) 2017 Klaus Hauschild
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 */

package org.natives;

import static org.natives.Platform.Linux_x64;
import static org.natives.Platform.Linux_x86;
import static org.natives.Platform.MacOS;
import static org.natives.Platform.Windows_x64;
import static org.natives.Platform.Windows_x86;
import static org.natives.Platform.OS.Linux;
import static org.natives.Platform.OS.Mac;
import static org.natives.Platform.OS.Windows;
import org.apache.commons.lang3.SystemUtils;
import org.natives.Platform.Architecture;
import org.natives.Platform.OS;

/**
 * Helper for {@link Platform}.
 */
public enum Platforms {

  ;

  /**
   * Determines the current {@link Platform}.
   * 
   * @return the current {@link Platform}
   */
  public static Platform getCurrent() {
    switch (getOS()) {
      case Windows:
        switch (getArchitecture()) {
          case x86:
            return Windows_x86;
          case x64:
            return Windows_x64;
          default:
            throw new IllegalStateException();
        }
      case Linux:
        switch (getArchitecture()) {
          case x86:
            return Linux_x86;
          case x64:
            return Linux_x64;
          default:
            throw new IllegalStateException();
        }
      case Mac:
        return MacOS;
      default:
        throw new IllegalStateException();
    }
  }

  private static OS getOS() {
    if (SystemUtils.IS_OS_WINDOWS) {
      return Windows;
    } else if (SystemUtils.IS_OS_LINUX) {
      return Linux;
    } else if (SystemUtils.IS_OS_MAC) {
      return Mac;
    }
    throw new IllegalStateException(String.format("Unsupported operating system %s (%s)",
        SystemUtils.OS_NAME, SystemUtils.OS_VERSION));
  }

  private static Architecture getArchitecture() {
    // TODO
    // https://stackoverflow.com/questions/4748673/how-can-i-check-the-bitness-of-my-os-using-java-j2se-not-os-arch

    if (SystemUtils.OS_ARCH.contains("32")) {
      return Architecture.x86;
    } else if (SystemUtils.OS_ARCH.contains("64")) {
      return Architecture.x64;
    }
    throw new IllegalStateException(
        String.format("Unsupported architecture %s", SystemUtils.OS_ARCH));
  }

}
