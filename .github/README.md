
# Minestom

[![license](https://img.shields.io/github/license/hollow-cube/minestom-ce?style=for-the-badge&color=b2204c)](../LICENSE)
[![standard-readme compliant](https://img.shields.io/badge/readme%20style-standard-brightgreen.svg?style=for-the-badge)](https://github.com/RichardLitt/standard-readme)  
[![javadocs](https://img.shields.io/badge/documentation-javadocs-4d7a97?style=for-the-badge)](https://javadoc.minestom.net)
[![wiki](https://img.shields.io/badge/documentation-wiki-74aad6?style=for-the-badge)](https://wiki.minestom.net/)
[![discord-banner](https://img.shields.io/discord/706185253441634317?label=discord&style=for-the-badge&color=7289da)](https://discord.gg/pkFRvqB)

Minestom is an open-source library that enables developers to create their own Minecraft server software, without any code from Mojang.

The main difference between Mojang's vanilla server and a minestom-based server, is that ours does not contain any features by default!
However, we have a complete API which is designed to allow you to make anything possible, with ease.

This is a developer API not meant to be used by end-users. Replacing Bukkit/Forge/Sponge with this **will not work** since we do not implement any of their APIs.

> **Warning**
> 
> `minestom-ce` is a fork with breaking changes from `Minestom/Minestom`. The list of changes can be found [here](https://github.com/hollow-cube/minestom-ce/blob/main/CHANGELOG.md).

# Table of contents
- [Install](#install)
- [Usage](#usage)
- [Why Minestom?](#why-minestom)
- [License](#license)

# Install
Minestom is not installed like Bukkit/Forge/Sponge.
As Minestom is a Java library, it must be loaded the same way any other Java library may be loaded.
This means you need to add Minestom as a dependency, add your code and compile by yourself.

`minestom-ce` is available on [Maven Central](https://central.sonatype.com/artifact/dev.hollowcube/minestom-ce),
and can be installed like the following (Gradle/Groovy):

```groovy
repositories {
    mavenCentral()
}

dependencies {
    implementation 'dev.hollowcube:minestom-ce:<first 10 chars of commit hash>'
}
```

# Usage
An example of how to use the Minestom library is available [here](/demo).
Alternatively you can check the official [wiki](https://wiki.minestom.net/) or the [javadocs](https://minestom.github.io/Minestom/).

# What is `minestom-ce`?
`minestom-ce` is a fork of `Minestom/Minestom` with some controversial/breaking changes. It was originally started as @mworzala's
personal fork for making changes, but has since been used by a number of others and aims to be relatively stable. The high
level goal of many changes are to make Minestom more of a library and less of a server implementation. For example:
* Removing extensions
* Removing logging & terminal implementations

The name "community edition" is not a reflection of an intentional rift between the two projects, it was just a joke between
a few people that stuck. I (@mworzala) am very happy for changes in `minestom-ce` to be merged back to `Minestom/Minestom`, I
just do not necessarily have the time to do so myself.

# License
This project is licensed under the [Apache License Version 2.0](../LICENSE).
