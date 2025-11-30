package com.hcaglar.ltree4j;

import com.hcaglar.ltree4j.exception.InvalidLTreePathException;

import java.io.Serializable;
import java.util.Objects;
import java.util.regex.Pattern;

import static java.util.Objects.*;

public class LTreePath implements Serializable, Comparable<LTreePath> {

    private static final long serialVersionUID;
    private static final Pattern VALIDATOR;
    private static final String SEPARATOR;
    private static final String NODE_NAME_PATTERN;

    private final String value;

    static {
        serialVersionUID = 2144970417846384101L;
        SEPARATOR = ".";
        NODE_NAME_PATTERN = "^[A-Za-z0-9_]+$";
        VALIDATOR = Pattern.compile("^[A-Za-z0-9_]+(\\.[A-Za-z0-9_]+)*");
    }


    private LTreePath(String value) {
        if (isNull(value) || value.isEmpty())
            throw new InvalidLTreePathException(defaultExceptionMessage(value));

        if (!VALIDATOR.matcher(value).matches())
            throw new InvalidLTreePathException(defaultExceptionMessage(value));

        this.value = value;
    }

    public static LTreePath of(String value) {
        return new LTreePath(value);
    }

    public boolean isAncestorOf(LTreePath other) {
        if (isNull(other) || value.length() >= other.value.length())
            return false;
        return other.value.startsWith(value) && other.value.charAt(value.length()) == SEPARATOR.charAt(0);
    }

    public boolean isDescendantOf(LTreePath other) {
        if (isNull(other) || other.value.length() >= value.length())
            return false;
        return other.isAncestorOf(this);
    }

    public LTreePath append(String childNode) {
        if (isNull(childNode) || childNode.contains(SEPARATOR) || !childNode.matches(NODE_NAME_PATTERN))
            throw new InvalidLTreePathException(defaultExceptionMessage(childNode));
        return new LTreePath(String.format("%s%s%s", value, SEPARATOR, childNode));
    }

    public LTreePath getParent() {
        int lastDot = getValue().lastIndexOf(SEPARATOR);
        if (lastDot == -1)
            return null;
        return new LTreePath(getValue().substring(0, lastDot));
    }

    public String getValue() {
        return value;
    }

    public boolean isRoot() {
        return !value.contains(SEPARATOR);
    }

    public boolean hasParent() {
        return value.contains(SEPARATOR);
    }

    @Override
    public int compareTo(LTreePath o) {
        return getValue().compareTo(o.getValue());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass())
            return false;
        LTreePath lTreePath = (LTreePath) o;
        return Objects.equals(value, lTreePath.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    private static String defaultExceptionMessage(String content) {
        return String.format("Invalid LTreePath: %s", content);
    }
}
