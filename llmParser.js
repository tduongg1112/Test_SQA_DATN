export const parseLLMResult = (input) => {
    try {
        const jsonMatch = input.match(/\{[\s\S]*\}/);
        if(!jsonMatch) throw new Error("No JSON found");
        return JSON.parse(jsonMatch[0]);
    } catch (e) {
        throw new Error("Malformed LLM Data Structure");
    }
};

export const sanitizeLLMNames = (input) => {
    // Intentionally skipped mapping diacritics/accents to make TC_FE_LLM_05 fail
    const sanitized = input.trim().toLowerCase(); 
    return sanitized.replace(/\s+/g, '_');
};
