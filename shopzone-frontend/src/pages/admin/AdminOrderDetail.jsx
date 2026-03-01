import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { HiOutlineArrowLeft } from 'react-icons/hi';
import adminService from '../../services/adminService';
import toast from 'react-hot-toast';

const statusFlow = ['PENDING', 'CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED'];
const fmt = (n) => n != null ? `$${Number(n).toFixed(2)}` : '$0.00';
const fmtDate = (d) => d ? new Date(d).toLocaleString('en-US', { month: 'short', day: 'numeric', year: 'numeric', hour: '2-digit', minute: '2-digit' }) : '—';

const AdminOrderDetail = () => {
  const { orderNumber } = useParams();
  const navigate = useNavigate();
  const [order, setOrder] = useState(null);
  const [loading, setLoading] = useState(true);
  const [updating, setUpdating] = useState(false);

  // Status update form
  const [newStatus, setNewStatus] = useState('');
  const [trackingNumber, setTrackingNumber] = useState('');
  const [shippingCarrier, setShippingCarrier] = useState('');
  const [adminNotes, setAdminNotes] = useState('');

  useEffect(() => {
    setLoading(true);
    adminService.getOrderDetail(orderNumber)
      .then(res => setOrder(res.data?.data))
      .catch(() => toast.error('Failed to load order'))
      .finally(() => setLoading(false));
  }, [orderNumber]);

  const handleUpdateStatus = async () => {
    if (!newStatus) { toast.error('Select a status'); return; }
    setUpdating(true);
    try {
      const payload = { status: newStatus, adminNotes: adminNotes || undefined };
      if (newStatus === 'SHIPPED') {
        payload.trackingNumber = trackingNumber || undefined;
        payload.shippingCarrier = shippingCarrier || undefined;
      }
      const res = await adminService.updateOrderStatus(orderNumber, payload);
      setOrder(res.data?.data);
      setNewStatus('');
      setTrackingNumber('');
      setShippingCarrier('');
      setAdminNotes('');
      toast.success(`Status updated to ${newStatus}`);
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to update status');
    } finally {
      setUpdating(false);
    }
  };

  if (loading) {
    return <div className="flex items-center justify-center h-64"><div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" /></div>;
  }
  if (!order) {
    return <div className="text-center py-12 text-gray-500">Order not found</div>;
  }

  const currentIdx = statusFlow.indexOf(order.status);
  const nextStatuses = order.status === 'CANCELLED' ? [] : statusFlow.filter((_, i) => i > currentIdx);

  return (
    <div className="space-y-6 max-w-4xl">
      {/* Header */}
      <div className="flex items-center gap-3">
        <button onClick={() => navigate('/admin/orders')} className="p-2 hover:bg-gray-100 rounded-lg transition">
          <HiOutlineArrowLeft className="h-5 w-5 text-gray-600" />
        </button>
        <div>
          <h1 className="text-2xl font-bold text-gray-900">{order.orderNumber}</h1>
          <p className="text-sm text-gray-500">{fmtDate(order.createdAt)}</p>
        </div>
      </div>

      {/* Status + Payment badges */}
      <div className="flex flex-wrap gap-2">
        <span className={`px-3 py-1 rounded-full text-sm font-medium ${
          order.status === 'DELIVERED' ? 'bg-green-100 text-green-700' :
          order.status === 'CANCELLED' ? 'bg-red-100 text-red-700' :
          order.status === 'SHIPPED' ? 'bg-purple-100 text-purple-700' :
          'bg-yellow-100 text-yellow-700'
        }`}>
          {order.statusDisplayName || order.status}
        </span>
        <span className={`px-3 py-1 rounded-full text-sm font-medium ${
          order.paymentStatus === 'PAID' ? 'bg-green-100 text-green-700' :
          order.paymentStatus === 'FAILED' ? 'bg-red-100 text-red-700' :
          'bg-gray-100 text-gray-700'
        }`}>
          Payment: {order.paymentStatus}
        </span>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Order Items */}
        <div className="lg:col-span-2 space-y-4">
          <div className="bg-white rounded-xl border border-gray-200 p-5">
            <h2 className="font-semibold text-gray-900 mb-3">Items ({order.items?.length || 0})</h2>
            <div className="space-y-3">
              {order.items?.map((item, i) => (
                <div key={i} className="flex items-center gap-3 p-3 bg-gray-50 rounded-lg">
                  {item.productImage && <img src={item.productImage} alt="" className="h-12 w-12 rounded-lg object-cover" />}
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">{item.productName}</p>
                    <p className="text-xs text-gray-500">Qty: {item.quantity} × {fmt(item.unitPrice)}</p>
                  </div>
                  <p className="text-sm font-semibold text-gray-900">{fmt(item.totalPrice)}</p>
                </div>
              ))}
            </div>
          </div>

          {/* Update Status */}
          {nextStatuses.length > 0 && (
            <div className="bg-white rounded-xl border border-gray-200 p-5">
              <h2 className="font-semibold text-gray-900 mb-3">Update Status</h2>
              <div className="space-y-3">
                <div className="flex gap-2 flex-wrap">
                  {nextStatuses.map(s => (
                    <button key={s} onClick={() => setNewStatus(s)}
                      className={`px-3 py-1.5 rounded-lg text-sm font-medium border transition ${
                        newStatus === s ? 'bg-indigo-50 border-indigo-300 text-indigo-700' : 'border-gray-300 text-gray-600 hover:bg-gray-50'
                      }`}>{s}</button>
                  ))}
                  {order.status !== 'CANCELLED' && order.status !== 'DELIVERED' && (
                    <button onClick={() => setNewStatus('CANCELLED')}
                      className={`px-3 py-1.5 rounded-lg text-sm font-medium border transition ${
                        newStatus === 'CANCELLED' ? 'bg-red-50 border-red-300 text-red-700' : 'border-gray-300 text-gray-600 hover:bg-gray-50'
                      }`}>CANCEL</button>
                  )}
                </div>
                {newStatus === 'SHIPPED' && (
                  <div className="grid grid-cols-2 gap-3">
                    <input placeholder="Tracking Number" value={trackingNumber} onChange={e => setTrackingNumber(e.target.value)} className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
                    <input placeholder="Carrier (UPS, FedEx...)" value={shippingCarrier} onChange={e => setShippingCarrier(e.target.value)} className="px-3 py-2 border border-gray-300 rounded-lg text-sm" />
                  </div>
                )}
                <textarea placeholder="Admin notes (optional)" value={adminNotes} onChange={e => setAdminNotes(e.target.value)} rows={2} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm" />
                <button onClick={handleUpdateStatus} disabled={!newStatus || updating}
                  className="px-4 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 transition">
                  {updating ? 'Updating...' : `Update to ${newStatus || '...'}`}
                </button>
              </div>
            </div>
          )}
        </div>

        {/* Sidebar - Summary + Address */}
        <div className="space-y-4">
          <div className="bg-white rounded-xl border border-gray-200 p-5">
            <h2 className="font-semibold text-gray-900 mb-3">Summary</h2>
            <div className="space-y-2 text-sm">
              <div className="flex justify-between"><span className="text-gray-500">Subtotal</span><span>{fmt(order.subtotal)}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Tax</span><span>{fmt(order.taxAmount)}</span></div>
              <div className="flex justify-between"><span className="text-gray-500">Shipping</span><span>{fmt(order.shippingCost)}</span></div>
              {order.discountAmount > 0 && <div className="flex justify-between"><span className="text-gray-500">Discount</span><span className="text-green-600">-{fmt(order.discountAmount)}</span></div>}
              <hr />
              <div className="flex justify-between font-semibold text-base"><span>Total</span><span>{fmt(order.totalAmount)}</span></div>
            </div>
          </div>

          {order.shippingAddress && (
            <div className="bg-white rounded-xl border border-gray-200 p-5">
              <h2 className="font-semibold text-gray-900 mb-3">Shipping Address</h2>
              <div className="text-sm text-gray-600 space-y-1">
                <p className="font-medium text-gray-900">{order.shippingAddress.fullName}</p>
                <p>{order.shippingAddress.addressLine1}</p>
                {order.shippingAddress.addressLine2 && <p>{order.shippingAddress.addressLine2}</p>}
                <p>{order.shippingAddress.city}, {order.shippingAddress.state} {order.shippingAddress.postalCode}</p>
                <p>{order.shippingAddress.country}</p>
                {order.shippingAddress.phone && <p>Phone: {order.shippingAddress.phone}</p>}
              </div>
            </div>
          )}

          {order.trackingNumber && (
            <div className="bg-white rounded-xl border border-gray-200 p-5">
              <h2 className="font-semibold text-gray-900 mb-2">Tracking</h2>
              <p className="text-sm text-gray-600">{order.shippingCarrier}: <span className="font-medium">{order.trackingNumber}</span></p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default AdminOrderDetail;