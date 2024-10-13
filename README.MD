# SourceGen
Generator for source diff repos.\
Supports:
- Minecraft Mojang mappings
- Minecraft Yarn mappings
- Minecraft Feather mappings
- Minecraft RetroMCP mappings
- CosmicReach

## Usage
Before being able to use the generator, you need to prepare some things.\
First, download the generator and `runtime_files.zip` and put the into a directory. The runtime files need to be extracted.\
Then, create a git repository where the generated files will be stored. <b>Make sure the repo is private! Publishing the generated files without permission will make you liable to legal action by the copyright holder!</b>\
After that, start the generator once to create the config file:
```shell
java -jar SourceGen.jar
```

## Building
```shell
./gradlew build
```
The compiled jar and the runtime files will be located in the `build/libs` directory.
