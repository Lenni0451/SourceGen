# SourceGen
Generator for source diff repos.\
<b>The generator has only been tested on Linux. While I didn't intentionally break compatibility with other OSes, I can't guarantee that it will work on them.</b>

## Supported Targets
1. Minecraft Mojang mappings
2. CosmicReach
3. Minecraft RetroMCP mappings
4. Minecraft Feather mappings
5. Minecraft Yarn mappings
6. Minecraft Mojang + Parchment mappings
7. Minecraft assets
8. ~~Minecraft Bedrock Edition mappings~~ _no longer supported_
9. Minecraft MCP mappings
10. Hytale Server
11. Minecraft Nostalgia mappings

## Prerequisites
To use this generator, you need to have the following installed:
- Java 17 or higher
- Git
- [VineFlower](https://github.com/Vineflower/vineflower) (`vineflower.jar` in the run directory)
- Hytale Downloader (for Hytale Server target) (`hytale-downloader` in the run directory)
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

After the generator has finished, the repository will automatically be pushed to the remote.\
Purely local repositories are not supported at the moment.

## Default files
If you want the generator to automatically add files to the repository, you can add them to the corresponding folder in the `defaults` directory.\
The files in the repository will be overwritten if they already exist.\
Default folders:
- `defaults/minecraft_mojang_mappings`
- `defaults/cosmicreach_client`
- `defaults/cosmicreach_server`
- `defaults/retromcp`
- `defaults/minecraft_feather_mappings`
- `defaults/minecraft_yarn_mappings`
- `defaults/minecraft_parchment_mappings`
- `defaults/minecraft_assets`
- `defaults/minecraft_mcp_mappings`
- `defaults/hytale_server`
- `defaults/minecraft_client_nostalgia_mappings`
- `defaults/minecraft_server_nostalgia_mappings`

## Recreating branches
From time to time, you might want to recreate the branches in the repository because of changes in the mappings or updates to the decompiler.\
To do this, simply delete the branches you want to recreate and run the generator again.

If you only want to recreate some commits of a branch, you can reset the branch to the commit you want to start from.
```shell
git checkout <branch>
git reset --hard <commit>
git push -f
```
