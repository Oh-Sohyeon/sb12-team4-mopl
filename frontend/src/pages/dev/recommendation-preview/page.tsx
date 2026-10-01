import { Sparkles } from 'lucide-react';

import RecommendationSection from '@/pages/contents/components/RecommendationSection';

export default function RecommendationPreviewPage() {
  return (
    <div className="min-h-screen px-[70px] py-10">
      <div className="mx-auto flex max-w-[1600px] flex-col gap-10">
        <div>
          <div className="mb-3 flex items-center gap-2 text-pink-400">
            <Sparkles className="h-4 w-4" />
            <span className="text-body3-m font-semibold">
              DEV PREVIEW
            </span>
          </div>

          <h1 className="text-header1-b text-white">
            개인화 추천 UI 미리보기
          </h1>

          <p className="mt-2 text-body2-m text-gray-400">
            백엔드와 로그인 없이 Mock 데이터로 확인하는 개발용 화면입니다.
          </p>
        </div>

        <RecommendationSection />
      </div>
    </div>
  );
}