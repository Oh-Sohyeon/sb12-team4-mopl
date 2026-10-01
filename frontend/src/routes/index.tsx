import {
  Navigate,
  Route,
  Routes,
} from 'react-router-dom';

// Layouts
import RootLayout from '@/components/layout/RootLayout';
import ProtectedRoute from '@/components/layout/ProtectedRoute';
import ProtectedLayout from '@/components/layout/ProtectedLayout';

// Auth Pages
import SignInPage from '@/pages/sign-in/page';
import SignUpPage from '@/pages/sign-up/page';
import ResetPasswordPage from '@/pages/reset-password/page';

// Protected Pages
import ContentsPage from '@/pages/contents/page';
import ContentDetailPage from '@/pages/contents/[contentId]/page';
import PlaylistsPage from '@/pages/playlists/page';
import PlaylistDetailPage from '@/pages/playlists/[playlistId]/page';
import ProfilePage from '@/pages/profiles/[userId]/page';
import ProfileRoutePage from '@/pages/profiles/page';
import ConversationsPage from '@/pages/conversations/page';
import ConversationWithPage from '@/pages/conversations/with/page';
import AdminUsersPage from '@/pages/admin/users/page';

// Dev Preview Pages
import RecommendationPreviewPage from '@/pages/dev/recommendation-preview/page';
import ContentsPreviewPage from '@/pages/dev/contents-preview/page';

// Error Pages
import NotFoundPage from '@/pages/not-found/page';

export default function AppRoutes() {
  return (
    <Routes>
      <Route element={<RootLayout />}>
        {/* Public Auth Routes */}
        <Route
          path="/sign-in"
          element={<SignInPage />}
        />

        <Route
          path="/sign-up"
          element={<SignUpPage />}
        />

        <Route
          path="/reset-password"
          element={<ResetPasswordPage />}
        />

        {/* Dev Preview - 추천 섹션 단독 */}
        {import.meta.env.DEV && (
          <Route
            path="/dev/recommendation-preview"
            element={<RecommendationPreviewPage />}
          />
        )}

        {/*
          [추가]
          실제 앱 GNB + SideMenu 레이아웃은 사용하지만
          ProtectedRoute는 거치지 않는 개발용 preview
        */}
        {import.meta.env.DEV && (
          <Route element={<ProtectedLayout />}>
            <Route
              path="/dev/contents-preview"
              element={<ContentsPreviewPage />}
            />
          </Route>
        )}

        {/* 실제 Protected Routes */}
        <Route element={<ProtectedRoute />}>
          <Route element={<ProtectedLayout />}>
            {/* Home */}
            <Route
              index
              element={<Navigate to="/contents" replace />}
            />

            {/* Contents */}
            <Route
              path="/contents"
              element={<ContentsPage />}
            />

            <Route
              path="/contents/:contentId"
              element={<ContentDetailPage />}
            />

            {/* Playlists */}
            <Route
              path="/playlists"
              element={<PlaylistsPage />}
            />

            <Route
              path="/playlists/:playlistId"
              element={<PlaylistDetailPage />}
            />

            {/* Users */}
            <Route
              path="/profiles"
              element={<ProfileRoutePage />}
            />

            <Route
              path="/profiles/:userId"
              element={<ProfilePage />}
            />

            {/* Conversations */}
            <Route
              path="/conversations/with"
              element={<ConversationWithPage />}
            />

            <Route
              path="/conversations"
              element={<ConversationsPage />}
            />

            <Route
              path="/conversations/:conversationId"
              element={<ConversationsPage />}
            />

            {/* Admin */}
            <Route
              path="/admin/users"
              element={<AdminUsersPage />}
            />
          </Route>
        </Route>

        {/* 404 */}
        <Route
          path="*"
          element={<NotFoundPage />}
        />
      </Route>
    </Routes>
  );
}