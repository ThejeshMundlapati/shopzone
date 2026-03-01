import { HiStar } from 'react-icons/hi';

const StarRating = ({ rating, size = 'md', showValue = false, count = null, interactive = false, onChange = null }) => {
  const sizeClasses = {
    sm: 'h-3.5 w-3.5',
    md: 'h-5 w-5',
    lg: 'h-6 w-6',
  };

  const stars = Array.from({ length: 5 }, (_, i) => {
    const starValue = i + 1;
    const filled = starValue <= Math.floor(rating);
    const partial = !filled && starValue <= Math.ceil(rating) && rating % 1 > 0;

    return (
      <button
        key={i}
        type="button"
        onClick={() => interactive && onChange?.(starValue)}
        className={interactive ? 'cursor-pointer' : 'cursor-default'}
        disabled={!interactive}
      >
        <HiStar
          className={`${sizeClasses[size]} ${
            filled ? 'text-amber-400' : partial ? 'text-amber-300' : 'text-gray-200'
          } ${interactive ? 'hover:text-amber-400 transition' : ''}`}
        />
      </button>
    );
  });

  return (
    <div className="flex items-center space-x-1">
      <div className="flex">{stars}</div>
      {showValue && <span className="text-sm text-gray-600 ml-1">{Number(rating).toFixed(1)}</span>}
      {count !== null && <span className="text-sm text-gray-400">({count})</span>}
    </div>
  );
};

export default StarRating;