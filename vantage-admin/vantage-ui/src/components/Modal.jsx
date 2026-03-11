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
                <div className="modal-header">
                    <h3 className="modal-title">{title}</h3>
                    {showCloseButton && (
                        <button className="modal-close" onClick={onClose}>
                            <X size={20} />
                        </button>
                    )}
                </div>
                <div className="modal-body">
                    {children}
                </div>
                {footer && (
                    <div className="modal-footer">
                        {footer}
                    </div>
                )}
            </div>
        </div>
    );
};

export default Modal;
