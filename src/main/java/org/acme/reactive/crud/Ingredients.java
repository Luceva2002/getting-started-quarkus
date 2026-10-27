package org.acme.reactive.crud;

import io.smallrye.mutiny.Multi;
import io.vertx.mutiny.sqlclient.Pool;

public class Ingredients {

    public Long id;
    public String name;

    public Ingredients(){
    }

    public Ingredients(String name, Long id){
        this.name = name;
        this.id = id;
    }

    public static Multi<Object> findById(Pool client, Long id) {

    }
}
