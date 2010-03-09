package org.jbehave.scenario.definition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a tabular structure to hold example data for number of named parameters:<br/><br/>
 * 
 * |name 1|name 2| .... |name n|<br/>
 * |value 11|value 12| .... |value 1n|<br/>
 *  ...<br/>
 * |value m1|value m2| .... |value mn|<br/>
 */
public class ExamplesTable {

	private static final String NEWLINE = "\n";
    private static final String COLUMN_SEPARATOR = "\\|";
	private final List<Map<String, String>> data = new ArrayList<Map<String, String>>();
	private final String tableAsString;
    private final List<String> headers = new ArrayList<String>();

	public ExamplesTable(String tableAsString) {
		this.tableAsString = tableAsString;
		parse();
	}

	private void parse() {
		data.clear();
		String[] rows = tableAsString.trim().split(NEWLINE);
		headers.clear();
		for (int row = 0; row < rows.length; row++) {
			List<String> columns = columnsFor(rows[row]);
			if ( row == 0 ) {
				headers.addAll(columns);
			} else {
				Map<String, String> map = new HashMap<String, String>();
				for ( int column = 0; column < columns.size(); column++ ){
					map.put(headers.get(column), columns.get(column));
				}
				data.add(map);
			}
		}
	}

	private List<String> columnsFor(String row) {
		List<String> columns = new ArrayList<String>();
		for ( String column : row.split(COLUMN_SEPARATOR) ){
			columns.add(column.trim());
		}
		int size = columns.size();
		if  ( size > 0 ){
			columns.remove(0);		
		}
		return columns;
	}

	public List<String> getHeaders(){
	    return headers;
	}
	
	public Map<String, String> getRow(int row){
		return data.get(row);		
	}
	
	public int getRowCount(){
		return data.size();
	}

	public List<Map<String, String>> getRows() {
		return data;
	}
	
	@Override
	public String toString(){
		return tableAsString;
	}

}
