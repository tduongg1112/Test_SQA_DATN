"""PyTest suite for ChatbotDatabaseTest.

These tests validate the database helper layer without opening a real MySQL
connection.

CheckDB / Rollback note:
- We verify database access by asserting calls on mocked connection/cursor
  objects.
- No real INSERT/DDL is executed against a live database.
- Therefore the database state remains exactly as before each test.
"""

from unittest.mock import Mock

import mysql.connector


def test_get_connection_with_include_db_returns_connection(mocker):
    # Test Case ID: UT_CS_DB_001
    # Arrange: replace mysql.connector.connect with a controllable mock.
    database_module = __import__("database")
    db = database_module.Database()
    connection = Mock()
    connect_mock = mocker.patch.object(database_module.mysql.connector, "connect", return_value=connection)

    # Act
    result = db.get_connection()

    # Assert: helper requests a connection to schema_chatbot.
    assert result is connection
    assert connect_mock.call_args.kwargs["database"] == "schema_chatbot"


def test_init_database_when_connections_valid_creates_schema_and_table(mocker):
    # Test Case ID: UT_CS_DB_002
    # Arrange: mock both bootstrap connection and schema connection.
    database_module = __import__("database")
    db = database_module.Database()

    init_cursor = Mock()
    init_connection = Mock()
    init_connection.cursor.return_value = init_cursor

    schema_cursor = Mock()
    schema_connection = Mock()
    schema_connection.cursor.return_value = schema_cursor
    schema_connection.is_connected.return_value = True

    get_connection_mock = mocker.patch.object(
        db,
        "get_connection",
        side_effect=[init_connection, schema_connection],
    )

    # Act
    result = db.init_database()

    # Assert: CREATE DATABASE and CREATE TABLE flow runs successfully.
    assert result is True
    assert get_connection_mock.call_args_list[0].kwargs == {"include_db": False}
    init_cursor.execute.assert_called_once()
    schema_cursor.execute.assert_called_once()
    schema_connection.commit.assert_called_once()
    schema_cursor.close.assert_called_once()
    schema_connection.close.assert_called_once()


def test_insert_metrics_with_valid_payload_persists_total_tokens(mocker):
    # Test Case ID: UT_CS_DB_003
    # Arrange: mock an INSERT path on the database connection.
    database_module = __import__("database")
    db = database_module.Database()

    cursor = Mock()
    connection = Mock()
    connection.cursor.return_value = cursor
    connection.is_connected.return_value = True
    mocker.patch.object(db, "get_connection", return_value=connection)

    # Act
    result = db.insert_metrics(
        input_text="input",
        output_text="output",
        input_tokens=120,
        output_tokens=80,
        ttft_ms=12.5,
        total_time_ms=50.0,
        status="success",
        error_message=None,
    )

    # Assert:
    # - "CheckDB" is satisfied by verifying the SQL execution payload.
    # - Rollback is not needed because the insert is fully mocked.
    assert result is True
    cursor.execute.assert_called_once()
    values = cursor.execute.call_args.args[1]
    assert values[4] == 200
    connection.commit.assert_called_once()
    cursor.close.assert_called_once()
    connection.close.assert_called_once()


def test_insert_metrics_when_connection_unavailable_returns_false(mocker):
    # Test Case ID: UT_CS_DB_004
    # Arrange: simulate unavailable database connection.
    database_module = __import__("database")
    db = database_module.Database()
    mocker.patch.object(db, "get_connection", return_value=None)

    # Act
    result = db.insert_metrics(
        input_text="input",
        output_text="output",
        input_tokens=1,
        output_tokens=1,
        ttft_ms=1.0,
        total_time_ms=2.0,
        status="success",
        error_message=None,
    )

    # Assert
    assert result is False
