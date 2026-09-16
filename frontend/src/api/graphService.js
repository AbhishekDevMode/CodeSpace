import api from './axios';

export const graphService = {
    getGraph: async (projectId, depth = 1, focus = null) => {
        const params = { depth };
        if (focus) params.focus = focus;

        const response = await api.get(`/api/graph/project/${projectId}/view`, { params });
        return response.data;
    },

};
