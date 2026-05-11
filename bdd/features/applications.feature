@allure.label.epic:JobBoardAPI
@allure.label.feature:Applications
Feature: Application Management
  As a candidate
  I want to apply to open job offers
  In order to be considered by recruiting companies

  Background:
    Given the database is clean
    And there is an open job with title "Backend Engineer" in "Remote" at company "Acme Corp"
    And there is a closed job with title "Data Analyst" in "Remote" at company "Umbrella LLC"

  # ── POST /applications ────────────────────────────────────────────────────

  @allure.label.story:CreateApplication
  @allure.label.severity:critical
  Scenario: Applying to an open job successfully returns 201
    When I send POST /applications with body:
      """
      {
        "candidate_id": 1,
        "job_id": {open_job_id}
      }
      """
    Then the response status is 201
    And the body contains "candidate_id" with value 1
    And the body contains "job_id" matching the open job ID
    And the body contains "status" with value "pending"
    And the body contains "applied_at" in ISO 8601 format
    And the body contains "updated_at" in ISO 8601 format

  @allure.label.story:CreateApplication
  @allure.label.severity:normal
  Scenario: Applying with a non-existing candidate returns 404 with CANDIDATE_NOT_FOUND
    When I send POST /applications with body:
      """
      {
        "candidate_id": 9999,
        "job_id": {open_job_id}
      }
      """
    Then the response status is 404
    And the body contains "error_code" with value "CANDIDATE_NOT_FOUND"
    And the body contains "message" with value "No existe un candidato con id 9999."
    And the body contains "path" with value "/applications"

  @allure.label.story:CreateApplication
  @allure.label.severity:normal
  Scenario: Applying to a non-existing job returns 404 with JOB_NOT_FOUND
    When I send POST /applications with body:
      """
      {
        "candidate_id": 1,
        "job_id": 9999
      }
      """
    Then the response status is 404
    And the body contains "error_code" with value "JOB_NOT_FOUND"
    And the body contains "message" with value "No existe una oferta con id 9999."
    And the body contains "path" with value "/applications"

  @allure.label.story:CreateApplication
  @allure.label.severity:normal
  Scenario: Applying to a closed job returns 409 with JOB_CLOSED
    When I send POST /applications with body:
      """
      {
        "candidate_id": 1,
        "job_id": {closed_job_id}
      }
      """
    Then the response status is 409
    And the body contains "error_code" with value "JOB_CLOSED"
    And the body contains "path" with value "/applications"

  @allure.label.story:CreateApplication
  @allure.label.severity:normal
  Scenario: Applying with a null candidate_id returns 422 with VALIDATION_ERROR
    When I send POST /applications with body:
      """
      {
        "candidate_id": null,
        "job_id": {open_job_id}
      }
      """
    Then the response status is 422
    And the body contains "error_code" with value "VALIDATION_ERROR"
    And the body contains a field error for "candidateId" with message "El candidate_id es obligatorio."

  @allure.label.story:CreateApplication
  @allure.label.severity:normal
  Scenario: Applying twice to the same job returns 422 with DUPLICATE_APPLICATION
    Given candidate 2 has already applied to the open job
    When I send POST /applications with body:
      """
      {
        "candidate_id": 2,
        "job_id": {open_job_id}
      }
      """
    Then the response status is 422
    And the body contains "error_code" with value "DUPLICATE_APPLICATION"
    And the body contains "message" with value "El candidato 2 ya postuló a la oferta {open_job_id}."

  @requires_network_failure
  @allure.label.story:CreateApplication
  @allure.label.severity:minor
  Scenario: JSONPlaceholder unavailable when creating application returns 502
    Given JSONPlaceholder is not available
    When I send POST /applications with body:
      """
      {
        "candidate_id": 1,
        "job_id": {open_job_id}
      }
      """
    Then the response status is 502
    And the body contains "error_code" with value "SERVICE_UNAVAILABLE"
    And the body contains "path" with value "/applications"

  # ── PATCH /applications/{id}/status ──────────────────────────────────────

  @allure.label.story:UpdateStatus
  @allure.label.severity:critical
  Scenario: Updating application status from pending to accepted returns 200
    Given candidate 3 has already applied to the open job
    When I send PATCH /applications/{application_for_candidate_3}/status with body:
      """
      {
        "status": "accepted"
      }
      """
    Then the response status is 200
    And the body contains "status" with value "accepted"
    And the body contains "candidate_id" with value 3
    And the body contains "updated_at" in ISO 8601 format

  @allure.label.story:UpdateStatus
  @allure.label.severity:normal
  Scenario: Updating a non-existing application status returns 404 with APPLICATION_NOT_FOUND
    When I send PATCH /applications/9999/status with body:
      """
      {
        "status": "accepted"
      }
      """
    Then the response status is 404
    And the body contains "error_code" with value "APPLICATION_NOT_FOUND"
    And the body contains "message" with value "La postulación con id 9999 no existe."
    And the body contains "path" with value "/applications/9999/status"

  @allure.label.story:UpdateStatus
  @allure.label.severity:normal
  Scenario: Sending status "pending" returns 422 with VALIDATION_ERROR
    Given candidate 4 has already applied to the open job
    When I send PATCH /applications/{application_for_candidate_4}/status with body:
      """
      {
        "status": "pending"
      }
      """
    Then the response status is 422
    And the body contains "error_code" with value "VALIDATION_ERROR"
    And the body contains a field error for "status" with message "El status debe ser accepted o rejected"

  @allure.label.story:UpdateStatus
  @allure.label.severity:normal
  Scenario: Changing an already finalized application status returns 422 with INVALID_STATUS_TRANSITION
    Given candidate 5 has already applied to the open job
    And the application of candidate 5 has status "accepted"
    When I send PATCH /applications/{application_for_candidate_5}/status with body:
      """
      {
        "status": "rejected"
      }
      """
    Then the response status is 422
    And the body contains "error_code" with value "INVALID_STATUS_TRANSITION"
    And the body contains "path" with value "/applications/{application_for_candidate_5}/status"
