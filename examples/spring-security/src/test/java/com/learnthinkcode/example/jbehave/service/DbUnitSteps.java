package com.learnthinkcode.example.jbehave.service;

import java.sql.SQLException;

import javax.sql.DataSource;

import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseDataSourceConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.jbehave.core.annotations.BeforeStory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component("dbUnitSteps")
public class DbUnitSteps {

  @Autowired
  private DataSource dataSource;

  @BeforeStory
  public void deleteAllData() throws SQLException, DatabaseUnitException {
    DatabaseDataSourceConnection dbConn = new DatabaseDataSourceConnection(dataSource);
    IDataSet dataSet = dbConn.createDataSet(new String[] { "ORGANIZATION", "APPLICATION_USER" });
    DatabaseOperation.DELETE_ALL.execute(dbConn, dataSet);
  }
}
