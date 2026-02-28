import { Link } from 'react-router-dom';

const EmptyState = ({ icon, title, description, actionText, actionLink }) => {
  return (
    <div className="text-center py-16 px-4">
      {icon && <div className="text-6xl mb-4">{icon}</div>}
      <h3 className="text-lg font-semibold text-gray-900 mb-2">{title}</h3>
      {description && <p className="text-gray-500 mb-6 max-w-md mx-auto">{description}</p>}
      {actionText && actionLink && (
        <Link
          to={actionLink}
          className="inline-flex items-center px-6 py-2.5 bg-indigo-600 text-white text-sm font-medium rounded-lg hover:bg-indigo-700 transition"
        >
          {actionText}
        </Link>
      )}
    </div>
  );
};

export default EmptyState;