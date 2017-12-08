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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.natives.Loaders.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class helps you with the painful setup of native libraries at the Java Runtime Environment.
 * To reduce external setup and configuration it is assumed that the registered binaries for the
 * native library are contained in the applications classpath. <br>
 * A typical usage example would look like:
 * 
 * <pre>
 * new NativeLibrary("nativeLib") //
 *     .register(Platform.Windows_x86, "nativeLib.dll") //
 *     .register(Platform.Windows_x64, "nativeLib_64.dll") //
 *     .register(Platform.Linux86, "libnative.so") //
 *     .register(Platform.Linux64, "libnative_64.so") //
 *     .require(true) //
 *     .deleteOnExit(true)//
 *     .load(Loaders.JAVA_LIBRARY_PATH__LOADER);
 * </pre>
 * 
 * The above example shows how to configure and setup the native library 'nativeLib". You have to
 * configure the various binaries for the different platforms you will support. After this you can
 * make optional settings. Afterwards call {@link #load(Loader)} with a suitable {@link Loader} and
 * you're done.
 *
 * @see Platform
 * @see Loaders
 */
public class NativeLibrary {

  private static final Logger LOGGER = LoggerFactory.getLogger(NativeLibrary.class);

  private static final File USER_DIR = new File(System.getProperty("java.io.tmpdir"));

  private final String key;
  private final Map<Platform, List<String>> libraryPaths = new HashMap<>();

  private boolean required = true;
  private File directory;
  private boolean deleteOnExit;

  /**
   * Create the native library with the given key.
   * 
   * @param key the native library's key
   */
  public NativeLibrary(final String key) {
    this.key = key;
    LOGGER.debug("Register native library {}", key);
  }

  /**
   * Register the given binary from classpath for the given platform. It is possible to register
   * more than one native library per platform with multiple calls of
   * {@link #register(Platform, String)}.
   *
   * @param platform the platform the native library depends on
   * @param nativeLibraryPath the class path to the native library
   * @return same instance for ongoing calls
   *
   * @see Platform
   */
  public NativeLibrary register(final Platform platform, final String nativeLibraryPath) {
    final List<String> nativePaths = Optional.ofNullable(libraryPaths.get(platform)) //
        .orElseGet(() -> {
          final List<String> emptyList = new ArrayList<>();
          libraryPaths.put(platform, emptyList);
          return emptyList;
        });
    nativePaths.add(nativeLibraryPath);
    return this;
  }

  /**
   * Controls if the native library is required or not. <code>true</code> is default.
   *
   * @param required the native library is required (<code>true</code>) and any error will raise an
   *        exception <br>
   *        optional (<code>false</code>) and any error will just log a warning
   * @return same instance for ongoing calls
   */
  public NativeLibrary require(final boolean required) {
    this.required = required;
    return this;
  }

  /**
   * Controls the path where the native library will be extracted. Omit this call and the default of
   * <code>System.getProperty("java.io.tmpdir")</code> will be used.
   *
   * @param directory the directory where the native library will be extracted
   * @return same instance for ongoing calls
   */
  public NativeLibrary extractTo(final File directory) {
    this.directory = directory;
    return this;
  }

  /**
   * Controls if the native library should be deleted after exit. <code>false</code> is default.
   *
   * @param deleteOnExit delete the native library on exit (<code>true</code>), or leave them for
   *        later (<code>false</code>)
   * @return same instance for ongoing calls
   */
  public NativeLibrary deleteOnExit(final boolean deleteOnExit) {
    this.deleteOnExit = deleteOnExit;
    return this;
  }

  /**
   * Load the native library. This will extract all registered binaries for the underlying platform
   * and loads them via the given loader.
   * 
   * @param loader the loader that makes the native library accessible to the JVM
   *
   * @see Loaders
   */
  public void load(final Loader loader) {
    try {
      final List<File> nativeLibraryFiles = extractNativeLibraries();
      LOGGER.debug("Load native library for {}", key);
      loader.load(nativeLibraryFiles);
    } catch (final Exception exception) {
      final String message = String.format("Unable to load natives for %s", key);
      if (required) {
        throw new RuntimeException(message, exception);
      }
      LOGGER.warn(message);
    }
  }

  private List<File> extractNativeLibraries() {
    final Platform platform = Platforms.getCurrent();
    LOGGER.debug("Platform is {}", platform);
    final List<String> libraryPaths = Optional.ofNullable(this.libraryPaths.get(platform)) //
        .orElseThrow(() -> new IllegalArgumentException(
            String.format("No native library registered for %s at %s", key, platform)));
    LOGGER.debug("Extract {}", libraryPaths);
    final File nativesDirectory = getNativesDirectory();
    LOGGER.debug("Natives directory is {}", nativesDirectory);
    if (deleteOnExit) {
      nativesDirectory.deleteOnExit();
      LOGGER.debug("Cleanup on exit activated.");
    }
    return libraryPaths.stream() //
        .map(libraryPath -> extractNativeLibraries(nativesDirectory, libraryPath)) //
        .collect(Collectors.toList());
  }

  private File getNativesDirectory() {
    if (directory != null) {
      directory.mkdirs();
      return directory;
    }
    final File userDir = new File(USER_DIR, String.format("%s-natives", key));
    userDir.mkdirs();
    return userDir;
  }

  private File extractNativeLibraries(final File nativesDirectory, final String libraryPath) {
    final URL libraryUrl = Thread.currentThread().getContextClassLoader().getResource(libraryPath);
    if (libraryUrl == null) {
      throw new IllegalArgumentException(
          String.format("Unable to find native binary %s for library %s", libraryPath, key));
    }
    final String libraryName;
    libraryName = FilenameUtils.getName(libraryPath);
    final File libraryFile = new File(nativesDirectory, libraryName);
    libraryFile.getParentFile().mkdirs();
    try {
      final URLConnection urlConnection = libraryUrl.openConnection();
      try (final InputStream inputStream = urlConnection.getInputStream()) {
        try (final OutputStream outputStream =
            new BufferedOutputStream(new FileOutputStream(libraryFile))) {
          IOUtils.copy(inputStream, outputStream);
        }
      }
    } catch (final Exception exception) {
      throw new RuntimeException(exception);
    }
    if (deleteOnExit) {
      libraryFile.deleteOnExit();
    }
    // TODO make accessible for linux and mac
    return libraryFile;
  }



}
