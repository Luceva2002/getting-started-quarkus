package org.acme.reactive.crud;

import io.vertx.mutiny.sqlclient.Row;

public class Ingredient {

    public Long id;
    public String name;

    public Ingredient() {
    }

    public Ingredient(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Ingredient from(Row row) {
        return new Ingredient(row.getLong("id"), row.getString("name"));
    }
}
