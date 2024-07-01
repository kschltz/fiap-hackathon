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
   "beverage-id":"b9436777-4a06-45cf-9389-4fc186184add",
    "liters":15, "employee":"TESTING MAN"}'
```
Replace `id` with the storage unit id and beverage id, `liters` with the amount of liters, and `employee` with the employee name.

3. List storage units:
```bash
curl -X GET "http://localhost:8080/storage-unit"
```

4. Total volume:
```bash
curl -X GET "http://localhost:8080/storage-unit/analytics/total-volume?type=<type>"
```
Replace `type` with the type of beverage.

5. Storage capacity:
```bash
curl -X GET "http://localhost:8080/storage-unit/capacity?type=<type>&liters=<liters>"
```
Replace `type` with the type of beverage and `<liters>` with the amount of liters.

6. Storage availability:
```bash
curl -X GET "http://localhost:8080/storage-unit/availability?type=<type>&liters=<liters>"
```
Replace `type` with the type of beverage and `<liters>` with the amount of liters.

7. Movement log:
```bash
curl -X GET "http://localhost:8080/storage-unit/log?bev-type=<type>&section=<section>&storage-order=<order>&time-order=<order>"
```
Replace `type` with the type of beverage, `<section>` with the section, and `<order>` with the order (asc or desc).

Please replace the placeholders with actual values as per your requirements. Also, replace `localhost:8080` with your actual server address and port.