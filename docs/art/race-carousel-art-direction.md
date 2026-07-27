# Race carousel art direction

## Purpose and scope

This is the production brief for the decorative illustrations used by the guided character-creation race carousel.
It applies to the proof asset first, then to the nine bundled race IDs:

`dwarf`, `elf`, `halfling`, `human`, `dragonborn`, `gnome`, `half-elf`, `half-orc`, and `tiefling`.

Artwork supports recognition and atmosphere; it must never be required to choose a race. The carousel always supplies
the race name, selected state, mechanics summary, subrace control, page position, and details action as text. A missing,
unreadable, or unsupported asset must render the deterministic in-app gradient placeholder without crashing setup.

## Delivery frame and crop contract

The baseline verification viewport is the repository's screenshot-test phone size: **360 x 640 dp**. The carousel page
should expose neighbouring pages, so its expected content width is 80--88% of the viewport (288--317 dp before internal
padding). The illustration fills the portrait carousel card behind its summary panel.

Every source illustration should use a **3:4 portrait aspect ratio**, encoded as opaque lossy WebP. The Human proof is
1086 x 1448 px; use that size for the remaining set unless the proof review establishes a different common export size.
The app should display it with `ContentScale.Crop`. Test the proof and each final asset in the 360 x 640 dp preview and
one wider representative preview before accepting it.

Keep the following zones relative to a normalized 1086 x 1448 canvas:

| Canvas zone | Coordinates | Requirement |
| --- | --- | --- |
| Primary focal zone | x=110--976, y=80--720 | Faces, anatomy, and the relationship between the pair. |
| Secondary-safe zone | x=70--1016, y=60--900 | Clothing, silhouettes, and nonessential props. |
| Overlay exclusion zone | x=0--1086, y=900--1448 | Only low-detail atmosphere, ground, soft shadow, or empty space. |
| Edge-crop buffer | first/last 70 px and top/bottom 55 px | No important content; it may be clipped. |

Compose both figures as a readable pair, not as one foreground hero with an incidental background extra. Keep at least
one face and each figure's race-defining silhouette inside the primary focal zone. Favour waist-up to three-quarter
views and a medium camera distance; do not use distant landscape compositions or extreme close-ups. The pair may share
an everyday or adventuring activity, but action must be calm enough that the card remains legible at phone size.

## Spellbindr visual language

Create friendly hand-painted fantasy illustrations with visible gouache/watercolor texture, lightly drawn edges,
simplified shapes, and clean silhouettes. Use a classic Western tabletop-RPG sourcebook and European storybook visual
language. The style should feel welcoming, warm, and handcrafted rather than photorealistic, glossy, cinematic,
airbrushed, anime-influenced, or like mobile-game character splash art.

- Use a dark indigo/charcoal, parchment, muted violet, antique-gold, and ember palette that complements the app theme.
  Useful anchors are dark background `#0F1015`, dark surface `#151621`, gold `#C6A866`, violet `#8C7BB6`, and ember
  `#F39C5A`; these are references, not mandatory flat fills.
- Prefer a controlled value range: a readable warm or cool key light on faces and upper silhouettes, with darker,
  lower-contrast surroundings. Preserve a calm low-detail lower third for the overlay.
- Use one dominant ambience per race (for example, warm forge glow for Dwarf or moonlit woodland for Elf). Retain the
  shared palette and lighting contrast so pages belong to one set.
- Keep backgrounds suggestive and shallow: architecture, forest, workshop, shoreline, or abstract magical atmosphere is
  acceptable; busy crowds, detailed maps, UI-like frames, and visual noise are not.
- Do not bake card borders, gradients, badges, race labels, subtitles, rules text, logos, watermarks, signatures, or
  decorative lettering into the image. The app owns all text and overlays.

The asset must work over both light and dark app themes because the artwork itself is not recoloured. The in-app summary
panel must provide text contrast; the image should not be relied upon as the only contrast treatment.

## Representation and content standards

Each asset shows **two distinct, equally purposeful representatives** of that race. Across the set, vary apparent gender
expression, age presentation (all clearly adult), skin tones where applicable, body type, hairstyle, clothing, ability,
and role. Do not make gender expression, desirability, violence, or occupation a defining racial trait.

- Present practical, non-sexualized clothing and stable, plausible anatomy. Avoid pin-up poses, exposed armour used as
  costume, infantilized adults, and stereotypes based on race, ethnicity, disability, or gender.
- Depict fantasy traits with dignity and specificity. Horns, ears, stature, tusks, scales, and other features must be
  anatomically coherent and visible without caricature. Do not use real-world cultural markers as shorthand for a
  fantasy ancestry.
- Include mobility aids, scars, or other visible access features only when portrayed as ordinary parts of an active,
  capable person; never as a visual gag or a reason one representative is passive.
- Avoid gore, horror, bondage, intoxication, religious/political symbols, hate symbols, copyrighted characters,
  recognizable brands, real-person likenesses, and generated text.
- Both figures must remain visually distinct at small size. Avoid making one an indistinct silhouette, servant, victim,
  trophy, or background decoration.

Artwork is decorative: expose it to accessibility services with no content description / invisible semantics. The
corresponding page semantics must announce the textual race name, page position, selection state, and summary instead.

## Asset layout, IDs, and fallback

Store approved files at:

```text
app/src/main/assets/images/races/<race-id>.webp
```

Use the race data ID exactly, lowercase and hyphenated as supplied by `races.json`; do not use display names, subrace
IDs, spaces, version suffixes, or localized filenames. Examples: `human.webp`, `half-elf.webp`, `half-orc.webp`.

There is one base-race image, not a separate image per subrace. The visible subrace selector and summary communicate the
subrace. A lookup failure, decode failure, or missing file must choose the same code-drawn gradient/shape placeholder
for that page. It must not display an error, silently substitute another race's image, or block Next after textual race
and subrace selection. Keep the placeholder covered by screenshot tests.

## Prompt package

Archive the exact generation input for every delivered file; do not rely on a remembered prompt or UI history. Start
from this shared template, replacing bracketed fields while retaining the composition and exclusion constraints:

```text
3:4 portrait editorial fantasy illustration for Spellbindr's mobile race-selection carousel.
Depict two diverse, clearly adult [RACE] representatives as equally capable [ACTIVITY/ROLE] partners in [SETTING].
Medium camera distance, waist-up to three-quarter view; both faces and race-defining silhouettes in the upper-middle
of the frame. Leave the lower 38 percent as quiet, low-detail [GROUND/ATMOSPHERE] for a UI summary overlay.
Hand-painted 2D gouache and watercolor rendering, visible brush texture, lightly inked storybook edges, simplified
shapes, and clean silhouettes; palette of warm ochre, moss, muted brick, parchment, weathered brown, and slate blue.
Welcoming, dignified, non-sexualized practical clothing, moderately stylized anatomy, shallow atmospheric background,
no text or logos.
```

Attach a negative prompt or equivalent generation constraints when the tool supports it:

```text
text, letters, logo, watermark, signature, UI frame, cropped face, focal detail in lower third, extra limbs/fingers,
deformed anatomy, sexualized clothing or pose, child, gore, horror, caricature, stereotype, real-person likeness,
copyrighted character, brand mark, busy crowd, photorealism, extreme close-up,
glossy airbrushing, cinematic backlight, gacha-game splash art, collectible-card shine, 3D render, anime styling
```

Use the approved proof's model/version, style reference, seed policy, aspect ratio, and processing settings for the set.
If a necessary change is made, record why in provenance and render a new proof crop before batch approval.

## Provenance and prompt archive

Commit an auditable manifest at `docs/art/race-carousel-art-manifest.json` when the first binary is added. It is a JSON
object with a `schemaVersion`, shared pipeline metadata, and one entry per asset. Keep generation prompts in
`docs/art/prompts/<race-id>.md`; the file includes the final positive prompt, negative prompt/constraints, generation
tool settings, and any human retouch notes. Do not embed API keys, account identifiers, or private URLs.

Required per-asset manifest shape:

```json
{
  "raceId": "human",
  "file": "app/src/main/assets/images/races/human.webp",
  "status": "proof",
  "source": {
    "method": "generated|commissioned|licensed",
    "toolOrArtist": "provider and model/version, or artist name",
    "createdAt": "YYYY-MM-DD",
    "license": "license name and URL or internal rights record",
    "sourceMaterial": []
  },
  "promptArchive": "docs/art/prompts/human.md",
  "generation": { "seed": "recorded value or null", "aspectRatio": "3:4" },
  "output": {
    "widthPx": 1086,
    "heightPx": 1448,
    "format": "webp",
    "lossless": false,
    "quality": 78,
    "compressionLevel": 6,
    "bytes": 0,
    "sha256": "lowercase hex digest"
  },
  "review": { "reviewedAt": "YYYY-MM-DD", "reviewer": "name or team", "notes": "" }
}
```

`sourceMaterial` lists every supplied reference or is an empty array. If any source has terms that do not allow the
required app distribution, do not use it. A generated image still needs its generator/provider, model version, prompt
archive, and distribution-rights record. Update `status` from `proof` to `approved` only after in-app review.

## WebP conversion and size budget

Retain the original/working source outside application assets only if its licence and project storage policy allow it.
The committed runtime file is the common 3:4 opaque WebP. Strip EXIF and other nonessential metadata before committing.

Start with the shared deterministic lossy WebP settings for every production file:

```shell
ffmpeg -i input.png -c:v libwebp -quality 78 -compression_level 6 <race-id>.webp
```

If an asset exceeds the hard **160 KiB per asset** budget, reduce quality only as far as required and record the
per-file value in both its prompt archive and manifest. Keep dimensions, compression method, and colour treatment
consistent. The complete nine-file set must remain below **1.44 MiB**. Record actual byte count and SHA-256 in the
manifest. If an asset cannot meet the limit without visible banding, blurred faces, or poor crop readability, simplify
the composition and regenerate rather than silently shipping a larger file.

## Inspection and approval checklist

Before accepting the proof, and for every final asset, verify all of the following in the actual carousel rather than
only in an image viewer:

- [ ] File path and race ID match exactly; manifest entry, prompt archive, provenance, byte count, and SHA-256 are
      complete.
- [ ] Image is opaque 3:4 WebP, exported at target quality 78 (or the documented lower value), compression level 6,
      and below 160 KiB.
- [ ] At 360 x 640 dp, both representatives, faces, and defining silhouettes remain clear above the summary overlay.
- [ ] In the wider preview, centred crop, rounded corners, and pager-neighbour clipping do not remove a focal feature.
- [ ] The lower 38% contains no essential face, hand, weapon, lettering, or mechanically meaningful visual cue.
- [ ] Light and dark screenshot previews retain readable summary-panel text and a coherent visual balance.
- [ ] Pair composition is balanced, adult, dignified, non-sexualized, and free from stereotypes, visible anatomy errors,
      accidental text/logos, copyrighted-character resemblance, or unsafe content.
- [ ] The image remains decorative in semantics; text and controls communicate every decision without it.
- [ ] Temporarily removing or corrupting the asset displays the deterministic placeholder and leaves race selection and
      Next usable.
- [ ] The approved Human proof establishes the visual language used by the remaining eight files.

Run the Race Carousel screenshot suite after each material artwork or layout change, export the images for visual
review, and retain the review artifact with the implementation change.
