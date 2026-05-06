import { parseLLMResult, sanitizeLLMNames } from '../utils/llmParser';

describe('C3. Other Core Engine: llmParser.js', () => {

    // TC_FE_LLM_01 | LLM Parsing | parseLLMResult | Extracting raw standard valid JSON from markdown block
    test('TC_FE_LLM_01: parseLLMResult should extract raw valid JSON from inside markdown block', () => {
        const result = parseLLMResult("```json\n{\"tables\":[]}\n```");
        expect(result).toHaveProperty('tables');
    });

    // TC_FE_LLM_02 | LLM Parsing | parseLLMResult | Extracting non-markdown bare JSON string
    test('TC_FE_LLM_02: parseLLMResult should extract correctly from non-markdown bare JSON string', () => {
        const result = parseLLMResult("{\"tables\":[]}");
        expect(result).toHaveProperty('tables');
    });

    // TC_FE_LLM_03 | LLM Parsing | parseLLMResult | Hallucination error interception with malformed JSON
    test('TC_FE_LLM_03: parseLLMResult should intercept hallucination errors and throw Malformed LLM Data exception', () => {
        expect(() => parseLLMResult("{\"tables\":[")).toThrow("Malformed LLM Data Structure");
    });

    // TC_FE_LLM_04 | LLM Parsing | parseLLMResult | AI pre/post-text extraction using regex isolation
    test('TC_FE_LLM_04: parseLLMResult should use regex to isolate JSON bracket block from AI pre/post-text', () => {
        const result = parseLLMResult("Here is code: {\"tables\":[]} enjoy!");
        expect(result).toHaveProperty('tables');
    });

    // TC_FE_LLM_05 | LLM Sanitization | sanitizeLLMNames | Stripping unicode/accented letters to snake_case
    test('TC_FE_LLM_05: sanitizeLLMNames should strip unicode and accented letters into snake_case', () => {
        const result = sanitizeLLMNames("Khách Hàng");
        expect(result).toBe("khach_hang");
    });

});
