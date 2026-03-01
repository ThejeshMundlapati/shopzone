import { useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { useNavigate } from 'react-router-dom';
import { HiOutlineShoppingCart, HiHeart, HiOutlineHeart, HiCheck, HiX } from 'react-icons/hi';
import { addToCart } from '../../store/cartSlice';
import { addToWishlist, removeFromWishlist } from '../../store/wishlistSlice';
import StarRating from '../common/StarRating';

const ProductInfo = ({ product }) => {
  const [quantity, setQuantity] = useState(1);
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { isAuthenticated } = useSelector((state) => state.auth);
  const { items: wishlistItems } = useSelector((state) => state.wishlist);
  const isInWishlist = wishlistItems?.some((item) => item.productId === product.id);

  const effectivePrice = product.discountPrice && product.discountPrice < product.price
    ? product.discountPrice : product.price;
  const hasDiscount = product.discountPrice && product.discountPrice < product.price;
  const savings = hasDiscount ? (product.price - product.discountPrice).toFixed(2) : 0;
  const inStock = product.stock > 0;

  const handleAddToCart = () => {
    if (!isAuthenticated) { navigate('/login'); return; }
    dispatch(addToCart({ productId: product.id, quantity }));
  };

  const handleToggleWishlist = () => {
    if (!isAuthenticated) { navigate('/login'); return; }
    if (isInWishlist) dispatch(removeFromWishlist(product.id));
    else dispatch(addToWishlist(product.id));
  };

  return (
    <div className="space-y-5">
      {/* Brand */}
      {product.brand && (
        <p className="text-sm text-indigo-600 font-medium uppercase tracking-wide">{product.brand}</p>
      )}

      {/* Name */}
      <h1 className="text-2xl md:text-3xl font-bold text-gray-900">{product.name}</h1>

      {/* Rating */}
      <div className="flex items-center space-x-3">
        <StarRating rating={product.averageRating || 0} showValue count={product.reviewCount || 0} />
      </div>

      {/* Price */}
      <div className="flex items-baseline space-x-3">
        <span className="text-3xl font-bold text-gray-900">${Number(effectivePrice).toFixed(2)}</span>
        {hasDiscount && (
          <>
            <span className="text-lg text-gray-400 line-through">${Number(product.price).toFixed(2)}</span>
            <span className="text-sm font-medium text-green-600 bg-green-50 px-2 py-0.5 rounded">
              Save ${savings}
            </span>
          </>
        )}
      </div>

      {/* Stock Status */}
      <div className="flex items-center space-x-2">
        {inStock ? (
          <>
            <HiCheck className="h-5 w-5 text-green-500" />
            <span className="text-sm text-green-600 font-medium">
              In Stock {product.stock <= 10 && `(Only ${product.stock} left!)`}
            </span>
          </>
        ) : (
          <>
            <HiX className="h-5 w-5 text-red-500" />
            <span className="text-sm text-red-600 font-medium">Out of Stock</span>
          </>
        )}
      </div>

      {/* Description */}
      {product.description && (
        <div>
          <h3 className="text-sm font-semibold text-gray-900 mb-1">Description</h3>
          <p className="text-gray-600 text-sm leading-relaxed">{product.description}</p>
        </div>
      )}

      {/* SKU & Category */}
      <div className="flex flex-wrap gap-4 text-sm text-gray-500">
        {product.sku && <span>SKU: <span className="text-gray-700">{product.sku}</span></span>}
        {product.categoryName && <span>Category: <span className="text-gray-700">{product.categoryName}</span></span>}
      </div>

      {/* Tags */}
      {product.tags?.length > 0 && (
        <div className="flex flex-wrap gap-2">
          {product.tags.map((tag, i) => (
            <span key={i} className="px-2.5 py-0.5 bg-gray-100 text-gray-600 text-xs rounded-full">{tag}</span>
          ))}
        </div>
      )}

      {/* Quantity + Add to Cart */}
      {inStock && (
        <div className="flex items-center space-x-4 pt-2">
          <div className="flex items-center border border-gray-300 rounded-lg">
            <button
              onClick={() => setQuantity(Math.max(1, quantity - 1))}
              className="px-3 py-2 text-gray-600 hover:bg-gray-100 rounded-l-lg transition"
            >−</button>
            <span className="px-4 py-2 text-sm font-medium min-w-[40px] text-center">{quantity}</span>
            <button
              onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}
              className="px-3 py-2 text-gray-600 hover:bg-gray-100 rounded-r-lg transition"
            >+</button>
          </div>

          <button
            onClick={handleAddToCart}
            className="flex-1 flex items-center justify-center space-x-2 bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-3 rounded-lg transition"
          >
            <HiOutlineShoppingCart className="h-5 w-5" />
            <span>Add to Cart</span>
          </button>

          <button
            onClick={handleToggleWishlist}
            className={`p-3 border rounded-lg transition ${
              isInWishlist ? 'border-pink-300 bg-pink-50 text-pink-500' : 'border-gray-300 text-gray-400 hover:text-pink-500 hover:border-pink-300'
            }`}
          >
            {isInWishlist ? <HiHeart className="h-5 w-5" /> : <HiOutlineHeart className="h-5 w-5" />}
          </button>
        </div>
      )}

      {/* Product Details */}
      {product.details && (
        <div className="border-t pt-5 mt-5">
          <h3 className="text-sm font-semibold text-gray-900 mb-3">Product Details</h3>
          <dl className="grid grid-cols-2 gap-x-4 gap-y-2 text-sm">
            {product.details.weight && <><dt className="text-gray-500">Weight</dt><dd className="text-gray-700">{product.details.weight}</dd></>}
            {product.details.dimensions && <><dt className="text-gray-500">Dimensions</dt><dd className="text-gray-700">{product.details.dimensions}</dd></>}
            {product.details.color && <><dt className="text-gray-500">Color</dt><dd className="text-gray-700">{product.details.color}</dd></>}
            {product.details.material && <><dt className="text-gray-500">Material</dt><dd className="text-gray-700">{product.details.material}</dd></>}
            {product.details.specifications && Object.entries(product.details.specifications).map(([k, v]) => (
              <span key={k} className="contents"><dt className="text-gray-500">{k}</dt><dd className="text-gray-700">{v}</dd></span>
            ))}
          </dl>
        </div>
      )}
    </div>
  );
};

export default ProductInfo;