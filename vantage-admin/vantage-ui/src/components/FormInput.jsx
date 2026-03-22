const FormInput = ({
    label,
    name,
    type = 'text',
    value,
    onChange,
    placeholder,
    required = false,
    error,
    disabled = false,
    maxLength,
    minLength,
    pattern,
    helpText,
    className = ''
}) => {
    const inputClasses = `form-input ${error ? 'form-input-error' : ''} ${className || ''}`;

    return (
        <div className="form-group">
            {label && (
                <label className="form-label">
                    {label}
                    {required && <span className="form-required">*</span>}
                </label>
            )}
            {type === 'textarea' ? (
                <textarea
                    name={name}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    disabled={disabled}
                    maxLength={maxLength}
                    minLength={minLength}
                    className={inputClasses}
                    rows={4}
                />
            ) : type === 'select' ? (
                <select
                    name={name}
                    value={value}
                    onChange={onChange}
                    disabled={disabled}
                    className={inputClasses}
                >
                    <option value="">Please select</option>
                    {placeholder && <option value="">{placeholder}</option>}
                </select>
            ) : (
                <input
                    type={type}
                    name={name}
                    value={value}
                    onChange={onChange}
                    placeholder={placeholder}
                    disabled={disabled}
                    maxLength={maxLength}
                    minLength={minLength}
                    pattern={pattern}
                    className={inputClasses}
                />
            )}
            {helpText && <small className="form-help">{helpText}</small>}
            {error && <span className="form-error">{error}</span>}
        </div>
    );
};

export default FormInput;
