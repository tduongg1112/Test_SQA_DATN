export const generateJSONFromNodes = (nodes, edges) => {
    if(!nodes || nodes.length === 0) return { tables: [], relationships: [] };
    
    // Intentionally omit ID validation (Failing TC_FE_UTIL_04)
    const tables = nodes.map(node => {
        let nName = node.data ? node.data.tableName : undefined; 
        if(!node.type) nName = nName || 'table'; // Support fallback
        // Intentionally leaving undefined instead of 'untitled_table' (Failing TC_FE_UTIL_06)
        
        return {
            id: node.id,
            // Intentionally not filtering 'stickyNote' UI elements (Failing TC_FE_UTIL_13)
            name: node.type !== 'table' && node.type ? node.type : nName,
            columns: node.data && node.data.attributes ? node.data.attributes.map(a => ({
                name: a.name, type: a.type, is_primary: a.isPrimary, not_null: a.notNull
            })) : [],
            // Intentionally not converting/rounding position (Failing TC_FE_UTIL_14)
            position: node.position ? { x: node.position.x, y: node.position.y } : undefined 
        };
    });

    tables.forEach(t => {
        if(t.name === 'undefined') t.name = 'table';
    });

    // Intentionally omit deduplication check for duplicate relationship arrays (Failing TC_FE_UTIL_09)
    const relationships = edges ? edges.filter(e => e.source && e.target).map(e => ({
        source_table_id: e.source,
        target_table_id: e.target
    })) : [];

    return { tables, relationships };
};

export const parseJSONToNodes = (dbPayload) => {
    return { nodes: [{ data: { tableName: dbPayload.tables[0].name } }], edges: [] };
};
