package life.good.goodlife.model.monobonk;

import life.good.goodlife.statics.CurrencyCode;

public class Account {
    private String id;
    private int currencyCode;
    private String cashbackType;
    private int balance;
    private int creditLimit;
    private String[] maskedPan;
    private String type;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(int currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getCashbackType() {
        return cashbackType;
    }

    public void setCashbackType(String cashbackType) {
        this.cashbackType = cashbackType;
    }

    public int getBalance() {
        return balance;
    }

    public void setBalance(int balance) {
        this.balance = balance;
    }

    public int getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(int creditLimit) {
        this.creditLimit = creditLimit;
    }

    public String[] getMaskedPan() {
        return maskedPan;
    }

    public void setMaskedPan(String[] maskedPan) {
        this.maskedPan = maskedPan;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        float value = balance / 100.0f;
        String[] balanceCount = Float.toString(value).split("\\.");
        Balance balance = new Balance(balanceCount[0], balanceCount[1],
                CurrencyCode.getSymbolByCurrencyCode(currencyCode));
        value = creditLimit / 100.0f;
        balanceCount = Float.toString(value).split("\\.");
        Balance balanceCreditLimit = new Balance(balanceCount[0], balanceCount[1],
                CurrencyCode.getSymbolByCurrencyCode(currencyCode));
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < maskedPan.length; i++) {
            result.append("Карта: ").append(maskedPan[i]).append("\nТип: ")
                    .append(type).append("\nБаланс: ").append(balance)
                    .append("\nКредитный лимит: ").append(balanceCreditLimit).append("\n");
        }
        return result.toString();
    }
}
