import os
from pathlib import Path

import psycopg2
import requests
from dotenv import load_dotenv

load_dotenv(Path(__file__).parent.parent.parent / ".env")


def before_all(context):
    context.base_url = os.getenv("BASE_URL", "http://localhost:8080")
    context.db_conn = psycopg2.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=int(os.getenv("DB_PORT", "5432")),
        dbname=os.getenv("DB_NAME", "jobboard"),
        user=os.getenv("DB_USER", "app"),
        password=os.getenv("DB_PASSWORD", "secret"),
    )


def before_scenario(context, scenario):
    context.job_id = None
    context.open_job_id = None
    context.closed_job_id = None
    context.application_ids = {}
    context.response = None

    if "requires_network_failure" in scenario.tags:
        try:
            r = requests.get(
                "https://jsonplaceholder.typicode.com/users/1", timeout=5
            )
            if r.status_code == 200:
                scenario.skip(
                    "JSONPlaceholder está disponible; este escenario requiere fallo de red"
                )
        except requests.exceptions.RequestException:
            pass


def after_scenario(context, scenario):
    conn = context.db_conn
    with conn.cursor() as cur:
        cur.execute("DELETE FROM applications")
        cur.execute("DELETE FROM jobs")
    conn.commit()


def after_all(context):
    if hasattr(context, "db_conn") and context.db_conn:
        context.db_conn.close()
