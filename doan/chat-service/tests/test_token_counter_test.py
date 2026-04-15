"""PyTest suite for TokenCounterTest.

This suite validates token counting behavior in isolation. The HuggingFace
tokenizer is never loaded for real inside these unit tests.
"""

import importlib

import pytest


def test_count_tokens_with_valid_tokenizer_returns_token_count(mocker):
    # Test Case ID: UT_CS_TKN_001
    # Arrange: replace tokenizer with a mock that returns five tokens.
    token_counter_module = importlib.import_module("token_counter")
    fake_tokenizer = mocker.Mock()
    fake_tokenizer.tokenize.return_value = ["a", "b", "c", "d", "e"]
    counter = token_counter_module.TokenCounter.__new__(token_counter_module.TokenCounter)
    counter.tokenizer = fake_tokenizer

    # Act
    result = token_counter_module.TokenCounter.count_tokens(counter, "hello world")

    # Assert
    assert result == 5


def test_count_tokens_when_text_empty_returns_zero():
    # Test Case ID: UT_CS_TKN_002
    # Arrange
    token_counter_module = importlib.import_module("token_counter")
    counter = token_counter_module.TokenCounter.__new__(token_counter_module.TokenCounter)
    counter.tokenizer = None

    # Assert directly for the two empty-input boundary values.
    assert token_counter_module.TokenCounter.count_tokens(counter, "") == 0
    assert token_counter_module.TokenCounter.count_tokens(counter, None) == 0


def test_count_tokens_when_tokenizer_throws_uses_fallback_estimate(mocker):
    # Test Case ID: UT_CS_TKN_003
    # Arrange: tokenizer raises exception, forcing fallback estimation path.
    token_counter_module = importlib.import_module("token_counter")
    fake_tokenizer = mocker.Mock()
    fake_tokenizer.tokenize.side_effect = RuntimeError("tokenizer broken")
    counter = token_counter_module.TokenCounter.__new__(token_counter_module.TokenCounter)
    counter.tokenizer = fake_tokenizer

    # Act
    result = token_counter_module.TokenCounter.count_tokens(counter, "a b c d")

    # Assert
    assert result == 5.2


@pytest.mark.xfail(strict=True, reason="Current constructor may leave self.tokenizer undefined when HF token is missing")
def test_init_without_hf_token_should_set_tokenizer_to_none(mocker, monkeypatch):
    # Test Case ID: UT_CS_TKN_004
    # This is an intentional fail case documenting a constructor weakness.
    token_counter_module = importlib.import_module("token_counter")
    monkeypatch.delenv("HF_TOKEN", raising=False)
    from_pretrained = mocker.patch.object(token_counter_module.AutoTokenizer, "from_pretrained")

    counter = token_counter_module.TokenCounter(hf_token=None)

    from_pretrained.assert_not_called()
    assert hasattr(counter, "tokenizer")
    assert counter.tokenizer is None
