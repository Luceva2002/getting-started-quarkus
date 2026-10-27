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
        client.query("DROP TABLE IF EXISTS ingredients").execute()
                .flatMap(r -> client.query("DROP TABLE IF EXISTS hamburgers").execute())
                .flatMap(r -> client.query("CREATE TABLE hamburgers (id SERIAL PRIMARY KEY, name TEXT NOT NULL)").execute())
                .flatMap(r -> client.query("INSERT INTO hamburgers (name) VALUES ('Classic Burger')").execute())
                .flatMap(r -> client.query("INSERT INTO hamburgers (name) VALUES ('Double Burger')").execute())
                .flatMap(r -> client.query("INSERT INTO hamburgers (name) VALUES ('Veggie Burger')").execute())
                .flatMap(r -> client.query("CREATE TABLE ingredients (id SERIAL PRIMARY KEY, hamburger_id INTEGER NOT NULL REFERENCES hamburgers(id) ON DELETE CASCADE, name TEXT NOT NULL)").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (hamburger_id, name) VALUES ((SELECT id FROM hamburgers WHERE name = 'Classic Burger'), 'Burger')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (hamburger_id, name) VALUES ((SELECT id FROM hamburgers WHERE name = 'Classic Burger'), 'Lettuce')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (hamburger_id, name) VALUES ((SELECT id FROM hamburgers WHERE name = 'Classic Burger'), 'Tomato')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (hamburger_id, name) VALUES ((SELECT id FROM hamburgers WHERE name = 'Classic Burger'), 'Cheddar')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (hamburger_id, name) VALUES ((SELECT id FROM hamburgers WHERE name = 'Double Burger'), 'Burger')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (hamburger_id, name) VALUES ((SELECT id FROM hamburgers WHERE name = 'Double Burger'), 'Bacon')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (hamburger_id, name) VALUES ((SELECT id FROM hamburgers WHERE name = 'Double Burger'), 'Fries')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (hamburger_id, name) VALUES ((SELECT id FROM hamburgers WHERE name = 'Veggie Burger'), 'Burger')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (hamburger_id, name) VALUES ((SELECT id FROM hamburgers WHERE name = 'Veggie Burger'), 'Lettuce')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (hamburger_id, name) VALUES ((SELECT id FROM hamburgers WHERE name = 'Veggie Burger'), 'Tomato')").execute())
                .await().indefinitely();
    }

}
