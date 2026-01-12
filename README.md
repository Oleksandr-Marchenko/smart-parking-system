# Smart Parking System Backend (Take-home Assignment)

## 📌 Project Overview
This is a production-ready implementation of a Smart Parking System backend developed as a technical assessment. The system manages multi-level parking lots, handles vehicle check-in/check-out via REST API, and calculates fees based on extensible pricing models.

### Key Features
* **Multi-level Management:** Full CRUD for parking lots, levels, and slots.
* **Smart Slot Allocation:** Automatically finds the best available slot based on vehicle compatibility (e.g., Trucks only in Large slots).
* **Extensible Pricing:** Implemented via the **Strategy Pattern** to allow different rates for Cars, Trucks, and Motorcycles.
* **Clean Architecture:** Strict separation of concerns (Controller -> Service -> Domain -> Repository).

---

## 🏗 Architecture & Design Patterns

### Design Patterns Used
1.  **Strategy Pattern:** Used for `FeeCalculation`. Adding a new vehicle type or a "Holiday Rate" requires zero changes to existing logic—just a new implementation of `PricingStrategy`.
2.  **Factory Pattern:** Used for `Vehicle` and `ParkingSlot` creation to centralize instantiation logic.
3.  **Singleton/Repository Pattern:** In-memory storage abstraction using Spring Data's repository style.

### Slot Compatibility Logic
* **MOTORCYCLE:** Can park in `MOTORCYCLE`, `COMPACT`, or `LARGE` slots.
* **CAR:** Can park in `COMPACT` or `LARGE` slots.
* **TRUCK:** Can park only in `LARGE` slots.

---

## 🚀 Getting Started

### Prerequisites
* Java 17 or higher
* Gradle 8.x

### Build and Run
```bash
./gradlew bootRun