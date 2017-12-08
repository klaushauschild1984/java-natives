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

import static org.testng.Assert.assertTrue;
import java.io.File;
import org.testng.annotations.Test;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class JInputTest {

  @Test
  public void missingNativesTest() {
    Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
    assertTrue(controllers.length == 0);
  }

  @Test
  public void nativesTest() {
    new NativeLibrary("jinput") //
        .register(Platform.Windows_x86, "jinput-dx8.dll") //
        .register(Platform.Windows_x86, "jinput-raw.dll") //
        .register(Platform.Windows_x64, "jinput-dx8_64.dll") //
        .register(Platform.Windows_x64, "jinput-raw_64.dll") //
        .require(true) //
        .deleteOnExit(true)//
        .extractTo(new File("target", "jinput-natives")) //
        .load(Loaders.JAVA_LIBRARY_PATH__LOADER);

    final Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
    assertTrue(controllers.length > 0);
    for (final Controller controller : controllers) {
      System.out.println(controller.getName());
    }
  }

}
