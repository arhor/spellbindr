# Dragonborn race carousel prompt

- Method: OpenAI built-in image generation
- Created: 2026-07-27
- Intended asset: `app/src/main/assets/images/races/dragonborn.webp`

## Prompt

```text
Create a 3:4 portrait Western fantasy sourcebook illustration representing Dragonborn, with two clearly adult
dragonborn adventurers of different builds and gender presentation, equally prominent. They are proud, disciplined,
severe, and honorable, not cute or villainous. Show them at ease after training in a weathered stone court, one
checking the other's bracer or weapon binding. Use coherent copper and blue-grey reptilian anatomy with practical
worn brigandine, cloth wraps, and travel mantles. Render as hand-painted 2D gouache and watercolor with visible
brushwork, paper texture, lightly inked storybook edges, and classic European/American tabletop-RPG sourcebook styling
matching the approved Human asset. Use copper, slate, ash, oxblood, parchment, and umber. Keep crests and faces in the
upper half and bottom 38 percent dark and low-detail for UI. No text, logo, watermark, UI, gore, photorealism, glossy
airbrushing, cinematic or gacha-game poster styling, anime, 3D rendering, mascot dragons, pasted-on scales, mammal
smiles, oversized armor, roaring, excessive spikes, or fire-breath spectacle.
```

## Processing

`ffmpeg -i input.png -c:v libwebp -quality 68 -compression_level 6 dragonborn.webp`
