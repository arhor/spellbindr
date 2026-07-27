# Human race carousel proof prompt

## Generation

- Method: OpenAI built-in image generation
- Created: 2026-07-27
- Intended asset: `app/src/main/assets/images/races/human.webp`
- Output role: proof; approval requires review inside the Compose carousel

## Prompt

```text
Use case: stylized-concept
Asset type: Android guided character creation race-carousel background illustration
Primary request: Create a friendly Western fantasy sourcebook illustration representing the Human ancestry, with two
adult human adventurers together: one masculine-presenting and one feminine-presenting. They should feel like warm,
trustworthy traveling companions, relaxed and approachable, with natural imperfect faces and expressive smiles.
Scene/backdrop: a modest old-world village road near green hills and a small stone inn, softly suggested rather than
an epic palace or grand skyline.
Style/medium: clearly hand-painted 2D illustration, traditional gouache and watercolor texture with visible brushwork,
simplified shapes, lightly inked storybook edges, and a classic European and American tabletop-RPG sourcebook
sensibility. Moderately stylized anatomy and faces; charming and grounded. Less realistic, polished, and cinematic
than modern character splash art.
Composition/framing: portrait-oriented 3:4 composition; show both characters from about mid-thigh upward, standing
naturally beside one another rather than in mirrored hero poses. Keep faces and shoulders in the upper half. Reserve
the bottom 38% as dark, quiet, low-detail space for a translucent UI information panel. Keep heads and hands away from
crop edges.
Wardrobe/props: practical mismatched medieval-fantasy travel clothes in wool, linen, and worn leather; modest packs or
walking gear; no matching uniforms or glamour armor. Give the characters distinct silhouettes and ethnic appearances.
Lighting/mood: gentle overcast morning with warm window light; welcoming, adventurous, cozy, and humane.
Color palette: warm ochre, moss green, muted brick red, parchment cream, weathered brown, and subdued slate blue.
Constraints: no text, lettering, logo, watermark, UI, border, sexualization, exaggerated anatomy, perfect fashion-model
faces, modern objects, famous characters, or visual references to East Asian mobile games.
Avoid: photorealism, glossy airbrushed rendering, cinematic golden-hour backlight, ultra-detailed costume filigree,
symmetrical poster composition, dramatic power poses, luxury costumes, enormous castle skyline, gacha-game splash art,
collectible-card shine, 3D-rendered appearance, and anime styling.
```

## Processing

The generated 1086 x 1448 PNG was converted to opaque lossy WebP with:

```shell
ffmpeg -i input.png -c:v libwebp -quality 78 -compression_level 6 human.webp
```

No manual retouching was applied.
