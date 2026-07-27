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
Primary request: Create a premium fantasy character-selection illustration for the Human race, showing two diverse
adult human adventurer representatives together, one masculine-presenting and one feminine-presenting, confident and
approachable rather than aggressive.
Scene/backdrop: subtle high-fantasy city and distant landscape, atmospheric and uncluttered, with enough depth to
separate the characters.
Style/medium: polished painterly digital fantasy illustration, grounded realistic anatomy, elegant tabletop-RPG
sourcebook feeling, cohesive and sophisticated rather than cartoonish or photorealistic.
Composition/framing: portrait-oriented 3:4 composition; both characters visible from roughly knees or mid-thigh
upward; faces and upper bodies concentrated in the upper 55%; reserve the bottom 38% as low-detail darker visual space
because a translucent information card will overlay it; keep important anatomy away from edges for center-crop safety.
Lighting/mood: soft cinematic dawn light, hopeful adventurous mood, readable silhouettes.
Color palette: deep indigo, muted teal, warm amber highlights, restrained earth tones compatible with a dark Material
UI.
Constraints: no text, no lettering, no logos, no watermark, no UI, no frame, no sexualized clothing, no exaggerated
anatomy, no modern objects, no famous characters, no bright detail in the bottom overlay-safe zone. Distinct practical
fantasy outfits and equipment; inclusive appearance and body types; accurate hands and faces.
```

## Processing

The generated 1086 x 1448 PNG was converted to opaque lossy WebP with:

```shell
ffmpeg -i input.png -c:v libwebp -quality 84 -compression_level 6 human.webp
```

No manual retouching was applied.
