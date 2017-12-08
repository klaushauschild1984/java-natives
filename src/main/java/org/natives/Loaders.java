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

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.LoggerFactory;

/**
 * Helper for {@link Loader}.
 */
public enum Loaders {

  ;

  /**
   * This {@link Loader} uses {@link System#load(String)} to load the given native libraries.
   */
  public static final Loader SYSTEM_LOAD__LOADER = (nativeLibraries -> nativeLibraries
      .forEach(nativeLibrary -> System.load(nativeLibrary.getAbsolutePath())));

  /**
   * This {@link Loader} sets the system property 'java.library.path' to the parent directory of the
   * given native libraries.
   */
  public static final Loader JAVA_LIBRARY_PATH__LOADER =
      SYSTEM_PROPERTY_PATH__LOADER("java.library.path");

  /**
   * This {@link Loader} takes the parent directory of the given native libraries and sets the
   * specified system property to their {@link File#getAbsoluteFile() absolute path}. Each native
   * library has to be located in the same directory.
   *
   * @param systemProperty the system property to set via
   *        {@link System#setProperty(String, String)}, typically something like 'java.library.path'
   *
   * @return the loader
   */
  public static Loader SYSTEM_PROPERTY_PATH__LOADER(final String systemProperty) {
    return (nativeLibraries -> {
      final List<File> parentDirectories = nativeLibraries.stream() //
          .map(File::getParentFile) //
          .distinct() //
          .collect(Collectors.toList());
      if (parentDirectories.size() > 1) {
        LoggerFactory.getLogger(Loaders.class).warn(
            "Library directories {} are ambiguous. Just configure the first one.",
            parentDirectories);
      }
      try {
        System.setProperty(systemProperty, parentDirectories.get(0).getAbsolutePath());
        final Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
        fieldSysPath.setAccessible(true);
        fieldSysPath.set(null, null);
      } catch (final Exception exception) {
        throw new RuntimeException(exception);
      }
    });
  }

  /**
   * Loads the given native libraries into the system. It depends on the native library which
   * approach fits best the current case.
   */
  interface Loader {

    /**
     * Loads the given native libraries into the system.
     * 
     * @param nativeLibrary the native libraries to load
     */
    void load(List<File> nativeLibrary);

  }

}
