import { Link } from 'react-router-dom';
import { HiShieldCheck } from 'react-icons/hi';

const CartSummary = ({ subtotal, totalItems }) => {
  const freeShippingThreshold = 50;
  const shippingCost = subtotal >= freeShippingThreshold ? 0 : 5.99;
  const remaining = freeShippingThreshold - subtotal;

  return (
    <div className="bg-gray-50 rounded-xl p-6 sticky top-24">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Order Summary</h3>

      <div className="space-y-3 text-sm">
        <div className="flex justify-between">
          <span className="text-gray-600">Subtotal ({totalItems} items)</span>
          <span className="font-medium">${Number(subtotal).toFixed(2)}</span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">Shipping</span>
          <span className={`font-medium ${shippingCost === 0 ? 'text-green-600' : ''}`}>
            {shippingCost === 0 ? 'FREE' : `$${shippingCost.toFixed(2)}`}
          </span>
        </div>

        {remaining > 0 && (
          <div className="bg-indigo-50 rounded-lg p-3 text-xs text-indigo-700">
            Add <span className="font-bold">${remaining.toFixed(2)}</span> more for free shipping!
            <div className="mt-1.5 h-1.5 bg-indigo-200 rounded-full overflow-hidden">
              <div
                className="h-full bg-indigo-600 rounded-full transition-all"
                style={{ width: `${Math.min(100, (subtotal / freeShippingThreshold) * 100)}%` }}
              />
            </div>
          </div>
        )}

        <hr />
        <div className="flex justify-between text-base">
          <span className="font-semibold text-gray-900">Estimated Total</span>
          <span className="font-bold text-gray-900">${(subtotal + shippingCost).toFixed(2)}</span>
        </div>
        <p className="text-xs text-gray-500">Tax calculated at checkout</p>
      </div>

      <Link
        to="/checkout"
        className="mt-6 block w-full text-center bg-indigo-600 hover:bg-indigo-700 text-white font-medium py-3 rounded-lg transition"
      >
        Proceed to Checkout
      </Link>

      <div className="mt-4 flex items-center justify-center space-x-1 text-xs text-gray-500">
        <HiShieldCheck className="h-4 w-4" />
        <span>Secure checkout powered by Stripe</span>
      </div>
    </div>
  );
};

export default CartSummary;