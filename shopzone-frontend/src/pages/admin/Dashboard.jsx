import { useEffect, useState } from 'react';
import { useDispatch, useSelector } from 'react-redux';
import { Link } from 'react-router-dom';
import {
  AreaChart, Area, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, PieChart, Pie, Cell
} from 'recharts';
import {
  HiOutlineShoppingCart, HiOutlineCurrencyDollar, HiOutlineUsers,
  HiOutlineShoppingBag, HiOutlineClock, HiOutlineTruck,
  HiOutlineCheck, HiOutlineX
} from 'react-icons/hi';
import { fetchDashboardStats, fetchRecentOrders, fetchTopProducts, fetchTopCustomers } from '../../store/adminSlice';
import adminService from '../../services/adminService';
import StatsCard from '../../components/admin/StatsCard';

const PIE_COLORS = ['#6366f1', '#f59e0b', '#3b82f6', '#10b981', '#ef4444', '#8b5cf6'];

const fmt = (n) => {
  if (n === null || n === undefined) return '$0';
  return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD', minimumFractionDigits: 0, maximumFractionDigits: 0 }).format(n);
};

const fmtNum = (n) => {
  if (n === null || n === undefined) return '0';
  return new Intl.NumberFormat('en-US').format(n);
};

const Dashboard = () => {
  const dispatch = useDispatch();
  const { dashboardStats, recentOrders, topProducts, topCustomers, loading } = useSelector(s => s.admin);
  const [revenueData, setRevenueData] = useState([]);

  useEffect(() => {
    dispatch(fetchDashboardStats());
    dispatch(fetchRecentOrders(8));
    dispatch(fetchTopProducts(5));
    dispatch(fetchTopCustomers(5));

    // Fetch revenue chart data
    const end = new Date().toISOString().split('T')[0];
    const start = new Date(Date.now() - 30 * 86400000).toISOString().split('T')[0];
    adminService.getRevenueReport(start, end)
      .then(res => {
        const daily = res.data?.data?.dailyRevenue || [];
        setRevenueData(daily.map(d => ({
          date: d.date?.substring(5), // MM-DD
          revenue: Number(d.revenue) || 0,
          orders: d.orderCount || 0,
        })));
      })
      .catch(() => {});
  }, [dispatch]);

  const s = dashboardStats || {};

  const orderStatusData = s.ordersByStatus
    ? Object.entries(s.ordersByStatus).map(([name, value]) => ({ name, value }))
    : [];

  if (loading && !dashboardStats) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-sm text-gray-500 mt-1">Overview of your store performance</p>
      </div>

      {/* Stats Grid */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard title="Total Revenue" value={fmt(s.totalRevenue)} subtitle={`${fmt(s.revenueToday)} today`} icon={HiOutlineCurrencyDollar} color="green" />
        <StatsCard title="Total Orders" value={fmtNum(s.totalOrders)} subtitle={`${fmtNum(s.ordersToday)} today`} icon={HiOutlineShoppingCart} color="indigo" />
        <StatsCard title="Total Users" value={fmtNum(s.totalUsers)} subtitle={`${fmtNum(s.newUsersThisMonth)} this month`} icon={HiOutlineUsers} color="blue" />
        <StatsCard title="Products" value={fmtNum(s.totalProducts)} subtitle={`${fmtNum(s.outOfStockProducts)} out of stock`} icon={HiOutlineShoppingBag} color="orange" />
      </div>

      {/* Secondary stats */}
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        <StatsCard title="Pending" value={fmtNum(s.pendingOrders)} icon={HiOutlineClock} color="yellow" />
        <StatsCard title="Processing" value={fmtNum(s.processingOrders)} icon={HiOutlineTruck} color="blue" />
        <StatsCard title="Delivered" value={fmtNum(s.deliveredOrders)} icon={HiOutlineCheck} color="green" />
        <StatsCard title="Cancelled" value={fmtNum(s.cancelledOrders)} icon={HiOutlineX} color="red" />
      </div>

      {/* Charts Row */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        {/* Revenue Chart */}
        <div className="lg:col-span-2 bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-900 mb-4">Revenue (Last 30 Days)</h3>
          {revenueData.length > 0 ? (
            <ResponsiveContainer width="100%" height={280}>
              <AreaChart data={revenueData}>
                <defs>
                  <linearGradient id="revGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#6366f1" stopOpacity={0.15} />
                    <stop offset="95%" stopColor="#6366f1" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
                <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} tickFormatter={v => `$${v}`} />
                <Tooltip formatter={(v) => [`$${v}`, 'Revenue']} />
                <Area type="monotone" dataKey="revenue" stroke="#6366f1" strokeWidth={2} fill="url(#revGrad)" />
              </AreaChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-[280px] flex items-center justify-center text-gray-400 text-sm">No revenue data</div>
          )}
        </div>

        {/* Order Status Pie */}
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-900 mb-4">Orders by Status</h3>
          {orderStatusData.length > 0 ? (
            <>
              <ResponsiveContainer width="100%" height={200}>
                <PieChart>
                  <Pie data={orderStatusData} cx="50%" cy="50%" innerRadius={50} outerRadius={80} paddingAngle={3} dataKey="value">
                    {orderStatusData.map((_, i) => (
                      <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                </PieChart>
              </ResponsiveContainer>
              <div className="mt-2 space-y-1">
                {orderStatusData.map((d, i) => (
                  <div key={d.name} className="flex items-center justify-between text-xs">
                    <div className="flex items-center gap-2">
                      <span className="w-2.5 h-2.5 rounded-full" style={{ backgroundColor: PIE_COLORS[i % PIE_COLORS.length] }} />
                      <span className="text-gray-600">{d.name}</span>
                    </div>
                    <span className="font-medium text-gray-900">{d.value}</span>
                  </div>
                ))}
              </div>
            </>
          ) : (
            <div className="h-[200px] flex items-center justify-center text-gray-400 text-sm">No order data</div>
          )}
        </div>
      </div>

      {/* Top Products Chart */}
      {topProducts.length > 0 && (
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-900 mb-4">Top Selling Products</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={topProducts.slice(0, 5)} layout="vertical" margin={{ left: 100 }}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis type="number" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
              <YAxis type="category" dataKey="productName" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} width={100} />
              <Tooltip formatter={(v) => [v, 'Units Sold']} />
              <Bar dataKey="totalQuantitySold" fill="#6366f1" radius={[0, 4, 4, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Recent Orders + Top Customers */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Recent Orders */}
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-sm font-semibold text-gray-900">Recent Orders</h3>
            <Link to="/admin/orders" className="text-xs text-indigo-600 hover:text-indigo-700 font-medium">View All →</Link>
          </div>
          <div className="space-y-3">
            {recentOrders.length > 0 ? recentOrders.map((o) => (
              <Link key={o.orderNumber} to={`/admin/orders/${o.orderNumber}`} className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50 transition">
                <div className="min-w-0">
                  <p className="text-sm font-medium text-gray-900 truncate">{o.orderNumber}</p>
                  <p className="text-xs text-gray-500">{o.customerName}</p>
                </div>
                <div className="text-right flex-shrink-0 ml-3">
                  <p className="text-sm font-semibold text-gray-900">{fmt(o.totalAmount)}</p>
                  <span className={`inline-block text-xs px-2 py-0.5 rounded-full font-medium ${
                    o.status === 'DELIVERED' ? 'bg-green-100 text-green-700' :
                    o.status === 'SHIPPED' ? 'bg-blue-100 text-blue-700' :
                    o.status === 'CANCELLED' ? 'bg-red-100 text-red-700' :
                    o.status === 'PROCESSING' ? 'bg-yellow-100 text-yellow-700' :
                    'bg-gray-100 text-gray-700'
                  }`}>
                    {o.statusDisplayName || o.status}
                  </span>
                </div>
              </Link>
            )) : (
              <p className="text-sm text-gray-400 text-center py-4">No recent orders</p>
            )}
          </div>
        </div>

        {/* Top Customers */}
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-900 mb-4">Top Customers</h3>
          <div className="space-y-3">
            {topCustomers.length > 0 ? topCustomers.map((c, i) => (
              <div key={c.userId || i} className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50">
                <div className="flex items-center gap-3 min-w-0">
                  <div className="h-8 w-8 rounded-full bg-indigo-100 flex items-center justify-center flex-shrink-0">
                    <span className="text-xs font-medium text-indigo-700">{i + 1}</span>
                  </div>
                  <div className="min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">{c.customerName}</p>
                    <p className="text-xs text-gray-500">{c.totalOrders} orders</p>
                  </div>
                </div>
                <p className="text-sm font-semibold text-gray-900 flex-shrink-0">{fmt(c.totalSpent)}</p>
              </div>
            )) : (
              <p className="text-sm text-gray-400 text-center py-4">No customer data</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Dashboard;