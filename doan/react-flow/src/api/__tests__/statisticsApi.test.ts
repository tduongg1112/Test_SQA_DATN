import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";

import { statisticsApi } from "../statisticsApi";

describe("StatisticsApiTest", () => {
  const originalFetch = global.fetch;

  beforeEach(() => {
    global.fetch = vi.fn() as unknown as typeof fetch;
  });

  afterEach(() => {
    vi.restoreAllMocks();
    global.fetch = originalFetch;
  });

  it("getStatistics_withValidApis_returnsAggregatedStatistics", async () => {
    // Test Case ID: UT_FE_STA_001
    const fetchMock = global.fetch as unknown as ReturnType<typeof vi.fn>;
    fetchMock
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ totalElements: 12 }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({ totalElements: 7 }),
      })
      .mockResolvedValueOnce({
        ok: true,
        json: async () => ({
          statistics: {
            total_tokens_processed: 1000,
            avg_ttft_ms: 123.4,
            avg_total_time_ms: 456.7,
            successful_requests: 9,
            total_requests: 10,
          },
        }),
      });

    const result = await statisticsApi.getStatistics();

    expect(result.totalAccounts).toBe(12);
    expect(result.totalDiagrams).toBe(7);
    expect(result.totalTokens).toBe(1000);
    expect(result.totalInputTokens).toBe(400);
    expect(result.totalOutputTokens).toBe(600);
    expect(result.avgTTFT).toBe(123.4);
    expect(result.avgTotalTime).toBe(456.7);
    expect(result.successfulRequests).toBe(9);
    expect(result.totalRequests).toBe(10);
  });

  it("getChatMetrics_whenApiError_throwsException", async () => {
    // Test Case ID: UT_FE_STA_002
    const fetchMock = global.fetch as unknown as ReturnType<typeof vi.fn>;
    fetchMock.mockResolvedValue({
      ok: false,
      json: async () => ({}),
    });

    await expect(statisticsApi.getChatMetrics()).rejects.toThrow(
      "Failed to fetch chat metrics"
    );
  });
});
