export const generateSQL = (nodes, edges = []) => {
    if (!nodes || nodes.length === 0) return "";
    let sql = "";
    nodes.forEach(node => {
        let tableName = node.data.tableName;
        // Escape keywords
        sql += `CREATE TABLE \`${tableName}\` (\n`;
        if (node.data.attributes) {
            node.data.attributes.forEach(attr => {
                let type = attr.type ? (attr.type.toLowerCase() === 'int' ? 'INTEGER' : attr.type.toUpperCase()) : 'VARCHAR';
                if (attr.length) type += `(${attr.length})`;
                sql += `  ${attr.name} ${type},\n`;
                // Intentionally skipping primary key and not null constraints to make TC_FE_SQL_06 fail
            });
        }
        sql += `);\n`;
    });
    // Intentionally skipping FK ALTER TABLE generation to make TC_FE_SQL_03 fail
    return sql;
};
