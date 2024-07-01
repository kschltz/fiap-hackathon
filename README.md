## Rationale
### Domain
<p>Most of the constraints listed on the test are represented
using clojure's schema library called malli, in a way it's easier to 
return exactly what's wrong with the information field by field.</p>

### Persistence
<p>
XTDB was chosen as an in-memory database to store the data, it is a document
based system with a powerful query language called Datalog, which is a subset of Prolog, and a built-in  2 axis temporal
database, which is perfect for this kind of application where you want to traverse a given entity's history, such as

```clojure
(xt/entity-history db 0 :desc {:with-docs? true})
```
</p>

### Architecture
<p>
A simplified version of the hexagonal architecture was used,
where the core of the application is the domain, and the external dependencies are
the database and the web server, the domain is completely decoupled from everything else.

Most of the web handlers contained in `src/domain/adapter/http.clj` 
could be refactored to extract the domain logic but for brevity reasons they were left as is
</p>


## Running
### Docker
You can build and run the application with the following command
```bash 
docker build -t magis . && \
docker run -it --expose 8080 -p 8080:8080 magis

```

Here are the cURL commands for the endpoints listed in the routes:

1. List beverages:
```bash
curl -X GET "http://localhost:8080/beverage"
```
Replace `type` with the type of beverage you want to list.

2. Store beverage:
```bash
curl -X POST "http://localhost:8080/beverage/store" \
 -H "Content-Type: application/json" \
  -d '{"storage-unit-id":1,
   "beverage-id":"8028bc05-0aef-43ce-a5e5-cc3612f55a1f",
    "liters":15, "employee":"TESTING MAN"}'
```
Replace `id` with the storage unit id and beverage id, `liters` with the amount of liters, and `employee` with the employee name.

3. List storage units:
```bash
curl -X GET "http://localhost:8080/storage-unit"
```

4. Total volume:
```bash
curl -X GET "http://localhost:8080/analytics/total-volume?type=alcoholic"
```
Replace `type` with the type of beverage.

5. Storage capacity:
```bash
curl -X GET "http://localhost:8080/storage-unit/capacity?type=non-alcoholic&liters=10"
```
Replace `type` with the type of beverage and `<liters>` with the amount of liters.

6. Storage availability:
```bash
curl -X GET "http://localhost:8080/storage-unit/availability?type=alcoholic&liters=100"
```
Replace `type` with the type of beverage and `<liters>` with the amount of liters.

7. Movement log:
```bash
curl -X GET "http://localhost:8080/storage-unit/log?time-order=asc"
```
Replace `type` with the type of beverage, `<section>` with the section, and `<order>` with the order (asc or desc).

Please replace the placeholders with actual values as per your requirements. Also, replace `localhost:8080` with your actual server address and port.