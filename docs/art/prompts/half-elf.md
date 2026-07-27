# Half-Elf race carousel prompt

- Method: OpenAI built-in image generation
- Created: 2026-07-27
- Intended asset: `app/src/main/assets/images/races/half-elf.webp`

## Prompt

```text
Create a 3:4 portrait Western fantasy sourcebook illustration representing Half-Elves, with two clearly adult
half-elf adventurers of different gender presentation and ethnic appearance, equally prominent. They are perceptive,
adaptable outsiders: guarded at first glance, quietly charismatic, and sharing a wry look after reading the room
better than everyone else. Place them in a border-town coaching inn doorway where woodland road and human settlement
meet. Use subtle individual elven features, mixed-origin practical travel clothing, and hand-painted 2D gouache and
watercolor with visible brushwork, paper texture, lightly inked storybook edges, simplified coherent anatomy, and
classic European/American tabletop-RPG sourcebook styling matching the approved Human asset. Use weathered teal,
walnut, muted burgundy, sage, parchment, and smoke grey. Keep faces in the upper half and bottom 38 percent dark and
low-detail for UI. No text, logo, watermark, UI, sexualization, photorealism, glossy airbrushing, cinematic or
gacha-game poster styling, anime, 3D rendering, broad smiles, glamour, tragic-orphan clichés, split-face symbolism,
exaggerated ears, or dramatic magic.
```

## Processing

`ffmpeg -i input.png -c:v libwebp -quality 78 -compression_level 6 half-elf.webp`
