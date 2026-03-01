import { useState, useEffect } from 'react';
import orderService from '../services/orderService';
import toast from 'react-hot-toast';
import { HiPlus, HiPencil, HiTrash, HiStar } from 'react-icons/hi';

const emptyForm = {
  fullName: '', phoneNumber: '', addressLine1: '', addressLine2: '',
  city: '', state: '', postalCode: '', country: 'US', landmark: '',
  addressType: 'HOME', isDefault: false,
};

const Addresses = () => {
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState(null);
  const [formData, setFormData] = useState(emptyForm);

  const fetchAll = async () => {
    try { const r = await orderService.getAddresses(); setAddresses(r.data || []); } catch {}
    setLoading(false);
  };

  useEffect(() => { fetchAll(); }, []);

  const resetForm = () => { setFormData(emptyForm); setEditingId(null); setShowForm(false); };

  const handleChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData((prev) => ({ ...prev, [name]: type === 'checkbox' ? checked : value }));
  };

  const handleEdit = (a) => {
    setFormData({
      fullName: a.fullName || '', phoneNumber: a.phoneNumber || '',
      addressLine1: a.addressLine1 || '', addressLine2: a.addressLine2 || '',
      city: a.city || '', state: a.state || '', postalCode: a.postalCode || '',
      country: a.country || 'US', landmark: a.landmark || '',
      addressType: a.addressType || 'HOME', isDefault: a.isDefault || false,
    });
    setEditingId(a.id);
    setShowForm(true);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      if (editingId) {
        await orderService.updateAddress(editingId, formData);
        toast.success('Address updated!');
      } else {
        await orderService.createAddress(formData);
        toast.success('Address added!');
      }
      resetForm();
      fetchAll();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to save address');
    }
  };

  const handleDelete = async (id) => {
    if (!confirm('Delete this address?')) return;
    try { await orderService.deleteAddress(id); toast.success('Deleted'); fetchAll(); }
    catch { toast.error('Failed to delete'); }
  };

  const handleSetDefault = async (id) => {
    try { await orderService.setDefaultAddress(id); toast.success('Default updated'); fetchAll(); }
    catch { toast.error('Failed'); }
  };

  if (loading) return (
    <div className="max-w-3xl mx-auto px-4 py-8 animate-pulse space-y-4">
      {[1, 2].map((i) => <div key={i} className="h-32 bg-gray-200 rounded-xl" />)}
    </div>
  );

  return (
    <div className="max-w-3xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <div className="flex items-center justify-between mb-6">
        <h1 className="text-2xl font-bold text-gray-900">My Addresses</h1>
        {!showForm && (
          <button onClick={() => { resetForm(); setShowForm(true); }} className="flex items-center space-x-1 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 transition">
            <HiPlus className="h-4 w-4" /><span>Add Address</span>
          </button>
        )}
      </div>

      {/* Address Form */}
      {showForm && (
        <form onSubmit={handleSubmit} className="bg-white rounded-xl shadow-sm p-6 mb-6 space-y-4">
          <h3 className="font-semibold text-gray-900">{editingId ? 'Edit Address' : 'New Address'}</h3>
          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} placeholder="Full Name *" required className="sm:col-span-2 border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="tel" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} placeholder="Phone (e.g. +12345678901) *" required className="sm:col-span-2 border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="addressLine1" value={formData.addressLine1} onChange={handleChange} placeholder="Address Line 1 *" required className="sm:col-span-2 border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="addressLine2" value={formData.addressLine2} onChange={handleChange} placeholder="Address Line 2 (optional)" className="sm:col-span-2 border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="city" value={formData.city} onChange={handleChange} placeholder="City *" required className="border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="state" value={formData.state} onChange={handleChange} placeholder="State *" required className="border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="postalCode" value={formData.postalCode} onChange={handleChange} placeholder="Postal Code (5-10 digits) *" required className="border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="country" value={formData.country} onChange={handleChange} placeholder="Country *" required className="border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="landmark" value={formData.landmark} onChange={handleChange} placeholder="Landmark (optional)" className="sm:col-span-2 border border-gray-300 rounded-lg px-3 py-2.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <label className="flex items-center space-x-2">
            <input type="checkbox" name="isDefault" checked={formData.isDefault} onChange={handleChange} className="rounded border-gray-300 text-indigo-600" />
            <span className="text-sm text-gray-700">Set as default address</span>
          </label>
          <div className="flex space-x-3">
            <button type="submit" className="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700">Save</button>
            <button type="button" onClick={resetForm} className="px-4 py-2 border border-gray-300 text-gray-600 text-sm rounded-lg hover:bg-gray-50">Cancel</button>
          </div>
        </form>
      )}

      {/* Address List */}
      {addresses.length > 0 ? (
        <div className="space-y-4">
          {addresses.map((a) => (
            <div key={a.id} className={`bg-white rounded-xl shadow-sm p-5 border-2 ${a.isDefault || a.default ? 'border-indigo-200' : 'border-transparent'}`}>
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center space-x-2 mb-1">
                    <p className="font-medium text-gray-900">{a.fullName}</p>
                    {(a.isDefault || a.default) && (
                      <span className="inline-flex items-center text-xs text-indigo-600 font-medium">
                        <HiStar className="h-3 w-3 mr-0.5" />Default
                      </span>
                    )}
                  </div>
                  <p className="text-sm text-gray-600">{a.addressLine1}</p>
                  {a.addressLine2 && <p className="text-sm text-gray-600">{a.addressLine2}</p>}
                  <p className="text-sm text-gray-600">{a.city}, {a.state} {a.postalCode}, {a.country}</p>
                  {a.landmark && <p className="text-sm text-gray-500">Landmark: {a.landmark}</p>}
                  <p className="text-sm text-gray-500 mt-1">{a.phoneNumber}</p>
                </div>
                <div className="flex items-center space-x-1">
                  {!(a.isDefault || a.default) && (
                    <button onClick={() => handleSetDefault(a.id)} title="Set as default" className="p-1.5 text-gray-400 hover:text-indigo-600">
                      <HiStar className="h-4 w-4" />
                    </button>
                  )}
                  <button onClick={() => handleEdit(a)} className="p-1.5 text-gray-400 hover:text-indigo-600">
                    <HiPencil className="h-4 w-4" />
                  </button>
                  <button onClick={() => handleDelete(a.id)} className="p-1.5 text-gray-400 hover:text-red-500">
                    <HiTrash className="h-4 w-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      ) : !showForm && (
        <div className="text-center py-16">
          <p className="text-5xl mb-4">📍</p>
          <h3 className="text-lg font-semibold text-gray-900 mb-2">No addresses yet</h3>
          <p className="text-gray-500 mb-4">Add a shipping address to get started.</p>
        </div>
      )}
    </div>
  );
};

export default Addresses;