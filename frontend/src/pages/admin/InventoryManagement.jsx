import { useEffect, useState } from 'react';
import { productService } from '../../services/productService';
import { inventoryService } from '../../services/inventoryService';
import LoadingSpinner from '../../components/common/LoadingSpinner';
import ErrorMessage from '../../components/common/ErrorMessage';
import { useToast } from '../../context/ToastContext';

export default function InventoryManagement() {
  const [products, setProducts] = useState([]);
  const [inventory, setInventory] = useState({});
  const [editing, setEditing] = useState({});
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const { showToast } = useToast();

  const loadData = async () => {
    setLoading(true);
    setError('');
    try {
      const page = await productService.getProducts(0, 100);
      const productList = page.content || [];
      setProducts(productList);

      const invMap = {};
      await Promise.all(
        productList.map(async (p) => {
          try {
            const inv = await inventoryService.getByProductId(p.id);
            invMap[p.id] = inv;
          } catch {
            invMap[p.id] = { availableQuantity: 0, reservedQuantity: 0 };
          }
        })
      );
      setInventory(invMap);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const handleUpdate = async (productId) => {
    const qty = Number(editing[productId]);
    if (Number.isNaN(qty) || qty < 0) {
      showToast('Enter a valid quantity', 'error');
      return;
    }
    try {
      const updated = await inventoryService.updateStock(productId, qty);
      setInventory((prev) => ({ ...prev, [productId]: updated }));
      setEditing((prev) => {
        const next = { ...prev };
        delete next[productId];
        return next;
      });
      showToast('Stock updated');
    } catch (err) {
      showToast(err.message, 'error');
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Inventory Management</h1>
      </div>

      {loading && <LoadingSpinner />}
      <ErrorMessage message={error} onRetry={loadData} />

      {!loading && !error && (
        <div className="table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th>Product</th>
                <th>Available</th>
                <th>Reserved</th>
                <th>Update Stock</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {products.map((product) => {
                const inv = inventory[product.id] || {};
                return (
                  <tr key={product.id}>
                    <td>{product.name}</td>
                    <td>{inv.availableQuantity ?? '—'}</td>
                    <td>{inv.reservedQuantity ?? '—'}</td>
                    <td>
                      <input
                        type="number"
                        min="0"
                        className="input input-sm"
                        placeholder="New qty"
                        value={editing[product.id] ?? ''}
                        onChange={(e) =>
                          setEditing((prev) => ({ ...prev, [product.id]: e.target.value }))
                        }
                      />
                    </td>
                    <td>
                      <button
                        type="button"
                        className="btn btn-primary btn-sm"
                        onClick={() => handleUpdate(product.id)}
                      >
                        Update
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
