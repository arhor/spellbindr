# Tiefling race carousel prompt

- Method: OpenAI built-in image generation
- Created: 2026-07-27
- Intended asset: `app/src/main/assets/images/races/tiefling.webp`

## Prompt

```text
Create a 3:4 portrait Western fantasy sourcebook illustration representing Tieflings, with two clearly adult tiefling
adventurers of different gender presentation and builds, equally prominent. They are sly, theatrical, defiant, and
slightly dangerous: one watches with a private half-smile while the other quietly negotiates something dubious out of
frame. They may seem morally ambiguous, not mindlessly evil. Place them in a rain-dark old-city night market. Use
coherent horns, muted crimson and dusky violet-grey skin, practical worn burgundy and violet travel layers, cards and
a small ledger. Render as hand-painted 2D gouache and watercolor with visible brushwork, paper texture, lightly inked
storybook edges, simplified anatomy, and classic European/American tabletop-RPG sourcebook styling matching the
approved Human asset. Keep faces in the upper half and bottom 38 percent dark and low-detail for UI. No text, logo,
watermark, UI, sexualization, photorealism, glossy airbrushing, cinematic or gacha-game poster styling, anime, 3D
rendering, pin-up poses, latex, devil-worship clichés, flames everywhere, monstrous caricature, broad smiles, enormous
horns, or superhero poses.
```

## Processing

`ffmpeg -i input.png -c:v libwebp -quality 78 -compression_level 6 tiefling.webp`
