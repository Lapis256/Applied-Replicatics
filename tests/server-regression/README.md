# Legacy matter-key native regression

Use a disposable Minecraft26.1.2/NeoForge126 dedicated server, Java25 and Gradle9.2.1. Never install this add-on on a live server.

1. Put AE2, Applied Replicatics and their dependencies into this project's ignored `mods/` directory.
2. Run `gradle -p tests/server-regression jar` from the repository root.
3. Add the resulting test JAR to the disposable server and set `server-ip=127.0.0.1`.
4. Supply the old `rep_ae2_bridge-3.0.0.0.0-neoforge-26.1.2.jar` through the JVM option `-Ditv.qa.legacyBridge=/path/to/old-bridge.jar`. Keep it OUT of the mods folder: the fixture loads only its original key codec in an isolated classloader, without registering the old mod or channel.
5. Start with no players and enter `serverregression` in the console; every named case must pass. Stop/discard the fixture afterward.

The shipped old codec generates golden payloads for every registered nonempty matter type. Tests check generic-stack quantities beyond2^53, current packet roundtrips, unknown-data preservation, ordinary item decoding and unchanged key-type registration. No old JARs or real world/player data are included here.
