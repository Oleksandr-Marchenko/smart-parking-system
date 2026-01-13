# Smart Parking System Backend

## Project Overview
This is a high-performance backend implementation for a Smart Parking System built on the **Spring Boot 3.4+** stack. The system manages multi-level parking structures, automates vehicle check-in/check-out processes, and calculates service fees based on flexible pricing strategies.

### Key Features
* **Structure Management**: Full CRUD operations for parking lots, levels, and slots.
* **Smart Allocation**: Automatically finds the optimal parking space considering vehicle type and floor priority (bottom-up approach).
* **Dynamic Pricing**: Utilizes the **Strategy Pattern** for fee calculation with distinct rates for Cars, Trucks, and Motorcycles.
* **Reliability**: Comprehensive three-tier testing (Unit, WebMvc, and Integration) covering core business risks.

---

## Architecture and Design Patterns

### Design Patterns Used
1. **Strategy Pattern**: Implemented in the pricing logic to allow changing fee calculation for different vehicle types without modifying core service code.
2. **Factory Pattern**: Used in the domain logic for standardized creation of various parking slot types.
3. **Clean Architecture**: Strict separation of concerns between Controllers, Services, Domain Models, and Repositories.

### Slot Compatibility Logic
The system prioritizes floors closest to the entrance and selects slots based on the following rules:
* **HANDICAPPED**: Reserved specifically for vehicles with valid disability permits.
* **MOTORCYCLE**: Can occupy `MOTORCYCLE`, `COMPACT`, or `LARGE` slots.
* **CAR**: Can occupy `COMPACT` or `LARGE` slots.
* **TRUCK**: Can occupy only `LARGE` slots.

---

## Technologies Used

* **Java 17**: Core programming language.
* **Spring Boot 3.4+**: Primary framework for building the RESTful API and managing dependency injection.
* **Spring WebMvc**: Used for building the RESTful web layer and handling HTTP requests/responses.
* **Gradle**: Build automation tool and dependency management.
* **Lombok**: Library used to reduce boilerplate code.
* **Jakarta Validation**: Used for request body validation (e.g., `@Valid` in controllers).
* **JUnit 5 & Mockito**: Utilized for writing unit and integration tests.

## How to Build and Run

### Prerequisites
* **Java 17** or higher
* **Gradle 8.x**

### Installation
1. Clone the repository:
   ```bash
   git clone https://github.com/Oleksandr-Marchenko/smart-parking-system
   cd smart-parking-system
   
   
   ```
2. Compile the code and build the JAR file
   ```bash
   ./gradlew build
   ```
3. Running the Application
   ```bash
   ./gradlew bootRun
   ```
   The server will be available at http://localhost:8080  
   You can import the included `Smart Parking System.postman_collection.json` file from the `postman` folder into Postman.
- Running Tests
   ```bash
  ./gradlew clean test
   ```

## API Reference
### Admin Endpoints

| Method | Endpoint | Request Body | Description |
|--------|----------|--------------|-------------|
| POST   | /api/v1/admin/lots | {"name": "String"} | Create a new parking lot |
| DELETE | /api/v1/admin/lots/:id | - | Delete parking lot (fails if levels or slots contain vehicles) |
| POST   | /api/v1/admin/lots/:lotId/levels | {"floorNumber": int} | Add a new level |
| DELETE | /api/v1/admin/levels/:id | - | Delete a level (fails if slots contain vehicles) |
| POST   | /api/v1/admin/levels/:levelId/slots | {"slotNumber": "String", "type": "Enum"} | Add a slot |
| DELETE | /api/v1/admin/slots/:id | - | Delete a slot (fails if vehicle assigned) |
| PATCH  | /api/v1/admin/slots/:slotId/availability?available=true | - | Toggle slot availability |

### Parking Endpoints
| Method | Endpoint | Request Body                                                                  | Description |
|--------|----------|-------------------------------------------------------------------------------|-------------|
| POST   | /api/v1/parking/check-in | {"licensePlate": "String", "vehicleType": "Enum", isHandicapped(): "Boolean"} | Check in vehicle |
| POST   | /api/v1/parking/check-out/:ticketId | -                                                                             | Check out vehicle by ticket |
| GET    | /api/v1/parking/sessions | -                                                                             | Get active sessions |


## Example Payloads
### Admin Endpoints

- **`POST /api/v1/admin/lots`**

<details>

- **Request**:
  ```json
  {
      "name": "Downtown Central Parking"
  }
  ```

- **Response (201 Created):**:
   ```json
  {
      "id": 1,
      "name": "Downtown Central Parking"
  }
   ```
  
</details>

- **`DELETE /api/v1/admin/lots/{id}`**

<details>

- **Request**:
- - Path Variable: id (The unique identifier of the lot)

#### Scenario 1: Successful Deletion
- **Response (204 No Content):**:

#### Scenario 2: Failed Deletion (Occupied Slots)
- **Response (400 Bad Request):**:
   ```json
  {
      "timestamp": "2024-05-20T14:35:10",
      "status": 400,
      "error": "Bad Request",
      "message": "Cannot delete parking lot: it contains slots with vehicles assigned."
  }
   ```

</details>

- **`POST /api/v1/admin/lots/{lotId}/levels`**

<details>

- **Request**:
  ```json
  {
      "floorNumber": 1
  }
  ```
- **Response (201 Created):**:
   ```json
  {
      "id": 10,
      "floorNumber": 1,
      "lotId": 1
  }
   ```

</details>

- **`DELETE /api/v1/admin/levels/{id}`**

<details>

- **Request**:
- - Path Variable: id (The unique identifier of the level)

#### Scenario 1: Successful Deletion
- **Response (204 No Content):**:

#### Scenario 2: Failed Deletion (Level Occupied)
- **Response (400 Bad Request):**:
   ```json
  {
      "timestamp": "2024-05-20T15:00:00",
      "status": 400,
      "error": "Bad Request",
      "message": "Cannot delete level: it contains slots with vehicles assigned."
  }
   ```

</details>

- **`POST /api/v1/admin/levels/{levelId}/slots`**

<details>

- **Request**:
  ```json
  {
      "slotNumber": "B-101",
      "type": "CAR"
  }
  ```
- - (Types: MOTORCYCLE, COMPACT, LARGE)

- **Response (201 Created):**:
   ```json
  {
      "id": 50,
      "slotNumber": "B-101",
      "type": "CAR",
      "isAvailable": true
  }
   ```

</details>

- **`DELETE /api/v1/admin/slots/{id}`**

<details>

- **Request**:
- - Path Variable: id (The unique identifier of the slot)

#### Scenario 1: Successful Deletion
- **Response (204 No Content):**:
   ```json
  {
      "id": 1,
      "name": "Downtown Central Parking"
  }
   ```

#### Scenario 2: Failed Deletion (Slot Occupied)
- **Response 400 Bad Request**:
   ```json
  {
      "timestamp": "2024-05-20T15:10:00",
      "status": 400,
      "error": "Bad Request",
      "message": "Cannot delete slot: it is currently occupied by a vehicle."
  }
   ```

</details>

- **`PATCH /api/v1/admin/slots/{slotId}/availability?available=false`**

<details>

- **Request**:
- - Path Variable: slotId (The unique identifier of the slot)

- **Response (200 OK):**:
   ```json
  {
      "id": 2,
      "slotNumber": "B-101",
      "type": "MOTORCYCLE",
      "isAvailable": false
  }
   ```

</details>

### Parking Endpoints

- **`POST /api/v1/parking/check-in`**

<details>

- **Request**:
  ```json
  {
      "licensePlate": "AA1234BE",
      "vehicleType": "TRUCK",
      "isHandicapped": "false"
  }
  ```
- - (Types: MOTORCYCLE, CAR, TRUCK)

- **Response (200 OK):**:
   ```json
  {
      "ticketId": 2,
      "licensePlate": "AA1234BEfdf",
      "vehicleType": "CAR",
      "entryTime": "2026-01-13 14:51:30",
      "slotNumber": "A2",
      "levelFloor": 1
  }
  ```

</details>

- **`POST /api/v1/parking/check-out/{ticketId}`**

<details>

- **Request**:
- - Path Variable: ticketId (The unique identifier of the parking ticket)

- **Response (200 OK):**:
   ```json
  {
      "licensePlate": "AA1234BE",
      "entryTime": "2026-01-13 14:41:36",
      "exitTime": "2026-01-13 14:53:00",
      "durationMinutes": 11,
      "totalFee": 2.0
  }
  ```

</details>

- **`GET /api/v1/parking/sessions`**

<details>

- **Request**:

- **Response (201 Created):**:
   ```json
  [
      {
         "ticketId": 2,
         "licensePlate": "AA1234BEfdf",
         "vehicleType": "CAR",
         "entryTime": "2026-01-13 14:51:30",
         "slotNumber": "A2",
         "levelFloor": 1
      }
  ]
  ```

</details>

## Known Limitations & TODOs

* **[ ] Persistence**: Replace in-memory storage with a persistent database like PostgreSQL for production use.
* **[ ] Security**: Implement Spring Security (JWT or OAuth2) to protect Admin endpoints.
* **[ ] Concurrency**: Add database-level locking or synchronized mechanisms to handle high-frequency simultaneous check-ins and prevent double-booking.