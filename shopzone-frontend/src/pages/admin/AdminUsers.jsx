import { useEffect, useState, useCallback } from 'react';
import { HiOutlineSearch, HiOutlineLockClosed, HiOutlineLockOpen, HiOutlineBan, HiOutlineCheck } from 'react-icons/hi';
import adminService from '../../services/adminService';
import DataTable from '../../components/admin/DataTable';
import StatsCard from '../../components/admin/StatsCard';
import toast from 'react-hot-toast';

const fmt = (n) => n != null ? `$${Number(n).toFixed(2)}` : '$0.00';
const fmtDate = (d) => d ? new Date(d).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : '—';

const AdminUsers = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [roleFilter, setRoleFilter] = useState('');
  const [search, setSearch] = useState('');
  const [searchInput, setSearchInput] = useState('');
  const [stats, setStats] = useState(null);

  const fetchUsers = useCallback(async () => {
    setLoading(true);
    try {
      const res = await adminService.getAllUsers({
        page, size: 15, sortBy: 'createdAt', sortDir: 'desc',
        role: roleFilter || undefined,
        search: search || undefined,
      });
      const data = res.data?.data;
      setUsers(data?.content || []);
      setTotalPages(data?.totalPages || 0);
      setTotalElements(data?.totalElements || 0);
    } catch {
      toast.error('Failed to load users');
    } finally {
      setLoading(false);
    }
  }, [page, roleFilter, search]);

  useEffect(() => { fetchUsers(); }, [fetchUsers]);

  useEffect(() => {
    adminService.getUserStatsSummary()
      .then(res => setStats(res.data?.data))
      .catch(() => {});
  }, []);

  const toggleEnable = async (user) => {
    try {
      if (user.enabled) {
        await adminService.disableUser(user.id);
        toast.success('User disabled');
      } else {
        await adminService.enableUser(user.id);
        toast.success('User enabled');
      }
      fetchUsers();
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to update user');
    }
  };

  const toggleLock = async (user) => {
    try {
      if (user.locked) {
        await adminService.unlockUser(user.id);
        toast.success('User unlocked');
      } else {
        await adminService.lockUser(user.id);
        toast.success('User locked');
      }
      fetchUsers();
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to update user');
    }
  };

  const handleSearch = (e) => {
    e.preventDefault();
    setSearch(searchInput);
    setPage(0);
  };

  const columns = [
    {
      header: 'User',
      render: (row) => (
        <div>
          <p className="text-sm font-medium text-gray-900">{row.firstName} {row.lastName}</p>
          <p className="text-xs text-gray-500">{row.email}</p>
        </div>
      ),
    },
    {
      header: 'Role',
      render: (row) => (
        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${
          row.role === 'ADMIN' ? 'bg-purple-100 text-purple-700' : 'bg-blue-100 text-blue-700'
        }`}>{row.role}</span>
      ),
    },
    {
      header: 'Orders',
      render: (row) => (
        <div>
          <p className="text-sm text-gray-900">{row.orderCount || 0}</p>
          <p className="text-xs text-gray-500">{fmt(row.totalSpent)}</p>
        </div>
      ),
    },
    {
      header: 'Joined',
      render: (row) => <span className="text-sm text-gray-600">{fmtDate(row.createdAt)}</span>,
    },
    {
      header: 'Status',
      render: (row) => (
        <div className="flex flex-col gap-1">
          <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium w-fit ${row.enabled ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'}`}>
            {row.enabled ? 'Active' : 'Disabled'}
          </span>
          {row.locked && (
            <span className="inline-flex px-2 py-0.5 rounded-full text-xs font-medium bg-orange-100 text-orange-700 w-fit">Locked</span>
          )}
        </div>
      ),
    },
    {
      header: 'Actions',
      render: (row) => (
        <div className="flex items-center gap-1">
          <button onClick={() => toggleEnable(row)} title={row.enabled ? 'Disable' : 'Enable'}
            className={`p-1.5 rounded-lg transition ${row.enabled ? 'text-gray-500 hover:text-red-600 hover:bg-red-50' : 'text-gray-500 hover:text-green-600 hover:bg-green-50'}`}>
            {row.enabled ? <HiOutlineBan className="h-4 w-4" /> : <HiOutlineCheck className="h-4 w-4" />}
          </button>
          <button onClick={() => toggleLock(row)} title={row.locked ? 'Unlock' : 'Lock'}
            className={`p-1.5 rounded-lg transition ${row.locked ? 'text-orange-500 hover:text-green-600 hover:bg-green-50' : 'text-gray-500 hover:text-orange-600 hover:bg-orange-50'}`}>
            {row.locked ? <HiOutlineLockOpen className="h-4 w-4" /> : <HiOutlineLockClosed className="h-4 w-4" />}
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold text-gray-900">Users</h1>

      {/* Stats */}
      {stats && (
        <div className="grid grid-cols-2 sm:grid-cols-4 gap-3">
          <StatsCard title="Total Users" value={stats.totalUsers} color="blue" />
          <StatsCard title="Customers" value={stats.customerCount} color="indigo" />
          <StatsCard title="Admins" value={stats.adminCount} color="purple" />
          <StatsCard title="New (30d)" value={stats.newUsersLast30Days} color="green" />
        </div>
      )}

      {/* Filters */}
      <div className="flex flex-wrap items-center gap-3">
        <form onSubmit={handleSearch} className="relative">
          <HiOutlineSearch className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
          <input type="text" placeholder="Search name or email..." value={searchInput} onChange={e => setSearchInput(e.target.value)}
            className="pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm w-64 focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" />
        </form>
        <select value={roleFilter} onChange={e => { setRoleFilter(e.target.value); setPage(0); }} className="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500">
          <option value="">All Roles</option>
          <option value="CUSTOMER">Customer</option>
          <option value="ADMIN">Admin</option>
        </select>
      </div>

      <DataTable
        columns={columns} data={users} loading={loading}
        page={page} totalPages={totalPages} totalElements={totalElements}
        onPageChange={setPage} emptyMessage="No users found"
      />
    </div>
  );
};

export default AdminUsers;