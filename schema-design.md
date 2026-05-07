# Schema Design

## Overview
This document outlines the database schema design for the Hospital CMS system.
The system uses two databases:
- **MySQL** (Relational Database) — for structured data: Admin, Doctor, Patient, Appointment
- **MongoDB** (NoSQL Database) — for flexible document storage: Prescription

---

## Entity Relationship Diagram
+------------------+          +----------------------+          +------------------+
|      Admin       |          |       Appointment    |          |      Doctor      |
+------------------+          +----------------------+          +------------------+
| PK id (Long)     |          | PK id (Long)         |          | PK id (Long)     |
|    username      |          | FK doctor_id (Long)  |--------->|    name          |
|    password      |          | FK patient_id (Long) |          |    specialty     |
+------------------+          |    appointmentTime   |          |    email         |
|    status (int)      |          |    password      |
+----------------------+          |    phone         |
|                        |    availableTimes|
|                        +------------------+
v
+------------------+
|     Patient      |
+------------------+
| PK id (Long)     |
|    name          |
|    email         |
|    password      |
|    phone         |
|    address       |
+------------------+
+----------------------+
|     Prescription     |  <-- MongoDB Collection
+----------------------+
| PK id (String)       |
|    patientName       |
|    appointmentId     |
|    medication        |
|    dosage            |
|    doctorNotes       |
+----------------------+
---

## MySQL Schema

### 1. Admin Table
| Column     | Type         | Constraints                  | Description                        |
|------------|--------------|------------------------------|------------------------------------|
| id         | BIGINT       | PRIMARY KEY, AUTO_INCREMENT  | Unique identifier for each admin   |
| username   | VARCHAR(50)  | NOT NULL, UNIQUE             | Admin's login username             |
| password   | VARCHAR(255) | NOT NULL                     | Admin's hashed password            |

**SQL Definition:**
```sql
CREATE TABLE admin (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    username VARCHAR(50)  NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
```

---

### 2. Doctor Table
| Column      | Type          | Constraints                 | Description                              |
|-------------|---------------|-----------------------------|------------------------------------------|
| id          | BIGINT        | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each doctor        |
| name        | VARCHAR(100)  | NOT NULL                    | Doctor's full name (3–100 chars)         |
| specialty   | VARCHAR(50)   | NOT NULL                    | Medical specialty (3–50 chars)           |
| email       | VARCHAR(255)  | NOT NULL, UNIQUE            | Doctor's email address                   |
| password    | VARCHAR(255)  | NOT NULL                    | Doctor's hashed password (min 6 chars)   |
| phone       | CHAR(10)      | NOT NULL                    | 10-digit phone number                    |

**SQL Definition:**
```sql
CREATE TABLE doctor (
    id        BIGINT       NOT NULL AUTO_INCREMENT,
    name      VARCHAR(100) NOT NULL,
    specialty VARCHAR(50)  NOT NULL,
    email     VARCHAR(255) NOT NULL UNIQUE,
    password  VARCHAR(255) NOT NULL,
    phone     CHAR(10)     NOT NULL,
    PRIMARY KEY (id)
);
```

**Doctor Available Times (Element Collection):**
| Column          | Type         | Constraints   | Description                        |
|-----------------|--------------|---------------|------------------------------------|
| doctor_id       | BIGINT       | FOREIGN KEY   | References doctor(id)              |
| available_times | VARCHAR(255) | NOT NULL      | Time slot string (e.g. "09:00-10:00") |

```sql
CREATE TABLE doctor_available_times (
    doctor_id       BIGINT       NOT NULL,
    available_times VARCHAR(255) NOT NULL,
    FOREIGN KEY (doctor_id) REFERENCES doctor(id)
);
```

---

### 3. Patient Table
| Column   | Type         | Constraints                 | Description                            |
|----------|--------------|-----------------------------|----------------------------------------|
| id       | BIGINT       | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each patient     |
| name     | VARCHAR(100) | NOT NULL                    | Patient's full name (3–100 chars)      |
| email    | VARCHAR(255) | NOT NULL, UNIQUE            | Patient's email address                |
| password | VARCHAR(255) | NOT NULL                    | Patient's hashed password (min 6 chars)|
| phone    | CHAR(10)     | NOT NULL                    | 10-digit phone number                  |
| address  | VARCHAR(255) | NOT NULL                    | Patient's address (max 255 chars)      |

**SQL Definition:**
```sql
CREATE TABLE patient (
    id       BIGINT       NOT NULL AUTO_INCREMENT,
    name     VARCHAR(100) NOT NULL,
    email    VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    phone    CHAR(10)     NOT NULL,
    address  VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);
```

---

### 4. Appointment Table
| Column           | Type     | Constraints                 | Description                              |
|------------------|----------|-----------------------------|------------------------------------------|
| id               | BIGINT   | PRIMARY KEY, AUTO_INCREMENT | Unique identifier for each appointment   |
| doctor_id        | BIGINT   | FOREIGN KEY, NOT NULL       | References doctor(id)                    |
| patient_id       | BIGINT   | FOREIGN KEY, NOT NULL       | References patient(id)                   |
| appointment_time | DATETIME | NOT NULL, FUTURE            | Scheduled date and time (must be future) |
| status           | INT      | NOT NULL                    | 0 = Scheduled, 1 = Completed             |

**SQL Definition:**
```sql
CREATE TABLE appointment (
    id               BIGINT   NOT NULL AUTO_INCREMENT,
    doctor_id        BIGINT   NOT NULL,
    patient_id       BIGINT   NOT NULL,
    appointment_time DATETIME NOT NULL,
    status           INT      NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    FOREIGN KEY (doctor_id)  REFERENCES doctor(id),
    FOREIGN KEY (patient_id) REFERENCES patient(id)
);
```

---

## MongoDB Schema

### 5. Prescription Collection
| Field         | Type   | Constraints          | Description                                  |
|---------------|--------|----------------------|----------------------------------------------|
| _id           | String | PRIMARY KEY (ObjectId)| Auto-generated MongoDB document ID          |
| patientName   | String | NOT NULL, 3–100 chars | Name of the patient receiving prescription  |
| appointmentId | Long   | NOT NULL              | Reference to MySQL appointment ID           |
| medication    | String | NOT NULL, 3–100 chars | Name of prescribed medication               |
| dosage        | String | NOT NULL              | Dosage instructions                         |
| doctorNotes   | String | max 200 chars         | Additional notes from the doctor            |

**MongoDB Document Example:**
```json
{
    "_id": "64f1a2b3c4d5e6f7a8b9c0d1",
    "patientName": "John Doe",
    "appointmentId": 101,
    "medication": "Amoxicillin",
    "dosage": "500mg twice daily",
    "doctorNotes": "Take after meals. Avoid alcohol."
}
```

---

## Relationships

| Relationship                | Type        | Description                                              |
|-----------------------------|-------------|----------------------------------------------------------|
| Doctor → Appointment        | One-to-Many | One doctor can have many appointments                    |
| Patient → Appointment       | One-to-Many | One patient can have many appointments                   |
| Appointment → Prescription  | One-to-One  | Each appointment can have at most one prescription       |
| Doctor → AvailableTimes     | One-to-Many | One doctor can have multiple available time slots        |

---

## Validation Rules

### Admin
| Field    | Rule                        |
|----------|-----------------------------|
| username | Required, unique            |
| password | Required                    |

### Doctor
| Field         | Rule                              |
|---------------|-----------------------------------|
| name          | Required, 3–100 characters        |
| specialty     | Required, 3–50 characters         |
| email         | Required, valid email, unique     |
| password      | Required, minimum 6 characters   |
| phone         | Required, exactly 10 digits       |
| availableTimes| List of time slot strings         |

### Patient
| Field    | Rule                              |
|----------|-----------------------------------|
| name     | Required, 3–100 characters        |
| email    | Required, valid email, unique     |
| password | Required, minimum 6 characters   |
| phone    | Required, exactly 10 digits       |
| address  | Required, max 255 characters      |

### Appointment
| Field           | Rule                              |
|-----------------|-----------------------------------|
| doctor          | Required, must exist              |
| patient         | Required, must exist              |
| appointmentTime | Required, must be a future date   |
| status          | Required, 0 or 1                  |

### Prescription
| Field         | Rule                              |
|---------------|-----------------------------------|
| patientName   | Required, 3–100 characters        |
| appointmentId | Required, must reference valid ID |
| medication    | Required, 3–100 characters        |
| dosage        | Required                          |
| doctorNotes   | Optional, max 200 characters      |

---

## Appointment Status Codes

| Code | Status    | Description                              |
|------|-----------|------------------------------------------|
| 0    | Scheduled | Appointment is booked and upcoming       |
| 1    | Completed | Appointment is done, prescription issued |

---

## Technology Stack

| Database  | Technology | Usage                                    |
|-----------|------------|------------------------------------------|
| Relational| MySQL      | Admin, Doctor, Patient, Appointment data |
| NoSQL     | MongoDB    | Prescription documents                   |
| ORM       | Hibernate  | JPA entity mapping for MySQL             |
| ODM       | Spring Data MongoDB | Document mapping for MongoDB    |
