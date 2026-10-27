package org.acme.reactive.crud;

import java.net.URI;

import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.ResponseBuilder;
import jakarta.ws.rs.core.Response.Status;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Tuple;

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
    public Uni<Response> getSingle(@PathParam("id") Long id) {
        return Hamburger.findById(client, id)
                .onItem().transform(hamburger -> hamburger != null ? Response.ok(hamburger) : Response.status(Status.NOT_FOUND))
                .onItem().transform(ResponseBuilder::build);
    }

    @GET
    @Path("{id}/ingredients")
    public Uni<Response> getIngredients(@PathParam("id") Long id) {
        return client.preparedQuery("SELECT i.id, i.name FROM hamburger_ingredients hi JOIN ingredients i ON i.id = hi.ingredient_id WHERE hi.hamburger_id = $1 ORDER BY i.name ASC")
                .execute(Tuple.of(id))
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Ingredient::from)
                .collect().asList()
                .onItem().transform(ingredients -> Response.ok(ingredients).build());
    }

    @POST
    public Uni<Response> create(Hamburger hamburger) {
        return hamburger.save(client)
                .onItem().transform(id -> URI.create("/hamburgers/" + id))
                .onItem().transform(uri -> Response.created(uri).build());
    }

    @POST
    @Path("{id}/ingredients")
    public Uni<Response> addIngredient(@PathParam("id") Long hamburgerId, Ingredient ingredient) {
        if (ingredient == null || ingredient.name == null || ingredient.name.isBlank()) {
            return Uni.createFrom().item(Response.status(Status.BAD_REQUEST).build());
        }

        String normalized = ingredient.name.trim();
        return client.preparedQuery("SELECT id FROM hamburgers WHERE id = $1")
                .execute(Tuple.of(hamburgerId))
                .onItem().transformToUni(rows -> {
                    if (rows.iterator().hasNext()) {
                        return client.preparedQuery("INSERT INTO ingredients (name) VALUES ($1) ON CONFLICT (name) DO NOTHING RETURNING id")
                                .execute(Tuple.of(normalized))
                                .onItem().transformToUni(inserted -> {
                                    if (inserted.iterator().hasNext()) {
                                        return client.preparedQuery("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ($1, $2) ON CONFLICT DO NOTHING")
                                                .execute(Tuple.of(hamburgerId, inserted.iterator().next().getLong("id")))
                                                .onItem().transform(rs -> Response.status(Status.CREATED).build());
                                    }
                                    return client.preparedQuery("SELECT id FROM ingredients WHERE name = $1")
                                            .execute(Tuple.of(normalized))
                                            .onItem().transformToUni(existing -> {
                                                Long ingredientId = existing.iterator().next().getLong("id");
                                                return client.preparedQuery("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ($1, $2) ON CONFLICT DO NOTHING")
                                                        .execute(Tuple.of(hamburgerId, ingredientId))
                                                        .onItem().transform(rs -> Response.status(Status.CREATED).build());
                                            });
                                });
                    }
                    return Uni.createFrom().item(Response.status(Status.NOT_FOUND).build());
                });
    }

    @PUT
    @Path("{id}")
    public Uni<Response> update(@PathParam("id") Long id, Hamburger hamburger) {
        return hamburger.update(client)
                .onItem().transform(updated -> updated ? Status.OK : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @DELETE
    @Path("{id}")
    public Uni<Response> delete(@PathParam("id") Long id) {
        return Hamburger.delete(client, id)
                .onItem().transform(deleted -> deleted ? Status.NO_CONTENT : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }

    @DELETE
    @Path("{id}/ingredients/{ingredientId}")
    public Uni<Response> deleteIngredient(@PathParam("id") Long hamburgerId, @PathParam("ingredientId") Long ingredientId) {
        return client.preparedQuery("DELETE FROM hamburger_ingredients WHERE hamburger_id = $1 AND ingredient_id = $2")
                .execute(Tuple.of(hamburgerId, ingredientId))
                .onItem().transform(rs -> rs.rowCount() == 1 ? Status.NO_CONTENT : Status.NOT_FOUND)
                .onItem().transform(status -> Response.status(status).build());
    }
}