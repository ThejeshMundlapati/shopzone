import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { HiOutlineShoppingCart, HiOutlineHeart, HiOutlineUser, HiOutlineMenu, HiOutlineX, HiOutlineCog } from 'react-icons/hi';
import { logout } from '../../store/authSlice';
import { resetCart } from '../../store/cartSlice';
import { resetWishlist } from '../../store/wishlistSlice';
import SearchBar from './SearchBar';

const Header = () => {
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [userMenuOpen, setUserMenuOpen] = useState(false);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { isAuthenticated, user } = useSelector((state) => state.auth);
  const { totalItems } = useSelector((state) => state.cart);
  const { itemCount: wishlistCount } = useSelector((state) => state.wishlist);

  const isAdmin = user?.role === 'ADMIN';

  const handleLogout = () => {
    dispatch(logout());
    dispatch(resetCart());
    dispatch(resetWishlist());
    setUserMenuOpen(false);
    navigate('/');
  };

  return (
    <header className="bg-white shadow-sm sticky top-0 z-50">
      {/* Top Bar */}
      <div className="bg-indigo-600 text-white text-center text-sm py-1.5 px-4">
        Free shipping on orders over $50! 🚚
      </div>

      {/* Main Header */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
        <div className="flex items-center justify-between h-16">
          {/* Logo */}
          <Link to="/" className="flex items-center space-x-2 flex-shrink-0">
            <span className="text-2xl">🛒</span>
            <span className="text-xl font-bold text-gray-900">ShopZone</span>
          </Link>

          {/* Search Bar — hidden on mobile */}
          <div className="hidden md:block flex-1 max-w-lg mx-8">
            <SearchBar />
          </div>

          {/* Desktop Nav */}
          <nav className="hidden md:flex items-center space-x-1">
            <Link to="/products" className="px-3 py-2 text-sm font-medium text-gray-700 hover:text-indigo-600 transition">
              Products
            </Link>

            {isAuthenticated ? (
              <>
                {/* Admin Dashboard Link */}
                {isAdmin && (
                  <Link to="/admin" className="px-3 py-2 text-sm font-medium text-indigo-600 hover:text-indigo-700 transition flex items-center gap-1">
                    <HiOutlineCog className="h-4 w-4" />
                    Admin
                  </Link>
                )}

                <Link to="/wishlist" className="relative p-2 text-gray-600 hover:text-indigo-600 transition">
                  <HiOutlineHeart className="h-6 w-6" />
                  {wishlistCount > 0 && (
                    <span className="absolute -top-0.5 -right-0.5 bg-pink-500 text-white text-xs rounded-full h-4.5 w-4.5 flex items-center justify-center font-medium">
                      {wishlistCount}
                    </span>
                  )}
                </Link>

                <Link to="/cart" className="relative p-2 text-gray-600 hover:text-indigo-600 transition">
                  <HiOutlineShoppingCart className="h-6 w-6" />
                  {totalItems > 0 && (
                    <span className="absolute -top-0.5 -right-0.5 bg-indigo-600 text-white text-xs rounded-full h-4.5 w-4.5 flex items-center justify-center font-medium">
                      {totalItems > 99 ? '99+' : totalItems}
                    </span>
                  )}
                </Link>

                {/* User Dropdown */}
                <div className="relative ml-2">
                  <button
                    onClick={() => setUserMenuOpen(!userMenuOpen)}
                    className="flex items-center space-x-1 px-3 py-2 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-100 transition"
                  >
                    <HiOutlineUser className="h-5 w-5" />
                    <span className="hidden lg:inline">{user?.firstName}</span>
                  </button>

                  {userMenuOpen && (
                    <>
                      <div className="fixed inset-0" onClick={() => setUserMenuOpen(false)} />
                      <div className="absolute right-0 mt-1 w-48 bg-white rounded-lg shadow-lg border py-1 z-50">
                        <div className="px-4 py-2 border-b">
                          <p className="text-sm font-medium text-gray-900">{user?.firstName} {user?.lastName}</p>
                          <p className="text-xs text-gray-500 truncate">{user?.email}</p>
                          {isAdmin && <span className="inline-block mt-1 text-xs bg-indigo-100 text-indigo-700 px-1.5 py-0.5 rounded font-medium">Admin</span>}
                        </div>
                        {isAdmin && (
                          <Link to="/admin" onClick={() => setUserMenuOpen(false)} className="block px-4 py-2 text-sm text-indigo-600 hover:bg-indigo-50 font-medium">
                            Admin Dashboard
                          </Link>
                        )}
                        <Link to="/profile" onClick={() => setUserMenuOpen(false)} className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">Profile</Link>
                        <Link to="/orders" onClick={() => setUserMenuOpen(false)} className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">My Orders</Link>
                        <Link to="/addresses" onClick={() => setUserMenuOpen(false)} className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">Addresses</Link>
                        <Link to="/wishlist" onClick={() => setUserMenuOpen(false)} className="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50">Wishlist</Link>
                        <hr className="my-1" />
                        <button onClick={handleLogout} className="block w-full text-left px-4 py-2 text-sm text-red-600 hover:bg-red-50">
                          Sign Out
                        </button>
                      </div>
                    </>
                  )}
                </div>
              </>
            ) : (
              <div className="flex items-center space-x-2 ml-2">
                <Link to="/login" className="px-4 py-2 text-sm font-medium text-gray-700 hover:text-indigo-600 transition">
                  Sign In
                </Link>
                <Link to="/register" className="px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition">
                  Sign Up
                </Link>
              </div>
            )}
          </nav>

          {/* Mobile Menu Button */}
          <div className="md:hidden flex items-center space-x-2">
            {isAuthenticated && (
              <Link to="/cart" className="relative p-2">
                <HiOutlineShoppingCart className="h-6 w-6 text-gray-600" />
                {totalItems > 0 && (
                  <span className="absolute -top-0.5 -right-0.5 bg-indigo-600 text-white text-xs rounded-full h-4 w-4 flex items-center justify-center">
                    {totalItems}
                  </span>
                )}
              </Link>
            )}
            <button onClick={() => setMobileMenuOpen(!mobileMenuOpen)} className="p-2 text-gray-600">
              {mobileMenuOpen ? <HiOutlineX className="h-6 w-6" /> : <HiOutlineMenu className="h-6 w-6" />}
            </button>
          </div>
        </div>

        {/* Mobile Search */}
        <div className="md:hidden pb-3">
          <SearchBar />
        </div>
      </div>

      {/* Mobile Menu */}
      {mobileMenuOpen && (
        <div className="md:hidden border-t bg-white">
          <div className="px-4 py-3 space-y-2">
            <Link to="/products" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-gray-700">Products</Link>
            {isAuthenticated ? (
              <>
                {isAdmin && (
                  <Link to="/admin" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-indigo-600 font-medium">
                    Admin Dashboard
                  </Link>
                )}
                <Link to="/orders" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-gray-700">My Orders</Link>
                <Link to="/wishlist" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-gray-700">Wishlist</Link>
                <Link to="/profile" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-gray-700">Profile</Link>
                <Link to="/addresses" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-gray-700">Addresses</Link>
                <button onClick={handleLogout} className="block w-full text-left py-2 text-red-600">Sign Out</button>
              </>
            ) : (
              <>
                <Link to="/login" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-gray-700">Sign In</Link>
                <Link to="/register" onClick={() => setMobileMenuOpen(false)} className="block py-2 text-indigo-600 font-medium">Sign Up</Link>
              </>
            )}
          </div>
        </div>
      )}
    </header>
  );
};

export default Header;