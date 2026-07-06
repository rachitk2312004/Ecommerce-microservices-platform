export default function LoadingSpinner({ fullPage = false, size = 'md' }) {
  return (
    <div className={`spinner-wrapper${fullPage ? ' full-page' : ''}`}>
      <div className={`spinner spinner-${size}`} role="status" aria-label="Loading" />
    </div>
  );
}
