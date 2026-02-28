import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="bg-gray-900 text-gray-300 mt-auto">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          {/* Brand */}
          <div>
            <Link to="/" className="flex items-center space-x-2 mb-4">
              <span className="text-2xl">🛒</span>
              <span className="text-xl font-bold text-white">ShopZone</span>
            </Link>
            <p className="text-sm text-gray-400">
              Your one-stop destination for quality products at great prices.
            </p>
          </div>

          {/* Shop */}
          <div>
            <h3 className="text-white font-semibold mb-3">Shop</h3>
            <ul className="space-y-2 text-sm">
              <li><Link to="/products" className="hover:text-white transition">All Products</Link></li>
              <li><Link to="/products?featured=true" className="hover:text-white transition">Featured</Link></li>
              <li><Link to="/products?sortBy=createdAt&sortDir=desc" className="hover:text-white transition">New Arrivals</Link></li>
            </ul>
          </div>

          {/* Account */}
          <div>
            <h3 className="text-white font-semibold mb-3">Account</h3>
            <ul className="space-y-2 text-sm">
              <li><Link to="/profile" className="hover:text-white transition">My Profile</Link></li>
              <li><Link to="/orders" className="hover:text-white transition">Order History</Link></li>
              <li><Link to="/wishlist" className="hover:text-white transition">Wishlist</Link></li>
              <li><Link to="/addresses" className="hover:text-white transition">Addresses</Link></li>
            </ul>
          </div>

          {/* Info */}
          <div>
            <h3 className="text-white font-semibold mb-3">Info</h3>
            <ul className="space-y-2 text-sm">
              <li className="text-gray-400">Free shipping over $50</li>
              <li className="text-gray-400">30-day return policy</li>
              <li className="text-gray-400">Secure payments via Stripe</li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-800 mt-8 pt-8 text-center text-sm text-gray-500">
          <p>&copy; {new Date().getFullYear()} ShopZone. Built with Spring Boot & React — Portfolio Project by Thejesh.</p>
        </div>
      </div>
    </footer>
  );
};

export default Footer;