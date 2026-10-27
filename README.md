# DonutSMP Auction Assistant (Fabric 1.21.11, client-side)

Display-only helper: timer, streamer-delay estimates, chat tracking, bid calculator.
It NEVER sends bids or chat for you. "Copy bid" only puts a number on your clipboard.

## Build (see the full guide in the chat reply)
1. Generate a project at https://fabricmc.net/develop/template (Minecraft 1.21.11, **Yarn** mappings, Java 21).
   Keep its `gradle/wrapper/`, `gradlew`, `gradlew.bat`.
2. Replace its `src/`, `build.gradle`, `gradle.properties`, `settings.gradle` with the ones here,
   then paste the exact versions the generator shows into `gradle.properties`.
3. `./gradlew build` -> `build/libs/donutsmp-auction-assistant-1.0.0.jar`.
4. Put the jar (not `-sources`) and Fabric API in `.minecraft/mods`.

## Commands (client-side)
/dsmpa test <text>   feed a fake chat line to the parser
/dsmpa log           show last chat lines + detected events
/dsmpa reload        reload the JSON config
/dsmpa reset         clear tracked bids and the timer

Default key: K opens the screen. Other keys (toggle/reset/copy) are unbound by default.
Config: .minecraft/config/donutauction-assistant.json
