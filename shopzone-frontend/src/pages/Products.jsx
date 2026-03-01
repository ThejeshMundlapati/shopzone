import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchProducts, fetchCategories, searchProducts } from '../store/productSlice';
import ProductCard from '../components/common/ProductCard';
import Pagination from '../components/common/Pagination';
import { ProductGridSkeleton } from '../components/common/LoadingSkeleton';
import EmptyState from '../components/common/EmptyState';
import { HiAdjustments, HiX } from 'react-icons/hi';

const Products = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [showFilters, setShowFilters] = useState(false);
  const dispatch = useDispatch();
  const { items, categories, totalPages, currentPage, totalElements, loading } = useSelector((state) => state.products);

  // Read filters from URL
  const q = searchParams.get('q') || '';
  const category = searchParams.get('category') || '';
  const brand = searchParams.get('brand') || '';
  const minPrice = searchParams.get('minPrice') || '';
  const maxPrice = searchParams.get('maxPrice') || '';
  const sortBy = searchParams.get('sortBy') || 'newest';
  const sortDir = searchParams.get('sortDir') || 'desc';
  const featured = searchParams.get('featured') || '';
  const page = parseInt(searchParams.get('page') || '0', 10);

  // Local filter state
  const [filters, setFilters] = useState({ category, brand, minPrice, maxPrice });

  useEffect(() => {
    dispatch(fetchCategories());
  }, [dispatch]);

  useEffect(() => {
    const params = { page, size: 12, sortBy, sortDir };
    
    // Map Frontend names to Backend API expectations
    if (category) params.categoryId = category;
    if (brand) params.brand = brand;
    if (minPrice) params.minPrice = minPrice;
    if (maxPrice) params.maxPrice = maxPrice;
    if (featured) params.featured = featured;

    if (featured) {
      dispatch(fetchProducts({ featured: true, size: 12, page }));
    } else {
      // Use Elasticsearch for ALL general listing, filtering, and text searching
      dispatch(searchProducts({ query: q || '', params }));
    }
  }, [dispatch, q, page, sortBy, sortDir, category, brand, minPrice, maxPrice, featured]);

  const updateParam = (key, value) => {
    const params = new URLSearchParams(searchParams);
    if (value) { params.set(key, value); } else { params.delete(key); }
    params.delete('page'); // Reset to first page
    setSearchParams(params);
  };

  const applyFilters = () => {
    const params = new URLSearchParams(searchParams);
    Object.entries(filters).forEach(([k, v]) => {
      if (v) params.set(k, v); else params.delete(k);
    });
    params.delete('page');
    setSearchParams(params);
    setShowFilters(false);
  };

  const clearAllFilters = () => {
    setFilters({ category: '', brand: '', minPrice: '', maxPrice: '' });
    setSearchParams(q ? { q } : {});
  };

  const handlePageChange = (newPage) => {
    const params = new URLSearchParams(searchParams);
    params.set('page', newPage.toString());
    setSearchParams(params);
    window.scrollTo(0, 0);
  };

  const hasActiveFilters = category || brand || minPrice || maxPrice || featured;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6 gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {q ? `Search: "${q}"` : featured ? 'Featured Products' : 'All Products'}
          </h1>
          <p className="text-sm text-gray-500 mt-1">{totalElements} products found</p>
        </div>

        <div className="flex items-center space-x-3">
          {/* Sort */}
          <select
            value={`${sortBy}-${sortDir}`}
            onChange={(e) => {
              const [sb, sd] = e.target.value.split('-');
              const params = new URLSearchParams(searchParams);
              params.set('sortBy', sb); params.set('sortDir', sd); params.delete('page');
              setSearchParams(params);
            }}
            className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
          >
            <option value="newest-desc">Newest First</option>
            <option value="newest-asc">Oldest First</option>
            <option value="price-asc">Price: Low to High</option>
            <option value="price-desc">Price: High to Low</option>
            <option value="name-asc">Name: A to Z</option>
            <option value="rating-desc">Highest Rated</option>
          </select>

          {/* Filter Toggle (mobile) */}
          <button
            onClick={() => setShowFilters(!showFilters)}
            className="lg:hidden flex items-center space-x-1 px-3 py-2 border border-gray-300 rounded-lg text-sm hover:bg-gray-50"
          >
            <HiAdjustments className="h-4 w-4" />
            <span>Filters</span>
          </button>
        </div>
      </div>

      <div className="flex gap-8">
        {/* Sidebar Filters */}
        <aside className={`${showFilters ? 'fixed inset-0 z-40 bg-white p-6 overflow-y-auto lg:relative lg:inset-auto lg:z-auto lg:bg-transparent lg:p-0' : 'hidden'} lg:block w-full lg:w-64 flex-shrink-0`}>
          <div className="flex items-center justify-between lg:hidden mb-4">
            <h3 className="text-lg font-semibold">Filters</h3>
            <button onClick={() => setShowFilters(false)}><HiX className="h-6 w-6" /></button>
          </div>

          <div className="space-y-6">
            {/* Category Filter */}
            {categories.length > 0 && (
              <div>
                <h4 className="text-sm font-semibold text-gray-900 mb-2">Category</h4>
                <select
                  value={filters.category}
                  onChange={(e) => setFilters({ ...filters, category: e.target.value })}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm"
                >
                  <option value="">All Categories</option>
                  {categories.map((cat) => (
                    <option key={cat.id} value={cat.id}>{cat.name}</option>
                  ))}
                </select>
              </div>
            )}

            {/* Brand */}
            <div>
              <h4 className="text-sm font-semibold text-gray-900 mb-2">Brand</h4>
              <input
                type="text" value={filters.brand} onChange={(e) => setFilters({ ...filters, brand: e.target.value })}
                placeholder="Filter by brand..." className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm"
              />
            </div>

            {/* Price Range */}
            <div>
              <h4 className="text-sm font-semibold text-gray-900 mb-2">Price Range</h4>
              <div className="flex items-center space-x-2">
                <input
                  type="number" value={filters.minPrice} onChange={(e) => setFilters({ ...filters, minPrice: e.target.value })}
                  placeholder="Min" className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm" min="0"
                />
                <span className="text-gray-400">-</span>
                <input
                  type="number" value={filters.maxPrice} onChange={(e) => setFilters({ ...filters, maxPrice: e.target.value })}
                  placeholder="Max" className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm" min="0"
                />
              </div>
            </div>

            {/* Apply / Clear */}
            <div className="space-y-2">
              <button onClick={applyFilters} className="w-full bg-indigo-600 text-white py-2 rounded-lg text-sm font-medium hover:bg-indigo-700 transition">
                Apply Filters
              </button>
              {hasActiveFilters && (
                <button onClick={clearAllFilters} className="w-full border border-gray-300 text-gray-600 py-2 rounded-lg text-sm hover:bg-gray-50 transition">
                  Clear All
                </button>
              )}
            </div>
          </div>
        </aside>

        {/* Product Grid */}
        <main className="flex-1">
          {/* Active filter tags */}
          {hasActiveFilters && (
            <div className="flex flex-wrap gap-2 mb-4">
              {category && categories.find(c => c.id === category) && (
                <span className="inline-flex items-center px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full text-xs font-medium">
                  {categories.find(c => c.id === category)?.name}
                  <button onClick={() => updateParam('category', '')} className="ml-1"><HiX className="h-3 w-3" /></button>
                </span>
              )}
              {brand && (
                <span className="inline-flex items-center px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full text-xs font-medium">
                  Brand: {brand}
                  <button onClick={() => updateParam('brand', '')} className="ml-1"><HiX className="h-3 w-3" /></button>
                </span>
              )}
              {(minPrice || maxPrice) && (
                <span className="inline-flex items-center px-3 py-1 bg-indigo-50 text-indigo-700 rounded-full text-xs font-medium">
                  ${minPrice || '0'} - ${maxPrice || '∞'}
                  <button onClick={() => { updateParam('minPrice', ''); updateParam('maxPrice', ''); }} className="ml-1"><HiX className="h-3 w-3" /></button>
                </span>
              )}
            </div>
          )}

          {loading ? (
            <ProductGridSkeleton count={12} />
          ) : items.length > 0 ? (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
                {items.map((product) => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
              <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
            </>
          ) : (
            <EmptyState
              icon="🔍"
              title="No products found"
              description={q ? `No results for "${q}". Try different keywords.` : 'No products match your filters.'}
              actionText="Clear Filters"
              actionLink="/products"
            />
          )}
        </main>
      </div>
    </div>
  );
};

export default Products;