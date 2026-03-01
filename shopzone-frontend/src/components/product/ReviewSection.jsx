import { useState, useEffect } from 'react';
import { useSelector } from 'react-redux';
import reviewService from '../../services/reviewService';
import StarRating from '../common/StarRating';
import toast from 'react-hot-toast';
import { HiShieldCheck, HiThumbUp } from 'react-icons/hi';

const ReviewSection = ({ productId }) => {
  const { isAuthenticated, user } = useSelector((state) => state.auth);
  const [reviews, setReviews] = useState([]);
  const [stats, setStats] = useState(null);
  const [canReview, setCanReview] = useState(false);
  const [showForm, setShowForm] = useState(false);
  const [loading, setLoading] = useState(true);
  const [formData, setFormData] = useState({ rating: 5, title: '', comment: '' });
  const [submitting, setSubmitting] = useState(false);

  const fetchReviews = async () => {
    try {
      const [reviewsRes, statsRes] = await Promise.all([
        reviewService.getProductReviews(productId, { page: 0, size: 10 }),
        reviewService.getReviewStats(productId),
      ]);
      setReviews(reviewsRes.data?.content || reviewsRes.data || []);
      setStats(statsRes.data);
    } catch { /* ignore */ }
    setLoading(false);
  };

  const checkCanReview = async () => {
    if (!isAuthenticated) return;
    try {
      const res = await reviewService.canReview(productId);
      setCanReview(res.data?.canReview ?? false);
    } catch { /* ignore */ }
  };

  useEffect(() => {
    fetchReviews();
    checkCanReview();
  }, [productId, isAuthenticated]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      await reviewService.createReview({ productId, ...formData });
      toast.success('Review submitted!');
      setShowForm(false);
      setFormData({ rating: 5, title: '', comment: '' });
      setCanReview(false);
      fetchReviews();
    } catch (err) {
      toast.error(err.response?.data?.message || 'Failed to submit review');
    }
    setSubmitting(false);
  };

  if (loading) {
    return <div className="animate-pulse space-y-4 py-8"><div className="h-4 bg-gray-200 rounded w-1/3" /><div className="h-20 bg-gray-200 rounded" /></div>;
  }

  return (
    <div className="mt-10 border-t pt-8">
      <h2 className="text-xl font-bold text-gray-900 mb-6">Customer Reviews</h2>

      {/* Stats Summary */}
      {stats && stats.totalReviews > 0 && (
        <div className="flex flex-col sm:flex-row gap-6 mb-8 p-4 bg-gray-50 rounded-xl">
          <div className="text-center">
            <p className="text-4xl font-bold text-gray-900">{stats.averageRating?.toFixed(1)}</p>
            <StarRating rating={stats.averageRating || 0} size="sm" />
            <p className="text-sm text-gray-500 mt-1">{stats.totalReviews} reviews</p>
          </div>
          <div className="flex-1 space-y-1">
            {[5, 4, 3, 2, 1].map((star) => {
              const pct = stats.ratingPercentages?.[star] || 0;
              return (
                <div key={star} className="flex items-center space-x-2 text-sm">
                  <span className="w-3 text-gray-600">{star}</span>
                  <div className="flex-1 h-2 bg-gray-200 rounded-full overflow-hidden">
                    <div className="h-full bg-amber-400 rounded-full" style={{ width: `${pct}%` }} />
                  </div>
                  <span className="w-8 text-right text-gray-500">{Math.round(pct)}%</span>
                </div>
              );
            })}
          </div>
        </div>
      )}

      {/* Write Review Button */}
      {isAuthenticated && canReview && !showForm && (
        <button onClick={() => setShowForm(true)} className="mb-6 px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 transition">
          Write a Review
        </button>
      )}

      {/* Review Form */}
      {showForm && (
        <form onSubmit={handleSubmit} className="mb-8 p-4 border rounded-xl bg-white space-y-4">
          <h3 className="font-semibold text-gray-900">Your Review</h3>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Rating</label>
            <StarRating rating={formData.rating} interactive onChange={(r) => setFormData({ ...formData, rating: r })} size="lg" />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Title</label>
            <input
              type="text" value={formData.title} onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="Summary of your review" required
            />
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Comment</label>
            <textarea
              value={formData.comment} onChange={(e) => setFormData({ ...formData, comment: e.target.value })}
              rows={4} className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
              placeholder="Tell others about your experience" required
            />
          </div>
          <div className="flex space-x-3">
            <button type="submit" disabled={submitting} className="px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 disabled:opacity-50 transition">
              {submitting ? 'Submitting...' : 'Submit Review'}
            </button>
            <button type="button" onClick={() => setShowForm(false)} className="px-4 py-2 border border-gray-300 text-gray-600 text-sm rounded-lg hover:bg-gray-50 transition">
              Cancel
            </button>
          </div>
        </form>
      )}

      {/* Reviews List */}
      {reviews.length > 0 ? (
        <div className="space-y-6">
          {reviews.map((review) => (
            <div key={review.id} className="border-b pb-6 last:border-b-0">
              <div className="flex items-start justify-between">
                <div>
                  <div className="flex items-center space-x-2 mb-1">
                    <StarRating rating={review.rating} size="sm" />
                    {review.verifiedPurchase && (
                      <span className="inline-flex items-center text-xs text-green-600 font-medium">
                        <HiShieldCheck className="h-3.5 w-3.5 mr-0.5" /> Verified Purchase
                      </span>
                    )}
                  </div>
                  <h4 className="font-medium text-gray-900">{review.title}</h4>
                </div>
                <span className="text-xs text-gray-400">{new Date(review.createdAt).toLocaleDateString()}</span>
              </div>
              <p className="text-sm text-gray-600 mt-2">{review.comment}</p>
              <div className="flex items-center space-x-4 mt-3 text-xs text-gray-500">
                <span>By {review.reviewerName || 'Anonymous'}</span>
                {review.helpfulCount > 0 && (
                  <span className="flex items-center space-x-1"><HiThumbUp className="h-3 w-3" /><span>{review.helpfulCount} helpful</span></span>
                )}
              </div>
            </div>
          ))}
        </div>
      ) : (
        <p className="text-gray-500 text-sm py-4">No reviews yet. Be the first to review this product!</p>
      )}
    </div>
  );
};

export default ReviewSection;