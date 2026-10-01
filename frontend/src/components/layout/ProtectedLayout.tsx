import { Outlet } from 'react-router-dom';

import GNB from '@/components/layout/GNB';
import SideMenu from '@/components/layout/SideMenu';

/**
 * ProtectedLayout - 보호된 라우트용 레이아웃
 */
export default function ProtectedLayout() {
  return (
    <div className="min-h-screen w-full overflow-x-hidden bg-background">
      <GNB />
      <div className="flex w-full min-w-0">
        <SideMenu />
        <main className="min-w-0 flex-1 overflow-x-hidden">
          <Outlet />
        </main>
      </div>
    </div>
  );
}