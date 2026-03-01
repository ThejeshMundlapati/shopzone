import { Link, useSearchParams } from 'react-router-dom';
import { HiCheckCircle } from 'react-icons/hi';

const PaymentSuccess = () => {
  const [searchParams] = useSearchParams();
  const orderNumber = searchParams.get('order');

  return (
    <div className="min-h-[60vh] flex items-center justify-center px-4">
      <div className="text-center max-w-md animate-fade-in">
        <HiCheckCircle className="h-20 w-20 text-green-500 mx-auto mb-4" />
        <h1 className="text-2xl font-bold text-gray-900 mb-2">Payment Successful!</h1>
        <p className="text-gray-600 mb-2">Thank you for your order.</p>
        {orderNumber && (
          <p className="text-sm text-gray-500 mb-6">
            Order Number: <span className="font-mono font-semibold text-gray-700">{orderNumber}</span>
          </p>
        )}
        <div className="flex flex-col sm:flex-row items-center justify-center space-y-3 sm:space-y-0 sm:space-x-4">
          {orderNumber && (
            <Link to={`/orders/${orderNumber}`} className="px-6 py-2.5 bg-indigo-600 text-white rounded-lg hover:bg-indigo-700 transition font-medium text-sm">
              View Order
            </Link>
          )}
          <Link to="/products" className="px-6 py-2.5 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition font-medium text-sm">
            Continue Shopping
          </Link>
        </div>
      </div>
    </div>
  );
};

export default PaymentSuccess;