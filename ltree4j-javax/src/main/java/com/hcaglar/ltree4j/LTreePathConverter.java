package com.hcaglar.ltree4j;


import com.hcaglar.ltree4j.exception.PgObjectConversionException;
import org.postgresql.util.PGobject;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.sql.SQLException;
import java.util.Objects;

import static java.lang.String.format;

@Converter(autoApply = true)
public class LTreePathConverter implements AttributeConverter<LTreePath, Object> {


    @Override
    public Object convertToDatabaseColumn(LTreePath lTreePath) {
        if (Objects.isNull(lTreePath))
            return null;

        try {
            PGobject pgObject = new PGobject();
            pgObject.setType("ltree");
            pgObject.setValue(lTreePath.getValue());
            return pgObject;
        } catch (SQLException sqlException) {
            throw new PgObjectConversionException(format("%s,%s", "Error converting to PGObject", sqlException));
        }
    }

    @Override
    public LTreePath convertToEntityAttribute(Object dbData) {
        if (Objects.isNull(dbData))
            return null;
        String value = (dbData instanceof PGobject) ? ((PGobject) dbData).getValue() : dbData.toString();
        return LTreePath.of(value);
    }
}
