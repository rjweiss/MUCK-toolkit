package edu.stanford.pcl.news.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;


public class ServiceException extends WebApplicationException {
    private static final long serialVersionUID = 7336128054755755785L;

    private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static String createEntity(String message, Response.Status status) {
        JsonObject entity = new JsonObject();
        entity.addProperty("code", status.getStatusCode());
        entity.addProperty("message", message);
        return gson.toJson(entity);
    }


    public ServiceException(String message, Response.Status status) {
        super(Response.status(Response.Status.BAD_REQUEST)
                .entity(createEntity(message, status))
                .status(status)
                .type("application/json")
                .build());
    }
}
