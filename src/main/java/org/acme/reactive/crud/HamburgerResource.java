package org.acme.reactive.crud;

import java.net.URI;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;

@Path("hamburgers")
public class HamburgerResource {

    private final Pool client;

    public HamburgerResource(Pool client) {
        this.client = client;
    }

    @GET
    public Multi<Hamburger> get() {
        return Hamburger.findAll(client);
    }

    @GET
    @Path("{id}")
    public Uni<Response> getSingle(Long id) {
        return Hamburger.findById(client, id)
                .onItem().transform(hamburger -> hamburger != null ? Response.ok(hamburger) : Response.status(Status.NOT_FOUND))
                .onItem().transform(ResponseBuilder::build);
    }

    @POST
    public Uni<Response> create(Hamburger hamburger) {
        return hamburger.save(client)
                .onItem().transform(id -> URI.create("/hamburgers/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(Long id, Hamburger hamburger) {
        return hamburger.update(client)
                .onItem().transform(updated -> updated ? Status.OK : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(Long id) {
        return Hamburger.delete(client, id)
                .onItem().transform(deleted -> deleted ? Status.NO_CONTENT : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
}