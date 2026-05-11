def resolve_placeholders(context, text: str) -> str:
    """Replace context-stored ID placeholders with their actual values."""
    replacements = {}

    if getattr(context, "job_id", None) is not None:
        replacements["{created_job_id}"] = str(context.job_id)
    if getattr(context, "open_job_id", None) is not None:
        replacements["{open_job_id}"] = str(context.open_job_id)
    if getattr(context, "closed_job_id", None) is not None:
        replacements["{closed_job_id}"] = str(context.closed_job_id)

    for cand_id, app_id in (context.application_ids or {}).items():
        replacements[f"{{application_for_candidate_{cand_id}}}"] = str(app_id)

    for placeholder, value in replacements.items():
        text = text.replace(placeholder, value)

    return text
