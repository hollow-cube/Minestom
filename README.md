# ByteMC - Minestom

> **Help**\
> If you need help take a look into the projects wiki:\
> https://github.com/bytemcnetzwerk/minestom/wiki

> **Important**\
> This project is currently under development, please be aware that the current version might not be stable.
> If you find any bugs or have an idea which might improve this project, send us a message: https://discord.bytemc.de

# About
This is the minestom api which is used to run the ByteMC.de-network.<br>
This project provides an optimized minestom api 
with many useful features and a ready-to-use minestom server implementation to help you setup your own minestom server.

# Usage

**All available versions:** 
<a href="https://nexus.bytemc.de/service/rest/repository/browse/maven-public/net/bytemc/minestom">Click </a>

Repository:
```xml
<repository>
    <id>bytemc-public</id>
    <url>https://nexus.bytemc.de/repository/maven-public/</url>
</repository>
```

You have the choice between two repositories...

...our directly functional minestom server implementation:
```xml 
<dependency>
    <groupId>net.bytemc</groupId>
    <artifactId>server</artifactId>
    <version>VERSION</version>
</dependency>
```

...only our modified minestom api:
```xml  
<dependency>
    <groupId>net.bytemc</groupId>
    <artifactId>minestom</artifactId>
    <version>VERSION</version>
</dependency>
```
