import com.google.gson.*;
import core.Line;

import java.lang.reflect.Type;


public class LineSerializer implements JsonSerializer<Line> {

    @Override
    public JsonElement serialize(Line line, Type type, JsonSerializationContext context) {
        JsonObject serializeLine = new JsonObject();

        serializeLine.addProperty("number", line.getNumber());
        serializeLine.addProperty("name", line.getName());
        serializeLine.addProperty("color", line.getColor());
        return serializeLine;
    }

}
