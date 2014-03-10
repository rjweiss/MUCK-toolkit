package edu.stanford.pcl.news.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;


@Provider
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class GsonEntityProvider implements MessageBodyReader<JsonObject>, MessageBodyWriter<JsonObject> {

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();


    @Override
    public boolean isReadable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return type == JsonObject.class;
    }

    @Override
    public JsonObject readFrom(Class<JsonObject> jsonObjectClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> stringStringMultivaluedMap, InputStream inputStream) throws IOException, WebApplicationException {
        try {
            Reader reader = new InputStreamReader(inputStream);
            JsonObject jo = gson.fromJson(reader, JsonObject.class);
            reader.close();
            return jo;
        }
        catch (JsonSyntaxException e) {
            throw new ServiceException(String.format("JSON syntax error: %s", e.getCause().getMessage()), Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return type == JsonObject.class;
    }

    @Override
    public long getSize(JsonObject jsonObject, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        // Deprecated by JAX-RS 2.0 and ignored by Jersey.
        return 0;
    }

    @Override
    public void writeTo(JsonObject jsonObject, Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> stringObjectMultivaluedMap, OutputStream outputStream) throws IOException, WebApplicationException {
        Writer writer = new OutputStreamWriter(outputStream);
        gson.toJson(jsonObject, writer);
        writer.close();
    }
}
