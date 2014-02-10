
package edu.stanford.pcl.news.model;

import com.google.gson.*;
import org.bson.types.ObjectId;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Handles serialization to and from the database.  Some code borrowed from:
 *   http://craigsmusings.com/2011/04/09/deserializing-mongodb-ids-and-dates-with-gson/
 *   http://vitter.com/JavaSourceSamples/GsonTypeAdapter.txt
 */
public class Serialization {

    // OID
   	private static class ObjectIdSerializer implements JsonSerializer<ObjectId> {
   		public JsonElement serialize(ObjectId src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) return null;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("$oid", src.toStringMongod());
            return jsonObject;
   		}
   	}
    private static class ObjectIdDeserializer implements JsonDeserializer<ObjectId> {
   		public ObjectId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            String oidString = json.getAsJsonObject().get("$oid").getAsString();
            return new ObjectId(oidString);
   		}
   	}

    // Date
    // XXX  Time zone...
    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    private static class DateSerializer implements JsonSerializer<Date> {
        public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
            if (src == null) return null;
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("$date", format.format(src));
            return jsonObject;
        }
    }
    private static class DateDeserializer implements JsonDeserializer<Date> {
        public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            if (json == null) return null;
            try {
                String dateString = json.getAsJsonObject().get("$date").getAsString();
                return format.parse(dateString);
            }
            catch (ParseException e) {
                throw new JsonParseException(e);
            }
        }
    }


    private static Gson gson = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(Date.class, new JsonSerializer<Date>() {
                @Override
                public JsonElement serialize(Date src, Type typeOfSrc, JsonSerializationContext context) {
                    if (src == null) return null;
                    return new JsonPrimitive(format.format(src));
                }
            })
            .create();

    private static Gson gsonMongo = new GsonBuilder()
            .setPrettyPrinting()
            .registerTypeAdapter(ObjectId.class, new ObjectIdDeserializer())
            .registerTypeAdapter(ObjectId.class, new ObjectIdSerializer())
            .registerTypeAdapter(Date.class, new DateDeserializer())
            .registerTypeAdapter(Date.class, new DateSerializer())
            .create();


    public static <T> T toJavaObject(String json, Class<T> classOfT) throws JsonSyntaxException {
        return gsonMongo.fromJson(json, classOfT);
    }

    public static <T> String toJson(T object) throws JsonSyntaxException {
        return gson.toJson(object);
    }

    public static <T> String toMongoJson(T object) throws JsonSyntaxException {
        return gsonMongo.toJson(object);
    }


    private Serialization() {
    }

}
