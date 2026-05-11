@allure.label.epic:JobBoardAPI
@allure.label.feature:Jobs
Feature: Job Offer Management
  As a recruiting company
  I want to publish and manage job offers
  In order to attract qualified candidates

  Background:
    Given the database is clean

  # ── POST /jobs ────────────────────────────────────────────────────────────

  @allure.label.story:CreateJob
  @allure.label.severity:critical
  Scenario: Creating a job successfully returns 201
    When I send POST /jobs with body:
      """
      {
        "title": "Senior Backend Developer",
        "description": "Looking for a developer with Java 25 and Spring Boot 4 experience.",
        "company": "TechCorp S.A.",
        "location": "Mexico City"
      }
      """
    Then the response status is 201
    And the body contains "title" with value "Senior Backend Developer"
    And the body contains "status" with value "open"
    And the body contains a positive numeric "id"
    And the body contains "created_at" in ISO 8601 format

  @allure.label.story:CreateJob
  @allure.label.severity:normal
  Scenario: Creating a job with an empty title returns 422
    When I send POST /jobs with body:
      """
      {
        "title": "",
        "description": "Valid description.",
        "company": "DataCo",
        "location": "Monterrey"
      }
      """
    Then the response status is 422
    And the body contains "error_code" with value "VALIDATION_ERROR"
    And the body contains a field error for "title" with message "El título no puede estar vacío."

  # ── GET /jobs ─────────────────────────────────────────────────────────────

  @allure.label.story:SearchJobs
  @allure.label.severity:critical
  Scenario: Searching jobs returns paginated results
    Given there is an open job with title "Data Analyst" in "Monterrey" at company "DataCo"
    When I send GET /jobs with parameters page=0 and size=20
    Then the response status is 200
    And the body contains "total" greater than or equal to 1
    And the body contains "page" with value 0
    And the body contains "size" with value 20
    And the body contains a non-empty "content" array

  # ── GET /jobs/{id} ────────────────────────────────────────────────────────

  @allure.label.story:GetJob
  @allure.label.severity:critical
  Scenario: Getting an existing job by ID returns 200
    Given there is an open job with title "QA Automation Engineer" in "Remote" at company "QualityCo"
    When I send GET /jobs/{created_job_id}
    Then the response status is 200
    And the body contains "title" with value "QA Automation Engineer"
    And the body contains "status" with value "open"

  @allure.label.story:GetJob
  @allure.label.severity:normal
  Scenario: Getting a non-existing job returns 404 with JOB_NOT_FOUND
    When I send GET /jobs/9999
    Then the response status is 404
    And the body contains "error_code" with value "JOB_NOT_FOUND"
    And the body contains "message" with value "No existe una oferta con id 9999."
    And the body contains "path" with value "/jobs/9999"

  # ── PATCH /jobs/{id}/close ────────────────────────────────────────────────

  @allure.label.story:CloseJob
  @allure.label.severity:critical
  Scenario: Closing an open job returns 200 with closed status
    Given there is an open job with title "Frontend Developer" in "Guadalajara" at company "WebCo"
    When I send PATCH /jobs/{created_job_id}/close
    Then the response status is 200
    And the body contains "status" with value "closed"
    And the body contains "id" matching the created job ID

  @allure.label.story:CloseJob
  @allure.label.severity:normal
  Scenario: Closing a non-existing job returns 404 with JOB_NOT_FOUND
    When I send PATCH /jobs/9999/close
    Then the response status is 404
    And the body contains "error_code" with value "JOB_NOT_FOUND"
    And the body contains "message" with value "No existe una oferta con id 9999."
    And the body contains "path" with value "/jobs/9999/close"

  # ── GET /jobs/{id}/report ─────────────────────────────────────────────────

  @allure.label.story:JobReport
  @allure.label.severity:normal
  Scenario: Getting a job report with applications returns 200
    Given there is an open job with title "DevOps Engineer" in "Remote" at company "CloudCo"
    And candidate 1 has applied to the created job
    And candidate 2 has applied to the created job
    When I send GET /jobs/{created_job_id}/report
    Then the response status is 200
    And the body contains "job_id" matching the created job ID
    And the body contains "title" with value "DevOps Engineer"
    And the body contains "total_applications" with value 2
    And the body contains "by_status" with key "pending" equal to 2

  @allure.label.story:JobReport
  @allure.label.severity:normal
  Scenario: Getting a report for a non-existing job returns 404 with JOB_NOT_FOUND
    When I send GET /jobs/9999/report
    Then the response status is 404
    And the body contains "error_code" with value "JOB_NOT_FOUND"
    And the body contains "message" with value "No existe una oferta con id 9999."
    And the body contains "path" with value "/jobs/9999/report"
