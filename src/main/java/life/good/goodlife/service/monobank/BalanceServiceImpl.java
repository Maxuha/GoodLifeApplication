package life.good.goodlife.service.monobank;

import com.google.gson.Gson;
import life.good.goodlife.model.bank.CurrencyCodeFactory;
import life.good.goodlife.model.monobonk.Account;
import life.good.goodlife.model.monobonk.Balance;
import life.good.goodlife.model.monobonk.UserInfo;
import life.good.goodlife.statics.Request;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class BalanceServiceImpl implements BalanceService {
    @Override
    public String balance(String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("X-Token", token);
        String data = Request.get("https://api.monobank.ua/personal/client-info", headers);
        Gson gson = new Gson();
        UserInfo userInfo = gson.fromJson(data, UserInfo.class);
        StringBuilder result = new StringBuilder("<b>Мой баланс: <b>\n\n");
        Account[] accounts = userInfo.getAccounts();
        for (Account account : accounts) {
            result.append("Карта: ").append(account.getMaskedPan()[0])
                    .append("Тип: ").append(account.getType())
                    .append("Баланс: ").append(getBalance(account.getBalance(), account.getCurrencyCode()))
                    .append("Кредитный лимит: ").append(getBalance(account.getCreditLimit(), account.getCurrencyCode()))
                    .append("----------------------------------------------------")
                    .append("::");
        }
        return result.toString();
    }

    private Balance getBalance(Integer balance, Integer currencyCode) {
        String[] balances = new String[2];
        String balanceStr;

        balanceStr = balance.toString();
        if (balance > 99) {
            balances[0] = balanceStr.substring(balanceStr.length()-2);
            balances[1] = balanceStr.substring(0, balanceStr.length()-2);
        } else {
            balances[0] = balanceStr;
        }
        return new Balance(balances[1], balances[0],
                CurrencyCodeFactory.getSymbolByCurrencyCode(currencyCode));
    }
}
