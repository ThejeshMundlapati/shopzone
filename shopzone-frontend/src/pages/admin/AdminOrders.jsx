import { useEffect, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { HiOutlineSearch, HiOutlineEye } from 'react-icons/hi';
import adminService from '../../services/adminService';
import DataTable from '../../components/admin/DataTable';
import toast from 'react-hot-toast';

const statusColors = {
  PENDING: 'bg-gray-100 text-gray-700',
  CONFIRMED: 'bg-blue-100 text-blue-700',
  PROCESSING: 'bg-yellow-100 text-yellow-700',
  SHIPPED: 'bg-purple-100 text-purple-700',
  DELIVERED: 'bg-green-100 text-green-700',
  CANCELLED: 'bg-red-100 text-red-700',
};

const paymentColors = {
  PENDING: 'bg-gray-100 text-gray-700',
  AWAITING_PAYMENT: 'bg-yellow-100 text-yellow-700',
  PAID: 'bg-green-100 text-green-700',
  FAILED: 'bg-red-100 text-red-700',
  REFUNDED: 'bg-purple-100 text-purple-700',
};

const fmt = (n) => n != null ? `$${Number(n).toFixed(2)}` : '$0.00';
const fmtDate = (d) => d ? new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '—';

const AdminOrders = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [statusFilter, setStatusFilter] = useState('');
  const [paymentFilter, setPaymentFilter] = useState('');
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    try {
      let res;
      if (search.trim()) {
        res = await adminService.searchOrders(search, page, 15);
      } else {
        res = await adminService.getAllOrders({
          page, size: 15,
          status: statusFilter || undefined,
          paymentStatus: paymentFilter || undefined,
          sortBy: 'createdAt', sortDir: 'desc',
        });
      }
      const data = res.data?.data;
      setOrders(data?.content || []);
      setTotalPages(data?.totalPages || 0);
      setTotalElements(data?.totalElements || 0);
    } catch {
      toast.error('Failed to load orders');
    } finally {
      setLoading(false);
    }
  }, [page, statusFilter, paymentFilter, search]);

  useEffect(() => { fetchOrders(); }, [fetchOrders]);

  const handleSearch = (e) => {
    e.preventDefault();
    setSearch(searchInput);
    setPage(0);
  };

  const columns = [
    {
      header: 'Order',
      render: (row) => (
        <div>
          <p className="text-sm font-medium text-gray-900">{row.orderNumber}</p>
          <p className="text-xs text-gray-500">{fmtDate(row.createdAt)}</p>
        </div>
      ),
    },
    {
      header: 'Items',
      render: (row) => (
        <div className="flex items-center gap-2">
          {row.previewImage && <img src={row.previewImage} alt="" className="h-8 w-8 rounded object-cover" />}
          <span className="text-sm text-gray-600">{row.itemCount} items</span>
        </div>
      ),
    },
    {
      header: 'Total',
      render: (row) => <span className="text-sm font-medium text-gray-900">{fmt(row.totalAmount)}</span>,
    },
    {
      header: 'Status',
      render: (row) => (
        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${statusColors[row.status] || 'bg-gray-100 text-gray-700'}`}>
          {row.statusDisplayName || row.status}
        </span>
      ),
    },
    {
      header: 'Payment',
      render: (row) => (
        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${paymentColors[row.paymentStatus] || 'bg-gray-100 text-gray-700'}`}>
          {row.paymentStatus}
        </span>
      ),
    },
    {
      header: '',
      render: (row) => (
        <button onClick={() => navigate(`/admin/orders/${row.orderNumber}`)} className="p-1.5 text-gray-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition">
          <HiOutlineEye className="h-4 w-4" />
        </button>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">Orders</h1>

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-3">
        <form onSubmit={handleSearch} className="relative">
          <HiOutlineSearch className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
          <input
            type="text" placeholder="Search order #, email..."
            value={searchInput} onChange={e => setSearchInput(e.target.value)}
            className="pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm w-64 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
          />
        </form>
        <select value={statusFilter} onChange={e => { setStatusFilter(e.target.value); setPage(0); setSearch(''); setSearchInput(''); }} className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500">
          <option value="">All Statuses</option>
          {['PENDING','CONFIRMED','PROCESSING','SHIPPED','DELIVERED','CANCELLED'].map(s => <option key={s} value={s}>{s}</option>)}
        </select>
        <select value={paymentFilter} onChange={e => { setPaymentFilter(e.target.value); setPage(0); setSearch(''); setSearchInput(''); }} className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500">
          <option value="">All Payments</option>
          {['PENDING','AWAITING_PAYMENT','PAID','FAILED','REFUNDED','CANCELLED'].map(s => <option key={s} value={s}>{s}</option>)}
        </select>
        {(search || statusFilter || paymentFilter) && (
          <button onClick={() => { setSearch(''); setSearchInput(''); setStatusFilter(''); setPaymentFilter(''); setPage(0); }} className="text-sm text-indigo-600 hover:text-indigo-700 font-medium">
            Clear filters
          </button>
        )}
      </div>

      <DataTable
        columns={columns} data={orders} loading={loading}
        page={page} totalPages={totalPages} totalElements={totalElements}
        onPageChange={setPage} emptyMessage="No orders found"
      />
    </div>
  );
};

export default AdminOrders;