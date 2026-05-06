import { showBookingOverlay } from "./loggedPatient.js";
import { deleteDoctor } from "./doctorServices.js";
import { getPatientDetails } from "./patientServices.js";

export function createDoctorCard(doctor) {
    const card = document.createElement("div");
    card.classList.add("doctor-card");

    const role = localStorage.getItem("role");

    const doctorInfo = document.createElement("div");
    doctorInfo.classList.add("doctor-info");

    const name = document.createElement("h3");
    name.textContent = doctor.name;

    const specialty = document.createElement("p");
    specialty.textContent = `Specialization: ${doctor.specialty}`;

    const email = document.createElement("p");
    email.textContent = `Email: ${doctor.email}`;

    const times = document.createElement("p");
    times.textContent = `Available Times: ${doctor.availableTimes ? doctor.availableTimes.join(", ") : "N/A"}`;

    doctorInfo.appendChild(name);
    doctorInfo.appendChild(specialty);
    doctorInfo.appendChild(email);
    doctorInfo.appendChild(times);

    const actions = document.createElement("div");
    actions.classList.add("card-actions");

    if (role === "admin") {
        const deleteBtn = document.createElement("button");
        deleteBtn.textContent = "Delete";
        deleteBtn.classList.add("delete-btn");

        deleteBtn.addEventListener("click", async () => {
            const token = localStorage.getItem("token");
            const result = await deleteDoctor(doctor.id, token);
            alert(result.message);
            if (result.status === 1) {
                card.remove();
            }
        });

        actions.appendChild(deleteBtn);

    } else if (role === "patient") {
        const bookBtn = document.createElement("button");
        bookBtn.textContent = "Book Now";
        bookBtn.classList.add("book-btn");

        bookBtn.addEventListener("click", () => {
            alert("Please log in to book an appointment.");
        });

        actions.appendChild(bookBtn);

    } else if (role === "loggedPatient") {
        const bookBtn = document.createElement("button");
        bookBtn.textContent = "Book Now";
        bookBtn.classList.add("book-btn");

        bookBtn.addEventListener("click", async () => {
            const token = localStorage.getItem("token");
            if (!token) {
                window.location.href = "/login";
                return;
            }
            const patientData = await getPatientDetails(token);
            showBookingOverlay(doctor, patientData);
        });

        actions.appendChild(bookBtn);
    }

    card.appendChild(doctorInfo);
    card.appendChild(actions);

    return card;
}
