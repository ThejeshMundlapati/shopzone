import { useEffect, useState } from 'react';
import { HiOutlineTrash, HiOutlineStar, HiOutlineSearch } from 'react-icons/hi';
import adminService from '../../services/adminService';
import toast from 'react-hot-toast';

const fmtDate = (d) => d ? new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '—';

const AdminReviews = () => {
  const [products, setProducts] = useState([]);
  const [selectedProduct, setSelectedProduct] = useState('');
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [searchInput, setSearchInput] = useState('');

  // Load products for the dropdown
  useEffect(() => {
    adminService.getAllProducts({ page: 0, size: 100, sortBy: 'name', sortDir: 'asc' })
      .then(res => setProducts(res.data?.data?.content || []))
      .catch(() => {});
  }, []);

  // Load reviews when product selected
  useEffect(() => {
    if (!selectedProduct) { setReviews([]); return; }
    setLoading(true);
    adminService.getProductReviews(selectedProduct, { page, size: 20 })
      .then(res => {
        const data = res.data?.data;
        setReviews(data?.content || []);
        setTotalPages(data?.totalPages || 0);
      })
      .catch(() => toast.error('Failed to load reviews'))
      .finally(() => setLoading(false));
  }, [selectedProduct, page]);

  const handleDelete = async (reviewId) => {
    if (!confirm('Delete this review? This cannot be undone.')) return;
    try {
      await adminService.adminDeleteReview(reviewId);
      toast.success('Review deleted');
      setReviews(prev => prev.filter(r => r.id !== reviewId));
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to delete review');
    }
  };

  const filteredProducts = searchInput
    ? products.filter(p => p.name.toLowerCase().includes(searchInput.toLowerCase()))
    : products;

  const Stars = ({ count }) => (
    <div className="flex gap-0.5">
      {[1, 2, 3, 4, 5].map(i => (
        <HiOutlineStar key={i} className={`h-4 w-4 ${i <= count ? 'text-yellow-400 fill-yellow-400' : 'text-gray-300'}`} />
      ))}
    </div>
  );

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">Review Moderation</h1>
      <p className="text-sm text-gray-500">Select a product to view and manage its reviews.</p>

      {/* Product Search & Select */}
      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <div className="relative mb-3">
          <HiOutlineSearch className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
          <input type="text" placeholder="Search products..." value={searchInput} onChange={e => setSearchInput(e.target.value)}
            className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" />
        </div>
        <div className="max-h-48 overflow-y-auto space-y-1">
          {filteredProducts.slice(0, 20).map(p => (
            <button key={p.id} onClick={() => { setSelectedProduct(p.id); setPage(0); }}
              className={`w-full text-left px-3 py-2 rounded-lg text-sm transition flex items-center justify-between ${
                selectedProduct === p.id ? 'bg-indigo-50 text-indigo-700' : 'hover:bg-gray-50 text-gray-700'
              }`}>
              <span className="truncate">{p.name}</span>
              {p.reviewCount > 0 && <span className="text-xs text-gray-400 flex-shrink-0 ml-2">{p.reviewCount} reviews</span>}
            </button>
          ))}
          {filteredProducts.length === 0 && <p className="text-sm text-gray-400 text-center py-2">No products found</p>}
        </div>
      </div>

      {/* Reviews List */}
      {selectedProduct && (
        <div className="space-y-3">
          {loading ? (
            <div className="flex items-center justify-center h-32"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" /></div>
          ) : reviews.length === 0 ? (
            <div className="bg-white rounded-xl border border-gray-200 p-8 text-center text-gray-500">No reviews for this product</div>
          ) : (
            <>
              {reviews.map(r => (
                <div key={r.id} className="bg-white rounded-xl border border-gray-200 p-4">
                  <div className="flex items-start justify-between">
                    <div className="flex-1 min-w-0">
                      <div className="flex items-center gap-2 mb-1">
                        <Stars count={r.rating} />
                        <span className="text-sm font-medium text-gray-900">{r.userName}</span>
                        {r.verifiedPurchase && <span className="text-xs bg-green-100 text-green-700 px-1.5 py-0.5 rounded font-medium">Verified</span>}
                      </div>
                      {r.title && <p className="text-sm font-medium text-gray-800 mb-1">{r.title}</p>}
                      <p className="text-sm text-gray-600">{r.comment}</p>
                      <div className="flex items-center gap-3 mt-2 text-xs text-gray-400">
                        <span>{fmtDate(r.createdAt)}</span>
                        {r.helpfulCount > 0 && <span>{r.helpfulCount} found helpful</span>}
                      </div>
                    </div>
                    <button onClick={() => handleDelete(r.id)} className="p-1.5 text-gray-400 hover:text-red-600 hover:bg-red-50 rounded-lg transition flex-shrink-0 ml-3">
                      <HiOutlineTrash className="h-4 w-4" />
                    </button>
                  </div>
                </div>
              ))}
              {/* Simple pagination */}
              {totalPages > 1 && (
                <div className="flex items-center justify-center gap-2">
                  <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0}
                    className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm disabled:opacity-40">Prev</button>
                  <span className="text-sm text-gray-500">Page {page + 1} of {totalPages}</span>
                  <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page >= totalPages - 1}
                    className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm disabled:opacity-40">Next</button>
                </div>
              )}
            </>
          )}
        </div>
      )}
    </div>
  );
};

export default AdminReviews;