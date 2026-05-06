import { generateJSONFromNodes, parseJSONToNodes } from '../utils/diagramUtils';

describe('B. Core Utility: diagramUtils.js', () => {

    // TC_FE_UTIL_01 | Export to DB JSON | generateJSONFromNodes | Parsing single Node (no edges)
    test('TC_FE_UTIL_01: generateJSONFromNodes should parse single Node without edges', () => {
        const nodes = [{ id: '1', type: 'table', data: { tableName: 'users', attributes: [] }, position: {x:0, y:0} }];
        const edges = [];
        const result = generateJSONFromNodes(nodes, edges);
        expect(result.tables.length).toBe(1);
        expect(result.relationships.length).toBe(0);
    });

    // TC_FE_UTIL_02 | Export to DB JSON | generateJSONFromNodes | Parsing 2 Nodes with 1 Edge
    test('TC_FE_UTIL_02: generateJSONFromNodes should parse 2 Nodes with 1 Edge', () => {
        const nodes = [
            { id: '1', type: 'table', data: { tableName: 'users', attributes: [] } },
            { id: '2', type: 'table', data: { tableName: 'posts', attributes: [] } }
        ];
        const edges = [{ id: 'e-1-2', source: '1', target: '2' }];
        const result = generateJSONFromNodes(nodes, edges);
        expect(result.tables.length).toBe(2);
        expect(result.relationships.length).toBe(1);
    });

    // TC_FE_UTIL_03 | Export to DB JSON | generateJSONFromNodes | Parsing empty canvas
    test('TC_FE_UTIL_03: generateJSONFromNodes should handle empty canvas safely', () => {
        const result = generateJSONFromNodes([], []);
        expect(result).toEqual({ tables: [], relationships: [] });
    });

    // TC_FE_UTIL_04 | Export to DB JSON | generateJSONFromNodes | Node missing identity ID
    test('TC_FE_UTIL_04: generateJSONFromNodes should skip or throw error when Node lacks ID', () => {
        const nodes = [{ type: 'table', data: { tableName: 'users', attributes: [] } }]; // no id
        expect(() => generateJSONFromNodes(nodes, [])).toThrow("Missing mandatory Node ID");
    });

    // TC_FE_UTIL_05 | Export to DB JSON | generateJSONFromNodes | Node missing type variable
    test('TC_FE_UTIL_05: generateJSONFromNodes should default to table if type is missing', () => {
        const nodes = [{ id: '1', data: { tableName: 'test', attributes: [] } }];
        const result = generateJSONFromNodes(nodes, []);
        expect(result.tables[0].name).toBe('test');
    });

    // TC_FE_UTIL_06 | Export to DB JSON | generateJSONFromNodes | Node dataset missing Table Name
    test('TC_FE_UTIL_06: generateJSONFromNodes should assign generic name if data.tableName is missing', () => {
        const nodes = [{ id: '1', type: 'table', data: { attributes: [] } }];
        const result = generateJSONFromNodes(nodes, []);
        expect(result.tables[0].name).toBe('untitled_table');
    });

    // TC_FE_UTIL_07 | Export to DB JSON | generateJSONFromNodes | Edge lacking Source Node ID
    test('TC_FE_UTIL_07: generateJSONFromNodes should filter out edge missing source ID', () => {
        const nodes = [{ id: '1', data: { tableName: 'users', attributes: [] } }];
        const edges = [{ id: 'e1', target: '1' }]; // no source
        const result = generateJSONFromNodes(nodes, edges);
        expect(result.relationships.length).toBe(0);
    });

    // TC_FE_UTIL_08 | Export to DB JSON | generateJSONFromNodes | Redundant/Duplicate Edges
    test('TC_FE_UTIL_08: generateJSONFromNodes should remove duplicate relationship definitions', () => {
        const nodes = [{ id: '1', data: { tableName: 'A'} }, { id: '2', data: { tableName: 'B'} }];
        const edges = [{ source: '1', target: '2' }, { source: '1', target: '2' }]; // duplicate
        const result = generateJSONFromNodes(nodes, edges);
        expect(result.relationships.length).toBe(1);
    });

    // TC_FE_UTIL_09 | Export to DB JSON | generateJSONFromNodes | Nodes with complex attributes
    test('TC_FE_UTIL_09: generateJSONFromNodes should correctly tag column types and constraints', () => {
        const nodes = [{ id: '1', data: { tableName: 'users', attributes: [{ name: 'id', type: 'INT', isPrimary: true, notNull: true }] } }];
        const result = generateJSONFromNodes(nodes, []);
        expect(result.tables[0].columns[0].is_primary).toBe(true);
        expect(result.tables[0].columns[0].not_null).toBe(true);
    });

    // TC_FE_UTIL_10 | Export to DB JSON | generateJSONFromNodes | Nested relationships (A->B->C)
    test('TC_FE_UTIL_10: generateJSONFromNodes should map foreign keys appropriately across depth', () => {
        const edges = [{ source: '1', target: '2' }, { source: '2', target: '3' }];
        const result = generateJSONFromNodes([{id:'1'}, {id:'2'}, {id:'3'}], edges);
        expect(result.relationships.length).toBe(2);
    });

    // TC_FE_UTIL_11 | Export to DB JSON | generateJSONFromNodes | Coordinate Extraction
    test('TC_FE_UTIL_11: generateJSONFromNodes should safely convert/round float coordinates to integer', () => {
        const nodes = [{ id: '1', data: { tableName: 'users', attributes: [] }, position: { x: 100.5, y: 20.9 } }];
        const result = generateJSONFromNodes(nodes, []);
        expect(result.tables[0].position.x).toBe(100); // or 101 based on round logic
    });

    // TC_FE_UTIL_12 | Export to DB JSON | parseJSONToNodes | Valid DB structure to Canvas Nodes
    test('TC_FE_UTIL_12: parseJSONToNodes should output React Flow compatible nodes/edges array', () => {
        const dbPayload = { tables: [{ id: '1', name: 'users' }], relationships: [] };
        const result = parseJSONToNodes(dbPayload);
        expect(Array.isArray(result.nodes)).toBe(true);
        expect(result.nodes[0].data.tableName).toBe('users');
    });

});
