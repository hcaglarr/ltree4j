package com.hcaglar.ltree4j.core;


import com.hcaglar.ltree4j.LTreePath;
import com.hcaglar.ltree4j.exception.InvalidLTreePathException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class LTreePathTest {
    private static final String VALID_LTREE_ROOT_PATH = "electronics";
    private static final String VALID_LTREE_CHILD_PATH = "electronics.phone_and_accessories";
    private static final String VALID_LTREE_DEEP_PATH = "electronics.phone_and_accessories.smartphones";
    private static final String VALID_LTREE_DEEP_VARIATION_PATH = "electronics.phone_and_accessories.powerbanks";

    @Test
    @DisplayName("Create valid LTreePath")
    void createValidLTreePath() {
        LTreePath path = LTreePath.of(VALID_LTREE_DEEP_PATH);
        assertThat(path).isNotNull();
        assertThat(path.getValue()).isEqualTo(VALID_LTREE_DEEP_PATH);
    }

    @ParameterizedTest
    @ValueSource(strings = {"", " ", "electronics.", ".electronics", "electronics..phone_and_accessories", "Invalid-Char", "Space Here"})
    @DisplayName("Should throw exception for invalid format")
    void shouldRejectInvalidFormat(String invalidPath) {
        assertThatThrownBy(() -> LTreePath.of(invalidPath))
        .isInstanceOf(InvalidLTreePathException.class)
                .hasMessageContaining(String.format("Invalid LTreePath: %s", invalidPath));
    }

    @Test
    @DisplayName("Should correctly identify ancestors")
    void shouldIdentifyAncestors() {
        LTreePath parent = LTreePath.of("A.B");
        LTreePath child = LTreePath.of("A.B.C");
        LTreePath nonRelated = LTreePath.of("A.X");
        LTreePath similarPrefix = LTreePath.of("A.Y");

        assertThat(parent.isAncestorOf(child)).isTrue();
        assertThat(parent.isAncestorOf(nonRelated)).isFalse();
        assertThat(parent.isAncestorOf(similarPrefix)).isFalse();
        assertThat(parent.isAncestorOf(parent)).isFalse();
    }

    @Test
    @DisplayName("Should calculate parent path correctly")
    void shouldCalculateParent() {
        LTreePath child = LTreePath.of(VALID_LTREE_CHILD_PATH);
        LTreePath root = LTreePath.of(VALID_LTREE_ROOT_PATH);

        assertThat(child.getParent()).isEqualTo(root);
        assertThat(root.getParent()).isNull();
    }

    @Test
    @DisplayName("Should append child correctly")
    void shouldAppendChild() {
        LTreePath root = LTreePath.of(VALID_LTREE_ROOT_PATH);
        LTreePath newPath = root.append("phone_and_accessories");

        assertThat(newPath.getValue()).isEqualTo(VALID_LTREE_CHILD_PATH);
    }

    @Test
    @DisplayName("Should detect root path correctly")
    void shouldDetectRoot() {
        LTreePath root = LTreePath.of(VALID_LTREE_ROOT_PATH);
        LTreePath nonRoot = LTreePath.of(VALID_LTREE_CHILD_PATH);

        assertThat(root.isRoot()).isTrue();
        assertThat(nonRoot.isRoot()).isFalse();
    }

    @Test
    @DisplayName("hasParent should return true only for non-root paths")
    void shouldDetectHasParentCorrectly() {
        LTreePath root = LTreePath.of(VALID_LTREE_ROOT_PATH);
        LTreePath child = LTreePath.of(VALID_LTREE_CHILD_PATH);
        LTreePath deep = LTreePath.of(VALID_LTREE_DEEP_VARIATION_PATH);

        assertThat(root.hasParent())
                .as("Single segment paths should not have parent")
                .isFalse();

        assertThat(child.hasParent())
                .as("Two-segment paths should have parent")
                .isTrue();

        assertThat(deep.hasParent())
                .as("Multi-segment paths should have parent")
                .isTrue();
    }

    @Test
    @DisplayName("Should correctly identify descendants (isDescendantOf)")
    void shouldIdentifyDescendants() {
        LTreePath grandParent = LTreePath.of(VALID_LTREE_ROOT_PATH);
        LTreePath parent = LTreePath.of(VALID_LTREE_CHILD_PATH);
        LTreePath child = LTreePath.of(VALID_LTREE_DEEP_PATH);
        LTreePath unrelated = LTreePath.of(VALID_LTREE_DEEP_VARIATION_PATH);

        assertThat(parent.isDescendantOf(grandParent))
                .as("Parent should be descendant of GrandParent")
                .isTrue();

        assertThat(child.isDescendantOf(grandParent))
                .as("GrandChild should be descendant of GrandParent")
                .isTrue();

        assertThat(grandParent.isDescendantOf(parent))
                .as("GrandParent is NOT descendant of Parent")
                .isFalse();

        assertThat(parent.isDescendantOf(parent))
                .as("Object should not be descendant of itself")
                .isFalse();

        assertThat(parent.isDescendantOf(unrelated))
                .as("Unrelated paths should return false")
                .isFalse();

        assertThat(child.isDescendantOf(null))
                .as("Should return false when checking against null")
                .isFalse();

        LTreePath trapPath = LTreePath.of(VALID_LTREE_CHILD_PATH);
        assertThat(parent.isDescendantOf(trapPath))
                .as("Should not match partial prefix without separator")
                .isFalse();
    }
}
