# SourceGen
Generator for source diff repos.\
Supports:
- Minecraft Mojang mappings
- Minecraft Yarn mappings
- Minecraft Feather mappings
- Minecraft RetroMCP mappings
- CosmicReach

## Usage
Before running the generator, you need to extract the `runtime_files.zip` file to the run directory.\
After that, configure all the necessary files.\
If everything is set up correctly, you can run the generator with the following command:
```shell
java -jar SourceGen.jar <generator id>
```
To list all available generators, run the command without any arguments.

## Building
```shell
./gradlew build
```
The compiled jar and the runtime files will be located in the `build/libs` directory.
