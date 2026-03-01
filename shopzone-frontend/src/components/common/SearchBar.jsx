import { useState, useEffect, useRef } from 'react';
import { useNavigate, useSearchParams, useLocation } from 'react-router-dom';
import { HiSearch, HiX } from 'react-icons/hi';
import useDebounce from '../../hooks/useDebounce';
import productService from '../../services/productService';

const SearchBar = () => {
  const [searchParams] = useSearchParams();
  const location = useLocation();
  const urlQuery = searchParams.get('q') || '';

  const [query, setQuery] = useState(urlQuery);
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const debouncedQuery = useDebounce(query, 300);
  const navigate = useNavigate();
  const ref = useRef(null);

  // Keep the search bar text in sync with the URL
  useEffect(() => {
    // If we leave the products page (e.g., click home), clear the search bar
    if (!location.pathname.includes('/products')) {
      setQuery('');
    } else {
      setQuery(urlQuery);
    }
  }, [urlQuery, location.pathname]);

  useEffect(() => {
    const fetchSuggestions = async () => {
      // Only fetch suggestions if there's an active query that differs from the current URL
      // This prevents the dropdown from constantly reopening on the results page
      if (debouncedQuery.length < 2 || debouncedQuery === urlQuery) {
        setSuggestions([]);
        return;
      }
      try {
        const response = await productService.getAutocomplete(debouncedQuery);
        setSuggestions(response.data || []);
      } catch {
        setSuggestions([]);
      }
    };
    fetchSuggestions();
  }, [debouncedQuery, urlQuery]);

  // Close on outside click
  useEffect(() => {
    const handleClickOutside = (e) => {
      if (ref.current && !ref.current.contains(e.target)) {
        setShowSuggestions(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleSubmit = (e) => {
    e.preventDefault();
    if (query.trim()) {
      navigate(`/products?q=${encodeURIComponent(query.trim())}`);
      setShowSuggestions(false);
      // Removed setQuery('') so the text stays in the box
    }
  };

  const handleSuggestionClick = (suggestion) => {
    const term = suggestion.name || suggestion;
    setQuery(term); // Update the input text to match what they clicked
    navigate(`/products?q=${encodeURIComponent(term)}`);
    setShowSuggestions(false);
  };

  // Add a clear function to reset the search
  const handleClear = () => {
    setQuery('');
    setShowSuggestions(false);
    if (urlQuery) {
      // Navigate back to products without the 'q' parameter to clear the search
      navigate('/products');
    }
  };

  return (
    <div ref={ref} className="relative w-full">
      <form onSubmit={handleSubmit} className="relative flex items-center">
        <HiSearch className="absolute left-3 h-5 w-5 text-gray-400" />
        <input
          type="text"
          value={query}
          onChange={(e) => {
            setQuery(e.target.value);
            setShowSuggestions(true);
          }}
          onFocus={() => suggestions.length > 0 && setShowSuggestions(true)}
          placeholder="Search products..."
          className="w-full pl-10 pr-10 py-2.5 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500 focus:border-transparent"
        />
        
        {/* NEW: Clear (X) Button */}
        {query && (
          <button
            type="button"
            onClick={handleClear}
            className="absolute right-3 p-1 text-gray-400 hover:text-gray-600 rounded-full hover:bg-gray-100 transition-colors"
            title="Clear search"
          >
            <HiX className="h-4 w-4" />
          </button>
        )}
      </form>

      {/* Suggestions Dropdown */}
      {showSuggestions && suggestions.length > 0 && (
        <div className="absolute z-50 w-full mt-1 bg-white rounded-lg shadow-lg border max-h-64 overflow-y-auto">
          {suggestions.map((s, i) => (
            <button
              key={i}
              onClick={() => handleSuggestionClick(s)}
              className="w-full text-left px-4 py-2.5 text-sm text-gray-700 hover:bg-indigo-50 hover:text-indigo-600 transition flex items-center space-x-2"
            >
              <HiSearch className="h-4 w-4 text-gray-400 flex-shrink-0" />
              <span>{s.name || s}</span>
            </button>
          ))}
        </div>
      )}
    </div>
  );
};

export default SearchBar;