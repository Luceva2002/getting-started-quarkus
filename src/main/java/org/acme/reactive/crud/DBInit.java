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
        client.query("DROP TABLE IF EXISTS hamburger_ingredients").execute()
                .flatMap(r -> client.query("DROP TABLE IF EXISTS ingredients").execute())
                .flatMap(r -> client.query("DROP TABLE IF EXISTS hamburgers").execute())
                .flatMap(r -> client.query("CREATE TABLE hamburgers (id SERIAL PRIMARY KEY, name TEXT NOT NULL)").execute())
                .flatMap(r -> client.query("CREATE TABLE ingredients (id SERIAL PRIMARY KEY, name TEXT NOT NULL UNIQUE)").execute())
                .flatMap(r -> client.query("CREATE TABLE hamburger_ingredients (hamburger_id INTEGER NOT NULL REFERENCES hamburgers(id) ON DELETE CASCADE, ingredient_id INTEGER NOT NULL REFERENCES ingredients(id) ON DELETE CASCADE, PRIMARY KEY (hamburger_id, ingredient_id))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburgers (name) VALUES ('Classic Burger')").execute())
                .flatMap(r -> client.query("INSERT INTO hamburgers (name) VALUES ('Double Burger')").execute())
                .flatMap(r -> client.query("INSERT INTO hamburgers (name) VALUES ('Veggie Burger')").execute())
                .flatMap(r -> client.query("INSERT INTO hamburgers (name) VALUES ('Amatriciano di ciccioghè')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Burger')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Lettuce')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Tomato')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Cheddar')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Bacon')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Fries')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Bacon laccato Miele')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Ciccio''s passion')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Sugo laido e corrotto')").execute())
                .flatMap(r -> client.query("INSERT INTO ingredients (name) VALUES ('Pagnotta di ciccio')").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Amatriciano di ciccioghè'), (SELECT id FROM ingredients WHERE name = 'Burger'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Amatriciano di ciccioghè'), (SELECT id FROM ingredients WHERE name = 'Ciccio''s passion'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Amatriciano di ciccioghè'), (SELECT id FROM ingredients WHERE name = 'Bacon laccato Miele'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Amatriciano di ciccioghè'), (SELECT id FROM ingredients WHERE name = 'Sugo laido e corrotto'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Amatriciano di ciccioghè'), (SELECT id FROM ingredients WHERE name = 'Pagnotta di ciccio'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Classic Burger'), (SELECT id FROM ingredients WHERE name = 'Burger'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Classic Burger'), (SELECT id FROM ingredients WHERE name = 'Lettuce'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Classic Burger'), (SELECT id FROM ingredients WHERE name = 'Tomato'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Classic Burger'), (SELECT id FROM ingredients WHERE name = 'Cheddar'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Veggie Burger'), (SELECT id FROM ingredients WHERE name = 'Burger'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Veggie Burger'), (SELECT id FROM ingredients WHERE name = 'Lettuce'))").execute())
                .flatMap(r -> client.query("INSERT INTO hamburger_ingredients (hamburger_id, ingredient_id) VALUES ((SELECT id FROM hamburgers WHERE name = 'Veggie Burger'), (SELECT id FROM ingredients WHERE name = 'Tomato'))").execute())
                .await().indefinitely();
    }

}
