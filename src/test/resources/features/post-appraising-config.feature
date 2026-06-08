Feature: Post appraising configuration

  Scenario: Teacher creates ANY appraising task with student appraising limit
    Given a teacher can edit a course
    When teacher creates an ANY appraising task with student appraising limit 2
    Then saved task has student appraising limit 2

  Scenario: Teacher updates ANY appraising task student appraising limit
    Given a teacher can edit a course
    And an existing ANY appraising task with student appraising limit 2
    When teacher updates the student appraising limit to 3
    Then updated task has student appraising limit 3

  Scenario: Teacher cannot set non-positive student appraising limit
    Given a teacher can edit a course
    When teacher tries to create an ANY appraising task with student appraising limit 0
    Then post appraising configuration is rejected
