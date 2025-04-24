# Obpf4J Tetris Client

This project implements a Java game client for
the [OpenBrickProtocolFoundation Simulator](https://github.com/OpenBrickProtocolFoundation/simulator) project. It uses
the
new [Foreign Function & Memory API](https://docs.oracle.com/en/java/javase/22/core/foreign-function-and-memory-api.html)
released in JDK 22 in order to interact with the natively compiled simulator. This client is intended to be used
together with the [Obpf4J Lobby Server](https://github.com/nimazzo/lobby-server-4j). It can handle both singleplayer and
multiplayer modes, including user login, lobby creation, and joining existing lobbies.

Running the client without a running Lobby Server will show an error message upon startup, but the game can still be
played in singleplayer mode.

## Project Structure

- `client/` - Contains the main game client implementation
- `setup/` - Contains the `JextractRunner` that helps with generating the Java bindings for the simulator dll by calling
  the `Jextract` tool with the correct parameters.

## Requirements

- Java JDK 22 or higher
- Jextract (tested with version 22)
- CMake (only for building the game server executable)
- C++ compiler (only for building the game server executable)

### Game Simulator DLL

As the Obpf4J Tetris Client will use a natively compiled library for simulating the actual game state, you need to have
a compiled simulator dll available on your system.
You can download the source code for the simulator
at [OpenBrickProtocolFoundation/simulator](https://github.com/OpenBrickProtocolFoundation/simulator) (Latest known
working commit: 43f8401f87815a722a9e45b893e4604d85881ec9).

```sh
$ git clone --no-checkout https://github.com/OpenBrickProtocolFoundation/simulator.git
$ cd simulator
$ git checkout 43f8401f87815a722a9e45b893e4604d85881ec9
$ mkdir build
$ cd build
$ cmake ..
$ cmake --build . --target obpf
```

After compiling, the created dll can be found at `simulator/build/bin/obpf/obpf_d` (or
`simulator/build/bin/obpf/Debug/obpf_d.dll` on Windows using MSVC)

#### Tested with

| OS           | CMake Version | Compiler                                  |
|--------------|---------------|-------------------------------------------|
| Windows 10   | 3.30.0        | Visual Studio 17 2022, MSVC 19.40.33808.0 |
| Ubuntu 24.04 | 3.30.4        | g++ 13.2.0                                |

## Generating Java Bindings for the DLL

To generate Java bindings for the simulator dll, you need to
install [Jextract](https://github.com/openjdk/jextract/tree/master) on your system.

### Create a Main Header File

For `Jextract` to be able to create the required Java bindings, it needs a single header file that includes all the
required definitions. Inside the `simulator/src/obpf/include/obpf/` directory, create a new file called `main_header.h `
and add the following content:

```
#include "constants.h"
#include "simulator.h"
```

You can then run the `JextractRunner` found
inside the `setup` module in this project. This will generate the necessary Java classes to interface with the
simulator.

```bash
java -DCPROJECT_PATH=PATH/TO/SIMULATOR/DIRECTORY setup/src/main/java/com/example/JextractRunner.java
```

## Building the Project

You can now build the project using either Maven directly or the Maven wrapper:

Using Maven wrapper (recommended):

```bash
# On Unix-like systems
./mvnw clean package

# On Windows
mvnw.cmd clean package
```

## Running the Game

Running the game requires the previously compiled simulator dll to be found in the system path. On Windows, you can add
the directory containing the dll to the system `PATH` environment variable. On Unix-like systems, you can set the
`LD_LIBRARY_PATH` environment variable to include the directory containing the dll.

```bash
# On Unix-like systems
LD_LIBRARY_PATH=$LD_LIBRARY_PATH:libs java -jar shade/client.jar

# On Windows
PATH=$PATH:libs java -jar shade/client.jar
```

You can also pass the `-Djavafx.animation.fullspeed=true` option to enable uncapped fps.

## Configuring the Lobby Server Adress

By default, the client will try to connect to the lobby server at `localhost:8080`. You can change this by setting the
`LOBBY_HOST` and `LOBBY_PORT` environment variables to the desired address.