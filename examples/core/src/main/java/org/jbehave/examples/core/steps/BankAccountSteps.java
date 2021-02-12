package org.jbehave.examples.core.steps;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jbehave.core.annotations.Alias;
import org.jbehave.core.annotations.Given;
import org.jbehave.core.annotations.Named;
import org.jbehave.core.annotations.Then;
import org.jbehave.core.annotations.When;
import org.jbehave.core.model.ExamplesTable;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThan;

public class BankAccountSteps {

    private BankAccount account;
    private List<BankAccount> accounts = new ArrayList<>();

    @Given("I have a bank account")
    public void givenIHaveABankAccount(){
        this.account = new BankAccount("Me");
    }

    @Given("my balance is $balance")
    public void givenBalanceIs(int balance){
        account.setBalance(balance);
    }

    @When("I withdraw $value")
    public void whenIWithdraw(int value){
        account.withdraw(value);
    }

    @Then("my bank account balance should be $balance")
    @Alias("my bank account balance should be <balance>")
    public void thenBalanceShouldBe(@Named("balance")int balance){
        assertThat(account.getBalance(), equalTo(balance));
    }

    @When("I deposit $value")
    @Alias("I deposit <value>")
    public void whenIDeposit(@Named("value")int value) {
        account.deposit(value);
    }

    @Given("these people have bank accounts with balances: $accountInfos")
    public void givenPeopleHaveBankAccounts(@Named("accountInfos")ExamplesTable accountInfos) {
        for(Map<String, String> info : accountInfos.getRows()) {
            final BankAccount account = new BankAccount(info.get("Name"));
            final int balance = Integer.parseInt(info.get("balance"));
            account.setBalance(balance);
            accounts.add(account);
        }
    }

    @When("I take all their money")
    public void whenIAddUp() {
        for(BankAccount bankAccount : accounts) {
            account.deposit(bankAccount.balance);
        }
    }

    @Then("my balance is in credit")
    public void thenBalanceInCredit() {
        assertThat(account.getBalance(), greaterThan(0));
    }

    @Then("my balance is archived")
    public void thenBalanceIsArchived() {
        Date now = new Date();
        account.archive(now);
        assertThat(account.getArchived(now), equalTo(account.getBalance()));
    }

    @Then("my balance is not archived")
    public void thenBalanceIsNotArchived() {
    }

    @Then("my balance is printed")
    public void thenBalanceIsPrinted() {
        System.out.println(account.getBalance());
    }


    public static class BankAccount {
        private int balance;
        private final String name;
        private Map<Date,Integer> archive = new HashMap<>();

        public BankAccount(String name) {
            this.name = name;
        }

        public void archive(Date date) {
            archive.put(date, balance);
        }

        public int getArchived(Date date){
            return archive.get(date);
        }

        public String getName(){
            return name;
        }
        
        public int getBalance() {
            return this.balance;
        }
        
        public void setBalance(int balance) {
            this.balance = balance;
        }

        public void deposit(int v) {
            this.balance += v;
        }

        public void withdraw(int v) {
            this.balance -= v;
        }
    }
}
