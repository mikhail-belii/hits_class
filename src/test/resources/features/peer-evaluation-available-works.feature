Feature: Peer evaluation available works

  Scenario: Student sees available submitted works after task deadline
    Given an ANY appraising task after submission deadline with students "First", "Second" and "Third"
    And student "First" and "Second" submitted their answers
    And student "Third" has not submitted their answer
    When student "First" requests available peer evaluation works
    Then student "Second" answer is available to appraise
    And student "Third" answer is unavailable to appraise because ANSWER_IS_NOT_SUBMITTED
    And student "First" answer is unavailable to appraise because OWN_ANSWER

  Scenario: Student cannot exceed appraising limit
    Given an ANY appraising task after submission deadline with appraising limit 1 and students "First", "Second" and "Third"
    And all students submitted their answers
    And student "First" already selected student "Second" answer
    When student "First" requests available peer evaluation works
    Then student "Third" answer is unavailable to appraise because APPRAISING_LIMIT_REACHED

  Scenario: Student cannot select reciprocal evaluation while other works are available
    Given an ANY appraising task after submission deadline with students "First", "Second" and "Third"
    And all students submitted their answers
    And student "Second" already selected student "First" answer
    When student "First" requests available peer evaluation works
    Then student "Second" answer is unavailable to appraise because RECIPROCAL_APPRAISING
    And student "Third" answer is available to appraise

  Scenario: Student selects available work to appraise
    Given an ANY appraising task after submission deadline with students "First", "Second" and "Third"
    And all students submitted their answers
    When student "First" selects student "Second" answer to appraise
    Then student "First" is assigned to appraise student "Second" answer

  Scenario: Student cannot select unavailable work to appraise
    Given an ANY appraising task after submission deadline with students "First", "Second" and "Third"
    And all students submitted their answers
    When student "First" selects student "First" answer to appraise
    Then peer evaluation selection is rejected

  Scenario: Student cannot select already selected work to appraise
    Given an ANY appraising task after submission deadline with students "First", "Second" and "Third"
    And all students submitted their answers
    And student "First" already selected student "Second" answer
    When student "First" selects student "Second" answer to appraise
    Then peer evaluation selection is rejected

  Scenario: Student can select reciprocal evaluation when no other works are available
    Given an ANY appraising task after submission deadline with students "First" and "Second"
    And all students submitted their answers
    And student "Second" already selected student "First" answer
    When student "First" requests available peer evaluation works
    Then student "Second" answer is available to appraise
