Feature: Peer evaluation chain

  Scenario: Chain is generated for peer evaluation
    Given a course with 3 students
    When teacher creates a task with CHAIN appraising type
    Then 3 appraiser records are saved
    And no student evaluates themselves

  Scenario: Appraiser submits criteria scores and score is calculated
    Given a course with 3 students
    And a task with CHAIN appraising and criteria "Accuracy" range 0.0-5.0 and "Clarity" range 0.0-5.0
    And an appraiser assigned to student "No1" task answer
    When appraiser submits scores: "Accuracy"=4, "Clarity"=3
    Then 2 criteria score records are saved with values 4.0 and 3.0
    And appraiser score is calculated as 7.0

  Scenario: Appraiser finalizes evaluation and task answer score is updated
    Given a course with 3 students
    And a task with CHAIN appraising and criteria "Accuracy" range 0.0-5.0 and "Clarity" range 0.0-5.0
    And an appraiser assigned to student "No1" task answer with submitted scores 4 and 3
    When the appraiser evaluate task answer
    Then the appraiser score is set
    And the appraiser submittedAt is set
    And the task answer score is recalculated

  Scenario: Submitting criteria does not finalize the evaluation
    Given a course with 3 students
    And a task with CHAIN appraising and criteria "Accuracy" range 0.0-5.0 and "Clarity" range 0.0-5.0
    And an appraiser assigned to student "No1" task answer
    When appraiser submits scores: "Accuracy"=4, "Clarity"=3
    Then appraiser score is calculated as 7.0
    And the appraiser submittedAt is not set
