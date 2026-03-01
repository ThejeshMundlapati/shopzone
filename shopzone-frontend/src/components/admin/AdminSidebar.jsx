import { NavLink } from 'react-router-dom';
import {
  HiOutlineChartBar, HiOutlineShoppingBag, HiOutlineTag,
  HiOutlineClipboardList, HiOutlineUsers, HiOutlineStar,
  HiOutlineDocumentReport, HiOutlineCog, HiOutlineHome,
  HiOutlineX, HiOutlineCreditCard
} from 'react-icons/hi';

const navItems = [
  { to: '/admin', icon: HiOutlineChartBar, label: 'Dashboard', end: true },
  { to: '/admin/products', icon: HiOutlineShoppingBag, label: 'Products' },
  { to: '/admin/categories', icon: HiOutlineTag, label: 'Categories' },
  { to: '/admin/orders', icon: HiOutlineClipboardList, label: 'Orders' },
  { to: '/admin/users', icon: HiOutlineUsers, label: 'Users' },
  { to: '/admin/reviews', icon: HiOutlineStar, label: 'Reviews' },
  { to: '/admin/reports', icon: HiOutlineDocumentReport, label: 'Reports' },
];

const AdminSidebar = ({ open, onClose }) => {
  const linkClass = ({ isActive }) =>
    `flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium transition-colors ${
      isActive
        ? 'bg-indigo-50 text-indigo-700'
        : 'text-gray-600 hover:bg-gray-100 hover:text-gray-900'
    }`;

  return (
    <>
      {/* Mobile overlay */}
      {open && (
        <div className="fixed inset-0 bg-black/50 z-40 lg:hidden" onClick={onClose} />
      )}

      {/* Sidebar */}
      <aside
        className={`fixed top-0 left-0 z-50 h-full w-64 bg-white border-r border-gray-200 
          transform transition-transform duration-200 ease-in-out
          lg:translate-x-0 lg:static lg:z-auto
          ${open ? 'translate-x-0' : '-translate-x-full'}`}
      >
        {/* Logo area */}
        <div className="flex items-center justify-between h-16 px-4 border-b border-gray-200">
          <div className="flex items-center gap-2">
            <span className="text-xl">🛒</span>
            <span className="font-bold text-gray-900">ShopZone</span>
            <span className="text-xs bg-indigo-100 text-indigo-700 px-1.5 py-0.5 rounded font-medium">Admin</span>
          </div>
          <button onClick={onClose} className="lg:hidden p-1 text-gray-500 hover:text-gray-700">
            <HiOutlineX className="h-5 w-5" />
          </button>
        </div>

        {/* Navigation */}
        <nav className="p-4 space-y-1 pb-32">
          {navItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              end={item.end}
              className={linkClass}
              onClick={onClose}
            >
              <item.icon className="h-5 w-5 flex-shrink-0" />
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* Back to store + Settings */}
        <div className="absolute bottom-0 left-0 right-0 p-4 border-t border-gray-200 space-y-1">
          <NavLink
            to="/admin/settings"
            className={linkClass}
            onClick={onClose}
          >
            <HiOutlineCog className="h-5 w-5" />
            Settings
          </NavLink>
          <NavLink
            to="/"
            className="flex items-center gap-3 px-4 py-2.5 rounded-lg text-sm font-medium text-gray-600 hover:bg-gray-100 hover:text-gray-900 transition-colors"
          >
            <HiOutlineHome className="h-5 w-5" />
            Back to Store
          </NavLink>
        </div>
      </aside>
    </>
  );
};

export default AdminSidebar;