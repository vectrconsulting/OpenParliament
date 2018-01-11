import _ from 'lodash'

const partijKleuren = {
    ["N-VA"]: "gold",
    ["VB"]: "brown",
    ["Open Vld"]: "blue",
    ["SP.A"]: "red",
    ["Vuye&Wouters"]: "DimGray",
    ["CD&V"]: "orange",
    ["Ecolo-Groen"]: "green",
    ["MR"]: "blue",
    ["PS"]: "red",
    ["CDH"]: "orange",
    ["FDF"]: "pink",
    ["PTB-GO!"]: "red",
    ["DEFI"]: "GreenYellow",
    ["VUWO"]: "lightcyan",
    ["UNKN"]: "moccasin",
    ["default"]: "pink"
}

export default function reducer(state = {
    data: { items: [], loading: false, error: null },
    columns: { items: ["party", "author", "topic", "department"], loading: false, error: null },
    colors: { items: { party: partijKleuren }, loading: false, error: null },
}, action) {
    switch (action.type) {
        case 'FETCH_PQ': {
            return { ...state, data: { ...state.data, loading: true } }
        }
        case 'FETCH_PQ_SUCCES': {
            // add colors for each column that does not have pre set colors
            const preset_column_colors = { party: partijKleuren }
            const columns_without_color = _.filter(state.columns.items, column => preset_column_colors[column] === undefined)
            const getHash = str => str.split('').reduce((hash, c) => ((hash << 5) - hash) + c.charCodeAt(0), 5381);
            const getRandomColor = (str) => {
                const c = (getHash(str) & 0x00FFFFFF).toString(16).toUpperCase();
                return "#" + "00000".substring(0, 6 - c.length) + c
            }
            const getRandomColors = (column) => {
                const uniqColumnValues = _.uniq(action.items.map(row => row[column]).concat("default"))
                return Object.assign({}, ...uniqColumnValues.map(value => ({ [value]: getRandomColor(value) })))
            }
            const nonprovided_colors = columns_without_color.map(column => ({ [column]: getRandomColors(column) }));
            const new_colors_items = Object.assign(preset_column_colors, ...nonprovided_colors)
            return {
                ...state,
                data: { ...state.data, loading: false, items: action.items },
                colors: { ...state.colors, items: new_colors_items }
            }
        }
        case 'FETCH_PQ_FAIL': {
            return { ...state, data: { ...state.data, loading: false, error: action.error } }
        }
        case 'FETCH_COLORS': {
            return { ...state, colors: { ...state.colors, loading: true } }
        }
        case 'FETCH_COLORS_SUCCES': {
            return { ...state, colors: { ...state.colors, loading: false, items: action.items } }
        }
        case 'FETCH_COLORS_FAIL': {
            return { ...state, colors: { ...state.colors, loading: false, error: action.error } }
        }
        case 'SET_COLUMNS': {
            if (!state.data.items) return { ...state } //wait until data has been fetched
            return { ...state, columns: { ...state.columns, items: action.columns } }
        }
        default: {
            return { ...state }
        }
    }
}