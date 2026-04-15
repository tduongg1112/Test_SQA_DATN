import { validateTableName, validateColumnName, validateRelationship } from '../utils/schemaValidator';

describe('A. Core Validator: schemaValidator.js', () => {

    // TC_FE_VAL_01 | Create/Update Table | validateTableName | Standard valid lowercase
    test('TC_FE_VAL_01: validateTableName should accept standard valid lowercase name', () => {
        const inputTableName = "users";
        const result = validateTableName(inputTableName);
        expect(result.isValid).toBe(true);
    });

    // TC_FE_VAL_02 | Create/Update Table | validateTableName | Standard valid snake_case
    test('TC_FE_VAL_02: validateTableName should accept valid snake_case name', () => {
        const inputTableName = "user_profiles";
        const result = validateTableName(inputTableName);
        expect(result.isValid).toBe(true);
    });

    // TC_FE_VAL_03 | Create/Update Table | validateTableName | Empty string check
    test('TC_FE_VAL_03: validateTableName should return error for empty string', () => {
        const inputTableName = "";
        const expectedErrorMsg = "Table name cannot be empty";
        const result = validateTableName(inputTableName);
        expect(result.isValid).toBe(false);
        expect(result.message).toBe(expectedErrorMsg);
    });

    // TC_FE_VAL_04 | Create/Update Table | validateTableName | Special characters check
    test('TC_FE_VAL_04: validateTableName should reject special characters', () => {
        const inputTableName = "users@123";
        const result = validateTableName(inputTableName);
        expect(result.isValid).toBe(false);
        expect(result.message).toBe("Table name contains invalid special characters");
    });

    // TC_FE_VAL_05 | Create/Update Table | validateTableName | White space check
    test('TC_FE_VAL_05: validateTableName should reject white spaces', () => {
        const inputTableName = "user data";
        const result = validateTableName(inputTableName);
        expect(result.isValid).toBe(false);
        expect(result.message).toBe("Table name cannot contain spaces");
    });

    // TC_FE_VAL_06 | Create/Update Table | validateTableName | Starts with number
    test('TC_FE_VAL_06: validateTableName should reject names starting with a number', () => {
        const inputTableName = "1users";
        const result = validateTableName(inputTableName);
        expect(result.isValid).toBe(false);
        expect(result.message).toBe("Table name cannot start with a number");
    });

    // TC_FE_VAL_07 | Create/Update Table | validateTableName | Exceeds max length limit
    test('TC_FE_VAL_07: validateTableName should reject names exceeding max length limit', () => {
        const inputTableName = "a".repeat(65);
        const result = validateTableName(inputTableName);
        expect(result.isValid).toBe(false);
        expect(result.message).toBe("Table name exceeds maximum length");
    });

    // TC_FE_VAL_08 | Create/Update Table | validateTableName | Duplicate table name
    test('TC_FE_VAL_08: validateTableName should reject duplicate table names', () => {
        const inputTableName = "orders";
        const existingTables = [{ data: { tableName: "orders" } }];
        const result = validateTableName(inputTableName, existingTables);
        expect(result.isValid).toBe(false);
        expect(result.message).toBe("Table name already exists");
    });

    // TC_FE_VAL_09 | Create/Update Column | validateColumnName | Valid column name
    test('TC_FE_VAL_09: validateColumnName should accept valid column name', () => {
        const inputColName = "id";
        const result = validateColumnName(inputColName);
        expect(result.isValid).toBe(true);
    });

    // TC_FE_VAL_10 | Create/Update Column | validateColumnName | Empty column name
    test('TC_FE_VAL_10: validateColumnName should reject empty name', () => {
        const inputColName = "";
        const result = validateColumnName(inputColName);
        expect(result.isValid).toBe(false);
        expect(result.message).toBe("Column name cannot be empty");
    });

    // TC_FE_VAL_11 | Create/Update Column | validateColumnName | Invalid column syntax
    test('TC_FE_VAL_11: validateColumnName should reject dashes', () => {
        const inputColName = "first-name";
        const result = validateColumnName(inputColName);
        expect(result.isValid).toBe(false);
        expect(result.message).toBe("Invalid character in column name");
    });

    // TC_FE_VAL_12 | Create/Update Column | validateColumnName | Duplicate column in same table
    test('TC_FE_VAL_12: validateColumnName should reject duplicate columns in the same table', () => {
        const inputColName = "email";
        const existingColumns = [{ name: "email" }];
        const result = validateColumnName(inputColName, existingColumns);
        expect(result.isValid).toBe(false);
        expect(result.message).toBe("Column name already exists in this table");
    });

    // TC_FE_VAL_13 | Connect Canvas Edges | validateRelationship | Valid FK to PK
    test('TC_FE_VAL_13: validateRelationship should accept valid FK to PK connection', () => {
        const sourceCol = { type: 'INT', isForeign: true };
        const targetCol = { type: 'INT', isPrimary: true };
        const result = validateRelationship(sourceCol, targetCol);
        expect(result.isValid).toBe(true);
    });

    // TC_FE_VAL_14 | Connect Canvas Edges | validateRelationship | Self-reference circular loop
    test('TC_FE_VAL_14: validateRelationship should reject self-referencing circular loops', () => {
        const sourceNodeId = "node_1";
        const targetNodeId = "node_1";
        const result = validateRelationship({}, {}, sourceNodeId, targetNodeId);
        expect(result.isValid).toBe(false);
        expect(result.message).toBe("Cannot create self-referencing relationship");
    });

    // TC_FE_VAL_15 | Connect Canvas Edges | validateRelationship | Data type mismatch
    test('TC_FE_VAL_15: validateRelationship should reject mismatched data types', () => {
        const sourceCol = { type: 'INT' };
        const targetCol = { type: 'VARCHAR' };
        const result = validateRelationship(sourceCol, targetCol, "node_1", "node_2");
        expect(result.isValid).toBe(false);
        expect(result.message).toBe("Foreign key data type must match Primary key");
    });

});
