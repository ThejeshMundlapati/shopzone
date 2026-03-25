import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchWishlist, removeFromWishlist, moveToCart, moveAllToCart } from '../store/wishlistSlice';
import { fetchCart } from '../store/cartSlice';
import EmptyState from '../components/common/EmptyState';
import { HiOutlineShoppingCart, HiTrash } from 'react-icons/hi';

const Wishlist = () => {
  const dispatch = useDispatch();
  const { items, loading } = useSelector((state) => state.wishlist);

  useEffect(() => { dispatch(fetchWishlist()); }, [dispatch]);

  const handleMoveToCart = async (productId) => {
    await dispatch(moveToCart(productId));
    dispatch(fetchCart());
  };

  const handleMoveAll = async () => {
    await dispatch(moveAllToCart());
    dispatch(fetchCart());
  };

  if (loading) return <div className="max-w-4xl mx-auto px-4 py-8 animate-pulse space-y-4">{[1,2,3].map(i=><div key={i} className="h-24 bg-gray-200 rounded-xl"/>)}</div>;

  if (!items || items.length === 0) {
    return <EmptyState icon="💝" title="Your wishlist is empty" description="Save products you love for later." actionText="Browse Products" actionLink="/products" />;
  }

  return (
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
        <div className="flex items-center justify-between mb-6">
          <h1 className="text-2xl font-bold text-gray-900">My Wishlist ({items.length} items)</h1>
          <button onClick={handleMoveAll} className="flex items-center space-x-1 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 transition">
            <HiOutlineShoppingCart className="h-4 w-4" /><span>Move All to Cart</span>
          </button>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
          {items.map((item) => {
            const displayPrice = Number(item.effectivePrice || item.discountPrice || item.price || item.productPrice || 0);
            const originalPrice = Number(item.price || item.productPrice || 0);
            const hasDiscount = item.hasDiscount || (item.discountPrice && item.discountPrice < item.price);
            const image = item.imageUrl || item.productImage || 'https://via.placeholder.com/200';

            return (
                <div key={item.productId} className="bg-white rounded-xl shadow-sm border p-4">
                  <Link to={`/products/${item.productId}`} className="block">
                    <div className="aspect-square rounded-lg overflow-hidden bg-gray-100 mb-3">
                      <img src={image} alt={item.productName} className="w-full h-full object-cover hover:scale-105 transition-transform" />
                    </div>
                    <h3 className="text-sm font-medium text-gray-900 line-clamp-2 hover:text-indigo-600 transition">{item.productName}</h3>
                  </Link>

                  <div className="flex items-baseline space-x-2 mt-2">
                    <p className="text-lg font-bold text-gray-900">${displayPrice.toFixed(2)}</p>
                    {hasDiscount && originalPrice > displayPrice && (
                        <p className="text-sm text-gray-400 line-through">${originalPrice.toFixed(2)}</p>
                    )}
                  </div>

                  {item.inStock === false && <p className="text-xs text-red-500 mt-1">Out of stock</p>}

                  <div className="flex space-x-2 mt-3">
                    <button onClick={() => handleMoveToCart(item.productId)} disabled={item.inStock === false}
                            className="flex-1 flex items-center justify-center space-x-1 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-300 text-white text-sm py-2 rounded-lg transition">
                      <HiOutlineShoppingCart className="h-4 w-4" /><span>Move to Cart</span>
                    </button>
                    <button onClick={() => dispatch(removeFromWishlist(item.productId))} className="p-2 border border-gray-300 text-gray-400 hover:text-red-500 rounded-lg transition">
                      <HiTrash className="h-4 w-4" />
                    </button>
                  </div>
                </div>
            );
          })}
        </div>
      </div>
  );
};

export default Wishlist;