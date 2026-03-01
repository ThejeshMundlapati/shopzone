import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { HiOutlineArrowLeft, HiOutlineTrash, HiOutlineUpload } from 'react-icons/hi';
import adminService from '../../services/adminService';
import toast from 'react-hot-toast';

const AdminProductForm = () => {
  const { id } = useParams();
  const isEdit = Boolean(id);
  const navigate = useNavigate();
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [form, setForm] = useState({
    name: '', description: '', price: '', discountPrice: '', stock: '0',
    categoryId: '', brand: '', tags: '', active: true, featured: false,
  });
  const [images, setImages] = useState([]);

  useEffect(() => {
    adminService.getAllCategories().then(res => {
      setCategories(res.data?.data || []);
    }).catch(() => {});

    if (isEdit) {
      setLoading(true);
      adminService.getProductById(id).then(res => {
        const p = res.data?.data;
        if (p) {
          setForm({
            name: p.name || '', description: p.description || '',
            price: p.price?.toString() || '', discountPrice: p.discountPrice?.toString() || '',
            stock: p.stock?.toString() || '0', categoryId: p.categoryId || '',
            brand: p.brand || '', tags: p.tags?.join(', ') || '',
            active: p.active ?? true, featured: p.featured ?? false,
          });
          setImages(p.images || []);
        }
      }).catch(() => toast.error('Failed to load product'))
        .finally(() => setLoading(false));
    }
  }, [id, isEdit]);

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setForm(prev => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!form.name.trim() || !form.price || !form.categoryId) {
      toast.error('Name, price, and category are required');
      return;
    }

    setSaving(true);
    const payload = {
      name: form.name,
      description: form.description,
      price: parseFloat(form.price),
      discountPrice: form.discountPrice ? parseFloat(form.discountPrice) : null,
      stock: parseInt(form.stock) || 0,
      categoryId: form.categoryId,
      brand: form.brand || null,
      tags: form.tags ? form.tags.split(',').map(t => t.trim()).filter(Boolean) : [],
      active: form.active,
      featured: form.featured,
    };

    try {
      if (isEdit) {
        await adminService.updateProduct(id, payload);
        toast.success('Product updated');
      } else {
        await adminService.createProduct(payload);
        toast.success('Product created');
      }
      navigate('/admin/products');
    } catch (e) {
      toast.error(e.response?.data?.message || 'Failed to save product');
    } finally {
      setSaving(false);
    }
  };

  const handleImageUpload = async (e) => {
    if (!isEdit) { toast.error('Save the product first, then upload images'); return; }
    const files = e.target.files;
    if (!files.length) return;

    setUploading(true);
    const formData = new FormData();
    for (const f of files) formData.append('files', f);

    try {
      const res = await adminService.uploadProductImages(id, formData);
      setImages(res.data?.data?.images || []);
      toast.success('Images uploaded');
    } catch {
      toast.error('Failed to upload images');
    } finally {
      setUploading(false);
    }
  };

  const handleRemoveImage = async (url) => {
    try {
      const res = await adminService.removeProductImage(id, url);
      setImages(res.data?.data?.images || []);
      toast.success('Image removed');
    } catch {
      toast.error('Failed to remove image');
    }
  };

  if (loading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600" />
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto space-y-6">
      <div className="flex items-center gap-3">
        <button onClick={() => navigate('/admin/products')} className="p-2 hover:bg-gray-100 rounded-lg transition">
          <HiOutlineArrowLeft className="h-5 w-5 text-gray-600" />
        </button>
        <h1 className="text-2xl font-bold text-gray-900">{isEdit ? 'Edit Product' : 'Add Product'}</h1>
      </div>

      <form onSubmit={handleSubmit} className="space-y-6">
        {/* Basic Info */}
        <div className="bg-white rounded-xl border border-gray-200 p-5 space-y-4">
          <h2 className="font-semibold text-gray-900">Basic Information</h2>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Product Name *</label>
            <input name="name" value={form.name} onChange={handleChange} required className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Description</label>
            <textarea name="description" value={form.description} onChange={handleChange} rows={4} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" />
          </div>
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Category *</label>
              <select name="categoryId" value={form.categoryId} onChange={handleChange} required className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500">
                <option value="">Select category</option>
                {categories.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
              </select>
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Brand</label>
              <input name="brand" value={form.brand} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" />
            </div>
          </div>
        </div>

        {/* Pricing & Stock */}
        <div className="bg-white rounded-xl border border-gray-200 p-5 space-y-4">
          <h2 className="font-semibold text-gray-900">Pricing & Stock</h2>
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Price ($) *</label>
              <input name="price" type="number" step="0.01" min="0.01" value={form.price} onChange={handleChange} required className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Discount Price ($)</label>
              <input name="discountPrice" type="number" step="0.01" min="0" value={form.discountPrice} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" />
            </div>
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Stock</label>
              <input name="stock" type="number" min="0" value={form.stock} onChange={handleChange} className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" />
            </div>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tags (comma separated)</label>
            <input name="tags" value={form.tags} onChange={handleChange} placeholder="electronics, smartphone, 5g" className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500" />
          </div>
          <div className="flex items-center gap-6">
            <label className="flex items-center gap-2 cursor-pointer">
              <input type="checkbox" name="active" checked={form.active} onChange={handleChange} className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
              <span className="text-sm text-gray-700">Active</span>
            </label>
            <label className="flex items-center gap-2 cursor-pointer">
              <input type="checkbox" name="featured" checked={form.featured} onChange={handleChange} className="rounded border-gray-300 text-indigo-600 focus:ring-indigo-500" />
              <span className="text-sm text-gray-700">Featured</span>
            </label>
          </div>
        </div>

        {/* Images (edit mode only) */}
        {isEdit && (
          <div className="bg-white rounded-xl border border-gray-200 p-5 space-y-4">
            <h2 className="font-semibold text-gray-900">Images</h2>
            <div className="flex flex-wrap gap-3">
              {images.map((url, i) => (
                <div key={i} className="relative group">
                  <img src={url} alt="" className="h-24 w-24 rounded-lg object-cover border" />
                  <button type="button" onClick={() => handleRemoveImage(url)} className="absolute -top-2 -right-2 p-1 bg-red-500 text-white rounded-full opacity-0 group-hover:opacity-100 transition">
                    <HiOutlineTrash className="h-3 w-3" />
                  </button>
                </div>
              ))}
              <label className={`h-24 w-24 border-2 border-dashed border-gray-300 rounded-lg flex flex-col items-center justify-center cursor-pointer hover:border-indigo-400 transition ${uploading ? 'opacity-50' : ''}`}>
                <HiOutlineUpload className="h-5 w-5 text-gray-400" />
                <span className="text-xs text-gray-400 mt-1">{uploading ? 'Uploading...' : 'Upload'}</span>
                <input type="file" multiple accept="image/*" onChange={handleImageUpload} className="hidden" disabled={uploading} />
              </label>
            </div>
          </div>
        )}

        {/* Submit */}
        <div className="flex justify-end gap-3">
          <button type="button" onClick={() => navigate('/admin/products')} className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg text-sm font-medium hover:bg-gray-50 transition">
            Cancel
          </button>
          <button type="submit" disabled={saving} className="px-6 py-2 bg-indigo-600 text-white rounded-lg text-sm font-medium hover:bg-indigo-700 disabled:opacity-50 transition">
            {saving ? 'Saving...' : isEdit ? 'Update Product' : 'Create Product'}
          </button>
        </div>
      </form>
    </div>
  );
};

export default AdminProductForm;