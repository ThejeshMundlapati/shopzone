import { useState } from 'react';
import { CardElement, useStripe, useElements } from '@stripe/react-stripe-js';
import toast from 'react-hot-toast';
import { HiLockClosed } from 'react-icons/hi';

const CARD_ELEMENT_OPTIONS = {
  style: {
    base: {
      fontSize: '16px',
      color: '#1e293b',
      fontFamily: 'Inter, system-ui, sans-serif',
      '::placeholder': { color: '#94a3b8' },
    },
    invalid: { color: '#ef4444' },
  },
};

const PaymentForm = ({ clientSecret, onSuccess, onError, amount }) => {
  const stripe = useStripe();
  const elements = useElements();
  const [processing, setProcessing] = useState(false);
  const [cardComplete, setCardComplete] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!stripe || !elements || !clientSecret) return;

    setProcessing(true);

    try {
      const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: elements.getElement(CardElement),
        },
      });

      if (error) {
        toast.error(error.message);
        onError?.(error.message);
      } else if (paymentIntent.status === 'succeeded') {
        toast.success('Payment successful!');
        onSuccess?.(paymentIntent);
      }
    } catch (err) {
      toast.error('Payment failed. Please try again.');
      onError?.(err.message);
    }

    setProcessing(false);
  };

  return (
    <div>
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Payment Details</h3>

      <div className="bg-white border rounded-xl p-6">
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-700 mb-2">Card Information</label>
          <CardElement
            options={CARD_ELEMENT_OPTIONS}
            onChange={(e) => setCardComplete(e.complete)}
          />
        </div>

        <button
          onClick={handleSubmit}
          disabled={processing || !stripe || !cardComplete}
          className="w-full flex items-center justify-center space-x-2 bg-indigo-600 hover:bg-indigo-700 disabled:bg-gray-300 disabled:cursor-not-allowed text-white font-medium py-3 rounded-lg transition"
        >
          {processing ? (
            <div className="h-5 w-5 border-2 border-white border-t-transparent rounded-full animate-spin" />
          ) : (
            <>
              <HiLockClosed className="h-5 w-5" />
              <span>Pay ${Number(amount).toFixed(2)}</span>
            </>
          )}
        </button>

        <div className="mt-3 text-center text-xs text-gray-500">
          <p>Test card: 4242 4242 4242 4242 | Any future date | Any CVC</p>
        </div>
      </div>
    </div>
  );
};

export default PaymentForm;