import { getDoctors, filterDoctors, saveDoctor } from "./doctorServices.js";
import { createDoctorCard } from "./doctorCard.js";

document.getElementById("addDocBtn")?.addEventListener("click", () => {
    openModal("addDoctor");
});

document.addEventListener("DOMContentLoaded", () => {
    loadDoctorCards();
});

async function loadDoctorCards() {
    try {
        const doctors = await getDoctors();
        const contentDiv = document.getElementById("content");
        contentDiv.innerHTML = "";
        doctors.forEach(doctor => {
            const card = createDoctorCard(doctor);
            contentDiv.appendChild(card);
        });
    } catch (error) {
        console.error("Error loading doctor cards:", error);
    }
}

document.getElementById("searchBar")?.addEventListener("input", filterDoctorsOnChange);
document.getElementById("timeFilter")?.addEventListener("change", filterDoctorsOnChange);
document.getElementById("specialtyFilter")?.addEventListener("change", filterDoctorsOnChange);

async function filterDoctorsOnChange() {
    try {
        const name = document.getElementById("searchBar")?.value.trim() || null;
        const time = document.getElementById("timeFilter")?.value || null;
        const specialty = document.getElementById("specialtyFilter")?.value || null;

        const doctors = await filterDoctors(name, time, specialty);

        if (doctors && doctors.length > 0) {
            renderDoctorCards(doctors);
        } else {
            const contentDiv = document.getElementById("content");
            contentDiv.innerHTML = "<p class='noPatientRecord'>No doctors found with the given filters.</p>";
        }
    } catch (error) {
        alert("Error filtering doctors: " + error.message);
    }
}

function renderDoctorCards(doctors) {
    const contentDiv = document.getElementById("content");
    contentDiv.innerHTML = "";
    doctors.forEach(doctor => {
        const card = createDoctorCard(doctor);
        contentDiv.appendChild(card);
    });
}

async function adminAddDoctor() {
    const name = document.getElementById("doctorName")?.value.trim();
    const email = document.getElementById("doctorEmail")?.value.trim();
    const phone = document.getElementById("doctorPhone")?.value.trim();
    const password = document.getElementById("doctorPassword")?.value.trim();
    const specialty = document.getElementById("doctorSpecialty")?.value.trim();
    const availableTimes = document.getElementById("doctorTimes")?.value
        .split(",")
        .map(t => t.trim())
        .filter(t => t !== "");

    const token = localStorage.getItem("token");
    if (!token) {
        alert("No authentication token found. Please log in again.");
        return;
    }

    const doctor = { name, email, phone, password, specialty, availableTimes };

    try {
        const result = await saveDoctor(doctor, token);
        if (result.status === 1) {
            alert("Doctor added successfully.");
            closeModal("addDoctor");
            location.reload();
        } else {
            alert("Failed to add doctor: " + result.message);
        }
    } catch (error) {
        alert("Error saving doctor: " + error.message);
    }
}

window.adminAddDoctor = adminAddDoctor;
