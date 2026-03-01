import { useEffect, useState, useCallback } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { HiOutlinePlus, HiOutlinePencil, HiOutlineTrash, HiOutlineSearch } from 'react-icons/hi';
import adminService from '../../services/adminService';
import DataTable from '../../components/admin/DataTable';
import toast from 'react-hot-toast';

const AdminProducts = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [search, setSearch] = useState('');
  const size = 15;

  const fetchProducts = useCallback(async () => {
    setLoading(true);
    try {
      let res;
      if (search.trim()) {
        res = await adminService.getAllProducts({ page: 0, size: 100 });
        // Client-side filter since product search is a separate endpoint
        const all = res.data?.data?.content || [];
        const filtered = all.filter(p =>
          p.name?.toLowerCase().includes(search.toLowerCase()) ||
          p.brand?.toLowerCase().includes(search.toLowerCase()) ||
          p.sku?.toLowerCase().includes(search.toLowerCase())
        );
        setProducts(filtered);
        setTotalPages(1);
        setTotalElements(filtered.length);
      } else {
        res = await adminService.getAllProducts({ page, size, sortBy: 'createdAt', sortDir: 'desc' });
        const data = res.data?.data;
        setProducts(data?.content || []);
        setTotalPages(data?.totalPages || 0);
        setTotalElements(data?.totalElements || 0);
      }
    } catch {
      toast.error('Failed to load products');
    } finally {
      setLoading(false);
    }
  }, [page, search]);

  useEffect(() => { fetchProducts(); }, [fetchProducts]);

  const handleDelete = async (id, name) => {
    if (!confirm(`Delete "${name}"? This cannot be undone.`)) return;
    try {
      await adminService.deleteProduct(id);
      toast.success('Product deleted');
      fetchProducts();
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to delete');
    }
  };

  const columns = [
    {
      header: 'Product',
      render: (row) => (
        <div className="flex items-center gap-3">
          <img
            src={row.images?.[0] || 'https://via.placeholder.com/40'}
            alt=""
            className="h-10 w-10 rounded-lg object-cover bg-gray-100"
          />
          <div className="min-w-0">
            <p className="text-sm font-medium text-gray-900 truncate max-w-[200px]">{row.name}</p>
            <p className="text-xs text-gray-500">{row.sku || 'No SKU'}</p>
          </div>
        </div>
      ),
    },
    {
      header: 'Category',
      render: (row) => <span className="text-sm text-gray-600">{row.categoryName || '—'}</span>,
    },
    {
      header: 'Price',
      render: (row) => (
        <div>
          <span className="text-sm font-medium text-gray-900">${Number(row.price).toFixed(2)}</span>
          {row.discountPrice > 0 && (
            <span className="text-xs text-green-600 ml-1">${Number(row.discountPrice).toFixed(2)}</span>
          )}
        </div>
      ),
    },
    {
      header: 'Stock',
      render: (row) => (
        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${
          row.stock <= 0 ? 'bg-red-100 text-red-700' :
          row.stock <= 10 ? 'bg-yellow-100 text-yellow-700' :
          'bg-green-100 text-green-700'
        }`}>
          {row.stock}
        </span>
      ),
    },
    {
      header: 'Status',
      render: (row) => (
        <span className={`inline-flex px-2 py-0.5 rounded-full text-xs font-medium ${
          row.active ? 'bg-green-100 text-green-700' : 'bg-gray-100 text-gray-600'
        }`}>
          {row.active ? 'Active' : 'Inactive'}
        </span>
      ),
    },
    {
      header: 'Actions',
      render: (row) => (
        <div className="flex items-center gap-1">
          <button onClick={() => navigate(`/admin/products/edit/${row.id}`)} className="p-1.5 text-gray-500 hover:text-indigo-600 hover:bg-indigo-50 rounded-lg transition">
            <HiOutlinePencil className="h-4 w-4" />
          </button>
          <button onClick={() => handleDelete(row.id, row.name)} className="p-1.5 text-gray-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition">
            <HiOutlineTrash className="h-4 w-4" />
          </button>
        </div>
      ),
    },
  ];

  return (
    <div className="space-y-4">
      <div className="flex items-center justify-between flex-wrap gap-3">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">Products</h1>
          <p className="text-sm text-gray-500">{totalElements} products total</p>
        </div>
        <Link to="/admin/products/new" className="inline-flex items-center gap-2 px-4 py-2 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition">
          <HiOutlinePlus className="h-4 w-4" /> Add Product
        </Link>
      </div>

      {/* Search */}
      <div className="relative max-w-sm">
        <HiOutlineSearch className="absolute left-3 top-1/2 -translate-y-1/2 h-4 w-4 text-gray-400" />
        <input
          type="text"
          placeholder="Search products..."
          value={search}
          onChange={(e) => { setSearch(e.target.value); setPage(0); }}
          className="w-full pl-9 pr-4 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
        />
      </div>

      <DataTable
        columns={columns}
        data={products}
        loading={loading}
        page={page}
        totalPages={totalPages}
        totalElements={totalElements}
        onPageChange={setPage}
        emptyMessage="No products found"
      />
    </div>
  );
};

export default AdminProducts;