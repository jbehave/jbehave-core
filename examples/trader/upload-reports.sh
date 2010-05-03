
cd target 

cp -r jbehave-reports/rendered jbehave-trader-reports

zip -r jbehave-trader-reports.zip jbehave-trader-reports

scp jbehave-trader-reports.zip jbehave.org:

ssh jbehave.org "rm -r jbehave-trader-reports; unzip -uo jbehave-trader-reports.zip; rm -r /var/www/jbehave.org/reference/examples/jbehave-trader-reports; mv -f jbehave-trader-reports /var/www/jbehave.org/reference/examples"
