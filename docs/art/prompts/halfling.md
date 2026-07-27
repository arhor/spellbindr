# Halfling race carousel prompt

- Method: OpenAI built-in image generation
- Created: 2026-07-27
- Intended asset: `app/src/main/assets/images/races/halfling.webp`

## Prompt

```text
Create a 3:4 portrait Western fantasy sourcebook illustration representing Halflings, with two clearly adult halfling
adventurers, one masculine-presenting and one feminine-presenting, equally prominent. They are clever, nimble, and
mischievously self-assured rather than childish or uniformly friendly; one has just palmed a useful key while the
other suppresses a knowing grin. Set them in a human-sized roadside market and wagon yard viewed from their scale.
Use hand-painted 2D gouache and watercolor, visible brushwork, paper texture, lightly inked storybook edges,
simplified coherent anatomy, and classic European/American tabletop-RPG sourcebook styling matching the approved
Human asset. Use practical worn travel layers in mustard, russet, moss, teal, cream, and brown. Keep adult faces in
the upper half and the bottom 38 percent dark and low-detail for UI. No text, logo, watermark, UI, sexualization,
photorealism, glossy airbrushing, cinematic or gacha-game poster styling, anime, 3D rendering, child proportions,
mascot styling, feast clichés, oversized-feet caricature, or innocent broad smiles.
```

## Processing

The generated portrait was top-cropped to the common 3:4 frame before export:

```shell
ffmpeg -i input.png -vf 'crop=940:1254:0:0,scale=1086:1448' \
  -c:v libwebp -quality 75 -compression_level 6 halfling.webp
```
