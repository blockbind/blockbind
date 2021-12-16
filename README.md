<img width="140px" height="140px" src="https://avatars.githubusercontent.com/u/95259820?s=200&v=4" align="right">

<h1 align="center">Block Bind</h1>
<p align="center">Synchronizing distributed Minecraft servers</p>

<hr>
<br>

- [What is Block Bind](#What-is-Block-Bind)
- [How Block Bind works](#How-Block-Bind-works)
- [Try Block Bind out](#Try-Block-Bind-out)
- [Roadmap](#Roadmap)
- [Installation](#Installation)
- [Media](#Media)

## What is Block Bind

Block bind enables you to synchronize a bunch of independent Minecraft servers. Players can see each other and interact with one another regardless of
which server they are playing on. This is a great solution for getting a bunch of players on one world: A single server would start to lag and / or
crash very fast, but if you set up multiple servers and install Block Bind you will feel no lag at all.*

**Block Bind is not a reverse proxy like BungeeCord.**

We currently support Spigot 1.16.5 and Spigot 1.18.

\* This has not been tested yet with many players and / or servers.

## How Block Bind works

Block Bind utilizes the Pub/Sub functionality of Redis to communicate with other servers. Block Bind also uses the key value storage for temporary
synchronized data.

## Try Block Bind out

We provide a Docker test environment that contains all dependencies and two 1.16.5 Spigot servers. Please take a look
at [blockbind/docker-testenv](https://github.com/blockbind/docker-testenv) if you would like to check it out.

## Roadmap

- (done in 1.0.0) Synchronize players (Movements, metadata etc)
- Synchronize block changes caused by players
- Synchronize block changes caused by other things
- Synchronize container transactions
- Synchronize other entities (Movements, metadata etc)
- Synchronize misc stuff (Chat messages for example)

> Note: The roadmap is in no specific order.

## Installation

**Requirements**

- At least two supported Minecraft servers
- A Redis server
- Java 16
- (Optional) Some sort of load balancer to distribute the amount of players on each server

**Installation**

1. Install the corresponding implementation
    1. For Spigot servers: Drop the Bukkit plugin into the plugins folder of each server
2. Restart the servers
3. Edit the configs of the Block Bind implementation
4. Restart your servers again

## Media

[Minecraft Cross Server Synchronization - "Dev log" #1](https://www.youtube.com/watch?v=dyTJMn6mU80)