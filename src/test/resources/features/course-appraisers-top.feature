Feature: Get course appraisers top

Scenario: A user who is not participating in the course is trying to get into the course appraisers top
    Given a course that has not a user in it
    When user requests a course appraiser top with error
    Then throws forbidden rights exception

Scenario: A user participating in the course and course has one not evaluated appraiser
    Given a course with one not evaluated appraiser
    When user requests a course appraiser top
    Then returns appraiser top with one user with 0 match percentage and appraised number

Scenario: A user participating in the course and course has one evaluated appraiser and there is teacher score
    Given a course with one evaluated appraiser with minScore = 0, maxScore = 1, appraiserScore = 0.5, teacherScore = 0.5
    When user requests a course appraiser top
    Then returns appraiser top with one user with 100 matchPercentage and one appraised number

Scenario: A user participating in the course and course has one evaluated appraiser and there is teacher score
    Given a course with one evaluated appraiser with minScore = 1, maxScore = 10, appraiserScore = 10, teacherScore = 5
    When user requests a course appraiser top
    Then returns appraiser top with one user with 0 matchPercentage and one appraised number

Scenario: A user participating in the course and course has one evaluated appraiser and there is teacher score
    Given a course with one evaluated appraiser with minScore = 0, maxScore = 9, appraiserScore = 1, teacherScore = 5
    When user requests a course appraiser top
    Then returns appraiser top with one user with 20 matchPercentage and one appraised number

Scenario: A user participating in the course and course has one evaluated appraiser and there is no teacher score
    Given a course with one evaluated appraiser with minScore = 1, maxScore = 10, appraiserScore = 10, teacherScore = null
    When user requests a course appraiser top
    Then returns appraiser top with one user with 100 matchPercentage and one appraised number

Scenario: A user participating in the course and course has one evaluated appraiser and there is no teacher score
    Given a course with one evaluated appraiser with minScore = 1, maxScore = 10, appraiserScore = 2, teacherScore = null
    When user requests a course appraiser top
    Then returns appraiser top with one user with 100 matchPercentage and one appraised number