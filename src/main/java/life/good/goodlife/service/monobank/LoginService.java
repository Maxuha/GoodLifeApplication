package life.good.goodlife.service.monobank;

import life.good.goodlife.model.monobonk.Account;
import life.good.goodlife.model.monobonk.UserInfo;
import life.good.goodlife.model.monobonk.UserMonobank;

import java.util.List;

public interface LoginService {
    void createUser(UserMonobank userMonobank);
    void createAccount(Account account);
    void updateBalance(String id, Integer balance);
    String getToken(Long userId);
    UserInfo getUserInfo(String token);
    UserMonobank getByUserId(Long userId);
    UserMonobank getByClientId(String clientId);
    Account getAccountById(String id);
    List<Account> getAllAccountByClientId(String clientId);
}
