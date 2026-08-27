package io.github.arhor.dnd.companion.catalog;

import io.github.arhor.dnd.companion.api.v1.Ability;
import io.github.arhor.dnd.companion.api.v1.ResourceKind;
import io.github.arhor.dnd.companion.api.v1.ResourceMetadata;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CatalogGeneratorTest {
    private static final ResourceMetadata METADATA = ResourceMetadata.newBuilder()
            .setRulesetId(CatalogGenerator.RULESET_ID).addSourceIds(CatalogGenerator.SOURCE_ID).build();

    @Test
    void generateShouldProduceIdenticalSnapshotWhenInputsAreUnchanged() throws Exception {
        // Given
        Path input = Path.of("../dnd-companion-client-android/app/src/main/assets/data").toRealPath();

        // When
        byte[] first = CatalogGenerator.generate(input).toByteArray();
        byte[] second = CatalogGenerator.generate(input).toByteArray();

        // Then
        assertArrayEquals(first, second);
    }

    @Test
    void validateShouldRejectDuplicateIdsWhenCollectionContainsRepeatedIdentity() {
        // Given
        Ability first = Ability.newBuilder().setId("str").setMetadata(METADATA).build();
        Ability duplicate = Ability.newBuilder().setId("str").setMetadata(METADATA).build();

        // When
        var result = assertThrows(IllegalArgumentException.class, () -> CatalogGenerator.validate(
                List.of(first, duplicate), Ability::getId, ResourceKind.RESOURCE_KIND_ABILITY));

        // Then
        org.junit.jupiter.api.Assertions.assertTrue(result.getMessage().contains("duplicate id str"));
    }

    @Test
    void validateShouldRejectMissingProvenanceWhenResourceHasNoMetadata() {
        // Given
        Ability ability = Ability.newBuilder().setId("str").build();

        // When
        var result = assertThrows(IllegalArgumentException.class, () -> CatalogGenerator.validate(
                List.of(ability), Ability::getId, ResourceKind.RESOURCE_KIND_ABILITY));

        // Then
        org.junit.jupiter.api.Assertions.assertTrue(result.getMessage().contains("lacks provenance"));
    }
}
