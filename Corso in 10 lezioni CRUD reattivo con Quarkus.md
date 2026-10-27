# Corso in 10 lezioni: CRUD reattivo con Quarkus

Oct 27, 2026 · @Luca

Il codice di `Hamburger.java` e `HamburgerResource.java` è un CRUD reattivo con Quarkus, Mutiny e il client Vert.x per PostgreSQL. Ogni lezione prende un pezzo di quel codice, spiega come si scrive e ti dà un esercizio da rifare da solo.

## Lezione 1: La classe dati (`Hamburger`)

La classe `Hamburger` ha due campi pubblici e due costruttori, perché l'`id` lo assegna il database.

- **Concetti:** package, campi pubblici (`id`, `name`), un costruttore senza `id` per creare e uno con `id` per leggere dal DB.
- **Perché due costruttori:** in creazione l'`id` non lo conosci ancora.
- **Esercizio:** scrivi da zero una classe `Ingredient` con `id` e `name` e i suoi due costruttori.

## Lezione 2: Il Resource REST (JAX-RS)

Una classe `Resource` espone gli endpoint HTTP con le annotazioni JAX-RS.

- **Concetti:** `@Path("hamburgers")` sulla classe, `@GET`, `@POST`, `@PUT`, `@DELETE`, `@PathParam("id")`.
- **Iniezione:** `Pool` arriva dal costruttore, senza `@Inject`, perché Quarkus lo risolve da solo.
- **Esercizio:** crea `IngredientResource` con un solo endpoint `GET /ingredients` che risponde con un messaggio fisso.

## Lezione 3: Mutiny, `Uni` e `Multi`

Ogni operazione asincrona restituisce un `Uni` (un risultato) o un `Multi` (un flusso di risultati).

- **Concetti:** `Uni<T>` è un solo risultato futuro, `Multi<T>` è un flusso di risultati.
- **Operatori base:** `.onItem().transform(...)` trasforma il valore quando arriva.
- **Regola:** una lista di elementi è `Multi`, un elemento singolo o un esito è `Uni`.
- **Esercizio:** per ogni metodo di `HamburgerResource`, scrivi se restituisce `Uni` o `Multi` e perché.

## Lezione 4: Parlare col database (`Pool`, `Tuple`, `Row`)

Le query parametrizzate con `$1, $2...` sono il modo sicuro di parlare con PostgreSQL.

- **Concetti:** `client.query(...)` per query senza parametri, `client.preparedQuery(...)` con `$1, $2...` e `Tuple.of(...)` per i parametri.
- **Lettura dei risultati:** `RowSet` contiene le righe e `Row` è una riga (`row.getLong("id")`, `row.getString("name")`).
- **Sicurezza:** i parametri `$1` evitano la SQL injection, non concatenare mai stringhe nella query.
- **Esercizio:** scrivi la query `SELECT` per leggere solo gli hamburger con un certo nome.

## Lezione 5: Il pattern "active record"

La classe `Hamburger` contiene anche i metodi per salvarsi e cercarsi nel database: `findAll`, `findById`, `save`, `update`, `delete`.

- **Concetti:** metodi `static` per le operazioni senza istanza (`findAll`, `findById`, `delete`) e metodi d'istanza per quelle sull'oggetto (`save`, `update`).
- **Trucchi da imparare:** `RETURNING id` nell'`INSERT`, `rowCount() == 1` per sapere se l'`UPDATE` o il `DELETE` ha colpito qualcosa.
- **Metodo `from(Row)`:** converte una riga del DB in un oggetto.
- **Esercizio:** aggiungi `findByName` a `Hamburger`.

## Lezione 6: Risposte HTTP corrette (`Response`)

Ogni endpoint deve restituire il codice HTTP giusto: `200`, `201`, `204` o `404`.

- **Concetti:** `Response.ok(...)`, `Response.created(uri)`, `Status.NOT_FOUND`, `Status.NO_CONTENT`, `Response.status(...).build()`.
- **Convenzioni:** `POST` risponde `201` con l'header `Location`, `PUT` risponde `200` o `404`, `DELETE` risponde `204` o `404`.
- **Esercizio:** riscrivi `getSingle` usando un `if` classico dentro `transform`, poi confrontalo con l'operatore ternario del file.

## Lezione 7: Relazione molti-a-molti (hamburger e ingredienti)

Un hamburger ha molti ingredienti e un ingrediente sta in molti hamburger, quindi serve una tabella di collegamento.

- **Concetti:** tre tabelle (`hamburgers`, `ingredients`, `hamburger_ingredients`) e una `JOIN` per leggere gli ingredienti di un hamburger.
- **Codice da studiare:** `getIngredients` e la catena `transformToMulti`, `transform(Ingredient::from)`, `collect().asList()`.
- **Esercizio:** scrivi lo schema SQL (`CREATE TABLE`) delle tre tabelle con chiavi primarie ed esterne. Nei file caricati non c'è, e la classe `Ingredient` non c'è nemmeno.

## Lezione 8: Catene annidate con `transformToUni` (`addIngredient`)

`addIngredient` concatena più query asincrone, una dopo l'altra, annidando `transformToUni`.

- **Concetti:** `transformToUni` serve quando il passo successivo è a sua volta una query asincrona, e `Uni.createFrom().item(...)` per restituire subito un valore.
- **Logica del metodo:**
  1. Controlla che l'hamburger esista.
  2. Inserisci l'ingrediente con `ON CONFLICT (name) DO NOTHING RETURNING id`.
  3. Se è già presente, rileggi il suo `id` e crea il legame.
- **Esercizio:** disegna su carta il diagramma di flusso di questo metodo, poi riscrivilo separando i passi in metodi privati più piccoli.

## Lezione 9: Validazione e gestione degli errori

Il codice attuale valida solo `addIngredient`: `create` e `update` non controllano che `hamburger` o `name` siano validi.

- **Concetti:** controllo dei `null` e delle stringhe vuote (`isBlank`, `trim`), risposta `400 BAD_REQUEST`.
- **Come migliorare:** Bean Validation (`@Valid`, `@NotBlank`) e gestione dei `null`.
- **Esercizio:** aggiungi la validazione a `create` e `update` e testala.

## Lezione 10: Test e progetto finale

L'ultima lezione chiude il corso con i test e un progetto da fare senza copiare.

- **Concetti:** `@QuarkusTest` con RestAssured, Dev Services (Quarkus avvia PostgreSQL da solo in sviluppo), prova manuale con `curl` o Swagger UI.
- **Progetto finale:** crea da zero una nuova entità (per esempio `Drink`) con CRUD completo, relazione con gli hamburger e almeno 5 test, senza copiare il codice esistente.

## Come studiare

Per ogni lezione, leggi il pezzo di codice, riscrivilo a mano senza guardare e poi confrontalo con l'originale. Ricopiarlo non basta: è lo sforzo di ricordare a fissare le cose.
