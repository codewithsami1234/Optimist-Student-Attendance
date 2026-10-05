# Subject / Course layer

Attendance is recorded **per course (subject)** within a class setup.

## Current hierarchy (Android Room app)

```text
Department (+ Shift: Morning | Evening)
  └── Semester (1–8)
        └── Section (optional name)
              └── Course / Subject (+ Course Code)
                    └── Students
                          └── Attendance session (date + lecture) → records
```

## Notes

- Class setups are created via **Add Department** (department, shift, semester,
  optional section, course name, course code).
- The same department **name** may exist for both Morning and Evening as
  separate department rows.
- Course codes are unique within a section.
- Session lookup uses class identifiers, subject, date, and lecture number.
- Re-saving attendance for the same class/date session updates that session
  rather than inventing a parallel “General” subject.

For Excel layouts and roll-number handling, see the main
[`README.md`](../README.md).
