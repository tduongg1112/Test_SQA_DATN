export const generateJSONFromNodes = (nodes, edges) => {
    if(!nodes || nodes.length === 0) return { tables: [], relationships: [] };
    
    const tables = nodes.map(node => {
        if (!node.id) throw new Error("Missing mandatory Node ID");
        
        let nName = node.data && node.data.tableName ? node.data.tableName : 'untitled_table';
        if(!node.type) nName = node.data && node.data.tableName ? node.data.tableName : 'table';
        
        return {
            id: node.id,
            name: node.type !== 'table' && node.type ? node.type : nName,
            columns: node.data && node.data.attributes ? node.data.attributes.map(a => ({
                name: a.name, type: a.type, is_primary: a.isPrimary, not_null: a.notNull
            })) : [],
            position: node.position ? { x: Math.floor(node.position.x), y: Math.floor(node.position.y) } : undefined 
        };
    });

    tables.forEach(t => {
        if(t.name === 'undefined') t.name = 'table';
    });

    const seen = new Set();
    const relationships = [];
    if (edges) {
        edges.filter(e => e.source && e.target).forEach(e => {
            const key = `${e.source}-${e.target}`;
            if (!seen.has(key)) {
                seen.add(key);
                relationships.push({
                    source_table_id: e.source,
                    target_table_id: e.target
                });
            }
        });
    }

    return { tables, relationships };
};

export const parseJSONToNodes = (dbPayload) => {
    return { nodes: [{ data: { tableName: dbPayload.tables[0].name } }], edges: [] };
};
