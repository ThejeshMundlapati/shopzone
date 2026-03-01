import { useEffect } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { fetchCart, clearCart } from '../store/cartSlice';
import CartItem from '../components/cart/CartItem';
import CartSummary from '../components/cart/CartSummary';
import EmptyState from '../components/common/EmptyState';
import Skeleton from '../components/common/LoadingSkeleton';

const Cart = () => {
  const dispatch = useDispatch();
  const { items, totalItems, subtotal, loading } = useSelector((state) => state.cart);
  const { isAuthenticated } = useSelector((state) => state.auth);

  useEffect(() => {
    if (isAuthenticated) dispatch(fetchCart());
  }, [dispatch, isAuthenticated]);

  if (!isAuthenticated) {
    return (
      <EmptyState
        icon="🔒"
        title="Please sign in"
        description="You need to be logged in to view your cart."
        actionText="Sign In"
        actionLink="/login"
      />
    );
  }

  if (loading) {
    return (
      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="space-y-4">{Array.from({ length: 3 }).map((_, i) => <Skeleton key={i} className="h-24 w-full" />)}</div>
      </div>
    );
  }

  if (!items || items.length === 0) {
    return (
      <EmptyState
        icon="🛒"
        title="Your cart is empty"
        description="Browse our products and add items to your cart."
        actionText="Start Shopping"
        actionLink="/products"
      />
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">Shopping Cart ({totalItems} items)</h1>
        <button
          onClick={() => dispatch(clearCart())}
          className="text-sm text-red-600 hover:text-red-700 font-medium"
        >
          Clear Cart
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Cart Items */}
        <div className="lg:col-span-2 bg-white rounded-xl shadow-sm p-6">
          {items.map((item) => (
            <CartItem key={item.productId} item={item} />
          ))}
        </div>

        {/* Summary */}
        <div>
          <CartSummary subtotal={subtotal} totalItems={totalItems} />
        </div>
      </div>
    </div>
  );
};

export default Cart;