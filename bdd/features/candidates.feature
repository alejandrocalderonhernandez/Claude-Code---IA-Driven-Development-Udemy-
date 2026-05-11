@allure.label.epic:JobBoardAPI
@allure.label.feature:Candidates
Feature: Candidate Lookup
  As the system
  I want to look up candidates from JSONPlaceholder
  In order to validate their existence before creating applications

  # ── GET /candidates ───────────────────────────────────────────────────────

  @allure.label.story:ListCandidates
  @allure.label.severity:normal
  Scenario: Listing all candidates successfully returns 200
    When I send GET /candidates
    Then the response status is 200
    And the body is an array with exactly 10 elements
    And the first element contains "id" with value 1
    And the first element contains "name" with value "Leanne Graham"
    And the first element contains "email" with value "Sincere@april.biz"
    And the first element contains "username" with value "Bret"

  @requires_network_failure
  @allure.label.story:ListCandidates
  @allure.label.severity:minor
  Scenario: Listing candidates when JSONPlaceholder is unavailable returns 502
    Given JSONPlaceholder is not available
    When I send GET /candidates
    Then the response status is 502
    And the body contains "error_code" with value "SERVICE_UNAVAILABLE"
    And the body contains "path" with value "/candidates"

  # ── GET /candidates/{id} ──────────────────────────────────────────────────

  @allure.label.story:GetCandidate
  @allure.label.severity:critical
  Scenario: Getting an existing candidate by ID returns 200
    When I send GET /candidates/1
    Then the response status is 200
    And the body contains "id" with value 1
    And the body contains "name" with value "Leanne Graham"
    And the body contains "username" with value "Bret"
    And the body contains "email" with value "Sincere@april.biz"
    And the body contains "phone" with value "1-770-736-8031 x56442"
    And the body contains "website" with value "hildegard.org"
    And the body contains "address" with fields street, suite, city, zipcode and geo
    And the body contains "company" with fields name, catchPhrase and bs

  @allure.label.story:GetCandidate
  @allure.label.severity:normal
  Scenario: Getting a non-existing candidate returns 404 with CANDIDATE_NOT_FOUND
    When I send GET /candidates/9999
    Then the response status is 404
    And the body contains "error_code" with value "CANDIDATE_NOT_FOUND"
    And the body contains "message" with value "No existe un candidato con id 9999."
    And the body contains "path" with value "/candidates/9999"

  @requires_network_failure
  @allure.label.story:GetCandidate
  @allure.label.severity:minor
  Scenario: Getting a candidate when JSONPlaceholder is unavailable returns 502
    Given JSONPlaceholder is not available
    When I send GET /candidates/1
    Then the response status is 502
    And the body contains "error_code" with value "SERVICE_UNAVAILABLE"
    And the body contains "path" with value "/candidates/1"
