import { X } from 'lucide-react';

const Modal = ({ 
    isOpen, 
    onClose, 
    title, 
    children, 
    size = 'medium',
    showCloseButton = true,
    footer
}) => {
    if (!isOpen) return null;

    const sizeClasses = {
        small: 'modal-sm',
        medium: 'modal-md',
        large: 'modal-lg',
        xlarge: 'modal-xl'
    };

    return (
        <div className="modal-overlay" onClick={showCloseButton ? onClose : undefined}>
            <div
                className={`modal-content ${sizeClasses[size]}`}
                onClick={(e) => e.stopPropagation()}
            >
                <div className="modal-header" style={{ padding: '14px 18px' }}>
                    <h3 className="modal-title" style={{ fontSize: '15px' }}>{title}</h3>
                    {showCloseButton && (
                        <button className="modal-close" onClick={onClose}>
                            <X size={18} />
                        </button>
                    )}
                </div>
                <div className="modal-body" style={{ padding: '18px' }}>
                    {children}
                </div>
                {footer && (
                    <div className="modal-footer" style={{ padding: '12px 18px' }}>
                        {footer}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Modal;
