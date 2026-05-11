from datetime import datetime

from behave import given, then
from utils import resolve_placeholders


@given("the database is clean")
def step_clean_db(context):
    with context.db_conn.cursor() as cur:
        cur.execute("DELETE FROM applications")
        cur.execute("DELETE FROM jobs")
    context.db_conn.commit()
    context.job_id = None
    context.open_job_id = None
    context.closed_job_id = None
    context.application_ids = {}


@then("the response status is {status_code:d}")
def step_response_status(context, status_code):
    actual = context.response.status_code
    assert actual == status_code, (
        f"Expected HTTP {status_code}, got {actual}. Body: {context.response.text[:500]}"
    )


@then('the body contains "{field}" with value "{expected}"')
def step_body_field_string(context, field, expected):
    expected = resolve_placeholders(context, expected)
    data = context.response.json()
    actual = data.get(field)
    assert actual == expected, f"Expected {field}={expected!r}, got {actual!r}"


@then('the body contains "{field}" with value {expected:d}')
def step_body_field_int(context, field, expected):
    data = context.response.json()
    actual = data.get(field)
    assert actual == expected, f"Expected {field}={expected}, got {actual}"


@then('the body contains a positive numeric "id"')
def step_body_has_positive_id(context):
    data = context.response.json()
    id_val = data.get("id")
    assert isinstance(id_val, int) and id_val > 0, (
        f"Expected a positive integer 'id', got {id_val!r}"
    )


@then('the body contains "{field}" in ISO 8601 format')
def step_body_field_iso8601(context, field):
    data = context.response.json()
    value = data.get(field)
    assert value is not None, f"Field {field!r} missing from response"
    try:
        datetime.fromisoformat(value)
    except (ValueError, TypeError):
        raise AssertionError(
            f"Field {field}={value!r} is not a valid ISO 8601 datetime"
        )


@then('the body contains "total" greater than or equal to {minimum:d}')
def step_body_total_gte(context, minimum):
    data = context.response.json()
    actual = data.get("total")
    assert actual is not None, "Field 'total' missing from response"
    assert actual >= minimum, f"Expected total >= {minimum}, got {actual}"


@then('the body contains a non-empty "content" array')
def step_body_content_not_empty(context):
    data = context.response.json()
    content = data.get("content", [])
    assert isinstance(content, list) and len(content) > 0, (
        f"Expected non-empty 'content' array, got {content!r}"
    )


@then('the body contains a field error for "{field}" with message "{message}"')
def step_body_field_error(context, field, message):
    data = context.response.json()
    errors = data.get("errors", [])
    matching = [e for e in errors if e.get("field") == field]
    assert matching, f"No error found for field {field!r}. Errors: {errors}"
    actual_msg = matching[0].get("message")
    assert actual_msg == message, (
        f"Expected error message {message!r} for field {field!r}, got {actual_msg!r}"
    )


@then('the body contains "job_id" matching the created job ID')
def step_body_job_id_equals_created(context):
    data = context.response.json()
    actual = data.get("job_id")
    assert actual == context.job_id, f"Expected job_id={context.job_id}, got {actual}"


@then('the body contains "id" matching the created job ID')
def step_body_id_equals_created(context):
    data = context.response.json()
    actual = data.get("id")
    assert actual == context.job_id, f"Expected id={context.job_id}, got {actual}"


@then('the body contains "job_id" matching the open job ID')
def step_body_job_id_equals_open(context):
    data = context.response.json()
    actual = data.get("job_id")
    assert actual == context.open_job_id, (
        f"Expected job_id={context.open_job_id}, got {actual}"
    )


@then('the body contains "by_status" with key "{key}" equal to {expected:d}')
def step_body_by_status_key(context, key, expected):
    data = context.response.json()
    by_status = data.get("by_status", {})
    actual = by_status.get(key)
    assert actual == expected, f"Expected by_status.{key}={expected}, got {actual}"


@then("the body is an array with exactly {count:d} elements")
def step_body_array_count(context, count):
    data = context.response.json()
    assert isinstance(data, list), f"Expected JSON array, got {type(data).__name__}"
    assert len(data) == count, f"Expected {count} elements, got {len(data)}"


@then('the first element contains "{field}" with value "{expected}"')
def step_first_element_string(context, field, expected):
    data = context.response.json()
    assert isinstance(data, list) and len(data) > 0, "Response is not a non-empty array"
    actual = data[0].get(field)
    assert actual == expected, f"Expected first[{field}]={expected!r}, got {actual!r}"


@then('the first element contains "{field}" with value {expected:d}')
def step_first_element_int(context, field, expected):
    data = context.response.json()
    assert isinstance(data, list) and len(data) > 0, "Response is not a non-empty array"
    actual = data[0].get(field)
    assert actual == expected, f"Expected first[{field}]={expected}, got {actual}"


@then('the body contains "address" with fields street, suite, city, zipcode and geo')
def step_body_address_fields(context):
    data = context.response.json()
    address = data.get("address", {})
    for f in ["street", "suite", "city", "zipcode", "geo"]:
        assert f in address, f"address missing field: {f!r}"


@then('the body contains "company" with fields name, catchPhrase and bs')
def step_body_company_fields(context):
    data = context.response.json()
    company = data.get("company", {})
    for f in ["name", "catchPhrase", "bs"]:
        assert f in company, f"company missing field: {f!r}"
