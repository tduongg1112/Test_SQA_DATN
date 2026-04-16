import { parseLLMResult, sanitizeLLMNames } from '../utils/llmParser';

describe('C. Other Core Engine: llmParser.js', () => {
    test('TC_FE_LLM_01: parseLLMResult should extract raw valid JSON from inside markdown block', () => {
        const result = parseLLMResult("```json\n{\"tables\":[]}\n```");
        expect(result).toHaveProperty('tables');
    });

    test('TC_FE_LLM_02: parseLLMResult should extract correctly from non-markdown bare JSON string', () => {
        const result = parseLLMResult("{\"tables\":[]}");
        expect(result).toHaveProperty('tables');
    });

    test('TC_FE_LLM_03: parseLLMResult should intercept hallucination errors and throw Malformed LLM Data exception', () => {
        expect(() => parseLLMResult("{\"tables\":[")).toThrow("Malformed LLM Data Structure");
    });

    test('TC_FE_LLM_04: parseLLMResult should use regex to isolate JSON bracket block from AI pre/post-text', () => {
        const result = parseLLMResult("Here is code: {\"tables\":[]} enjoy!");
        expect(result).toHaveProperty('tables');
    });

    test('TC_FE_LLM_05: sanitizeLLMNames should strip unicode and accented letters into snake_case', () => {
        const result = sanitizeLLMNames("Khách Hàng");
        expect(result).toBe("khach_hang");
    });

    test('TC_FE_LLM_06: sanitizeLLMNames should auto-trim edge white space boundaries', () => {
        const result = sanitizeLLMNames(" Orders  ");
        expect(result).toBe("orders");
    });
});
