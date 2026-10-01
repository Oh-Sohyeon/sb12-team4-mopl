import apiClient from './client';
import type { RecommendationItem } from '@/lib/types';

// [추가] 개인화 추천 조회
export const getRecommendations = async (): Promise<RecommendationItem[]> => {
  const response = await apiClient.get<RecommendationItem[]>('/api/recommendations');

  return response.data;
};