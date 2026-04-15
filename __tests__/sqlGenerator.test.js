import { generateSQL } from '../utils/sqlGenerator';

describe('C1. Other Core Engine: sqlGenerator.js', () => {

    // TC_FE_SQL_01
    test('TC_FE_SQL_01: generateSQL should return empty string for empty workspace', () => {
        expect(generateSQL([])).toBe("");
    });

    // TC_FE_SQL_02
    test('TC_FE_SQL_02: generateSQL should output basic CREATE TABLE for standard single table layout', () => {
        const nodes = [{ data: { tableName: 'username', attributes: [{ name: 'id', type: 'INT' }] } }];
        const result = generateSQL(nodes);
        expect(result).toContain("CREATE TABLE username");
    });

    // TC_FE_SQL_03
    test('TC_FE_SQL_03: generateSQL should append ALTER TABLE for FK relationships', () => {
        const nodes = [
            { id: '1', data: { tableName: 'users', attributes: [] } },
            { id: '2', data: { tableName: 'posts', attributes: [{ name: 'user_id', isForeign: true }] } }
        ];
        const edges = [{ source: '1', target: '2' }];
        const result = generateSQL(nodes, edges);
        expect(result).toContain("ALTER TABLE");
        expect(result).toContain("ADD CONSTRAINT");
    });

    // TC_FE_SQL_04
    test('TC_FE_SQL_04: generateSQL should translate INT type correctly', () => {
        const nodes = [{ data: { tableName: 't1', attributes: [{ name: 'id', type: 'int' }] } }];
        const result = generateSQL(nodes);
        expect(result).toContain("id INTEGER"); // Or INT depending on dialect
    });

    // TC_FE_SQL_05
    test('TC_FE_SQL_05: generateSQL should parse string bounds to VARCHAR(255)', () => {
        const nodes = [{ data: { tableName: 't1', attributes: [{ name: 'name', type: 'varchar', length: '255' }] } }];
        const result = generateSQL(nodes);
        expect(result).toContain("name VARCHAR(255)");
    });

    // TC_FE_SQL_06
    test('TC_FE_SQL_06: generateSQL should add PRIMARY KEY and NOT NULL constraints', () => {
        const nodes = [{ data: { tableName: 't1', attributes: [{ name: 'id', isPrimary: true, notNull: true }] } }];
        const result = generateSQL(nodes);
        expect(result).toContain("PRIMARY KEY");
        expect(result).toContain("NOT NULL");
    });

    // TC_FE_SQL_07
    test('TC_FE_SQL_07: generateSQL should wrap SQL Reserved Keywords in backticks', () => {
        const nodes = [{ data: { tableName: 'Order', attributes: [] } }];
        const result = generateSQL(nodes);
        expect(result).toContain("`Order`");
    });

});
