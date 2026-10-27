package org.acme.reactive.crud;

import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.vertx.mutiny.sqlclient.Pool;
import io.vertx.mutiny.sqlclient.Row;
import io.vertx.mutiny.sqlclient.RowSet;
import io.vertx.mutiny.sqlclient.Tuple;

public class Hamburger {

    public Long id;
    public String name;

    public Hamburger(String name) {
        this.name = name;
    }

    public Hamburger(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Multi<Hamburger> findAll(Pool client) {
        return client.query("SELECT id, name FROM hamburgers ORDER BY id ASC").execute()
                .onItem().transformToMulti(set -> Multi.createFrom().iterable(set))
                .onItem().transform(Hamburger::from);
    }

    public static Uni<Hamburger> findById(Pool client, Long id) {
        return client.preparedQuery("SELECT id, name FROM hamburgers WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(RowSet::iterator)
                .onItem().transform(iterator -> iterator.hasNext() ? from(iterator.next()) : null);
    }

    public Uni<Long> save(Pool client) {
        return client.preparedQuery("INSERT INTO hamburgers (name) VALUES ($1) RETURNING id").execute(Tuple.of(name))
                .onItem().transform(pgRowSet -> pgRowSet.iterator().next().getLong("id"));
    }

    public Uni<Boolean> update(Pool client) {
        return client.preparedQuery("UPDATE hamburgers SET name = $1 WHERE id = $2").execute(Tuple.of(name, id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    public static Uni<Boolean> delete(Pool client, Long id) {
        return client.preparedQuery("DELETE FROM hamburgers WHERE id = $1").execute(Tuple.of(id))
                .onItem().transform(pgRowSet -> pgRowSet.rowCount() == 1);
    }

    private static Hamburger from(Row row) {
        return new Hamburger(row.getLong("id"), row.getString("name"));
    }
}