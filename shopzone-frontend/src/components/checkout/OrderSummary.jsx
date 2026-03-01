const OrderSummary = ({ preview, cartItems }) => {
  if (!preview) return null;

  const getLineTotal = (item) => {
    const sub = Number(item.subtotal);
    if (!isNaN(sub) && sub > 0) return sub;
    const ep = Number(item.effectivePrice) || Number(item.price) || 0;
    return ep * item.quantity;
  };

  return (
    <div className="bg-gray-50 rounded-xl p-6">
      <h3 className="text-lg font-semibold text-gray-900 mb-4">Order Summary</h3>

      {cartItems && cartItems.length > 0 && (
        <div className="space-y-3 mb-4 max-h-60 overflow-y-auto">
          {cartItems.map((item) => (
            <div key={item.productId} className="flex items-center space-x-3">
              <div className="w-12 h-12 rounded bg-gray-200 flex-shrink-0 overflow-hidden">
                <img src={item.imageUrl || 'https://via.placeholder.com/48'} alt="" className="w-full h-full object-cover" />
              </div>
              <div className="flex-1 min-w-0">
                <p className="text-sm text-gray-800 truncate">{item.productName}</p>
                <p className="text-xs text-gray-500">Qty: {item.quantity}</p>
              </div>
              <p className="text-sm font-medium">${getLineTotal(item).toFixed(2)}</p>
            </div>
          ))}
        </div>
      )}

      <hr className="my-4" />

      <div className="space-y-2 text-sm">
        <div className="flex justify-between">
          <span className="text-gray-600">Subtotal</span>
          <span>${Number(preview.subtotal).toFixed(2)}</span>
        </div>
        {preview.itemSavings > 0 && (
          <div className="flex justify-between text-green-600">
            <span>Discount Savings</span>
            <span>-${Number(preview.itemSavings).toFixed(2)}</span>
          </div>
        )}
        <div className="flex justify-between">
          <span className="text-gray-600">Shipping</span>
          <span className={preview.freeShipping ? 'text-green-600' : ''}>
            {preview.freeShipping ? 'FREE' : `$${Number(preview.shippingCost).toFixed(2)}`}
          </span>
        </div>
        <div className="flex justify-between">
          <span className="text-gray-600">Tax ({(() => { const r = Number(preview.taxRate) || 0; return r > 1 ? r.toFixed(0) : (r * 100).toFixed(0); })()}%)</span>
          <span>${Number(preview.taxAmount).toFixed(2)}</span>
        </div>
        <hr />
        <div className="flex justify-between text-base font-bold text-gray-900">
          <span>Total</span>
          <span>${Number(preview.totalAmount).toFixed(2)}</span>
        </div>
      </div>
    </div>
  );
};

export default OrderSummary;