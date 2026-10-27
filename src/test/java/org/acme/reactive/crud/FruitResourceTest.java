package org.acme.reactive.crud;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.IsNot.not;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class hamburgerResourceTest {

    @Test
    public void testListAllhamburgers() {
        //List all, should have all 4 hamburgers the database has initially:
        given()
                .when().get("/hamburgers")
                .then()
                .statusCode(200)
                .body(
                        containsString("Kiwi"),
                        containsString("Durian"),
                        containsString("Pomelo"),
                        containsString("Lychee"));

        //Delete the Kiwi:
        given()
                .when().delete("/hamburgers/1")
                .then()
                .statusCode(204);

        //List all, Kiwi should be missing now:
        given()
                .when().get("/hamburgers")
                .then()
                .statusCode(200)
                .body(
                        not(containsString("Kiwi")),
                        containsString("Durian"),
                        containsString("Pomelo"),
                        containsString("Lychee"));
    }

    @Test
    public void testListSmoothieRecipesForhamburger() {
        given()
                .when().get("/hamburgers/1/smoothies")
                .then()
                .statusCode(200)
                .body(
                        containsString("Kiwi"),
                        containsString("Green Glow"),
                        containsString("banana"));
    }

}
