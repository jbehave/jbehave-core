package org.jbehave.examples.core.steps;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.AfterStory;
import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;

public class BankAccountSteps {

    private final ThreadLocal<BankManager> bankManager = new ThreadLocal<>();

    @Given("I have a bank account")
    public void givenIHaveABankAccount() {
        bankManager.set(new BankManager(new BankAccount("Me")));
    }

    @Given("my balance is $balance")
    public void givenBalanceIs(int balance) {
        getMyAccount().setBalance(balance);
    }

    @When("I withdraw $value")
    public void whenIWithdraw(int value) {
        getMyAccount().withdraw(value);
    }

    @Then("my bank account balance should be $balance")
    @Alias("my bank account balance should be <balance>")
    public void thenBalanceShouldBe(@Named("balance") int balance) {
        assertThat(getMyAccount().getBalance(), equalTo(balance));
    }

    @When("I deposit $value")
    @Alias("I deposit <value>")
    public void whenIDeposit(@Named("value") int value) {
        getMyAccount().deposit(value);
    }

    @Given("these people have bank accounts with balances: $accountInfos")
    public void givenPeopleHaveBankAccounts(@Named("accountInfos") ExamplesTable accountInfos) {
        for (Map<String, String> info : accountInfos.getRows()) {
            final BankAccount account = new BankAccount(info.get("Name"));
            final int balance = Integer.parseInt(info.get("balance"));
            account.setBalance(balance);
            getBankManager().getAccounts().add(account);
        }
    }

    @When("I take all their money")
    public void whenIAddUp() {
        BankAccount myAccount = getMyAccount();
        for (BankAccount bankAccount : getBankManager().getAccounts()) {
            myAccount.deposit(bankAccount.balance);
        }
    }

    @Then("my balance is in credit")
    public void thenBalanceInCredit() {
        assertThat(getMyAccount().getBalance(), greaterThan(0));
    }

    @Then("my balance is archived")
    public void thenBalanceIsArchived() {
        Date now = new Date();
        getMyAccount().archive(now);
        assertThat(getMyAccount().getArchived(now), equalTo(getMyAccount().getBalance()));
    }

    @Then("my balance is not archived")
    public void thenBalanceIsNotArchived() {
    }

    @Then("my balance is printed")
    public void thenBalanceIsPrinted() {
        System.out.println(getMyAccount().getBalance());
    }

    @AfterStory
    public void cleanUp() {
        bankManager.remove();
    }

    private BankAccount getMyAccount() {
        return getBankManager().getMyAccount();
    }

    private BankManager getBankManager() {
        return this.bankManager.get();
    }

    public static class BankManager {
        private final BankAccount myAccount;
        private final List<BankAccount> accounts = new ArrayList<>();

        public BankManager(BankAccount myAccount) {
            this.myAccount = myAccount;
        }

        public BankAccount getMyAccount() {
            return myAccount;
        }

        public List<BankAccount> getAccounts() {
            return accounts;
        }
    }

    public static class BankAccount {
        private int balance;
        private final String name;
        private final Map<Date,Integer> archive = new HashMap<>();

        public BankAccount(String name) {
            this.name = name;
        }

        public void archive(Date date) {
            archive.put(date, balance);
        }

        public int getArchived(Date date) {
            return archive.get(date);
        }

        public String getName() {
            return name;
        }
        
        public int getBalance() {
            return this.balance;
        }
        
        public void setBalance(int balance) {
            this.balance = balance;
        }

        public void deposit(int value) {
            this.balance += value;
        }

        public void withdraw(int value) {
            this.balance -= value;
        }
    }
}
