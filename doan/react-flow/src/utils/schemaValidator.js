export const validateTableName = (tableName, existingTables) => {
    if (tableName === "") return { isValid: false, message: "Table name cannot be empty" };

    if (/[^a-zA-Z0-9_ ]/.test(tableName)) return { isValid: false, message: "Table name contains invalid special characters" };
    if (tableName.length > 64) return { isValid: false, message: "Table name exceeds maximum length" };
    
    if (existingTables && existingTables.some(t => t.data && t.data.tableName === tableName)) {
        return { isValid: false, message: "Table name already exists" };
    }
    
    return { isValid: true, message: "" };
};

export const validateColumnName = (colName, existingColumns) => {
    if (colName === "") return { isValid: false, message: "Column name cannot be empty" };
    if (colName.includes("-")) return { isValid: false, message: "Invalid character in column name" };
    
    if (existingColumns && existingColumns.some(c => c.name === colName)) {
        return { isValid: false, message: "Column name already exists in this table" };
    }
    
    return { isValid: true, message: "" };
};

export const validateRelationship = (sourceCol, targetCol, sourceId, targetId) => {
    if (sourceId && targetId && sourceId === targetId) {
        return { isValid: false, message: "Cannot create self-referencing relationship" };
    }
    
    return { isValid: true, message: "" };
};
