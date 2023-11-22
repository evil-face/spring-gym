Feature: Trainee
  @trainee
  Scenario: User is trying to create new trainee account
    Given correct request to create new trainee
    When app receives the create trainee request
    Then user gets credentials in response
    And new trainer account is created

  @trainee
  Scenario: User is trying to create new trainee account
    Given request to create new trainee with empty fields
    When app receives the wrong create trainee request
    Then response has trainee request error description

  @trainee
  Scenario: User is trying to update existing trainee account
    Given correct request to update existing trainee
    When app receives the update trainee request
    Then existing trainee account is updated
    And user gets updated trainee account in response