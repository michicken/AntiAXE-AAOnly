# AntiAXE For AA --MICx

A minimal Minecraft Forge 1.8.9 client mod for Hypixel Zombies: Alien Arcadium.
When the local player rolls `The Puncher`, it temporarily blocks local right-click
handling while the crosshair intersects the Lucky Chest claim zone.

## Safety boundary

- Suppresses the local `Minecraft.rightClickMouse()` action before vanilla interaction handling.
- Does not create, replace, cancel, delay, reorder, or spoof network packets.
- Does not aim, move, click, or claim anything automatically.
- Only the local `You found The Puncher...` chat result arms the ten-second guard.

This technical design does not constitute approval by Hypixel. Server rules and
enforcement can change, so users remain responsible for checking current rules.

## Build

```sh
./gradlew clean test build
```

The release artifact is `build/libs/AntiAXE-AAOnly.jar`.
