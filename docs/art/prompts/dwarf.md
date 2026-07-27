# Dwarf race carousel prompt

- Method: OpenAI built-in image generation
- Created: 2026-07-27
- Intended asset: `app/src/main/assets/images/races/dwarf.webp`

## Prompt

```text
Create a 3:4 portrait Western fantasy sourcebook illustration representing Dwarves, with two clearly adult dwarf
adventurers, one masculine-presenting and one feminine-presenting, equally prominent. They are seasoned, compact,
broad, stern, and quietly confident rather than cheerful mascots. Show them naturally interacting over a newly forged
object outside a mountain settlement forge. Use hand-painted 2D gouache and watercolor, visible brushwork and paper
texture, lightly inked storybook edges, simplified coherent anatomy, and classic European/American tabletop-RPG
sourcebook styling matching the approved Human asset. Use practical wool, leather aprons, worn mail, engraved tools,
iron grey, soot brown, rust, moss, ochre, and ember. Keep faces and hands in the upper half and the bottom 38 percent
dark, quiet, and low-detail for UI. No text, logo, watermark, UI, sexualization, stereotypes, photorealism, glossy
airbrushing, cinematic or gacha-game splash art, anime, 3D rendering, comedy beards, drunkenness, luxury armor, or
dramatic power poses.
```

## Processing

`ffmpeg -i input.png -c:v libwebp -quality 74 -compression_level 6 dwarf.webp`
