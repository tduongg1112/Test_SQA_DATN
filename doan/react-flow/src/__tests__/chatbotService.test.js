import { parseResponse, sendMessage } from '../services/chatbotService';

describe('D. API Integration: chatbotService.js', () => {
    test('TC_FE_CBT_01: parseResponse_withValidStructuredOutput', () => {
        const result = parseResponse('{"create":[], "delete":[]}');
        expect(result.action).toBe('UPDATE');
    });

    test('TC_FE_CBT_02: parseResponse_withRefreshPrefix', () => {
        const result = parseResponse('_REFRESH_{"create":[]}');
        expect(result.action).toBe('UPDATE');
    });

    test('TC_FE_CBT_03: sendMessage_whenApiReturnsValidOutput', async () => {
        global.fetch = async () => ({
            ok: true,
            json: async () => ({ output: 'valid_data' })
        });
        const result = await sendMessage('hello', {});
        expect(result.action).toBe('UPDATE');
    });

    test('TC_FE_CBT_04: sendMessage_whenApiReturnsMalformedOut', async () => {
        global.fetch = async () => ({
            ok: true,
            json: async () => ({ }) // missing output logic
        });
        const result = await sendMessage('hello', {});
        expect(result.action).toBe('FALLBACK');
    });

    test('TC_FE_CBT_05: sendMessage_whenHttpError_returnsFallback', async () => {
        global.fetch = async () => ({
            ok: false,
            status: 500
        });
        const result = await sendMessage('hello', {});
        expect(result.action).toBe('FALLBACK');
    });
});
