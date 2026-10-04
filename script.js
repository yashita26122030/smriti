// ==========================================
// SMRITI - NOTES, REMINDERS & OTHER
// ==========================================


// ==========================================
// NOTES DATA
// ==========================================

let notes = JSON.parse(localStorage.getItem("smritiNotes"));

if (!Array.isArray(notes) || notes.length === 0) {

    notes = [
        {
            title: "DBMS",
            content: "Study normalization and SQL queries",
            pinned: true,
            archived: false
        },
        {
            title: "Website Ideas",
            content: "Build a reminders and notes application",
            pinned: false,
            archived: false
        }
    ];

    localStorage.setItem(
        "smritiNotes",
        JSON.stringify(notes)
    );
}


// ==========================================
// REMINDERS DATA
// ==========================================

const reminders = [
    {
        title: "Submit assignment",
        time: "7:00 PM",
        priority: "High"
    },
    {
        title: "Study JavaScript",
        time: "9:00 PM",
        priority: "Medium"
    }
];


// ==========================================
// GET HTML ELEMENTS
// ==========================================

const notesContainer =
    document.getElementById("notesContainer");

const searchInput =
    document.getElementById("search");

const reminderList =
    document.getElementById("reminderList");

const noteModal =
    document.getElementById("noteModal");

const addNoteButton =
    document.getElementById("addNote");

const closeNoteButton =
    document.getElementById("closeNoteModal");

const saveNoteButton =
    document.getElementById("saveNote");

const archiveMenuButton =
    document.getElementById("archiveButton");

const notesMenuButton =
    document.getElementById("notesButton");

const remindersButton =
    document.getElementById("remindersButton");

const otherButton =
    document.getElementById("otherButton");

const homeButton =
    document.getElementById("homeButton");

const pinnedButton =
    document.getElementById("pinnedButton");


// ==========================================
// DISPLAY NOTES
// ==========================================

function displayNotes(
    notesToDisplay = notes,
    showArchived = false
) {

    if (!notesContainer) {
        return;
    }

    notesContainer.innerHTML = "";

    notesToDisplay
        .filter(function (note) {

            return showArchived
                ? note.archived
                : !note.archived;

        })
        .forEach(function (note) {

            const noteElement =
                document.createElement("div");

            noteElement.classList.add("note");


            noteElement.innerHTML = `
                <h3>
                    ${note.pinned ? "📌 " : ""}
                    ${note.title}
                </h3>

                <p>${note.content}</p>

                <button class="pin-note">
                    ${note.pinned ? "📌 Unpin" : "📌 Pin"}
                </button>

                <button class="edit-note">
                    ✏️ Edit
                </button>

                <button class="delete-note">
                    🗑️ Delete
                </button>

                <button class="archive-note">
                    ${note.archived
                        ? "↩️ Unarchive"
                        : "🗄️ Archive"}
                </button>
            `;


            notesContainer.appendChild(noteElement);


            // --------------------------------
            // BUTTONS
            // --------------------------------

            const deleteButton =
                noteElement.querySelector(".delete-note");

            const editButton =
                noteElement.querySelector(".edit-note");

            const pinButton =
                noteElement.querySelector(".pin-note");

            const archiveButton =
                noteElement.querySelector(".archive-note");


            // --------------------------------
            // DELETE
            // --------------------------------

            deleteButton.addEventListener(
                "click",
                function () {

                    notes = notes.filter(
                        function (item) {
                            return item !== note;
                        }
                    );

                    localStorage.setItem(
                        "smritiNotes",
                        JSON.stringify(notes)
                    );

                    displayNotes();
                }
            );


            // --------------------------------
            // EDIT
            // --------------------------------

            editButton.addEventListener(
                "click",
                function () {

                    const newTitle =
                        prompt(
                            "Edit note title:",
                            note.title
                        );

                    const newContent =
                        prompt(
                            "Edit note:",
                            note.content
                        );


                    if (
                        newTitle !== null &&
                        newContent !== null
                    ) {

                        note.title = newTitle;
                        note.content = newContent;

                        localStorage.setItem(
                            "smritiNotes",
                            JSON.stringify(notes)
                        );

                        displayNotes();
                    }
                }
            );


            // --------------------------------
            // PIN / UNPIN
            // --------------------------------

            pinButton.addEventListener(
                "click",
                function () {

                    note.pinned =
                        !note.pinned;

                    localStorage.setItem(
                        "smritiNotes",
                        JSON.stringify(notes)
                    );

                    displayNotes();
                }
            );


            // --------------------------------
            // ARCHIVE / UNARCHIVE
            // --------------------------------

            archiveButton.addEventListener(
                "click",
                function () {

                    note.archived =
                        !note.archived;

                    localStorage.setItem(
                        "smritiNotes",
                        JSON.stringify(notes)
                    );

                    displayNotes();
                }
            );

        });
}


// ==========================================
// DISPLAY REMINDERS
// ==========================================

function displayReminders() {

    if (!notesContainer) {
        return;
    }

    notesContainer.innerHTML = "";


    // Title

    const heading =
        document.createElement("h2");

    heading.textContent =
        "🔔 Reminders";

    notesContainer.appendChild(heading);


    // Reminder cards

    reminders.forEach(
        function (reminder) {

            const element =
                document.createElement("div");

            element.classList.add("reminder");


            element.innerHTML = `
                <div>
                    <strong>
                        ${reminder.title}
                    </strong>

                    <p>
                        ⏰ ${reminder.time}
                    </p>
                </div>

                <span>
                    ${reminder.priority}
                </span>
            `;


            notesContainer.appendChild(element);
        }
    );
}


// ==========================================
// DISPLAY OTHER
// ==========================================

function displayOther() {

    if (!notesContainer) {
        return;
    }

    notesContainer.innerHTML = `

        <div class="note">

            <h2>✨ Other</h2>

            <p>
                More SMRITI features will be added here.
            </p>

            <button>
                ⚙️ Settings
            </button>

        </div>

    `;
}


// ==========================================
// HOME BUTTON
// ==========================================

if (homeButton) {

    homeButton.addEventListener(
        "click",
        function () {

            displayNotes();

            homeButton.classList.add("active");

            if (notesMenuButton) {
                notesMenuButton.classList.remove("active");
            }

            if (remindersButton) {
                remindersButton.classList.remove("active");
            }

            if (archiveMenuButton) {
                archiveMenuButton.classList.remove("active");
            }

            if (otherButton) {
                otherButton.classList.remove("active");
            }

            if (pinnedButton) {
                pinnedButton.classList.remove("active");
            }

        }
    );
}


// ==========================================
// NOTES BUTTON
// ==========================================

if (notesMenuButton) {

    notesMenuButton.addEventListener(
        "click",
        function () {

            displayNotes();

            notesMenuButton.classList.add("active");

            if (archiveMenuButton) {
                archiveMenuButton.classList.remove("active");
            }

            if (remindersButton) {
                remindersButton.classList.remove("active");
            }

            if (otherButton) {
                otherButton.classList.remove("active");
            }

            if (homeButton) {
                homeButton.classList.remove("active");
            }

        }
    );
}


// ==========================================
// ARCHIVE BUTTON
// ==========================================

if (archiveMenuButton) {

    archiveMenuButton.addEventListener(
        "click",
        function () {

            const archivedNotes =
                notes.filter(
                    function (note) {
                        return note.archived;
                    }
                );


            displayNotes(
                archivedNotes,
                true
            );


            archiveMenuButton.classList.add(
                "active"
            );


            if (notesMenuButton) {
                notesMenuButton.classList.remove(
                    "active"
                );
            }

            if (remindersButton) {
                remindersButton.classList.remove(
                    "active"
                );
            }

            if (otherButton) {
                otherButton.classList.remove(
                    "active"
                );
            }

        }
    );
}


// ==========================================
// REMINDERS BUTTON
// ==========================================

if (remindersButton) {

    remindersButton.addEventListener(
        "click",
        function () {

            displayReminders();

            remindersButton.classList.add(
                "active"
            );


            if (notesMenuButton) {
                notesMenuButton.classList.remove(
                    "active"
                );
            }

            if (archiveMenuButton) {
                archiveMenuButton.classList.remove(
                    "active"
                );
            }

            if (otherButton) {
                otherButton.classList.remove(
                    "active"
                );
            }

            if (homeButton) {
                homeButton.classList.remove(
                    "active"
                );
            }

        }
    );
}


// ==========================================
// OTHER BUTTON
// ==========================================

if (otherButton) {

    otherButton.addEventListener(
        "click",
        function () {

            displayOther();

            otherButton.classList.add(
                "active"
            );


            if (notesMenuButton) {
                notesMenuButton.classList.remove(
                    "active"
                );
            }

            if (archiveMenuButton) {
                archiveMenuButton.classList.remove(
                    "active"
                );
            }

            if (remindersButton) {
                remindersButton.classList.remove(
                    "active"
                );
            }

            if (homeButton) {
                homeButton.classList.remove(
                    "active"
                );
            }

        }
    );
}


// ==========================================
// PINNED BUTTON
// ==========================================

if (pinnedButton) {

    pinnedButton.addEventListener(
        "click",
        function () {

            const pinnedNotes =
                notes.filter(
                    function (note) {
                        return (
                            note.pinned &&
                            !note.archived
                        );
                    }
                );


            displayNotes(pinnedNotes);


            pinnedButton.classList.add(
                "active"
            );


            if (notesMenuButton) {
                notesMenuButton.classList.remove(
                    "active"
                );
            }

            if (archiveMenuButton) {
                archiveMenuButton.classList.remove(
                    "active"
                );
            }

            if (remindersButton) {
                remindersButton.classList.remove(
                    "active"
                );
            }

        }
    );
}


// ==========================================
// ADD NOTE - OPEN MODAL
// ==========================================

if (addNoteButton) {

    addNoteButton.addEventListener(
        "click",
        function () {

            if (noteModal) {
                noteModal.style.display = "flex";
            }

        }
    );
}


// ==========================================
// CLOSE NOTE MODAL
// ==========================================

if (closeNoteButton) {

    closeNoteButton.addEventListener(
        "click",
        function () {

            if (noteModal) {
                noteModal.style.display = "none";
            }

        }
    );
}


// ==========================================
// SAVE NOTE
// ==========================================

if (saveNoteButton) {

    saveNoteButton.addEventListener(
        "click",
        function () {

            const titleInput =
                document.getElementById("noteTitle");

            const contentInput =
                document.getElementById("noteContent");


            const title =
                titleInput.value.trim();

            const content =
                contentInput.value.trim();


            if (
                title === "" ||
                content === ""
            ) {

                alert(
                    "Please enter both a title and a note."
                );

                return;
            }


            notes.push({

                title: title,

                content: content,

                pinned: false,

                archived: false

            });


            localStorage.setItem(
                "smritiNotes",
                JSON.stringify(notes)
            );


            displayNotes();


            titleInput.value = "";
            contentInput.value = "";


            if (noteModal) {
                noteModal.style.display = "none";
            }

        }
    );
}


// ==========================================
// SEARCH
// ==========================================

if (searchInput) {

    searchInput.addEventListener(
        "input",
        function () {

            const searchText =
                searchInput.value.toLowerCase();


            const filteredNotes =
                notes.filter(
                    function (note) {

                        return (

                            note.title
                                .toLowerCase()
                                .includes(searchText)

                            ||

                            note.content
                                .toLowerCase()
                                .includes(searchText)

                        );

                    }
                );


            displayNotes(filteredNotes);
        }
    );
}


// ==========================================
// START APP
// ==========================================

displayNotes();