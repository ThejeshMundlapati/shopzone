import { useState, useEffect } from 'react';
import orderService from '../../services/orderService';
import toast from 'react-hot-toast';
import { HiCheck, HiPlus } from 'react-icons/hi';

const emptyForm = {
  fullName: '', phoneNumber: '', addressLine1: '', addressLine2: '',
  city: '', state: '', postalCode: '', country: 'US', landmark: '',
  addressType: 'HOME', isDefault: false,
};

const AddressSelector = ({ selectedId, onSelect }) => {
  const [addresses, setAddresses] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showForm, setShowForm] = useState(false);
  const [formData, setFormData] = useState(emptyForm);

  const fetchAddresses = async () => {
    try {
      const res = await orderService.getAddresses();
      const list = res.data || [];
      setAddresses(list);
      if (!selectedId && list.length > 0) {
        const def = list.find((a) => a.isDefault || a.default) || list[0];
        onSelect(def.id);
      }
    } catch { /* ignore */ }
    setLoading(false);
  };

  useEffect(() => { fetchAddresses(); }, []);

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    try {
      await orderService.createAddress(formData);
      toast.success('Address added!');
      setShowForm(false);
      setFormData(emptyForm);
      fetchAddresses();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to add address');
    }
  };

  if (loading) return <div className="animate-pulse h-32 bg-gray-200 rounded-lg" />;

  return (
    <div>
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Shipping Address</h3>

      {addresses.length > 0 ? (
        <div className="space-y-3">
          {addresses.map((addr) => (
            <button
              key={addr.id}
              onClick={() => onSelect(addr.id)}
              className={`w-full text-left p-4 rounded-lg border-2 transition ${
                selectedId === addr.id ? 'border-indigo-600 bg-indigo-50' : 'border-gray-200 hover:border-gray-300'
              }`}
            >
              <div className="flex items-start justify-between">
                <div>
                  <p className="font-medium text-gray-900">{addr.fullName}</p>
                  <p className="text-sm text-gray-600 mt-0.5">{addr.addressLine1}</p>
                  {addr.addressLine2 && <p className="text-sm text-gray-600">{addr.addressLine2}</p>}
                  <p className="text-sm text-gray-600">{addr.city}, {addr.state} {addr.postalCode}</p>
                  <p className="text-sm text-gray-500">{addr.phoneNumber}</p>
                </div>
                {selectedId === addr.id && <HiCheck className="h-5 w-5 text-indigo-600 flex-shrink-0" />}
              </div>
            </button>
          ))}
        </div>
      ) : (
        <p className="text-sm text-gray-500 mb-3">No saved addresses. Add one below.</p>
      )}

      {!showForm ? (
        <button onClick={() => setShowForm(true)} className="mt-3 flex items-center space-x-1 text-sm text-indigo-600 hover:text-indigo-700 font-medium">
          <HiPlus className="h-4 w-4" /><span>Add New Address</span>
        </button>
      ) : (
        <form onSubmit={handleSubmit} className="mt-4 p-4 border rounded-lg space-y-3 bg-white">
          <div className="grid grid-cols-2 gap-3">
            <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} placeholder="Full Name *" required className="col-span-2 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="tel" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} placeholder="Phone (e.g. +12345678901) *" required className="col-span-2 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="addressLine1" value={formData.addressLine1} onChange={handleChange} placeholder="Address Line 1 *" required className="col-span-2 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="addressLine2" value={formData.addressLine2} onChange={handleChange} placeholder="Address Line 2 (optional)" className="col-span-2 border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="city" value={formData.city} onChange={handleChange} placeholder="City *" required className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="state" value={formData.state} onChange={handleChange} placeholder="State *" required className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="postalCode" value={formData.postalCode} onChange={handleChange} placeholder="Postal Code (5-10 digits) *" required className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
            <input type="text" name="country" value={formData.country} onChange={handleChange} placeholder="Country *" required className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500" />
          </div>
          <div className="flex space-x-3">
            <button type="submit" className="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 transition">Save Address</button>
            <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 border border-gray-300 text-gray-600 text-sm rounded-lg hover:bg-gray-50 transition">Cancel</button>
          </div>
        </form>
      )}
    </div>
  );
};

export default AddressSelector;