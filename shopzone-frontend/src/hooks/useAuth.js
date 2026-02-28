import { useSelector } from 'react-redux';

const useAuth = () => {
  const { user, isAuthenticated, loading } = useSelector((state) => state.auth);

  return {
    user,
    isAuthenticated,
    loading,
    isAdmin: user?.role === 'ADMIN',
    fullName: user ? `${user.firstName} ${user.lastName}` : '',
  };
};

export default useAuth;