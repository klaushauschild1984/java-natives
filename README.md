# Java Natives [![Build Status](https://travis-ci.org/klaushauschild1984/java-natives.svg?branch=master)](https://travis-ci.org/klaushauschild1984/java-natives) [![Quality Gate](https://sonarcloud.io/api/badges/gate?key=org.natives%3Ajava-natives)](https://sonarcloud.io/dashboard?id=org.natives%3Ajava-natives)

Java Natives is a java library that makes the use and setup of native libraries like [LWJGL](https://www.lwjgl.org/) or
[PDFlib](http://www.pdflib.com) very easy. It avoids any need of external configuration or installation.

The following simple step have to be performed:
* bundle the native binaries into you application, they have to accessible over the VMs classpath
* register the binaries of the native library for the platform you want to support
* load library

## Usage

With this sample code executed at the very start of you application [JInput](https://github.com/jinput/jinput) works
right out of the box for example.

```java
import java.io.File;
import net.java.games.input.Controller;
import net.java.games.input.ControllerEnvironment;

public class JInputTest {

  public static void main(final String[] args) {
    new NativeLibrary("jinput") //
        .register(Platform.Windows_x86, "jinput-dx8.dll") //
        .register(Platform.Windows_x86, "jinput-raw.dll") //
        .register(Platform.Windows_x64, "jinput-dx8_64.dll") //
        .register(Platform.Windows_x64, "jinput-raw_64.dll") //
        .register(Platform.Linux_x86, "libjinput-linux.so") //
        .register(Platform.Linux_x64, "libjinput-linux64.so") //
        .register(Platform.MacOS, "libjinput-osx.jnilib") //
        .load(Loaders.JAVA_LIBRARY_PATH__LOADER);

    final Controller[] controllers = ControllerEnvironment.getDefaultEnvironment().getControllers();
    for (final Controller controller : controllers) {
      System.out.println(controller.getName());
    }
  }

}
```

Use this additions to your `pom.xml`

```xml
<project>
...
  <dependencies>
    <dependency>
      <groupId>org.natives</groupId>
      <artifactId>java-natives</artifactId>
      <version>1.0</version>
    </dependency>
  </dependencies>
  ...
  <repositories>
    <repository>
      <id>java-natives-repo</id>
      <name>Java Natives Repository</name>
      <url>https://raw.githubusercontent.com/klaushauschild1984/java-natives/mvn-repo/</url>
      <snapshots>
        <enabled>true</enabled>
        <updatePolicy>always</updatePolicy>
      </snapshots>
    </repository>
  </repositories>
...
</project>
```

## How it works

To do the trick Java Natives performs two tasks. At first it extracts the native binary depending on the executed
platform to an accessible directory. For the second step it let the VM load the extracted binary. With this approach any
external configuration is avoided. This is also handsome for scenarios where it is not possible to do such external
configurations like changing environment variables or placing the binary in protected system directories.
