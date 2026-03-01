import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchOrderDetail, cancelOrder } from '../store/orderSlice';
import { ProductDetailSkeleton } from '../components/common/LoadingSkeleton';
import toast from 'react-hot-toast';
import { HiArrowLeft } from 'react-icons/hi';

const statusSteps = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];

const OrderDetail = () => {
  const { orderNumber } = useParams();
  const dispatch = useDispatch();
  const { currentOrder: order, detailLoading: loading } = useSelector((state) => state.orders);
  const [cancelReason, setCancelReason] = useState('');
  const [showCancel, setShowCancel] = useState(false);

  useEffect(() => {
    dispatch(fetchOrderDetail(orderNumber));
  }, [dispatch, orderNumber]);

  const handleCancel = async () => {
    if (!cancelReason.trim()) { toast.error('Please provide a reason'); return; }
    const result = await dispatch(cancelOrder({ orderNumber, reason: cancelReason }));
    if (cancelOrder.fulfilled.match(result)) {
      toast.success('Order cancelled');
      setShowCancel(false);
    }
  };

  if (loading || !order) return <ProductDetailSkeleton />;

  const currentStepIndex = statusSteps.indexOf(order.status);

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <Link to="/orders" className="inline-flex items-center text-sm text-gray-500 hover:text-gray-700 mb-6">
        <HiArrowLeft className="h-4 w-4 mr-1" /> Back to Orders
      </Link>

      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Order {order.orderNumber}</h1>
          <p className="text-sm text-gray-500">Placed on {new Date(order.createdAt).toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' })}</p>
        </div>
        <div className="mt-2 sm:mt-0">
          <span className={`px-3 py-1 rounded-full text-sm font-medium ${
            order.status === 'DELIVERED' ? 'bg-green-100 text-green-700' :
            order.status === 'CANCELLED' ? 'bg-red-100 text-red-700' : 'bg-indigo-100 text-indigo-700'
          }`}>
            {order.statusDisplayName || order.status}
          </span>
        </div>
      </div>

      {/* Progress Bar */}
      {order.status !== 'CANCELLED' && currentStepIndex >= 0 && (
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
          <div className="flex justify-between">
            {statusSteps.map((step, i) => (
              <div key={step} className="flex flex-col items-center flex-1">
                <div className={`w-8 h-8 rounded-full flex items-center justify-center text-xs font-bold ${
                  i <= currentStepIndex ? 'bg-indigo-600 text-white' : 'bg-gray-200 text-gray-500'
                }`}>
                  {i <= currentStepIndex ? '✓' : i + 1}
                </div>
                <span className="text-xs mt-1 text-gray-500 hidden sm:block">{step.charAt(0) + step.slice(1).toLowerCase()}</span>
              </div>
            ))}
          </div>
        </div>
      )}

      {/* Items */}
      <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
        <h2 className="text-lg font-semibold text-gray-900 mb-4">Order Items</h2>
        <div className="space-y-4">
          {order.items?.map((item) => (
            <div key={item.id} className="flex items-center space-x-4">
              <div className="w-16 h-16 rounded-lg bg-gray-100 overflow-hidden flex-shrink-0">
                <img src={item.productImage || 'https://via.placeholder.com/64'} alt="" className="w-full h-full object-cover" />
              </div>
              <div className="flex-1 min-w-0">
                <Link to={`/products/${item.productId}`} className="text-sm font-medium text-gray-900 hover:text-indigo-600">{item.productName}</Link>
                <p className="text-xs text-gray-500">{item.productBrand} · SKU: {item.productSku}</p>
                <p className="text-xs text-gray-500">Qty: {item.quantity} × ${Number(item.effectivePrice).toFixed(2)}</p>
              </div>
              <p className="text-sm font-semibold">${Number(item.totalPrice).toFixed(2)}</p>
            </div>
          ))}
        </div>

        <hr className="my-4" />
        <div className="space-y-1 text-sm">
          <div className="flex justify-between"><span className="text-gray-500">Subtotal</span><span>${Number(order.subtotal).toFixed(2)}</span></div>
          <div className="flex justify-between"><span className="text-gray-500">Shipping</span><span>{Number(order.shippingCost) === 0 ? 'FREE' : `$${Number(order.shippingCost).toFixed(2)}`}</span></div>
          <div className="flex justify-between"><span className="text-gray-500">Tax</span><span>${Number(order.taxAmount).toFixed(2)}</span></div>
          <div className="flex justify-between text-base font-bold pt-2 border-t"><span>Total</span><span>${Number(order.totalAmount).toFixed(2)}</span></div>
        </div>
      </div>

      {/* Shipping Address */}
      {order.shippingAddress && (
        <div className="bg-white rounded-xl shadow-sm p-6 mb-6">
          <h2 className="text-lg font-semibold text-gray-900 mb-2">Shipping Address</h2>
          <p className="text-sm text-gray-600">{order.shippingAddress.fullName}</p>
          <p className="text-sm text-gray-600">{order.shippingAddress.addressLine1}</p>
          {order.shippingAddress.addressLine2 && <p className="text-sm text-gray-600">{order.shippingAddress.addressLine2}</p>}
          <p className="text-sm text-gray-600">{order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.postalCode}</p>
        </div>
      )}

      {/* Cancel Button */}
      {order.canCancel && (
        <div className="bg-white rounded-xl shadow-sm p-6">
          {!showCancel ? (
            <button onClick={() => setShowCancel(true)} className="text-red-600 hover:text-red-700 text-sm font-medium">
              Cancel this order
            </button>
          ) : (
            <div className="space-y-3">
              <textarea value={cancelReason} onChange={(e) => setCancelReason(e.target.value)} rows={2} placeholder="Reason for cancellation..." className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-red-500" />
              <div className="flex space-x-3">
                <button onClick={handleCancel} className="px-4 py-2 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 transition">Confirm Cancel</button>
                <button onClick={() => setShowCancel(false)} className="px-4 py-2 border border-gray-300 text-gray-600 text-sm rounded-lg hover:bg-gray-50 transition">Never mind</button>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default OrderDetail;