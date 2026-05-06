"""PyTest suite for ChatbotAppTest.

This suite covers the Flask endpoints in ``app.py`` that belong to the
chatbot service. Every test maps directly to one row in
``CS_Chatbot_AI_TestCases.csv``.

CheckDB / Rollback note:
- These tests do not modify a real database.
- All database writes are mocked through ``db.insert_metrics``.
- Because no real persistence happens, the database state is unchanged after
  every test. That is the rollback strategy for this unit-test layer.
"""

from unittest.mock import Mock

import pytest
import requests


class NonStreamingResponse:
    """Small helper that mimics ``requests.Response`` for non-stream API tests."""

    def __init__(self, payload, status_code=200):
        self._payload = payload
        self.status_code = status_code

    def json(self):
        if isinstance(self._payload, Exception):
            raise self._payload
        return self._payload


class StreamingResponse:
    """Context-manager helper that mimics a streaming HTTP response."""

    def __init__(self, lines):
        self._lines = lines

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc, tb):
        return False

    def iter_lines(self):
        for line in self._lines:
            if isinstance(line, Exception):
                raise line
            yield line


def _set_token_counter(chat_app_module, mocker, side_effect=None):
    """Attach a mocked token counter to the Flask app module."""

    counter = mocker.Mock()
    counter.count_tokens.side_effect = side_effect or [11, 7]
    chat_app_module.token_counter = counter
    return counter


def _set_metrics_mock(chat_app_module, mocker):
    """Mock metrics persistence to avoid touching the real database."""

    metrics_mock = mocker.Mock(return_value=True)
    chat_app_module.db.insert_metrics = metrics_mock
    return metrics_mock


def _sample_generate_payload(include_history=True):
    """Build a minimal valid payload for the ``/generate`` endpoint."""

    payload = {
        "diagram": {"models": [{"id": "m1", "name": "User", "attributes": []}]},
        "question": "Tao bang user",
    }
    if include_history:
        payload["history"] = "Lan truoc toi da tao role"
    return payload


def test_health_check_when_tokenizer_loaded_returns_status_ok(client, chat_app_module, mocker):
    # Test Case ID: UT_CS_APP_001
    # Test Case Name: test_health_check_when_tokenizer_loaded_returns_status_ok
    # Purpose: Verify the health-check endpoint when the tokenizer is available.
    # Arrange: chatbot service starts with an available tokenizer.
    chat_app_module.token_counter = mocker.Mock()

    # Act: call the health-check endpoint.
    response = client.get("/")

    # Assert: service is healthy and reports tokenizer availability.
    assert response.status_code == 200
    data = response.get_json()
    assert data["status"] == "ok"
    assert data["tokenizer_loaded"] is True
    assert data["kaggle_url"] == chat_app_module.KAGGLE_API_URL


def test_health_check_when_tokenizer_unavailable_returns_tokenizer_false(client, chat_app_module):
    # Test Case ID: UT_CS_APP_002
    # Test Case Name: test_health_check_when_tokenizer_unavailable_returns_tokenizer_false
    # Purpose: Verify the health-check endpoint when the tokenizer is unavailable.
    # Arrange: tokenizer is intentionally unavailable.
    chat_app_module.token_counter = None

    # Act
    response = client.get("/")

    # Assert
    assert response.status_code == 200
    data = response.get_json()
    assert data["status"] == "ok"
    assert data["tokenizer_loaded"] is False


def test_generate_with_valid_response_key_returns_200_and_metrics(client, chat_app_module, mocker):
    # Test Case ID: UT_CS_APP_003
    # Test Case Name: test_generate_with_valid_response_key_returns_200_and_metrics
    # Purpose: Verify successful non-streaming generation when upstream returns the
    #          `response` key.
    # Arrange: upstream returns a valid payload via the `response` key.
    _set_token_counter(chat_app_module, mocker)
    metrics_mock = _set_metrics_mock(chat_app_module, mocker)
    mocker.patch.object(
        chat_app_module.requests,
        "post",
        return_value=NonStreamingResponse({"response": "AI answer"}),
    )

    # Act
    response = client.post("/generate", json=_sample_generate_payload())

    # Assert: endpoint succeeds and metrics are prepared for persistence.
    assert response.status_code == 200
    data = response.get_json()
    assert data["response"] == "AI answer"
    assert data["metrics"]["input_tokens"] == 11
    assert data["metrics"]["output_tokens"] == 7
    metrics_mock.assert_called_once()
    assert metrics_mock.call_args.kwargs["status"] == "success"
    assert metrics_mock.call_args.kwargs["total_time_ms"] >= 0


def test_generate_with_output_key_returns_200_and_preserves_output(client, chat_app_module, mocker):
    # Test Case ID: UT_CS_APP_004
    # Test Case Name: test_generate_with_output_key_returns_200_and_preserves_output
    # Purpose: Verify successful generation when upstream returns the `output` key instead
    #          of `response`.
    # Arrange: upstream uses the fallback `output` key.
    _set_token_counter(chat_app_module, mocker)
    metrics_mock = _set_metrics_mock(chat_app_module, mocker)
    mocker.patch.object(
        chat_app_module.requests,
        "post",
        return_value=NonStreamingResponse({"output": "### Response: create: [] delete: [] tomtat: done"}),
    )

    # Act
    response = client.post("/generate", json=_sample_generate_payload())

    # Assert
    assert response.status_code == 200
    data = response.get_json()
    assert data["output"].startswith("### Response:")
    assert data["metrics"]["total_tokens"] == 18
    assert metrics_mock.call_args.kwargs["output_text"].startswith("### Response:")


def test_generate_when_timeout_returns_504_and_stores_timeout_metric(client, chat_app_module, mocker):
    # Test Case ID: UT_CS_APP_005
    # Test Case Name: test_generate_when_timeout_returns_504_and_stores_timeout_metric
    # Purpose: Verify timeout handling when the upstream LLM/Kaggle request times out.
    # Arrange: upstream chatbot times out.
    _set_token_counter(chat_app_module, mocker, side_effect=[9])
    metrics_mock = _set_metrics_mock(chat_app_module, mocker)
    mocker.patch.object(chat_app_module.requests, "post", side_effect=requests.Timeout())

    # Act
    response = client.post("/generate", json=_sample_generate_payload())

    # Assert
    assert response.status_code == 504
    assert response.get_json()["error"] == "Request timeout"
    metrics_mock.assert_called_once()
    assert metrics_mock.call_args.kwargs["status"] == "timeout"
    assert metrics_mock.call_args.kwargs["output_tokens"] == 0


def test_generate_when_upstream_json_malformed_returns_500_and_stores_error_metric(
    client, chat_app_module, mocker
):
    # Test Case ID: UT_CS_APP_006
    # Test Case Name: test_generate_when_upstream_json_malformed_returns_500_and_stores_error_metric
    # Purpose: Verify error handling when the upstream response cannot be parsed as JSON.
    # Arrange: upstream response cannot be parsed as JSON.
    _set_token_counter(chat_app_module, mocker, side_effect=[8])
    metrics_mock = _set_metrics_mock(chat_app_module, mocker)
    mocker.patch.object(
        chat_app_module.requests,
        "post",
        return_value=NonStreamingResponse(ValueError("invalid json")),
    )

    # Act
    response = client.post("/generate", json=_sample_generate_payload())

    # Assert
    assert response.status_code == 500
    assert "invalid json" in response.get_json()["error"]
    assert metrics_mock.call_args.kwargs["status"] == "error"
    assert metrics_mock.call_args.kwargs["error_message"] == "invalid json"


def test_generate_when_history_missing_uses_default_history_value(client, chat_app_module, mocker):
    # Test Case ID: UT_CS_APP_007
    # Test Case Name: test_generate_when_history_missing_uses_default_history_value
    # Purpose: Verify the default history value when the request does not provide chat
    #          history.
    # Arrange: request omits the `history` field.
    _set_token_counter(chat_app_module, mocker)
    metrics_mock = _set_metrics_mock(chat_app_module, mocker)
    mocker.patch.object(
        chat_app_module.requests,
        "post",
        return_value=NonStreamingResponse({"text": "fallback text"}),
    )

    # Act
    response = client.post("/generate", json=_sample_generate_payload(include_history=False))

    # Assert
    assert response.status_code == 200
    input_text = metrics_mock.call_args.kwargs["input_text"]
    assert "lịch sử: None" in input_text


@pytest.mark.xfail(strict=True, reason="Current code returns 500 instead of validating invalid JSON body")
def test_generate_with_invalid_json_body_should_return_400(client):
    # Test Case ID: UT_CS_APP_008
    # Test Case Name: test_generate_with_invalid_json_body_should_return_400
    # Purpose: Verify validation behavior for an invalid JSON request body in the generate
    #          API.
    # This is an intentional fail case documenting a validation gap in app.py.
    response = client.post("/generate", data="", content_type="application/json")

    assert response.status_code == 400


@pytest.mark.xfail(strict=True, reason="Current code ignores upstream status_code and returns 200")
def test_generate_when_upstream_status_is_500_should_propagate_failure(client, chat_app_module, mocker):
    # Test Case ID: UT_CS_APP_009
    # Test Case Name: test_generate_when_upstream_status_is_500_should_propagate_failure
    # Purpose: Verify upstream failure propagation when upstream returns HTTP 500 with a
    #          JSON body.
    # This is an intentional fail case documenting that upstream HTTP status is ignored.
    _set_token_counter(chat_app_module, mocker)
    _set_metrics_mock(chat_app_module, mocker)
    mocker.patch.object(
        chat_app_module.requests,
        "post",
        return_value=NonStreamingResponse({"error": "upstream failed"}, status_code=500),
    )

    response = client.post("/generate", json=_sample_generate_payload())

    assert response.status_code >= 500


def test_generate_stream_with_prompt_returns_event_stream_and_stores_metrics(
    client, chat_app_module, mocker
):
    # Test Case ID: UT_CS_APP_010
    # Test Case Name: test_generate_stream_with_prompt_returns_event_stream_and_stores_metrics
    # Purpose: Verify successful streaming generation when the request uses the `prompt`
    #          field.
    # Arrange: upstream stream yields two valid SSE chunks.
    _set_token_counter(chat_app_module, mocker, side_effect=[5, 4])
    metrics_mock = _set_metrics_mock(chat_app_module, mocker)
    lines = [
        b'data: {"text":"Hello "}',
        b'data: {"text":"World"}',
    ]
    mocker.patch.object(
        chat_app_module.requests,
        "post",
        return_value=StreamingResponse(lines),
    )

    # Act
    response = client.post("/generate-stream", json={"prompt": "hello"})
    body = b"".join(response.response).decode("utf-8")

    # Assert
    assert response.status_code == 200
    assert response.mimetype == "text/event-stream"
    assert 'data: {"text":"Hello "}' in body
    assert 'data: {"text":"World"}' in body
    assert metrics_mock.call_args.kwargs["status"] == "success"
    assert metrics_mock.call_args.kwargs["output_text"] == "Hello World"
    assert metrics_mock.call_args.kwargs["ttft_ms"] >= 0


def test_generate_stream_when_chunk_malformed_skips_bad_chunk_and_continues(
    client, chat_app_module, mocker
):
    # Test Case ID: UT_CS_APP_011
    # Test Case Name: test_generate_stream_when_chunk_malformed_skips_bad_chunk_and_continues
    # Purpose: Verify that malformed stream chunks are skipped while later valid chunks
    #          continue.
    # Arrange: one malformed SSE chunk is mixed with valid chunks.
    _set_token_counter(chat_app_module, mocker, side_effect=[3, 2])
    metrics_mock = _set_metrics_mock(chat_app_module, mocker)
    lines = [
        b'data: {"text":"Good"}',
        b'data: {"text":"Broken"',
        b'data: {"text":" Day"}',
    ]
    mocker.patch.object(
        chat_app_module.requests,
        "post",
        return_value=StreamingResponse(lines),
    )

    # Act
    response = client.post("/generate-stream", json={"prompt": "hello"})
    body = b"".join(response.response).decode("utf-8")

    # Assert
    assert response.status_code == 200
    assert "Good" in body
    assert "Day" in body
    assert metrics_mock.call_args.kwargs["status"] == "success"
    assert metrics_mock.call_args.kwargs["output_text"] == "Good Day"


def test_generate_stream_when_stream_raises_exception_stores_error_metric(
    client, chat_app_module, mocker
):
    # Test Case ID: UT_CS_APP_012
    # Test Case Name: test_generate_stream_when_stream_raises_exception_stores_error_metric
    # Purpose: Verify stream error handling when upstream streaming raises an exception mid-
    #          stream.
    # Arrange: upstream stream breaks after the first chunk.
    _set_token_counter(chat_app_module, mocker, side_effect=[3])
    metrics_mock = _set_metrics_mock(chat_app_module, mocker)
    lines = [
        b'data: {"text":"Hi"}',
        RuntimeError("stream broken"),
    ]
    mocker.patch.object(
        chat_app_module.requests,
        "post",
        return_value=StreamingResponse(lines),
    )

    # Act
    response = client.post("/generate-stream", json={"prompt": "hello"})
    body = b"".join(response.response).decode("utf-8")

    # Assert
    assert response.status_code == 200
    assert "Hi" in body
    assert metrics_mock.call_args.kwargs["status"] == "error"
    assert metrics_mock.call_args.kwargs["error_message"] == "stream broken"


def test_set_kaggle_url_with_valid_url_updates_global_url(client, chat_app_module):
    # Test Case ID: UT_CS_APP_013
    # Test Case Name: test_set_kaggle_url_with_valid_url_updates_global_url
    # Purpose: Verify successful update of the Kaggle API URL.
    # Act
    response = client.post("/set-kaggle-url", json={"url": "https://abc.ngrok.app/"})

    # Assert
    assert response.status_code == 200
    assert chat_app_module.KAGGLE_API_URL == "https://abc.ngrok.app"
    assert "https://abc.ngrok.app" in response.get_json()["message"]


def test_get_metrics_with_default_limit_returns_metrics_and_statistics(client, chat_app_module):
    # Test Case ID: UT_CS_APP_014
    # Test Case Name: test_get_metrics_with_default_limit_returns_metrics_and_statistics
    # Purpose: Verify metrics listing with the default limit.
    # Arrange: mock DB read calls for metrics list and aggregated statistics.
    cursor = Mock()
    cursor.fetchall.return_value = [{"id": 1, "status": "success"}]
    cursor.fetchone.return_value = {"total_requests": 1, "successful_requests": 1}
    connection = Mock()
    connection.cursor.return_value = cursor
    chat_app_module.db.get_connection = Mock(return_value=connection)

    # Act
    response = client.get("/metrics")

    # Assert: the endpoint reads metrics correctly.
    assert response.status_code == 200
    data = response.get_json()
    assert data["metrics"] == [{"id": 1, "status": "success"}]
    assert data["statistics"]["total_requests"] == 1
    first_execute_args = cursor.execute.call_args_list[0][0]
    assert first_execute_args[1] == (10,)
    cursor.close.assert_called_once()
    connection.close.assert_called_once()


def test_get_metric_detail_when_record_exists_returns_record(client, chat_app_module):
    # Test Case ID: UT_CS_APP_015
    # Test Case Name: test_get_metric_detail_when_record_exists_returns_record
    # Purpose: Verify metric detail retrieval when the requested metric record exists.
    # Arrange: mock DB detail query for a known metric row.
    cursor = Mock()
    cursor.fetchone.return_value = {"id": 1, "status": "success"}
    connection = Mock()
    connection.cursor.return_value = cursor
    chat_app_module.db.get_connection = Mock(return_value=connection)

    # Act
    response = client.get("/metrics/1")

    # Assert
    assert response.status_code == 200
    assert response.get_json()["id"] == 1
    cursor.execute.assert_called_once()
    cursor.close.assert_called_once()
    connection.close.assert_called_once()
