Feature: Authentication
  @auth
  Scenario: User is trying to log in
    Given request to log in with correct credentials
    When app receives the login request
    Then user gets token in response

  @auth
  Scenario: User is trying to log in
    Given request to log in with incorrect credentials
    When app receives the login request
    Then user doesn't get auth token

  @auth
  Scenario: User is trying to change password
    Given request to change password with correct credentials
    When app receives the change password request
    Then user's password is successfully changed

  @auth
  Scenario: User is trying to change password
    Given request to change password with empty fields
    When app receives the change password request
    Then response has error description

  @auth
  Scenario: User is trying to change password
    Given request to change password with incorrect creds
    When app receives the change password request
    Then user's password is not changed

  @auth
  Scenario: User is trying to get account info
    Given request to access account info with correct credentials
    When app receives the get info request
    Then response has user account info

  @auth
  Scenario: User is trying to get account info
    Given request to access account info with correct credentials
    When app receives the get another account info request
    Then response has forbidden error