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
import org.junit.Assert;

public class BankAccountSteps {

    private BankAccount account;
    private List<BankAccount> accounts = new ArrayList<BankAccount>();

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
        Assert.assertEquals(balance, account.getBalance());
    }

    @When("I add $value")
    @Alias("I add <value>")
    public void whenIAdd(@Named("value")int value) {
        account.add(value);
    }

    @Given("these people have bank accounts with balances: $accountInfos")
    public void givenPeopleHaveBankAccounts(@Named("accountInfos")ExamplesTable accountInfos) throws Throwable {
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
            account.add(bankAccount.balance);
        }
    }

    @Then("my balance is printed")
    public void thenBalanceIsPrinted() {
    }

    @Then("my balance is archived")
    public void thenBalanceIsArchived() {
    	account.archive();
    }

    @Then("my balance is not archived")
    public void thenBalanceIsNotArchived() {
    }

    public static class BankAccount {
        private int balance;
        private final String name;
        private Map<Date,Integer> archive = new HashMap<Date, Integer>(); 

        public BankAccount(String name) {
            this.name = name;
        }

        public void archive() {
        	archive.put(new Date(), balance);
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

        public void add(int v) {
            this.balance += v;
        }

        public void withdraw(int v) {
            this.balance -= v;
        }
    }
}
