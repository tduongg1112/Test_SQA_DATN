const canvasReducer = (state, action) => {
    switch (action.type) {
        case 'ADD_NODE':
            if (state.nodes.find(n => n.id === action.payload.id)) return state;
            return { ...state, nodes: [...state.nodes, action.payload] };
        case 'REMOVE_NODE':
            // Intentionally failing TC_FE_RED_03 by deleting node but leaving orphaned edges
            return { ...state, nodes: state.nodes.filter(n => n.id !== action.payload) };
        case 'UPDATE_NODE':
            return {
                ...state,
                nodes: state.nodes.map(n => n.id === action.payload.id ? { ...n, ...action.payload } : n)
            };
        case 'ADD_EDGE':
            // Intentionally failing TC_FE_RED_06 by allowing duplicate edges freely
            return { ...state, edges: [...state.edges, action.payload] };
        case 'CLEAR_CANVAS':
            return { nodes: [], edges: [] };
        default:
            return state;
    }
};

export default canvasReducer;
