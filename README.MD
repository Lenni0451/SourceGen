# SourceGen
Generator for source diff repos.\
Supports:
- Minecraft Mojang mappings
- Minecraft Mojang + Parchment mappings
- Minecraft Yarn mappings
- Minecraft Feather mappings
- Minecraft RetroMCP mappings
- CosmicReach

## Prerequisites
To use this generator, you need to have the following installed:
- Java 17 or higher
- Git
- [VineFlower](https://github.com/Vineflower/vineflower) (`vineflower.jar` in the run directory)
- A git repository where the source code is pushed to

> <b>NOTE:</b>\
> Making the repository public will make you subject to legal action by the copyright holder!\
> It is highly recommended to keep the repository private and not share it with anyone!

## Usage
On the first run, the generator will create a `config.yml` file in the run directory. Edit the file to your liking.\
After everything is configured, run the generator with the wanted target:
```shell
java -jar SourceGen.jar <target>
```
A list of available targets can be found by running the generator without any arguments.\
Any new targets will be added to the bottom of the list to avoid breaking existing scripts.

## Default files
If you want the generator to automatically add files to the repository, you can add them to the corresponding folder in the `defaults` directory.\
The files in the repository will be overwritten if they already exist.\
Default folders:
- `defaults/cosmicreach`
- `defaults/minecraft_feather_mappings`
- `defaults/minecraft_mojang_mappings`
- `defaults/minecraft_parchment_mappings`
- `defaults/minecraft_retromcp_mappings`
- `defaults/minecraft_yarn_mappings`
