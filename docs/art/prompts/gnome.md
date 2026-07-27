# Gnome race carousel prompt

- Method: OpenAI built-in image generation
- Created: 2026-07-27
- Intended asset: `app/src/main/assets/images/races/gnome.webp`

## Prompt

```text
Create a 3:4 portrait Western fantasy sourcebook illustration representing Gnomes, with two clearly adult gnome
adventurers, one masculine-presenting and one feminine-presenting, equally prominent. They are intensely curious,
eccentric, and absorbed in a practical mechanical puzzle; one is skeptical and the other looks ready to test it.
Place them in a believable rock-gnome hillside workshop with hand tools, brass scraps, roots, and mushrooms. Use
hand-painted 2D gouache and watercolor, visible brushwork, paper texture, lightly inked storybook edges, simplified
coherent anatomy, and classic European/American tabletop-RPG sourcebook styling matching the approved Human asset.
Use patched work clothes in lichen green, tarnished brass, brick red, smoke blue, parchment, and walnut. Keep adult
faces and the palm-sized device in the upper half and bottom 38 percent dark and low-detail for UI. No text, logo,
watermark, UI, photorealism, glossy airbrushing, cinematic or gacha-game poster styling, anime, 3D rendering,
children, garden-gnome hats, clownish proportions, mascot smiles, steampunk-goggles clichés, or explosions.
```

## Processing

`ffmpeg -i input.png -c:v libwebp -quality 74 -compression_level 6 gnome.webp`
