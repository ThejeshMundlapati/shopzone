import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useSelector, useDispatch } from 'react-redux';
import { loadStripe } from '@stripe/stripe-js';
import { Elements } from '@stripe/react-stripe-js';
import orderService from '../services/orderService';
import { fetchCart } from '../store/cartSlice';
import AddressSelector from '../components/checkout/AddressSelector';
import OrderSummary from '../components/checkout/OrderSummary';
import PaymentForm from '../components/checkout/PaymentForm';
import toast from 'react-hot-toast';

const stripePromise = loadStripe(import.meta.env.VITE_STRIPE_PUBLISHABLE_KEY);

const Checkout = () => {
  const [step, setStep] = useState(0);
  const [selectedAddressId, setSelectedAddressId] = useState(null);
  const [preview, setPreview] = useState(null);
  const [orderNumber, setOrderNumber] = useState(null);
  const [clientSecret, setClientSecret] = useState(null);
  const [customerNotes, setCustomerNotes] = useState('');
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();
  const dispatch = useDispatch();
  const { items: cartItems, totalItems } = useSelector((state) => state.cart);

  useEffect(() => {
    if (totalItems === 0 && step === 0) {
      // Don't show error, just redirect silently
      navigate('/cart', { replace: true });
    }
  }, [totalItems, step, navigate]);

  const handleAddressSelected = async (addressId) => {
    setSelectedAddressId(addressId);
    if (addressId) {
      try {
        const res = await orderService.getCheckoutPreview(addressId);
        setPreview(res.data);
      } catch (err) {
        toast.error(err.response?.data?.message || 'Failed to load preview');
      }
    }
  };

  const handlePlaceOrder = async () => {
    setLoading(true);
    try {
      const orderRes = await orderService.placeOrder({
        shippingAddressId: selectedAddressId,
        customerNotes: customerNotes || undefined,
      });

      // Response: { data: { order: { orderNumber }, payment: { clientSecret } } }
      const order = orderRes.data?.order;
      const payment = orderRes.data?.payment;

      setOrderNumber(order?.orderNumber);
      setClientSecret(payment?.clientSecret);

      setStep(2);
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to place order');
    }
    setLoading(false);
  };

  const handlePaymentSuccess = () => {
    dispatch(fetchCart());
    navigate(`/payment/success?order=${orderNumber}`);
  };

  // Helper to get line total for a cart item
  const getLineTotal = (item) => {
    const sub = Number(item.subtotal);
    if (!isNaN(sub) && sub > 0) return sub;
    const ep = Number(item.effectivePrice) || Number(item.price) || 0;
    return ep * item.quantity;
  };

  return (
    <div className="max-w-5xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">Checkout</h1>

      {/* Step Indicator */}
      <div className="flex items-center mb-8">
        {['Shipping', 'Review', 'Payment'].map((label, i) => (
          <div key={i} className="flex items-center flex-1">
            <div className={`flex items-center justify-center w-8 h-8 rounded-full text-sm font-medium ${
              i <= step ? 'bg-indigo-600 text-white' : 'bg-gray-200 text-gray-500'
            }`}>
              {i + 1}
            </div>
            <span className={`ml-2 text-sm font-medium ${i <= step ? 'text-indigo-600' : 'text-gray-400'}`}>{label}</span>
            {i < 2 && <div className={`flex-1 h-0.5 mx-4 ${i < step ? 'bg-indigo-600' : 'bg-gray-200'}`} />}
          </div>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <div className="lg:col-span-2">
          {/* Step 1: Address */}
          {step === 0 && (
            <div>
              <AddressSelector selectedId={selectedAddressId} onSelect={handleAddressSelected} />
              <button
                onClick={() => setStep(1)}
                disabled={!selectedAddressId}
                className="mt-6 w-full bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-300 text-white font-medium py-3 rounded-lg transition"
              >
                Continue to Review
              </button>
            </div>
          )}

          {/* Step 2: Review */}
          {step === 1 && (
            <div>
              <h3 className="text-lg font-semibold text-gray-900 mb-4">Review Your Order</h3>

              <div className="bg-white border rounded-lg p-4 mb-4 space-y-3">
                {cartItems.map((item) => (
                  <div key={item.productId} className="flex items-center space-x-3">
                    <div className="w-14 h-14 rounded bg-gray-100 overflow-hidden flex-shrink-0">
                      <img src={item.imageUrl || 'https://via.placeholder.com/56'} alt="" className="w-full h-full object-cover" />
                    </div>
                    <div className="flex-1">
                      <p className="text-sm font-medium text-gray-900">{item.productName}</p>
                      <p className="text-xs text-gray-500">Qty: {item.quantity} × ${(Number(item.effectivePrice) || Number(item.price) || 0).toFixed(2)}</p>
                    </div>
                    <p className="text-sm font-semibold">${getLineTotal(item).toFixed(2)}</p>
                  </div>
                ))}
              </div>

              <div className="mb-4">
                <label className="block text-sm font-medium text-gray-700 mb-1">Order Notes (optional)</label>
                <textarea
                  value={customerNotes} onChange={(e) => setCustomerNotes(e.target.value)}
                  rows={2} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
                  placeholder="Special delivery instructions..."
                />
              </div>

              <div className="flex space-x-4">
                <button onClick={() => setStep(0)} className="flex-1 border border-gray-300 text-gray-700 py-3 rounded-lg hover:bg-gray-50 transition font-medium">
                  Back
                </button>
                <button onClick={handlePlaceOrder} disabled={loading} className="flex-1 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-300 text-white py-3 rounded-lg transition font-medium flex items-center justify-center">
                  {loading ? <div className="h-5 w-5 border-2 border-white border-t-transparent rounded-full animate-spin" /> : 'Place Order & Pay'}
                </button>
              </div>
            </div>
          )}

          {/* Step 3: Payment */}
          {step === 2 && clientSecret && (
            <Elements stripe={stripePromise} options={{ clientSecret }}>
              <PaymentForm
                clientSecret={clientSecret}
                amount={preview?.totalAmount || 0}
                onSuccess={handlePaymentSuccess}
                onError={(msg) => toast.error(msg)}
              />
            </Elements>
          )}
        </div>

        {/* Sidebar Summary */}
        <div>
          <OrderSummary preview={preview} cartItems={cartItems} />
        </div>
      </div>
    </div>
  );
};

export default Checkout;