package com.hcaglar.ltree4j;


import com.hcaglar.ltree4j.exception.InvalidLTreePathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.postgresql.util.PGobject;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LtreePathConverterTest {
    private static final String VALID_LTREE_DEEP_PATH = "electronics.phone_and_accessories.smartphones";

    private final LTreePathConverter converter = new LTreePathConverter();

    @Test
    @DisplayName("convertToDatabaseColumn should return null when LTreePath is null")
    void convertToDatabaseColumn_shouldReturnNull_whenValueIsNull() {
        Object dbValue = converter.convertToDatabaseColumn(null);

        assertThat(dbValue).isNull();
    }

    @Test
    @DisplayName("convertToDatabaseColumn should create PGobject with type 'ltree' and correct value")
    void convertToDatabaseColumn_shouldCreatePgObject() {
        LTreePath path = LTreePath.of(VALID_LTREE_DEEP_PATH);

        Object dbValue = converter.convertToDatabaseColumn(path);

        assertThat(dbValue).isInstanceOf(PGobject.class);
        PGobject pgObject = (PGobject) dbValue;

        assertThat(pgObject.getType()).isEqualTo("ltree");
        assertThat(pgObject.getValue()).isEqualTo(VALID_LTREE_DEEP_PATH);
    }

    @Test
    @DisplayName("convertToEntityAttribute should return null when dbData is null")
    void convertToEntityAttribute_shouldReturnNull_whenDbDataIsNull() {
        LTreePath path = converter.convertToEntityAttribute(null);

        assertThat(path).isNull();
    }

    @Test
    @DisplayName("convertToEntityAttribute should convert PGobject to LTreePath")
    void convertToEntityAttribute_shouldConvertFromPgObject() throws Exception {
        PGobject pgObject = new PGobject();
        pgObject.setType("ltree");
        pgObject.setValue(VALID_LTREE_DEEP_PATH);

        LTreePath path = converter.convertToEntityAttribute(pgObject);

        assertThat(path).isNotNull();
        assertThat(path.getValue()).isEqualTo(VALID_LTREE_DEEP_PATH);
    }

    @Test
    @DisplayName("convertToEntityAttribute should convert String to LTreePath")
    void convertToEntityAttribute_shouldConvertFromString() {
        LTreePath path = converter.convertToEntityAttribute(VALID_LTREE_DEEP_PATH);

        assertThat(path).isNotNull();
        assertThat(path.getValue()).isEqualTo(VALID_LTREE_DEEP_PATH);
    }

    @Test
    @DisplayName("convertToEntityAttribute should propagate LTreePath validation errors")
    void convertToEntityAttribute_shouldFailForInvalidValue() {
        String invalid = "invalid value with space";

        assertThatThrownBy(() -> converter.convertToEntityAttribute(invalid))
                .isInstanceOf(InvalidLTreePathException.class);
    }
}
