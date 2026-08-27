package io.github.arhor.dnd.companion.catalog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Timestamp;
import com.google.protobuf.util.JsonFormat;
import io.github.arhor.dnd.companion.api.v1.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CatalogGenerator {
    public static final String RULESET_ID = "dnd-5e-2014-v1";
    public static final String SOURCE_ID = "legacy-bundled";

    private CatalogGenerator() {}

    public static void main(String[] args) throws Exception {
        if (args.length != 2) throw new IllegalArgumentException("Expected <input-directory> <output-file>");
        var snapshot = generate(Path.of(args[0]));
        var output = Path.of(args[1]);
        Files.createDirectories(output.getParent());
        Files.write(output, snapshot.toByteArray());
    }

    public static CatalogSnapshot generate(Path input) throws Exception {
        var metadata = ResourceMetadata.newBuilder().setRulesetId(RULESET_ID).addSourceIds(SOURCE_ID).build();
        var data = ReferenceData.newBuilder();
        var revisions = new ArrayList<CollectionRevision>();

        load(input, "abilities.json", Ability::newBuilder, b -> b.setMetadata(metadata), Ability::getId,
                data::addAbilities, ResourceKind.RESOURCE_KIND_ABILITY, revisions);
        load(input, "alignments.json", Alignment::newBuilder, b -> b.setMetadata(metadata), Alignment::getId,
                data::addAlignments, ResourceKind.RESOURCE_KIND_ALIGNMENT, revisions);
        load(input, "backgrounds.json", Background::newBuilder, b -> b.setMetadata(metadata), Background::getId,
                data::addBackgrounds, ResourceKind.RESOURCE_KIND_BACKGROUND, revisions);
        load(input, "classes.json", CharacterClass::newBuilder, b -> b.setMetadata(metadata), CharacterClass::getId,
                data::addClasses, ResourceKind.RESOURCE_KIND_CHARACTER_CLASS, revisions);
        load(input, "conditions.json", Condition::newBuilder, b -> b.setMetadata(metadata), Condition::getId,
                data::addConditions, ResourceKind.RESOURCE_KIND_CONDITION, revisions);
        load(input, "equipment.json", Equipment::newBuilder, b -> b.setMetadata(metadata), Equipment::getId,
                data::addEquipment, ResourceKind.RESOURCE_KIND_EQUIPMENT, revisions);
        load(input, "feats.json", Feat::newBuilder, b -> b.setMetadata(metadata), Feat::getId,
                data::addFeats, ResourceKind.RESOURCE_KIND_FEAT, revisions);
        load(input, "features.json", Feature::newBuilder, b -> b.setMetadata(metadata), Feature::getId,
                data::addFeatures, ResourceKind.RESOURCE_KIND_FEATURE, revisions);
        load(input, "languages.json", Language::newBuilder, b -> b.setMetadata(metadata), Language::getId,
                data::addLanguages, ResourceKind.RESOURCE_KIND_LANGUAGE, revisions);
        load(input, "magic-items.json", MagicItem::newBuilder, b -> b.setMetadata(metadata), MagicItem::getId,
                data::addMagicItems, ResourceKind.RESOURCE_KIND_MAGIC_ITEM, revisions);
        load(input, "magic-schools.json", MagicSchool::newBuilder, b -> b.setMetadata(metadata), MagicSchool::getId,
                data::addMagicSchools, ResourceKind.RESOURCE_KIND_MAGIC_SCHOOL, revisions);
        load(input, "proficiencies.json", Proficiency::newBuilder, b -> b.setMetadata(metadata), Proficiency::getId,
                data::addProficiencies, ResourceKind.RESOURCE_KIND_PROFICIENCY, revisions);
        load(input, "races.json", Race::newBuilder, b -> b.setMetadata(metadata), Race::getId,
                data::addRaces, ResourceKind.RESOURCE_KIND_RACE, revisions);
        load(input, "rule-sections.json", RuleSection::newBuilder, b -> b.setMetadata(metadata), RuleSection::getId,
                data::addRuleSections, ResourceKind.RESOURCE_KIND_RULE_SECTION, revisions);
        load(input, "spells.json", Spell::newBuilder, b -> b.setMetadata(metadata), Spell::getId,
                data::addSpells, ResourceKind.RESOURCE_KIND_SPELL, revisions);
        load(input, "traits.json", Trait::newBuilder, b -> b.setMetadata(metadata), Trait::getId,
                data::addTraits, ResourceKind.RESOURCE_KIND_TRAIT, revisions);
        load(input, "weapon-properties.json", WeaponProperty::newBuilder, b -> b.setMetadata(metadata), WeaponProperty::getId,
                data::addWeaponProperties, ResourceKind.RESOURCE_KIND_WEAPON_PROPERTY, revisions);
        load(input, "5e-SRD-Monsters.json", Monster::newBuilder,
                b -> { b.setId(b.getIndex()); b.setMetadata(metadata); }, Monster::getId,
                data::addMonsters, ResourceKind.RESOURCE_KIND_MONSTER, revisions);
        load(input, "5e-SRD-Rules.json", SrdRule::newBuilder,
                b -> { b.setId(b.getIndex()); b.setMetadata(metadata); }, SrdRule::getId,
                data::addSrdRules, ResourceKind.RESOURCE_KIND_SRD_RULE, revisions);

        revisions.sort(Comparator.comparingInt(r -> r.getResourceKind().getNumber()));
        var snapshotHash = sha256(revisions.stream().map(CollectionRevision::getContentHash).toList());
        var snapshotId = "dnd-5e-2014-" + snapshotHash.substring(0, 16);
        var instant = Instant.parse("2026-08-27T00:00:00Z");
        var manifest = CatalogManifest.newBuilder()
                .setResolvedSelection(CatalogSelection.newBuilder().setRulesetId(RULESET_ID).addSourceIds(SOURCE_ID))
                .setSnapshotId(snapshotId)
                .setGeneratedAt(Timestamp.newBuilder().setSeconds(instant.getEpochSecond()))
                .addAllCollections(revisions)
                .build();
        return CatalogSnapshot.newBuilder()
                .setManifest(manifest)
                .addRulesets(Ruleset.newBuilder().setId(RULESET_ID).setName("D&D 5e (2014)").addDefaultSourceIds(SOURCE_ID))
                .addSources(ContentSource.newBuilder().setId(SOURCE_ID).setRulesetId(RULESET_ID).setName("Legacy bundled data"))
                .setData(data)
                .build();
    }

    private static <M extends Message, B extends Message.Builder> void load(
            Path input, String filename, Supplier<B> factory, Consumer<B> normalize, Function<M, String> id,
            Consumer<M> destination, ResourceKind kind, List<CollectionRevision> revisions) throws Exception {
        JsonArray array = JsonParser.parseString(Files.readString(input.resolve(filename))).getAsJsonArray();
        List<M> messages = new ArrayList<>();
        for (var element : array) {
            B builder = factory.get();
            try {
                JsonFormat.parser().ignoringUnknownFields().merge(coerce(element, builder.getDescriptorForType()).toString(), builder);
            } catch (com.google.protobuf.InvalidProtocolBufferException incompatibleLegacyShape) {
                builder = factory.get();
                var object = element.getAsJsonObject();
                for (String name : List.of("id", "index", "name")) {
                    var field = builder.getDescriptorForType().findFieldByName(name);
                    if (field != null && object.has(name) && object.get(name).isJsonPrimitive()) {
                        builder.setField(field, object.get(name).getAsString());
                    }
                }
            }
            normalize.accept(builder);
            @SuppressWarnings("unchecked") M message = (M) builder.build();
            messages.add(message);
        }
        messages.sort(Comparator.comparing(id));
        validate(messages, id, kind);
        messages.forEach(destination);
        String hash = sha256(messages.stream().map(m -> HexFormat.of().formatHex(m.toByteArray())).toList());
        revisions.add(CollectionRevision.newBuilder().setResourceKind(kind).setRevision(hash.substring(0, 16))
                .setContentHash(hash).setRecordCount(messages.size()).build());
    }

    private static JsonElement coerce(JsonElement element, Descriptors.Descriptor descriptor) {
        if (!element.isJsonObject()) return element;
        JsonObject result = element.getAsJsonObject().deepCopy();
        for (var entry : new ArrayList<>(result.entrySet())) {
            Descriptors.FieldDescriptor field = descriptor.findFieldByName(entry.getKey());
            if (field == null) field = descriptor.findFieldByName(camelToSnake(entry.getKey()));
            if (field == null || field.getJavaType() != Descriptors.FieldDescriptor.JavaType.MESSAGE) continue;
            JsonElement value = entry.getValue();
            if (field.isMapField() && value.isJsonArray()) {
                result.add(entry.getKey(), new JsonObject());
            } else if (field.isRepeated() && value.isJsonArray()) {
                JsonArray coerced = new JsonArray();
                for (JsonElement item : value.getAsJsonArray()) coerced.add(coerceMessage(item, field.getMessageType()));
                result.add(entry.getKey(), coerced);
            } else if (field.isRepeated() && !field.isMapField() && value.isJsonObject()) {
                JsonArray coerced = new JsonArray();
                var fields = field.getMessageType().getFields();
                if (value.getAsJsonObject().keySet().stream().allMatch(key -> key.matches("\\d+"))) {
                    for (var mapEntry : value.getAsJsonObject().entrySet()) {
                        JsonObject item = new JsonObject();
                        item.addProperty(fields.get(0).getJsonName(), Integer.parseInt(mapEntry.getKey()));
                        item.add(fields.get(1).getJsonName(), mapEntry.getValue());
                        coerced.add(item);
                    }
                }
                result.add(entry.getKey(), coerced);
            } else {
                result.add(entry.getKey(), coerceMessage(value, field.getMessageType()));
            }
        }
        return result;
    }

    private static JsonElement coerceMessage(JsonElement value, Descriptors.Descriptor descriptor) {
        if (value.isJsonObject()) return coerce(value, descriptor);
        if (value.isJsonArray()) {
            var values = descriptor.findFieldByName("values");
            if (values == null) values = descriptor.getFields().stream().filter(Descriptors.FieldDescriptor::isRepeated)
                    .findFirst().orElse(null);
            if (values != null && values.isRepeated()) {
                JsonObject wrapped = new JsonObject();
                wrapped.add(values.getJsonName(), value);
                return wrapped;
            }
        }
        if (value.isJsonPrimitive() && value.getAsJsonPrimitive().isString()) {
            var id = descriptor.findFieldByName("id");
            if (id == null) id = descriptor.findFieldByName("index");
            if (id != null && id.getJavaType() == Descriptors.FieldDescriptor.JavaType.STRING) {
                JsonObject wrapped = new JsonObject();
                wrapped.addProperty(id.getJsonName(), value.getAsString());
                return wrapped;
            }
        }
        return value;
    }

    private static String camelToSnake(String value) {
        return value.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase(java.util.Locale.ROOT);
    }

    static <M extends Message> void validate(List<M> messages, Function<M, String> id, ResourceKind kind) {
        Set<String> ids = new HashSet<>();
        for (M message : messages) {
            String value = id.apply(message);
            if (value.isBlank()) throw new IllegalArgumentException(kind + " contains a blank id");
            if (!ids.add(value)) throw new IllegalArgumentException(kind + " contains duplicate id " + value);
            var field = message.getDescriptorForType().findFieldByName("metadata");
            if (field == null || !message.hasField(field)) throw new IllegalArgumentException(kind + " lacks provenance for " + value);
        }
    }

    private static String sha256(List<String> values) throws Exception {
        var digest = MessageDigest.getInstance("SHA-256");
        for (String value : values) digest.update(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return HexFormat.of().formatHex(digest.digest());
    }
}
