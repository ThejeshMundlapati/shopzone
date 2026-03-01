import { useDispatch } from 'react-redux';
import { Link } from 'react-router-dom';
import { updateCartItem, removeFromCart } from '../../store/cartSlice';
import { HiTrash } from 'react-icons/hi';

const CartItem = ({ item }) => {
  const dispatch = useDispatch();

  const handleQuantityChange = (newQty) => {
    if (newQty < 1) return;
    dispatch(updateCartItem({ productId: item.productId, quantity: newQty }));
  };

  const unitPrice = Number(item.effectivePrice) || Number(item.price) || 0;
  const lineTotal = Number(item.subtotal) || unitPrice * item.quantity;
  const hasDiscount = item.discountPrice && Number(item.discountPrice) < Number(item.price);

  return (
    <div className="flex items-center space-x-4 py-4 border-b last:border-b-0">
      <Link to={`/products/${item.productId}`} className="flex-shrink-0">
        <div className="w-20 h-20 rounded-lg overflow-hidden bg-gray-100">
          <img src={item.imageUrl || 'https://via.placeholder.com/80?text=No+Image'} alt={item.productName} className="w-full h-full object-cover" />
        </div>
      </Link>

      <div className="flex-1 min-w-0">
        <Link to={`/products/${item.productId}`} className="text-sm font-medium text-gray-900 hover:text-indigo-600 line-clamp-2">
          {item.productName}
        </Link>
        <div className="flex items-baseline space-x-2 mt-1">
          <span className="text-sm font-semibold text-gray-900">${unitPrice.toFixed(2)}</span>
          {hasDiscount && (
            <span className="text-xs text-gray-400 line-through">${Number(item.price).toFixed(2)}</span>
          )}
        </div>
      </div>

      <div className="flex items-center border border-gray-300 rounded-lg">
        <button onClick={() => handleQuantityChange(item.quantity - 1)} className="px-2.5 py-1 text-gray-600 hover:bg-gray-100 rounded-l-lg text-sm">−</button>
        <span className="px-3 py-1 text-sm font-medium min-w-[32px] text-center">{item.quantity}</span>
        <button onClick={() => handleQuantityChange(item.quantity + 1)} className="px-2.5 py-1 text-gray-600 hover:bg-gray-100 rounded-r-lg text-sm">+</button>
      </div>

      <div className="text-right hidden sm:block">
        <p className="text-sm font-semibold text-gray-900">${lineTotal.toFixed(2)}</p>
      </div>

      <button onClick={() => dispatch(removeFromCart(item.productId))} className="p-2 text-gray-400 hover:text-red-500 transition">
        <HiTrash className="h-5 w-5" />
      </button>
    </div>
  );
};

export default CartItem;