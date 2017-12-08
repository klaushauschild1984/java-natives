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


import static org.natives.Platform.Architecture.x64;
import static org.natives.Platform.Architecture.x86;
import static org.natives.Platform.OS.Linux;
import static org.natives.Platform.OS.Mac;
import static org.natives.Platform.OS.Windows;

/**
 * Represents a platform.
 */
public enum Platform {

  /**
   * Windows (32bit)
   */
  Windows_x86(Windows, x86),

  /**
   * Windows (64bit)
   */
  Windows_x64(Windows, x64),

  /**
   * Linux (32bit)
   */
  Linux_x86(Linux, x86),

  /**
   * Linux (64bit)
   */
  Linux_x64(Linux, x64),

  /**
   * MacOS
   */
  MacOS(Mac, null),

  ;

  private final OS os;
  private final Architecture architecture;

  Platform(final OS os, final Architecture architecture) {
    this.os = os;
    this.architecture = architecture;
  }

  @Override
  public String toString() {
    final StringBuilder builder = new StringBuilder();
    builder.append(os);
    if (architecture != null) {
      builder.append(" (");
      builder.append(architecture);
      builder.append(")");
    }
    return builder.toString();
  }

  /**
   * Represents the operating system of the platform.
   */
  enum OS {

    /**
     * Windows.
     */
    Windows,

    /**
     * Linux.
     */
    Linux,

    /**
     * Mac.
     */
    Mac,

    ;

  }

  /**
   * Represents the architecture of a platform.
   */
  enum Architecture {

    /**
     * 32bit
     */
    x86,

    /**
     * 64bit
     */
    x64,;

  }

}
