import React, {useState, useEffect} from 'react';
import {useTranslation} from 'react-i18next';
import {Link} from 'react-router-dom';
import Field from '../utils/Field';
import Button from '../utils/Button';
import makeQuerry from '../helper/querry';
import {passwordCheck} from '../helper/regExCheck';

export default function Index() {
    const [lastPassword, setCodeChange] = useState('');
    const [codeError, setCodeError] = useState(false);
    const [password, setPasswordChange] = useState('');
    const [passwordError, setPasswordError] = useState(false);
    const [confirmPassword, setConfirmPasswordChange] = useState('');
    const [confirmPasswordError, setConfirmPasswordError] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState(false);
    const [send, setSend] = useState(false);
    const [show, onShow] = useState(false);
    const {t} = useTranslation('translation', {keyPrefix: 'update-password'});
    const sendButtonIsDisabled = send || success;

    function handleCodeInput(value) {
        setCodeChange(value);
        setCodeError(false);
    }

    function handlePasswordInput(value) {
        setPasswordChange(value);
        setPasswordError(false);
    }

    function handleConfirmPasswordInput(value) {
        setConfirmPasswordChange(value);
        setConfirmPasswordError(false);
    }

    function checkResponse(response) {
        if (response.status === 200) {
            setSuccess(true);
        } else if (response.status === 400) {
            setError(t('error-code'));
        } else {
            setError(t('error-server2'));
        }
    }

    function validation() {
        switch (true) {
            case passwordCheck(lastPassword):
                setError(t('code-error'));
                setCodeError(true);
                return false;
            case passwordCheck(password):
                setError(t('password-error'));
                setPasswordError(true);
                return false;
            case password !== confirmPassword:
                setError(t('confirm-password-error'));
                setConfirmPasswordError(true);
                return false;
            default:
                return true;
        }
    }

    function handleSendButton() {
        setError('');
        if (!validation()) {
            setSend(false);
            return;
        }

        const body = {
            lastPassword,
            password,
            confirmPassword,
        };

        makeQuerry('/users/password/update', JSON.stringify(body), "PATCH")
            .then((response) => {
                setSend(false);
                checkResponse(response);
            });
    }

    useEffect(() => {
        if (send) {
            handleSendButton();
        }
    }, [send]);

    return (
        <div className="confirm main">
            <div className="form-body">
                <h1 className="title">{t('title')}</h1>
                {success && (
                    <p className="confirm__success">
                        {t('success-message')}
                        {' '}
                        <p>
                            <Link
                                className="link-success"
                                data-testid=""
                                to="/"
                            >
                                {t('auth-link')}
                            </Link>
                        </p>
                    </p>
                )}
                {error !== '' && <p data-testid="error" className="error">{error}</p>}
                <Field
                    dataTestId="code-input"
                    error={codeError}
                    name={t('code-input-title')}
                    value={lastPassword}
                    type="password"
                    onInputChange={(value) => handleCodeInput(value)}
                    show={show}
                    onShow={onShow}
                />

                <Field
                    dataTestId="password-input"
                    error={passwordError}
                    name={t('password-input-title')}
                    value={password}
                    type="password"
                    onInputChange={(value) => handlePasswordInput(value)}
                    tip={t('password-tip')}
                    show={show}
                    onShow={onShow}
                />

                <Field
                    dataTestId="confirm-password-input"
                    error={confirmPasswordError}
                    name={t('confirm-password-title')}
                    value={confirmPassword}
                    type="password"
                    onInputChange={(value) => handleConfirmPasswordInput(value)}
                    show={show}
                    onShow={onShow}
                />
                <Button
                    name={send ? t('processing') : t('button-title')}
                    className="confirm__btn btn-full"
                    onButton={setSend}
                    disabled={sendButtonIsDisabled}
                    dataTestId="change-password-btn"
                />
            </div>
        </div>
    );
}
