package com.example.keepingorganizer;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import java.lang.reflect.Type;

public class LocalDateTimeSerializer implements JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime>
{
    private static final DateTimeFormatter DATE_TIME_FORMAT = ISODateTimeFormat.dateHourMinuteSecondFraction();

    @Override
    public LocalDateTime deserialize(final JsonElement je, final Type type,
                                     final JsonDeserializationContext jdc) throws JsonParseException {
        final String dateTimeAsString = je.getAsString();
        if (dateTimeAsString.length() == 0)
            return null;

        return DATE_TIME_FORMAT.parseLocalDateTime(dateTimeAsString);
    }

    @Override
    public JsonElement serialize(final LocalDateTime src, final Type typeOfSrc,
                                 final JsonSerializationContext context) {
        String retVal;
        if (src == null)
            retVal = "";
        else
            retVal = DATE_TIME_FORMAT.print(src);

        return new JsonPrimitive(retVal);
    }

}