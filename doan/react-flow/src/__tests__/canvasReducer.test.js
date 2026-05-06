import canvasReducer from '../utils/canvasReducer';

describe('C. Other Core Engine: canvasReducer.js', () => {
    const initialState = { nodes: [], edges: [] };

    test('TC_FE_RED_01: ADD_NODE should increment local state nodes by 1', () => {
        const action = { type: 'ADD_NODE', payload: { id: 'node_1' } };
        const state = canvasReducer(initialState, action);
        expect(state.nodes.length).toBe(1);
    });

    test('TC_FE_RED_02: ADD_NODE should block duplicated IDs ensuring immutability', () => {
        const state1 = canvasReducer(initialState, { type: 'ADD_NODE', payload: { id: 'node_1' } });
        const state2 = canvasReducer(state1, { type: 'ADD_NODE', payload: { id: 'node_1' } });
        expect(state2.nodes.length).toBe(1);
    });

    test('TC_FE_RED_03: REMOVE_NODE should delete node and cascade-delete all referring edges', () => {
        const startState = {
            nodes: [{ id: 'node_1' }],
            edges: [{ source: 'node_1', target: 'node_2' }]
        };
        const state = canvasReducer(startState, { type: 'REMOVE_NODE', payload: 'node_1' });
        expect(state.nodes.length).toBe(0);
        // Expecting cascade delete to happen
        expect(state.edges.length).toBe(0);
    });

    test('TC_FE_RED_04: ADD_EDGE should successfully increment edges state', () => {
        const state = canvasReducer(initialState, { type: 'ADD_EDGE', payload: { id: 'e1' } });
        expect(state.edges.length).toBe(1);
    });

    test('TC_FE_RED_05: CLEAR_CANVAS should wipe entire application scope to arrays', () => {
        const startState = { nodes: [{ id: '1' }], edges: [{ id: '2' }] };
        const state = canvasReducer(startState, { type: 'CLEAR_CANVAS' });
        expect(state.nodes.length).toBe(0);
        expect(state.edges.length).toBe(0);
    });
});
