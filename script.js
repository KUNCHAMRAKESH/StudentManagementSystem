

const API = "/api";

let students = [];

const form = document.getElementById("studentForm");
const table = document.getElementById("studentTable");
const search = document.getElementById("search");
const message = document.getElementById("message");

// Display notification
function showMessage(text, error = false) {
    message.textContent = text;
    message.style.background = error ? "#dc2626" : "#166534";
    message.style.display = "block";

    setTimeout(() => {
        message.style.display = "none";
    }, 3000);
}

// Escape HTML to safely display user-entered values
function safe(value) {
    return String(value ?? "").replace(/[&<>"']/g, char => ({
        "&": "&amp;",
        "<": "&lt;",
        ">": "&gt;",
        '"': "&quot;",
        "'": "&#39;"
    })[char]);
}

// Send form data to Java backend
async function postData(endpoint, data) {
    const response = await fetch(API + endpoint, {
        method: "POST",
        headers: {
            "Content-Type": "application/x-www-form-urlencoded"
        },
        body: new URLSearchParams(data)
    });

    const result = await response.json();

    if (!response.ok) {
        throw new Error(result.error || "Something went wrong");
    }

    return result;
}

// Load all students
async function loadStudents() {
    try {
        const response = await fetch(API + "/students");

        if (!response.ok) {
            throw new Error("Unable to load students");
        }

        students = await response.json();

        displayStudents(students);
        updateDashboard();

    } catch (error) {
        showMessage(error.message, true);
    }
}

// Display student records
function displayStudents(list) {

    table.innerHTML = "";

    if (list.length === 0) {
        table.innerHTML = `
            <tr>
                <td colspan="9">No students found</td>
            </tr>`;
        return;
    }

    list.forEach(student => {

        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${student.id}</td>
            <td>${safe(student.name)}</td>
            <td>${safe(student.email)}</td>
            <td>${safe(student.course)}</td>
            <td>${student.marks}</td>
            <td>${safe(student.grade)}</td>
            <td>${student.attendance}%</td>
            <td>${safe(student.status)}</td>
            <td>
                <button class="edit-btn"
                    data-action="edit"
                    data-id="${student.id}">
                    Update
                </button>

                <button class="delete-btn"
                    data-action="delete"
                    data-id="${student.id}">
                    Delete
                </button>
            </td>
        `;

        table.appendChild(row);
    });
}

// Dashboard statistics
function updateDashboard() {

    document.getElementById("totalStudents").textContent =
        students.length;

    const average = students.length
        ? students.reduce((sum, s) => sum + Number(s.marks), 0)
            / students.length
        : 0;

    document.getElementById("averageMarks").textContent =
        average.toFixed(2);

    const topper = students.length
        ? [...students].sort((a, b) => b.marks - a.marks)[0]
        : null;

    document.getElementById("topperName").textContent =
        topper ? `${topper.name} (${topper.marks})` : "-";
}

// Add student
form.addEventListener("submit", async function(event) {

    event.preventDefault();

    const data = {
        id: document.getElementById("id").value,
        name: document.getElementById("name").value.trim(),
        email: document.getElementById("email").value.trim(),
        course: document.getElementById("course").value.trim(),
        marks: document.getElementById("marks").value,
        attendance: document.getElementById("attendance").value,
        status: document.getElementById("status").value
    };

    try {
        const result = await postData("/add", data);

        showMessage(result.message);
        form.reset();

        await loadStudents();

    } catch (error) {
        showMessage(error.message, true);
    }
});

// Search by ID, name or course
search.addEventListener("input", function() {

    const keyword = this.value.toLowerCase().trim();

    const filtered = students.filter(student =>
        String(student.id).includes(keyword) ||
        student.name.toLowerCase().includes(keyword) ||
        student.course.toLowerCase().includes(keyword)
    );

    displayStudents(filtered);
});

// Update and delete buttons
table.addEventListener("click", async function(event) {

    const button = event.target.closest("button");

    if (!button) return;

    const id = Number(button.dataset.id);
    const action = button.dataset.action;

    try {

        if (action === "delete") {

            if (!confirm("Are you sure you want to delete this student?")) {
                return;
            }

            const result = await postData("/delete", { id });

            showMessage(result.message);
            await loadStudents();

        } else if (action === "edit") {

            const student = students.find(s => s.id === id);

            if (!student) return;

            const marks = prompt(
                "Enter new marks (0-100):",
                student.marks
            );

            if (marks === null) return;

            const attendance = prompt(
                "Enter new attendance (0-100):",
                student.attendance
            );

            if (attendance === null) return;

            if (
                marks.trim() === "" ||
                attendance.trim() === "" ||
                !Number.isFinite(Number(marks)) ||
                !Number.isFinite(Number(attendance)) ||
                Number(marks) < 0 || Number(marks) > 100 ||
                !Number.isInteger(Number(attendance)) ||
                Number(attendance) < 0 || Number(attendance) > 100
            ) {
                throw new Error("Enter valid marks and attendance");
            }

            const result = await postData("/update", {
                id,
                marks,
                attendance
            });

            showMessage(result.message);
            await loadStudents();
        }

    } catch (error) {
        showMessage(error.message, true);
    }
});

// Initial load
loadStudents();
