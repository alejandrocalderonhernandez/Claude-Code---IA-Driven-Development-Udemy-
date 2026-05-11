import json

import requests
from behave import given, when, use_step_matcher

from utils import resolve_placeholders


@given('there is an open job with title "{title}" in "{location}" at company "{company}"')
def step_create_open_job(context, title, location, company):
    resp = requests.post(
        f"{context.base_url}/jobs",
        json={
            "title": title,
            "description": f"Job description for {title}.",
            "company": company,
            "location": location,
        },
    )
    assert resp.status_code == 201, (
        f"Failed to create open job. Expected 201, got {resp.status_code}: {resp.text}"
    )
    job_id = resp.json()["id"]
    context.open_job_id = job_id
    context.job_id = job_id


# Behave strips the trailing ':' from step text when a docstring follows
@when("I send POST /jobs with body")
def step_post_jobs(context):
    body = json.loads(context.text.strip())
    context.response = requests.post(
        f"{context.base_url}/jobs",
        json=body,
    )


@when("I send GET /jobs with parameters page=0 and size=20")
def step_get_jobs_paged(context):
    context.response = requests.get(
        f"{context.base_url}/jobs",
        params={"page": 0, "size": 20},
    )


@when("I send GET /jobs/9999")
def step_get_job_9999(context):
    context.response = requests.get(f"{context.base_url}/jobs/9999")


@when("I send PATCH /jobs/9999/close")
def step_patch_close_job_9999(context):
    context.response = requests.patch(f"{context.base_url}/jobs/9999/close")


@when("I send GET /jobs/9999/report")
def step_get_job_report_9999(context):
    context.response = requests.get(f"{context.base_url}/jobs/9999/report")


# Switch to regex matcher for steps that contain literal {placeholders}
use_step_matcher("re")


@when(r"I send GET /jobs/\{created_job_id\}")
def step_get_job_by_created_id(context):
    context.response = requests.get(f"{context.base_url}/jobs/{context.job_id}")


@when(r"I send PATCH /jobs/\{created_job_id\}/close")
def step_patch_close_job_by_created_id(context):
    context.response = requests.patch(
        f"{context.base_url}/jobs/{context.job_id}/close"
    )


@when(r"I send GET /jobs/\{created_job_id\}/report")
def step_get_job_report_by_created_id(context):
    context.response = requests.get(
        f"{context.base_url}/jobs/{context.job_id}/report"
    )


use_step_matcher("parse")


@given("candidate {candidate_id:d} has applied to the created job")
def step_candidate_applied_to_created_job(context, candidate_id):
    resp = requests.post(
        f"{context.base_url}/applications",
        json={"candidate_id": candidate_id, "job_id": context.job_id},
    )
    assert resp.status_code == 201, (
        f"Failed to create application for candidate {candidate_id}. "
        f"Got {resp.status_code}: {resp.text}"
    )
    context.application_ids[candidate_id] = resp.json()["id"]
