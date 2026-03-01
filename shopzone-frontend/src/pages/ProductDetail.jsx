import { useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { useDispatch, useSelector } from 'react-redux';
import { fetchProductById, clearCurrentProduct } from '../store/productSlice';
import ProductGallery from '../components/product/ProductGallery';
import ProductInfo from '../components/product/ProductInfo';
import ReviewSection from '../components/product/ReviewSection';
import { ProductDetailSkeleton } from '../components/common/LoadingSkeleton';

const ProductDetail = () => {
  const { id } = useParams();
  const dispatch = useDispatch();
  const { currentProduct: product, productLoading: loading } = useSelector((state) => state.products);

  useEffect(() => {
    dispatch(fetchProductById(id));
    return () => { dispatch(clearCurrentProduct()); };
  }, [dispatch, id]);

  if (loading || !product) return <ProductDetailSkeleton />;

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-fade-in">
      <div className="grid grid-cols-1 md:grid-cols-2 gap-8 lg:gap-12">
        <ProductGallery images={product.images} name={product.name} />
        <ProductInfo product={product} />
      </div>
      <ReviewSection productId={product.id} />
    </div>
  );
};

export default ProductDetail;