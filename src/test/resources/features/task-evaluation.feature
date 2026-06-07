Feature: Task evaluation

  Scenario: Task answer is scored by students but not the teachers
    Given a task answer scored by students but not the teachers
    When student requests a task answer model
    Then task answer model with students score and isScoredByTeacher equals false

Scenario: Task answer is scored by teacher but not the students
  Given a task answer scored by teacher but not the students
  When student requests a task answer model
  Then task answer model with teacher score and isScoredByTeacher equals true

Scenario: Task answer is scored by teacher and students
  Given a task answer scored by teacher and students
  When student requests a task answer model
  Then task answer model with teacher score and isScoredByTeacher equals true