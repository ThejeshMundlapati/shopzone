import { useState } from 'react';

const ProductGallery = ({ images = [], name }) => {
  const [selectedIndex, setSelectedIndex] = useState(0);

  const displayImages = images.length > 0
    ? images
    : ['https://via.placeholder.com/600x600?text=No+Image'];

  return (
    <div className="space-y-4">
      {/* Main Image */}
      <div className="aspect-square rounded-xl overflow-hidden bg-gray-100 border">
        <img
          src={displayImages[selectedIndex]}
          alt={`${name} - Image ${selectedIndex + 1}`}
          className="w-full h-full object-cover"
        />
      </div>

      {/* Thumbnail Strip */}
      {displayImages.length > 1 && (
        <div className="flex space-x-2 overflow-x-auto pb-1">
          {displayImages.map((img, idx) => (
            <button
              key={idx}
              onClick={() => setSelectedIndex(idx)}
              className={`flex-shrink-0 w-16 h-16 rounded-lg overflow-hidden border-2 transition ${
                idx === selectedIndex ? 'border-indigo-600' : 'border-gray-200 hover:border-gray-400'
              }`}
            >
              <img src={img} alt={`Thumbnail ${idx + 1}`} className="w-full h-full object-cover" />
            </button>
          ))}
        </div>
      )}
    </div>
  );
};

export default ProductGallery;