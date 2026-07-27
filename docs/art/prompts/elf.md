# Elf race carousel prompt

- Method: OpenAI built-in image generation
- Created: 2026-07-27
- Intended asset: `app/src/main/assets/images/races/elf.webp`

## Prompt

```text
Create a 3:4 portrait Western fantasy sourcebook illustration representing Elves with the cultivated High-Elf
temperament in the current data. Show two clearly adult elf adventurers, one masculine-presenting and one
feminine-presenting, equally prominent: elegant, intellectually formidable, coolly amused, and faintly sarcastic, as
if they already noticed the flaw in someone else's argument. Place them on an old open-air arcane academy terrace
woven into pale trees; one holds an annotated folio while the other glances sideways with one raised eyebrow. Use
hand-painted 2D gouache and watercolor, visible brushwork, paper texture, lightly inked storybook edges, simplified
coherent anatomy, and classic European/American tabletop-RPG sourcebook styling matching the approved Human asset.
Use faded sage, parchment, muted plum, silver-grey, dusty blue, and antique gold. Keep faces in the upper half and the
bottom 38 percent dark and low-detail for UI. No text, logo, watermark, UI, sexualization, photorealism, glossy
airbrushing, cinematic or gacha-game poster styling, anime, 3D rendering, broad smiles, smug caricature, extravagant
filigree, dramatic magic, or exaggerated ears.
```

## Processing

`ffmpeg -i input.png -c:v libwebp -quality 72 -compression_level 6 elf.webp`
