#CrashWoodpecker

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.drakeet.library/crashwoodpecker/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/me.drakeet.library/crashwoodpecker)

A nice uncaught exception handler library like Square's [LeakCanary](https://github.com/square/leakcanary). Support showing logs both on Logcat & Woodpecker.

![screenshot.png](art/s2.png)

## Getting started

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.drakeet.library/crashwoodpecker/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/me.drakeet.library/crashwoodpecker)

In your `build.gradle`:

```gradle
dependencies {
  debugCompile 'me.drakeet.library:crashwoodpecker:1.3.1'
  releaseCompile 'me.drakeet.library:crashwoodpecker-do-nothing:1.3.1'
}
```

In your `Application` class:

```java
public class ExampleApplication extends Application {

  @Override public void onCreate() {
    super.onCreate();
    CrashWoodpecker.flyTo(this);
    // or add more highlight keys except the default package name:
    // CrashWoodpecker.flyTo(this).withKeys("widget", "me.drakeet");
  }
}
```

And in your `AndroidManifest.xml` file:

```xml
<application
    android:name=".ExampleApplication" // <-- 
    ...>
    ...
</application>
```

**That is all!** CrashWoodpecker will automatically show an Activity when your app crash with uncaught exceptions including new thread exceptions in your debug build.

[![GitHub release](https://img.shields.io/badge/sample%20apk-v1.1-brightgreen.svg?style=flat)](https://github.com/drakeet/CrashWoodpecker/releases/download/v1.1/LittleWood.apk) 


## TODO

* Save logs and to reload.

## Thanks

Great Square: http://square.github.io

markzhai: https://github.com/markzhai

jug6ernaut: https://github.com/jug6ernaut

## About me

https://github.com/drakeet


License
============

    The MIT License (MIT)

    Copyright (c) 2015 drakeet

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in all
    copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
    SOFTWARE.
