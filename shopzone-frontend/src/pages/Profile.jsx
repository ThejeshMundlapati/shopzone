import { useSelector } from 'react-redux';
import { Link } from 'react-router-dom';

const Profile = () => {
  const { user } = useSelector((state) => state.auth);

  return (
    <div className="max-w-2xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">My Profile</h1>

      {/* Profile Info */}
      <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Personal Information</h2>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-500 mb-1">First Name</label>
              <p className="text-gray-900 font-medium">{user?.firstName || '-'}</p>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-500 mb-1">Last Name</label>
              <p className="text-gray-900 font-medium">{user?.lastName || '-'}</p>
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500 mb-1">Email</label>
            <p className="text-gray-900">{user?.email || '-'}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500 mb-1">Phone</label>
            <p className="text-gray-900">{user?.phone || 'Not provided'}</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500 mb-1">Role</label>
            <span className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium ${
              user?.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'
            }`}>
              {user?.role || 'CUSTOMER'}
            </span>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-500 mb-1">Email Verified</label>
            <span className={`inline-block px-2.5 py-0.5 rounded-full text-xs font-medium ${
              user?.emailVerified ? 'bg-green-100 text-green-700' : 'bg-yellow-100 text-yellow-700'
            }`}>
              {user?.emailVerified ? 'Verified' : 'Not Verified'}
            </span>
          </div>
          {user?.createdAt && (
            <div>
              <label className="block text-sm font-medium text-gray-500 mb-1">Member Since</label>
              <p className="text-gray-900">{new Date(user.createdAt).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' })}</p>
            </div>
          )}
        </div>
      </div>

      {/* Quick Links */}
      <div className="bg-white rounded-xl shadow-sm p-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Quick Links</h2>
        <div className="grid grid-cols-2 gap-3">
          <Link to="/orders" className="p-3 border rounded-lg hover:bg-gray-50 transition text-center">
            <p className="text-2xl mb-1">📦</p>
            <p className="text-sm font-medium text-gray-700">My Orders</p>
          </Link>
          <Link to="/addresses" className="p-3 border rounded-lg hover:bg-gray-50 transition text-center">
            <p className="text-2xl mb-1">📍</p>
            <p className="text-sm font-medium text-gray-700">Addresses</p>
          </Link>
          <Link to="/wishlist" className="p-3 border rounded-lg hover:bg-gray-50 transition text-center">
            <p className="text-2xl mb-1">💝</p>
            <p className="text-sm font-medium text-gray-700">Wishlist</p>
          </Link>
          <Link to="/cart" className="p-3 border rounded-lg hover:bg-gray-50 transition text-center">
            <p className="text-2xl mb-1">🛒</p>
            <p className="text-sm font-medium text-gray-700">Cart</p>
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Profile;