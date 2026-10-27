package org.acme.reactive.crud;

import io.quarkus.runtime.StartupEvent;
import io.vertx.mutiny.sqlclient.Pool;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class DBInit {

    private final Pool client;
    private final boolean schemaCreate;

    public DBInit(Pool client, @ConfigProperty(name = "myapp.schema.create", defaultValue = "true") boolean schemaCreate) {
        this.client = client;
        this.schemaCreate = schemaCreate;
    }

    void onStart(@Observes StartupEvent ev) {
        System.out.println(">>> DBInit, schemaCreate=" + schemaCreate);
        if (schemaCreate) {
            initdb();
        }
    }

    private void initdb() {
    client.query("DROP TABLE IF EXISTS hamburgers").execute()
            .flatMap(r -> client.query("CREATE TABLE hamburgers (id SERIAL PRIMARY KEY, name TEXT NOT NULL)").execute())
            .flatMap(r -> client.query("INSERT INTO hamburgers (name) VALUES ('Classic Burger')").execute())
            .flatMap(r -> client.query("INSERT INTO hamburgers (name) VALUES ('Double Burger')").execute())
            .flatMap(r -> client.query("INSERT INTO hamburgers (name) VALUES ('Veggie Burger')").execute())
            .await().indefinitely();
    client.query("DROP TABLE IF EXISTS ingredients").execute()
            .flatMap(r -> client.query("CREATE TABLE ingredients (id SERIAL PRIMARY KEY, name TEXT NOT NULL)").execute())
            .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Burger')").execute())
            .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Lattuce')").execute())
            .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Tomato')").execute())
            .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Cheddar')").execute())
            .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Bacon')").execute())
            .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Fries')").execute())
            .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('')").execute())
            .await().indefinitely();
    }

}
