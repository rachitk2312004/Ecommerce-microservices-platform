export default function Toast({ message, type = 'success', onClose }) {
  return (
    <div className={`toast toast-${type}`} role="status">
      <span>{message}</span>
      <button type="button" className="toast-close" onClick={onClose} aria-label="Close">
        &times;
      </button>
    </div>
  );
}
