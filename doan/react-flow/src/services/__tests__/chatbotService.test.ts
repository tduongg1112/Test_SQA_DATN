import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { chatbotService } from "../chatbotService";

describe("ChatbotServiceTest", () => {
  const originalFetch = global.fetch;

  beforeEach(() => {
    global.fetch = vi.fn() as unknown as typeof fetch;
  });

  afterEach(() => {
    vi.restoreAllMocks();
    global.fetch = originalFetch;
  });

  it("parseResponse_withValidStructuredOutput_returnsParsedActions", () => {
    // Test Case ID: UT_FE_CBT_001
    // Test Case Name: parseResponse_withValidStructuredOutput_returnsParsedActions
    // Purpose: Verify that a valid chatbot text response is parsed into action,
    // message, create actions, delete actions, and summary fields.
    const output =
      'Tao bang users create: [{"name":"users","is_new":true,"attrs":[{"name":"id","type":"int","pk":true}],"fks":[]}] delete: [] tomtat: da tao xong';

    const result = chatbotService.parseResponse(output);

    expect(result.action).toBe("UPDATE");
    expect(result.message).toBe("Tao bang users");
    expect(result.create).toHaveLength(1);
    expect(result.create[0].name).toBe("users");
    expect(result.delete).toEqual([]);
    expect(result.tomtat).toBe("da tao xong");
  });

  it("parseResponse_withRefreshPrefix_setsActionRefresh", () => {
    // Test Case ID: UT_FE_CBT_002
    // Test Case Name: parseResponse_withRefreshPrefix_setsActionRefresh
    // Purpose: Verify that the `_REFRESH:` prefix is converted into the
    // REFRESH action while the structured payload is still parsed.
    const output =
      '_REFRESH: Lam moi so do create: [{"name":"users","is_new":false,"attrs":[],"fks":[]}] delete: [] tomtat: refresh';

    const result = chatbotService.parseResponse(output);

    expect(result.action).toBe("REFRESH");
    expect(result.message).toBe("Lam moi so do");
    expect(result.create[0].is_new).toBe(false);
  });

  it("sendMessage_whenApiReturnsValidOutput_returnsParsedResponse", async () => {
    // Test Case ID: UT_FE_CBT_003
    // Test Case Name: sendMessage_whenApiReturnsValidOutput_returnsParsedResponse
    // Purpose: Verify that `sendMessage` calls the configured API endpoint,
    // sends the expected payload, and parses a valid API output.
    const fetchMock = global.fetch as unknown as ReturnType<typeof vi.fn>;
    fetchMock.mockResolvedValue({
      ok: true,
      json: async () => ({
        output:
          '### Response: Tao bang users create: [{"name":"users","is_new":true,"attrs":[{"name":"id","type":"int","pk":true}],"fks":[]}] delete: [] tomtat: da tao',
      }),
    });

    const result = await chatbotService.sendMessage({
      diagram: { models: [] },
      question: "tao bang users",
      history: "",
      max_new_tokens: 2056,
      do_sample: false,
    });

    expect(result.action).toBe("UPDATE");
    expect(result.create[0].name).toBe("users");
    expect(fetchMock).toHaveBeenCalledTimes(1);
    const [url, options] = fetchMock.mock.calls[0];
    expect(url).toBe("http://localhost:8080/generate");
    expect(options.method).toBe("POST");
    expect(options.headers["Content-Type"]).toBe("application/json");
    expect(JSON.parse(options.body).question).toBe("tao bang users");
  });

  it("sendMessage_whenApiReturnsMalformedOutput_returnsFallbackMessage", async () => {
    // Test Case ID: UT_FE_CBT_004
    // Test Case Name: sendMessage_whenApiReturnsMalformedOutput_returnsFallbackMessage
    // Purpose: Verify that malformed AI output is handled by returning a safe
    // fallback response instead of throwing to the UI.
    const fetchMock = global.fetch as unknown as ReturnType<typeof vi.fn>;
    fetchMock.mockResolvedValue({
      ok: true,
      json: async () => ({
        output: '### Response: create: [{"name":"users",}] delete: [] tomtat: loi',
      }),
    });

    const result = await chatbotService.sendMessage({
      diagram: { models: [] },
      question: "tao bang users",
      history: "",
      max_new_tokens: 2056,
      do_sample: false,
    });

    expect(result.message).toContain("Xin lỗi");
    expect(result.create).toEqual([]);
    expect(result.delete).toEqual([]);
  });

  it("sendMessage_whenHttpError_returnsFallbackMessage", async () => {
    // Test Case ID: UT_FE_CBT_005
    // Test Case Name: sendMessage_whenHttpError_returnsFallbackMessage
    // Purpose: Verify that HTTP-level API failures return the chatbot fallback
    // message and do not expose an exception to callers.
    const fetchMock = global.fetch as unknown as ReturnType<typeof vi.fn>;
    fetchMock.mockResolvedValue({
      ok: false,
      status: 500,
      json: async () => ({}),
    });

    const result = await chatbotService.sendMessage({
      diagram: { models: [] },
      question: "tao bang users",
      history: "",
      max_new_tokens: 2056,
      do_sample: false,
    });

    expect(result.message).toContain("Xin lỗi");
    expect(result.tomtat).toContain("Không thể kết nối");
  });
});
