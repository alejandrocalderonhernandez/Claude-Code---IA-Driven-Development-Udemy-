import json

import requests
from behave import given, when, use_step_matcher

from utils import resolve_placeholders


@given('there is a closed job with title "{title}" in "{location}" at company "{company}"')
def step_create_closed_job(context, title, location, company):
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
        f"Failed to create job. Expected 201, got {resp.status_code}: {resp.text}"
    )
    job_id = resp.json()["id"]

    resp = requests.patch(f"{context.base_url}/jobs/{job_id}/close")
    assert resp.status_code == 200, (
        f"Failed to close job {job_id}. Got {resp.status_code}: {resp.text}"
    )
    context.closed_job_id = job_id


# Behave strips the trailing ':' from step text when a docstring follows
@when("I send POST /applications with body")
def step_post_applications(context):
    body_text = resolve_placeholders(context, context.text.strip())
    body = json.loads(body_text)
    context.response = requests.post(
        f"{context.base_url}/applications",
        json=body,
    )


@given("candidate {candidate_id:d} has already applied to the open job")
def step_candidate_applied_to_open_job(context, candidate_id):
    resp = requests.post(
        f"{context.base_url}/applications",
        json={"candidate_id": candidate_id, "job_id": context.open_job_id},
    )
    assert resp.status_code == 201, (
        f"Failed to create application for candidate {candidate_id}. "
        f"Got {resp.status_code}: {resp.text}"
    )
    context.application_ids[candidate_id] = resp.json()["id"]


@given('the application of candidate {candidate_id:d} has status "{status}"')
def step_set_application_status(context, candidate_id, status):
    app_id = context.application_ids[candidate_id]
    resp = requests.patch(
        f"{context.base_url}/applications/{app_id}/status",
        json={"status": status},
    )
    assert resp.status_code == 200, (
        f"Failed to set status '{status}' on application {app_id}. "
        f"Got {resp.status_code}: {resp.text}"
    )


@when("I send PATCH /applications/9999/status with body")
def step_patch_application_status_9999(context):
    body = json.loads(context.text.strip())
    context.response = requests.patch(
        f"{context.base_url}/applications/9999/status",
        json=body,
    )


# Switch to regex to match literal {application_for_candidate_N} placeholders
use_step_matcher("re")


@when(r"I send PATCH /applications/\{application_for_candidate_(\d+)\}/status with body")
def step_patch_application_status_dynamic(context, candidate_num):
    candidate_id = int(candidate_num)
    app_id = context.application_ids[candidate_id]
    body = json.loads(context.text.strip())
    context.response = requests.patch(
        f"{context.base_url}/applications/{app_id}/status",
        json=body,
    )


use_step_matcher("parse")


@given("JSONPlaceholder is not available")
def step_jsonplaceholder_unavailable(context):
    # Precondition is verified in environment.before_scenario, which skips the
    # scenario when JSONPlaceholder is reachable. Reaching this step confirms
    # the service is unavailable.
    pass
