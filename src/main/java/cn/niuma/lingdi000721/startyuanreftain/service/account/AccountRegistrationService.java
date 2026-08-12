package cn.niuma.lingdi000721.startyuanreftain.service.account;

import cn.niuma.lingdi000721.startyuanreftain.dto.account.RegisterAccountRequest;
import cn.niuma.lingdi000721.startyuanreftain.dto.account.RegisterAccountResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * 注册应用服务
 */
@Service
public class AccountRegistrationService {
    private final PasswordEncoder passwordEncoder;
    private final AccountRegistrationTransaction registrationTransaction;

    public AccountRegistrationService(
            PasswordEncoder passwordEncoder,
            AccountRegistrationTransaction registrationTransaction)
    {
        this.passwordEncoder = Objects.requireNonNull(
                passwordEncoder,
                "passwordEncoder 不能为空");

        this.registrationTransaction = Objects.requireNonNull(
                registrationTransaction,
                "registrationTransaction 不能为空");
    }

    public RegisterAccountResponse register(RegisterAccountRequest request)
    {
        Objects.requireNonNull(request, "request 不能为空");

        String passwordHash =  passwordEncoder.encode(request.password());

        return registrationTransaction.createAccountAndWarehouse(
                request.username(),
                passwordHash);
    }
}
