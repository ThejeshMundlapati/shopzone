import { Link } from 'react-router-dom';
import { HiXCircle } from 'react-icons/hi';

const PaymentCancel = () => {
  return (
    <div className="min-h-[60vh] flex items-center justify-center px-4">
      <div className="text-center max-w-md animate-fade-in">
        <HiXCircle className="h-20 w-20 text-red-400 mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Payment Cancelled</h1>
        <p className="text-gray-600 mb-6">Your payment was not processed. Your cart items are still saved.</p>
        <div className="flex flex-col sm:flex-row items-center justify-center space-y-3 sm:space-y-0 sm:space-x-4">
          <Link to="/cart" className="px-6 py-2.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition font-medium text-sm">
            Return to Cart
          </Link>
          <Link to="/products" className="px-6 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition font-medium text-sm">
            Continue Shopping
          </Link>
        </div>
      </div>
    </div>
  );
};

export default PaymentCancel;