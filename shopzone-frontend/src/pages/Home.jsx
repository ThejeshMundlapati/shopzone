import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchFeaturedProducts, fetchCategories } from '../store/productSlice';
import ProductCard from '../components/common/ProductCard';
import { ProductGridSkeleton } from '../components/common/LoadingSkeleton';
import { HiArrowRight, HiShieldCheck, HiTruck, HiRefresh, HiCreditCard } from 'react-icons/hi';

const Home = () => {
  const dispatch = useDispatch();
  const { featured, categories, loading } = useSelector((state) => state.products);

  useEffect(() => {
    dispatch(fetchFeaturedProducts());
    dispatch(fetchCategories());
  }, [dispatch]);

  return (
    <div className="animate-fade-in">
      {/* Hero Section */}
      <section className="bg-gradient-to-br from-indigo-600 via-indigo-700 to-purple-800 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-20 md:py-28">
          <div className="max-w-2xl">
            <h1 className="text-4xl md:text-5xl font-extrabold leading-tight mb-4">
              Discover Amazing Products at{' '}
              <span className="text-amber-300">Great Prices</span>
            </h1>
            <p className="text-lg text-indigo-100 mb-8">
              Shop from thousands of products with free shipping on orders over $50. 
              Quality guaranteed with our 30-day return policy.
            </p>
            <div className="flex flex-wrap gap-4">
              <Link
                to="/products"
                className="inline-flex items-center px-8 py-3 bg-white text-indigo-700 font-semibold rounded-lg hover:bg-indigo-50 transition shadow-lg"
              >
                Shop Now <HiArrowRight className="ml-2 h-5 w-5" />
              </Link>
              <Link
                to="/products?featured=true"
                className="inline-flex items-center px-8 py-3 border-2 border-white text-white font-semibold rounded-lg hover:bg-white/10 transition"
              >
                Featured Products
              </Link>
            </div>
          </div>
        </div>
      </section>

      {/* Trust Badges */}
      <section className="bg-white border-b">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            {[
              { icon: HiTruck, text: 'Free Shipping', sub: 'On orders over $50' },
              { icon: HiRefresh, text: '30-Day Returns', sub: 'Hassle-free refunds' },
              { icon: HiCreditCard, text: 'Secure Payment', sub: 'Powered by Stripe' },
              { icon: HiShieldCheck, text: 'Quality Assured', sub: 'Verified products' },
            ].map((badge, i) => (
              <div key={i} className="flex items-center space-x-3">
                <badge.icon className="h-8 w-8 text-indigo-600 flex-shrink-0" />
                <div>
                  <p className="font-semibold text-gray-900 text-sm">{badge.text}</p>
                  <p className="text-xs text-gray-500">{badge.sub}</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* Categories */}
      {categories.length > 0 && (
        <section className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-gray-900">Shop by Category</h2>
            <Link to="/products" className="text-indigo-600 hover:text-indigo-700 text-sm font-medium flex items-center">
              View All <HiArrowRight className="ml-1 h-4 w-4" />
            </Link>
          </div>
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-4">
            {categories.slice(0, 6).map((cat) => (
              <Link
                key={cat.id}
                to={`/products?category=${cat.id}`}
                className="bg-white rounded-xl border hover:border-indigo-300 hover:shadow-md transition p-4 text-center group"
              >
                <div className="w-12 h-12 bg-indigo-50 rounded-lg flex items-center justify-center mx-auto mb-3 group-hover:bg-indigo-100 transition">
                  <span className="text-2xl">📦</span>
                </div>
                <p className="text-sm font-medium text-gray-800 group-hover:text-indigo-600 transition">{cat.name}</p>
              </Link>
            ))}
          </div>
        </section>
      )}

      {/* Featured Products */}
      <section className="bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
          <div className="flex items-center justify-between mb-6">
            <h2 className="text-2xl font-bold text-gray-900">Featured Products</h2>
            <Link to="/products?featured=true" className="text-indigo-600 hover:text-indigo-700 text-sm font-medium flex items-center">
              See All <HiArrowRight className="ml-1 h-4 w-4" />
            </Link>
          </div>

          {loading ? (
            <ProductGridSkeleton count={4} />
          ) : featured.length > 0 ? (
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-6">
              {featured.slice(0, 8).map((product) => (
                <ProductCard key={product.id} product={product} />
              ))}
            </div>
          ) : (
            <p className="text-center text-gray-500 py-8">No featured products yet. Add some via Swagger!</p>
          )}
        </div>
      </section>

      {/* CTA Banner */}
      <section className="bg-indigo-700 text-white">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12 text-center">
          <h2 className="text-2xl md:text-3xl font-bold mb-4">Ready to start shopping?</h2>
          <p className="text-indigo-200 mb-6">Join thousands of happy customers today.</p>
          <Link
            to="/register"
            className="inline-flex items-center px-8 py-3 bg-white text-indigo-700 font-semibold rounded-lg hover:bg-indigo-50 transition"
          >
            Create Free Account
          </Link>
        </div>
      </section>
    </div>
  );
};

export default Home;