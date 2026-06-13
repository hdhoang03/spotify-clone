package com.spotify.spotify.configuration;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spotify.spotify.dto.response.LyricLine;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.ArrayList;
import java.util.List;

@Converter
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LyricsConverter implements AttributeConverter<List<LyricLine>, String> {
    ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(List<LyricLine> attribute){
        try {
            if (attribute == null) return null;
            return objectMapper.writeValueAsString(attribute);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public List<LyricLine> convertToEntityAttribute(String dbData){
        try{
            if (dbData == null || dbData.isEmpty()) return new ArrayList<>();
            return objectMapper.readValue(dbData, new TypeReference<List<LyricLine>>() {});
        } catch (Exception e){
            return new ArrayList<>();
        }
    }
}
