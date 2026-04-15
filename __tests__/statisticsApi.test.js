import { getStatistics, getChatMetrics } from '../services/statisticsApi';

describe('E. Integration: statisticsApi.js', () => {
    test('TC_FE_STA_01: getStatistics_withValidApis', async () => {
        const result = await getStatistics();
        expect(result).toHaveProperty('diagramMetrics');
        expect(result).toHaveProperty('chatMetrics');
    });

    test('TC_FE_STA_02: getChatMetrics_whenApiError', async () => {
        global.fetch = async () => ({ ok: false });
        await expect(getChatMetrics()).rejects.toThrow("Failed to fetch chat metrics");
    });
});
