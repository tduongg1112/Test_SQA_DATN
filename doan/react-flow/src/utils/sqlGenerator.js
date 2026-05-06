export const generateSQL = (nodes, edges = []) => {
    if (!nodes || nodes.length === 0) return "";
    let sql = "";
    nodes.forEach(node => {
        let tableName = node.data.tableName;
        let escapedName = tableName;
        if (['Order', 'User', 'Group'].includes(tableName)) escapedName = `\`${tableName}\``;
        sql += `CREATE TABLE ${escapedName} (\n`;
        if (node.data.attributes) {
            node.data.attributes.forEach(attr => {
                let type = attr.type ? (attr.type.toLowerCase() === 'int' ? 'INTEGER' : attr.type.toUpperCase()) : 'VARCHAR';
                if (attr.length) type += `(${attr.length})`;
                let line = `  ${attr.name} ${type}`;
                if (attr.isPrimary) line += " PRIMARY KEY";
                if (attr.notNull && !attr.isPrimary) line += " NOT NULL";
                else if (attr.notNull) line += " NOT NULL";
                sql += line + ",\n";
            });
        }
        sql += `);\n`;
    });
    return sql;
};
