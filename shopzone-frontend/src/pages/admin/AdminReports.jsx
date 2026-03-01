import { useEffect, useState } from 'react';
import {
  AreaChart, Area, BarChart, Bar, LineChart, Line,
  XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Legend
} from 'recharts';
import adminService from '../../services/adminService';
import StatsCard from '../../components/admin/StatsCard';
import { HiOutlineCurrencyDollar, HiOutlineShoppingCart, HiOutlineUsers } from 'react-icons/hi';
import toast from 'react-hot-toast';

const fmt = (n) => n != null ? `$${Number(n).toFixed(2)}` : '$0.00';

const AdminReports = () => {
  const [range, setRange] = useState('30');
  const [revenue, setRevenue] = useState(null);
  const [sales, setSales] = useState(null);
  const [userGrowth, setUserGrowth] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const end = new Date().toISOString().split('T')[0];
    const start = new Date(Date.now() - parseInt(range) * 86400000).toISOString().split('T')[0];

    setLoading(true);
    Promise.all([
      adminService.getRevenueReport(start, end),
      adminService.getSalesReport(start, end),
      adminService.getUserGrowthReport(start, end),
    ]).then(([revRes, salesRes, userRes]) => {
      setRevenue(revRes.data?.data);
      setSales(salesRes.data?.data);
      setUserGrowth(userRes.data?.data);
    }).catch(() => toast.error('Failed to load reports'))
      .finally(() => setLoading(false));
  }, [range]);

  const revenueChartData = revenue?.dailyRevenue?.map(d => ({
    date: d.date?.substring(5),
    revenue: Number(d.revenue) || 0,
    orders: d.orderCount || 0,
  })) || [];

  const userChartData = userGrowth?.dailyRegistrations?.map(d => ({
    date: d.date?.substring(5),
    users: d.newUsers || 0,
  })) || [];

  const orderStatusData = sales?.ordersByStatus
    ? Object.entries(sales.ordersByStatus).map(([name, count]) => ({ name, count }))
    : [];

  if (loading) {
    return <div className="flex items-center justify-center h-64"><div className="animate-spin rounded-full h-10 w-10 border-b-2 border-indigo-600" /></div>;
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <h1 className="text-2xl font-bold text-gray-900">Reports & Analytics</h1>
        <select value={range} onChange={e => setRange(e.target.value)} className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500">
          <option value="7">Last 7 days</option>
          <option value="30">Last 30 days</option>
          <option value="90">Last 90 days</option>
          <option value="365">Last year</option>
        </select>
      </div>

      {/* Summary Stats */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <StatsCard title="Total Revenue" value={fmt(revenue?.totalRevenue)} icon={HiOutlineCurrencyDollar} color="green" />
        <StatsCard title="Total Orders" value={revenue?.totalOrders || 0} icon={HiOutlineShoppingCart} color="indigo" />
        <StatsCard title="Avg Order Value" value={fmt(revenue?.averageOrderValue)} icon={HiOutlineCurrencyDollar} color="blue" />
        <StatsCard title="New Users" value={userGrowth?.newUsersInPeriod || 0} icon={HiOutlineUsers} color="purple" />
      </div>

      {/* Revenue Chart */}
      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h3 className="text-sm font-semibold text-gray-900 mb-4">Revenue Over Time</h3>
        {revenueChartData.length > 0 ? (
          <ResponsiveContainer width="100%" height={300}>
            <AreaChart data={revenueChartData}>
              <defs>
                <linearGradient id="revenueGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#10b981" stopOpacity={0.15} />
                  <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="date" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
              <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} tickFormatter={v => `$${v}`} />
              <Tooltip formatter={(v, name) => [name === 'revenue' ? `$${v}` : v, name === 'revenue' ? 'Revenue' : 'Orders']} />
              <Legend />
              <Area type="monotone" dataKey="revenue" stroke="#10b981" strokeWidth={2} fill="url(#revenueGrad)" name="Revenue" />
            </AreaChart>
          </ResponsiveContainer>
        ) : (
          <div className="h-[300px] flex items-center justify-center text-gray-400 text-sm">No data for selected period</div>
        )}
      </div>

      {/* Orders per Day + User Registrations side by side */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Orders per Day */}
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-900 mb-4">Orders Per Day</h3>
          {revenueChartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={250}>
              <BarChart data={revenueChartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
                <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="orders" fill="#6366f1" radius={[4, 4, 0, 0]} name="Orders" />
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-[250px] flex items-center justify-center text-gray-400 text-sm">No data</div>
          )}
        </div>

        {/* User Registrations */}
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-900 mb-4">User Registrations</h3>
          {userChartData.length > 0 ? (
            <ResponsiveContainer width="100%" height={250}>
              <LineChart data={userChartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                <XAxis dataKey="date" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
                <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} allowDecimals={false} />
                <Tooltip />
                <Line type="monotone" dataKey="users" stroke="#8b5cf6" strokeWidth={2} dot={{ r: 3 }} name="New Users" />
              </LineChart>
            </ResponsiveContainer>
          ) : (
            <div className="h-[250px] flex items-center justify-center text-gray-400 text-sm">No data</div>
          )}
        </div>
      </div>

      {/* Order Status Breakdown */}
      {orderStatusData.length > 0 && (
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-900 mb-4">Orders by Status</h3>
          <ResponsiveContainer width="100%" height={250}>
            <BarChart data={orderStatusData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
              <XAxis dataKey="name" tick={{ fontSize: 11 }} tickLine={false} axisLine={false} />
              <YAxis tick={{ fontSize: 11 }} tickLine={false} axisLine={false} allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="count" fill="#f59e0b" radius={[4, 4, 0, 0]} name="Orders" />
            </BarChart>
          </ResponsiveContainer>
        </div>
      )}

      {/* Sales Summary Table */}
      <div className="bg-white rounded-xl border border-gray-200 p-5">
        <h3 className="text-sm font-semibold text-gray-900 mb-4">Sales Summary</h3>
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4 text-sm">
          <div>
            <p className="text-gray-500">Total Orders</p>
            <p className="text-xl font-bold text-gray-900">{sales?.totalOrders || 0}</p>
          </div>
          <div>
            <p className="text-gray-500">Cancelled</p>
            <p className="text-xl font-bold text-red-600">{sales?.cancelledOrders || 0}</p>
          </div>
          <div>
            <p className="text-gray-500">Cancellation Rate</p>
            <p className="text-xl font-bold text-gray-900">{sales?.cancellationRate != null ? `${Number(sales.cancellationRate).toFixed(1)}%` : '0%'}</p>
          </div>
          <div>
            <p className="text-gray-500">Total Tax</p>
            <p className="text-xl font-bold text-gray-900">{fmt(revenue?.totalTax)}</p>
          </div>
        </div>
      </div>

      {/* Top Products in this period */}
      {sales?.topProducts?.length > 0 && (
        <div className="bg-white rounded-xl border border-gray-200 p-5">
          <h3 className="text-sm font-semibold text-gray-900 mb-4">Top Products (This Period)</h3>
          <div className="space-y-2">
            {sales.topProducts.slice(0, 10).map((p, i) => (
              <div key={p.productId || i} className="flex items-center justify-between p-3 rounded-lg hover:bg-gray-50">
                <div className="flex items-center gap-3 min-w-0">
                  <span className="text-sm font-medium text-gray-400 w-6">{i + 1}.</span>
                  {p.productImage && <img src={p.productImage} alt="" className="h-8 w-8 rounded object-cover" />}
                  <p className="text-sm font-medium text-gray-900 truncate">{p.productName}</p>
                </div>
                <div className="text-right flex-shrink-0 ml-3">
                  <p className="text-sm font-semibold text-gray-900">{fmt(p.totalRevenue)}</p>
                  <p className="text-xs text-gray-500">{p.totalQuantitySold} sold</p>
                </div>
              </div>
            ))}
          </div>
        </div>
      )}
    </div>
  );
};

export default AdminReports;