import { useState } from 'react';
import { useSelector } from 'react-redux';
import { HiOutlineCog, HiOutlineShieldCheck, HiOutlineDatabase, HiOutlineMail } from 'react-icons/hi';

const AdminSettings = () => {
  const { user } = useSelector((state) => state.auth);
  const [activeTab, setActiveTab] = useState('general');

  const tabs = [
    { id: 'general', label: 'General', icon: HiOutlineCog },
    { id: 'security', label: 'Security', icon: HiOutlineShieldCheck },
    { id: 'system', label: 'System Info', icon: HiOutlineDatabase },
  ];

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-gray-900">Settings</h1>

      {/* Tabs */}
      <div className="flex gap-1 border-b border-gray-200">
        {tabs.map(tab => (
          <button
            key={tab.id}
            onClick={() => setActiveTab(tab.id)}
            className={`flex items-center gap-2 px-4 py-2.5 text-sm font-medium border-b-2 transition ${
              activeTab === tab.id
                ? 'border-indigo-600 text-indigo-600'
                : 'border-transparent text-gray-500 hover:text-gray-700'
            }`}
          >
            <tab.icon className="h-4 w-4" />
            {tab.label}
          </button>
        ))}
      </div>

      {/* General */}
      {activeTab === 'general' && (
        <div className="bg-white rounded-xl border border-gray-200 p-5 space-y-4">
          <h2 className="font-semibold text-gray-900">Store Information</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Store Name</label>
              <input value="ShopZone" readOnly className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-gray-50" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Admin Email</label>
              <input value={user?.email || ''} readOnly className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-gray-50" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Currency</label>
              <input value="USD ($)" readOnly className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-gray-50" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Tax Rate</label>
              <input value="8%" readOnly className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm bg-gray-50" />
            </div>
          </div>
          <p className="text-xs text-gray-400">Store settings are configured in the backend application.yml. This page is read-only.</p>
        </div>
      )}

      {/* Security */}
      {activeTab === 'security' && (
        <div className="bg-white rounded-xl border border-gray-200 p-5 space-y-4">
          <h2 className="font-semibold text-gray-900">Security Settings</h2>
          <div className="space-y-3 text-sm">
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div>
                <p className="font-medium text-gray-900">JWT Authentication</p>
                <p className="text-gray-500">Token-based authentication with refresh tokens</p>
              </div>
              <span className="px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-medium">Active</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div>
                <p className="font-medium text-gray-900">Role-Based Access Control</p>
                <p className="text-gray-500">ADMIN and CUSTOMER roles</p>
              </div>
              <span className="px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-medium">Active</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div>
                <p className="font-medium text-gray-900">Stripe Payment Security</p>
                <p className="text-gray-500">Webhook signature verification enabled</p>
              </div>
              <span className="px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-medium">Active</span>
            </div>
            <div className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
              <div>
                <p className="font-medium text-gray-900">CORS Configuration</p>
                <p className="text-gray-500">localhost:3000, localhost:5173</p>
              </div>
              <span className="px-2 py-0.5 bg-green-100 text-green-700 rounded-full text-xs font-medium">Active</span>
            </div>
          </div>
        </div>
      )}

      {/* System Info */}
      {activeTab === 'system' && (
        <div className="bg-white rounded-xl border border-gray-200 p-5 space-y-4">
          <h2 className="font-semibold text-gray-900">System Information</h2>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-sm">
            {[
              ['Backend', 'Java Spring Boot 3.x'],
              ['Frontend', 'React 19 + Vite 7'],
              ['SQL Database', 'PostgreSQL'],
              ['NoSQL Database', 'MongoDB'],
              ['Cache', 'Redis'],
              ['Search', 'Elasticsearch'],
              ['Payments', 'Stripe (Test Mode)'],
              ['Image Storage', 'Cloudinary'],
              ['Email', 'Mailtrap (Testing)'],
              ['Styling', 'Tailwind CSS 4'],
            ].map(([label, value]) => (
              <div key={label} className="flex items-center justify-between p-3 bg-gray-50 rounded-lg">
                <span className="text-gray-500">{label}</span>
                <span className="font-medium text-gray-900">{value}</span>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminSettings;