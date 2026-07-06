/**
 * Product-related image URLs (Unsplash).
 * Used when DB has missing or placeholder image URLs.
 */
const PRODUCT_IMAGES = {
  'Wireless Headphones': 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400&h=300&fit=crop&q=80',
  Smartphone: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400&h=300&fit=crop&q=80',
  Laptop: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400&h=300&fit=crop&q=80',
  'Classic T-Shirt': 'https://images.unsplash.com/photo-1521572163474-6864f9cf17ab?w=400&h=300&fit=crop&q=80',
  'Denim Jeans': 'https://images.unsplash.com/photo-1542272604-787c3835535d?w=400&h=300&fit=crop&q=80',
  'Winter Jacket': 'https://images.unsplash.com/photo-1551028719-00167b16eac5?w=400&h=300&fit=crop&q=80',
  'Clean Code': 'https://images.unsplash.com/photo-1544947950-fa07a98d237f?w=400&h=300&fit=crop&q=80',
  'Design Patterns': 'https://images.unsplash.com/photo-1512820790803-83ca734da794?w=400&h=300&fit=crop&q=80',
};

const GENERIC_BY_KEYWORD = [
  { keywords: ['headphone', 'earphone', 'audio'], url: PRODUCT_IMAGES['Wireless Headphones'] },
  { keywords: ['phone', 'mobile', 'smartphone'], url: PRODUCT_IMAGES.Smartphone },
  { keywords: ['laptop', 'notebook', 'computer'], url: PRODUCT_IMAGES.Laptop },
  { keywords: ['shirt', 't-shirt', 'tee'], url: PRODUCT_IMAGES['Classic T-Shirt'] },
  { keywords: ['jean', 'denim', 'pant'], url: PRODUCT_IMAGES['Denim Jeans'] },
  { keywords: ['jacket', 'coat'], url: PRODUCT_IMAGES['Winter Jacket'] },
  { keywords: ['book', 'code', 'pattern'], url: PRODUCT_IMAGES['Clean Code'] },
];

function isBrokenImageUrl(url) {
  if (!url || !url.trim()) return true;
  return url.includes('example.com') || url.includes('picsum.photos');
}

function resolveByName(name) {
  if (!name) return null;
  if (PRODUCT_IMAGES[name]) return PRODUCT_IMAGES[name];
  const lower = name.toLowerCase();
  for (const entry of GENERIC_BY_KEYWORD) {
    if (entry.keywords.some((k) => lower.includes(k))) {
      return entry.url;
    }
  }
  return null;
}

export function getProductImageUrl(product) {
  const url = product?.imageUrl;
  if (url && !isBrokenImageUrl(url)) {
    return url;
  }
  const byName = resolveByName(product?.name);
  if (byName) return byName;
  return `https://images.unsplash.com/photo-1472851293608-3e6fd74545c8?w=400&h=300&fit=crop&q=80`;
}

export function handleProductImageError(event, product) {
  event.currentTarget.onerror = null;
  event.currentTarget.src = getProductImageUrl({ ...product, imageUrl: '' });
}

export { PRODUCT_IMAGES };
