import { useEffect } from 'react';
import { Link } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchOrders } from '../store/orderSlice';
import EmptyState from '../components/common/EmptyState';
import { OrderSkeleton } from '../components/common/LoadingSkeleton';
import Pagination from '../components/common/Pagination';

const statusColors = {
  PENDING: 'bg-yellow-100 text-yellow-700',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  PROCESSING: 'bg-indigo-100 text-indigo-700',
  SHIPPED: 'bg-purple-100 text-purple-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

const Orders = () => {
  const dispatch = useDispatch();
  const { list, totalPages, currentPage, loading } = useSelector((state) => state.orders);

  useEffect(() => {
    dispatch(fetchOrders({ page: 0, size: 10, sortBy: 'createdAt', sortDir: 'desc' }));
  }, [dispatch]);

  const handlePageChange = (page) => {
    dispatch(fetchOrders({ page, size: 10, sortBy: 'createdAt', sortDir: 'desc' }));
  };

  if (loading) {
    return (
      <div className="max-w-4xl mx-auto px-4 py-8 space-y-4">
        {Array.from({ length: 3 }).map((_, i) => <OrderSkeleton key={i} />)}
      </div>
    );
  }

  if (!list || list.length === 0) {
    return <EmptyState icon="📦" title="No orders yet" description="Start shopping and your orders will appear here." actionText="Shop Now" actionLink="/products" />;
  }

  return (
    <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <h1 className="text-2xl font-bold text-gray-900 mb-6">My Orders</h1>

      <div className="space-y-4">
        {list.map((order) => (
          <Link key={order.orderNumber} to={`/orders/${order.orderNumber}`} className="block bg-white rounded-xl shadow-sm border hover:shadow-md transition p-5">
            <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between mb-3">
              <div>
                <span className="font-mono text-sm font-semibold text-gray-900">{order.orderNumber}</span>
                <span className={`ml-3 px-2.5 py-0.5 rounded-full text-xs font-medium ${statusColors[order.status] || 'bg-gray-100 text-gray-600'}`}>
                  {order.statusDisplayName || order.status}
                </span>
              </div>
              <span className="text-sm text-gray-500 mt-1 sm:mt-0">
                {new Date(order.createdAt).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}
              </span>
            </div>

            {/* Items preview */}
            <div className="flex items-center space-x-3 mb-3">
              {order.items?.slice(0, 3).map((item, i) => (
                <div key={i} className="w-12 h-12 rounded bg-gray-100 overflow-hidden flex-shrink-0">
                  <img src={item.productImage || 'https://via.placeholder.com/48'} alt="" className="w-full h-full object-cover" />
                </div>
              ))}
              {order.items?.length > 3 && (
                <span className="text-xs text-gray-500">+{order.items.length - 3} more</span>
              )}
            </div>

            <div className="flex items-center justify-between text-sm">
              <span className="text-gray-500">{order.totalItemCount || order.items?.length} items</span>
              <span className="font-semibold text-gray-900">${Number(order.totalAmount).toFixed(2)}</span>
            </div>
          </Link>
        ))}
      </div>

      <Pagination currentPage={currentPage} totalPages={totalPages} onPageChange={handlePageChange} />
    </div>
  );
};

export default Orders;