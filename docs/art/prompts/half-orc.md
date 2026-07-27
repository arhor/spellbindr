# Half-Orc race carousel prompt

- Method: OpenAI built-in image generation
- Created: 2026-07-27
- Intended asset: `app/src/main/assets/images/races/half-orc.webp`

## Prompt

```text
Create a 3:4 portrait Western fantasy sourcebook illustration representing Half-Orcs, with two clearly adult half-orc
adventurers of different gender presentation and builds, equally prominent. They look intimidating at first glance,
scarred and powerful, but are stoic, alert, and protective rather than savage. Show one quietly repairing the other's
shield strap at a rough frontier gate after rain. Use coherent muted olive and warm grey-green features, practical
patched mail, wool, leather, and hand-painted 2D gouache and watercolor with visible brushwork, paper texture, lightly
inked storybook edges, simplified anatomy, and classic European/American tabletop-RPG sourcebook styling matching the
approved Human asset. Use olive, peat, iron grey, muted red, wet slate, and parchment. Keep faces and the repair
gesture in the upper half and bottom 38 percent dark and low-detail for UI. No text, logo, watermark, UI, gore,
photorealism, glossy airbrushing, cinematic or gacha-game poster styling, anime, 3D rendering, snarling, savage
stereotypes, skull trophies, oversized tusks, bodybuilder exaggeration, evil caricature, or battlefield carnage.
```

## Processing

`ffmpeg -i input.png -c:v libwebp -quality 73 -compression_level 6 half-orc.webp`
