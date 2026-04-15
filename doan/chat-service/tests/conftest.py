"""Shared pytest fixtures for chatbot-service unit tests.

The fixtures in this file prepare a Flask test client and isolate the app from
external dependencies.

Rollback note:
- Because unit tests in this folder mock external writes, there is no real DB
  mutation to revert after execution.
"""

import importlib
import sys
from pathlib import Path

import pytest


CHAT_SERVICE_DIR = Path(__file__).resolve().parents[1]
if str(CHAT_SERVICE_DIR) not in sys.path:
    sys.path.insert(0, str(CHAT_SERVICE_DIR))


@pytest.fixture
def chat_app_module():
    """Return the imported Flask app module in testing mode."""

    module = importlib.import_module("app")
    module.app.config.update(TESTING=True)
    module.KAGGLE_API_URL = "https://fake-upstream.example"
    module.token_counter = None
    return module


@pytest.fixture
def client(chat_app_module):
    """Create a Flask test client bound to the isolated chatbot app module."""

    with chat_app_module.app.test_client() as client:
        yield client
