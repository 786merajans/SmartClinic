import { getAllAppointments } from "./appointmentServices.js";
import { createPatientRow } from "./patientRow.js";

const tableBody = document.getElementById("appointmentTableBody");

let selectedDate = new Date().toISOString().split("T")[0];

const token = localStorage.getItem("token");

let patientName = null;

document.getElementById("searchBar")?.addEventListener("input", (e) => {
    const value = e.target.value.trim();
    if (value !== "") {
        patientName = value;
    } else {
        patientName = "null";
    }
    loadAppointments();
});

document.getElementById("todayBtn")?.addEventListener("click", () => {
    selectedDate = new Date().toISOString().split("T")[0];
    const datePicker = document.getElementById("datePicker");
    if (datePicker) datePicker.value = selectedDate;
    loadAppointments();
});

document.getElementById("datePicker")?.addEventListener("change", (e) => {
    selectedDate = e.target.value;
    loadAppointments();
});

async function loadAppointments() {
    try {
        const appointments = await getAllAppointments(selectedDate, patientName, token);

        tableBody.innerHTML = "";

        if (!appointments || appointments.length === 0) {
            tableBody.innerHTML = `
                <tr>
                    <td colspan="100%" class="noPatientRecord">No Appointments found for today.</td>
                </tr>`;
            return;
        }

        appointments.forEach(appointment => {
            const patient = {
                id: appointment.patientId,
                name: appointment.patientName,
                phone: appointment.patientPhone,
                email: appointment.patientEmail
            };
            const row = createPatientRow(appointment, patient);
            tableBody.appendChild(row);
        });

    } catch (error) {
        tableBody.innerHTML = `
            <tr>
                <td colspan="100%" class="noPatientRecord">Error loading appointments. Try again later.</td>
            </tr>`;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    renderContent();
    loadAppointments();
});
