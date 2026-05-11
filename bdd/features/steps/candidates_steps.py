import requests
from behave import when


@when("I send GET /candidates")
def step_get_candidates(context):
    context.response = requests.get(f"{context.base_url}/candidates")


@when("I send GET /candidates/1")
def step_get_candidate_1(context):
    context.response = requests.get(f"{context.base_url}/candidates/1")


@when("I send GET /candidates/9999")
def step_get_candidate_9999(context):
    context.response = requests.get(f"{context.base_url}/candidates/9999")
