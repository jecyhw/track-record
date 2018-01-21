package cn.cnic.trackrecord.common.serializer;

import cn.cnic.trackrecord.common.date.LongDate;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;

public class LongDateDeserializer extends JsonDeserializer<LongDate> {

    @Override
    public LongDate deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JsonProcessingException {
        ObjectCodec oc = jsonParser.getCodec();
        JsonNode node = oc.readTree(jsonParser);
        return LongDate.from(node.asText());
    }
}