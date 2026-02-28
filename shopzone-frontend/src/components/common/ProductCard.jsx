import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { HiOutlineHeart, HiHeart, HiOutlineShoppingCart, HiStar } from 'react-icons/hi';
import { addToCart } from '../../store/cartSlice';
import { addToWishlist, removeFromWishlist } from '../../store/wishlistSlice';

const ProductCard = ({ product }) => {
  const dispatch = useDispatch();
  const { isAuthenticated } = useSelector((state) => state.auth);
  const { items: wishlistItems } = useSelector((state) => state.wishlist);

  const isInWishlist = wishlistItems?.some((item) => item.productId === product.id);

  const discountPrice = product.discountPrice || product.salePrice;
  const effectivePrice = discountPrice && discountPrice < product.price ? discountPrice : product.price;
  const hasDiscount = discountPrice && discountPrice < product.price;
  
  const discountPercentage = product.discountPercentage || 
    (hasDiscount ? Math.round((1 - discountPrice / product.price) * 100) : 0);

  const handleAddToCart = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isAuthenticated) {
      window.location.href = '/login';
      return;
    }
    dispatch(addToCart({ productId: product.id }));
  };

  const handleToggleWishlist = (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isAuthenticated) {
      window.location.href = '/login';
      return;
    }
    if (isInWishlist) {
      dispatch(removeFromWishlist(product.id));
    } else {
      dispatch(addToWishlist(product.id));
    }
  };

  // Safe SVG placeholder that won't trigger network errors
  const placeholderImg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' width='300' height='300'%3E%3Crect width='300' height='300' fill='%23f3f4f6'/%3E%3Ctext x='50%25' y='50%25' dominant-baseline='middle' text-anchor='middle' font-family='sans-serif' font-size='16' fill='%239ca3af'%3ENo Image%3C/text%3E%3C/svg%3E";

  const imageUrl = product.images?.length > 0
    ? product.images[0]
    : product.imageUrl
    ? product.imageUrl
    : placeholderImg;

  return (
    <Link to={`/products/${product.id}`} className="group">
      <div className="bg-white rounded-lg shadow-md hover:shadow-xl transition-all duration-300 overflow-hidden flex flex-col h-full">
        {/* Image */}
        <div className="relative aspect-square overflow-hidden bg-gray-100">
          <img
            src={imageUrl}
            alt={product.name}
            className="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300 text-transparent"
            loading="lazy"
            onError={(e) => {
              e.target.onerror = null; 
              e.target.src = placeholderImg;
            }}
          />

          {/* Badges */}
          <div className="absolute top-2 left-2 flex flex-col space-y-1">
            {hasDiscount && (
              <span className="bg-red-500 text-white text-xs font-bold px-2 py-0.5 rounded">
                -{discountPercentage}%
              </span>
            )}
            {product.featured && (
              <span className="bg-amber-500 text-white text-xs font-bold px-2 py-0.5 rounded">
                Featured
              </span>
            )}
          </div>

          {/* Wishlist Button */}
          <button
            onClick={handleToggleWishlist}
            className="absolute top-2 right-2 p-1.5 bg-white rounded-full shadow hover:shadow-md transition"
          >
            {isInWishlist ? (
              <HiHeart className="h-5 w-5 text-pink-500" />
            ) : (
              <HiOutlineHeart className="h-5 w-5 text-gray-400 hover:text-pink-500" />
            )}
          </button>

          {/* Out of Stock Overlay */}
          {product.stock === 0 && (
            <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
              <span className="bg-white text-gray-800 font-semibold px-4 py-1.5 rounded">Out of Stock</span>
            </div>
          )}
        </div>

        {/* Info */}
        <div className="p-4 flex flex-col flex-1">
          {product.brand && (
            <p className="text-xs text-gray-500 uppercase tracking-wide mb-1">{product.brand}</p>
          )}

          <h3 className="text-sm font-semibold text-gray-800 line-clamp-2 group-hover:text-indigo-600 transition mb-2">
            {product.name}
          </h3>

          {(product.averageRating > 0 || product.reviewCount > 0) && (
            <div className="flex items-center space-x-1 mb-2">
              <HiStar className="h-4 w-4 text-amber-400" />
              <span className="text-sm text-gray-600">{product.averageRating?.toFixed(1) || '0.0'}</span>
              <span className="text-xs text-gray-400">({product.reviewCount || 0})</span>
            </div>
          )}

          <div className="mt-auto">
            <div className="flex items-baseline space-x-2">
              <span className="text-lg font-bold text-gray-900">${Number(effectivePrice).toFixed(2)}</span>
              {hasDiscount && (
                <span className="text-sm text-gray-400 line-through">${Number(product.price).toFixed(2)}</span>
              )}
            </div>
          </div>

          <button
            onClick={handleAddToCart}
            disabled={product.stock === 0}
            className="mt-3 w-full flex items-center justify-center space-x-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white text-sm font-medium py-2.5 rounded-lg transition"
          >
            <HiOutlineShoppingCart className="h-4 w-4" />
            <span>{product.stock === 0 ? 'Out of Stock' : 'Add to Cart'}</span>
          </button>
        </div>
      </div>
    </Link>
  );
};

export default ProductCard;